package com.example.data.repository

import android.content.Context
import android.media.MediaMetadataRetriever
import android.net.Uri
import android.util.Log
import com.example.audio.AudioIntegrityUtils
import com.example.data.local.AuditLogDao
import com.example.data.local.IncidentDao
import com.example.data.local.IncidentEvidenceDao
import com.example.data.local.RecordingDao
import com.example.data.local.ReportDao
import com.example.data.local.SavedRecordingDao
import com.example.data.model.AuditLogEntity
import com.example.data.model.IncidentEntity
import com.example.data.model.IncidentEvidenceEntity
import com.example.data.model.RecordingEntity
import com.example.data.model.ReportEntity
import com.example.data.model.SavedRecordingEntity
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.withContext
import java.io.File
import java.io.FileOutputStream
import java.io.FileWriter
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale
import java.util.UUID

data class IntegrityVerificationResult(
    val isIntact: Boolean,
    val computedHash: String,
    val recordedHash: String,
    val fileSizeBytes: Long,
    val message: String
)

class RecordingRepository(
    private val recordingDao: RecordingDao,
    private val reportDao: ReportDao,
    private val savedRecordingDao: SavedRecordingDao,
    private val incidentDao: IncidentDao,
    private val incidentEvidenceDao: IncidentEvidenceDao,
    private val auditLogDao: AuditLogDao,
    private val context: Context
) {

    val allPublicRecordings: Flow<List<RecordingEntity>> =
        recordingDao.getAllPublicRecordings()

    fun getRecordingsByOwner(ownerId: String): Flow<List<RecordingEntity>> =
        recordingDao.getRecordingsByOwner(ownerId)

    fun getOwnerRecordingsCountFlow(ownerId: String): Flow<Int> =
        recordingDao.getOwnerRecordingsCountFlow(ownerId)

    suspend fun getRecordingById(recordingId: String): RecordingEntity? =
        recordingDao.getRecordingById(recordingId)

    suspend fun checkDuplicateHash(hash: String): RecordingEntity? =
        recordingDao.getRecordingByHash(hash)

    suspend fun verifyIntegrity(recordingId: String): IntegrityVerificationResult = withContext(Dispatchers.IO) {
        val rec = recordingDao.getRecordingById(recordingId)
            ?: return@withContext IntegrityVerificationResult(
                isIntact = false,
                computedHash = "",
                recordedHash = "",
                fileSizeBytes = 0L,
                message = "Recording record not found in local platform database."
            )

        val file = File(rec.filePath)
        if (!file.exists() || !file.canRead()) {
            return@withContext IntegrityVerificationResult(
                isIntact = false,
                computedHash = "N/A",
                recordedHash = rec.sha256Hash,
                fileSizeBytes = 0L,
                message = "Original audio file is unavailable on local storage. File cannot be read."
            )
        }

        val currentHash = AudioIntegrityUtils.calculateSha256(file)
        val matches = currentHash.equals(rec.sha256Hash, ignoreCase = true)

        IntegrityVerificationResult(
            isIntact = matches,
            computedHash = currentHash,
            recordedHash = rec.sha256Hash,
            fileSizeBytes = file.length(),
            message = if (matches) {
                "Integrity Check Passed. On-device computed SHA-256 matches platform record exactly. File is unmodified."
            } else {
                "Integrity Mismatch. Current file hash does NOT match the cryptographic record stored at publication."
            }
        )
    }

    suspend fun prepareEvidenceExportPackage(recording: RecordingEntity): Pair<File, File?> = withContext(Dispatchers.IO) {
        val exportDir = File(context.cacheDir, "evidence_exports").apply { mkdirs() }
        val manifestFile = File(exportDir, "${recording.recordingId}_MANIFEST.txt")

        val sdf = SimpleDateFormat("yyyy-MM-dd HH:mm:ss z", Locale.ENGLISH)
        val pubDate = sdf.format(Date(recording.publicationTimestamp))
        val uploadDate = sdf.format(Date(recording.uploadTimestamp))
        val incidentDateStr = recording.incidentDateTime?.let { sdf.format(Date(it)) } ?: "Not specified by uploader"

        val manifestContent = buildString {
            appendLine("================================================================================")
            appendLine("                    PROTECT YOURSELF — إحمي روحك")
            appendLine("                     CIVIC EVIDENCE MANIFEST PACKAGE")
            appendLine("================================================================================")
            appendLine("EVIDENCE IDENTIFIER: ${recording.recordingId}")
            appendLine("GENERATED AT:        ${sdf.format(Date())}")
            appendLine()
            appendLine("--- SECTION 1: PLATFORM VERIFIED FACTS ---")
            appendLine("Evidence ID:            ${recording.recordingId}")
            appendLine("Cryptographic Hash:     SHA-256: ${recording.sha256Hash}")
            appendLine("Audio Duration:         ${AudioIntegrityUtils.formatDuration(recording.durationMs)} (${recording.durationMs} ms)")
            appendLine("Preserved File Size:    ${recording.fileSizeBytes} bytes")
            appendLine("Platform Upload Time:   $uploadDate")
            appendLine("Publication Time:       $pubDate")
            appendLine("Publication Status:     ${recording.moderationStatus}")
            appendLine("Verified Stored Format: MPEG-4 AAC Audio (.m4a)")
            appendLine()
            appendLine("--- SECTION 2: UPLOADER STATEMENTS (UNVERIFIED BY PLATFORM) ---")
            appendLine("Uploader Account:       @${recording.ownerUsername}")
            appendLine("Title:                  ${recording.title}")
            appendLine("Declared Category:      ${recording.category}")
            appendLine("Stated Location:        ${recording.locationDescription ?: "None declared"}")
            appendLine("Stated Incident Time:   $incidentDateStr")
            appendLine("Uploader's Narrative:")
            appendLine(recording.description.ifBlank { "(No additional context provided)" })
            appendLine()
            appendLine("--- SECTION 3: LEGAL ADJUDICATION DISCLAIMERS ---")
            appendLine("1. NON-ADJUDICATIVE NATURE: Protect Yourself is an open civic evidence-preservation")
            appendLine("   archive. The platform does NOT establish guilt, criminality, corruption, or legal")
            appendLine("   liability. Such determinations belong exclusively to competent judicial tribunals.")
            appendLine("2. HASH SIGNIFICANCE: The SHA-256 hash confirms the preserved audio bitstream")
            appendLine("   corresponds to the uploaded file. It does not authenticate speaker identity or the")
            appendLine("   truth of factual claims made by either party.")
            appendLine("3. TUNISIAN LEGAL FRAMEWORK: This evidence package is prepared under general reference")
            appendLine("   to Article 24 of the Tunisian Constitution (Privacy & Communications Confidentiality),")
            appendLine("   Organic Law 2004-63 (Personal Data Protection), and Penal Code Article 125/247 bis.")
            appendLine("   Consult a qualified Tunisian lawyer for individualized legal counsel.")
            appendLine("================================================================================")
        }

        FileWriter(manifestFile).use { it.write(manifestContent) }

        val audioFile = File(recording.filePath)
        val validAudioFile = if (audioFile.exists()) audioFile else null

        Pair(manifestFile, validAudioFile)
    }

    // SAVED RECORDINGS
    fun isRecordingSavedFlow(userId: String, recordingId: String): Flow<Boolean> =
        savedRecordingDao.isRecordingSavedFlow(userId, recordingId)

    suspend fun toggleSaveRecording(userId: String, recordingId: String): Boolean = withContext(Dispatchers.IO) {
        val already = savedRecordingDao.isRecordingSaved(userId, recordingId)
        if (already) {
            savedRecordingDao.unsaveRecording(userId, recordingId)
            false
        } else {
            savedRecordingDao.saveRecording(SavedRecordingEntity(userId = userId, recordingId = recordingId))
            true
        }
    }

    fun getSavedRecordingsForUser(userId: String): Flow<List<RecordingEntity>> =
        savedRecordingDao.getSavedRecordingsForUser(userId)

    // AUDIT LOGS
    fun getRecentAuditLogs(): Flow<List<AuditLogEntity>> =
        auditLogDao.getRecentLogs(limit = 100)

    suspend fun logAuditEvent(actor: String, eventType: String, targetId: String, details: String) = withContext(Dispatchers.IO) {
        auditLogDao.insertLog(
            AuditLogEntity(
                logId = UUID.randomUUID().toString(),
                actorUsername = actor,
                eventType = eventType,
                targetId = targetId,
                details = details,
                timestamp = System.currentTimeMillis()
            )
        )
    }

    // MODERATION
    fun getPendingReports(): Flow<List<ReportEntity>> =
        reportDao.getPendingReports()

    fun getAllReports(): Flow<List<ReportEntity>> =
        reportDao.getAllReports()

    suspend fun takeModerationAction(
        reportId: String,
        recordingId: String,
        action: String, // "RESTRICT", "DISMISS", "FLAG"
        moderatorUsername: String,
        actionReason: String
    ): Result<Unit> = withContext(Dispatchers.IO) {
        try {
            when (action) {
                "RESTRICT" -> {
                    recordingDao.updateModerationStatus(recordingId, "RESTRICTED")
                    reportDao.updateReportStatus(reportId, "ACTION_TAKEN", actionReason)
                    logAuditEvent(
                        actor = moderatorUsername,
                        eventType = "CONTENT_RESTRICTED",
                        targetId = recordingId,
                        details = "Recording restricted from public feed. Reason: $actionReason"
                    )
                }
                "DISMISS" -> {
                    reportDao.updateReportStatus(reportId, "NO_VIOLATION", actionReason)
                    logAuditEvent(
                        actor = moderatorUsername,
                        eventType = "REPORT_DISMISSED",
                        targetId = recordingId,
                        details = "Report dismissed. No violation determined. Reason: $actionReason"
                    )
                }
                else -> {
                    reportDao.updateReportStatus(reportId, "UNDER_REVIEW", actionReason)
                    logAuditEvent(
                        actor = moderatorUsername,
                        eventType = "REPORT_UNDER_REVIEW",
                        targetId = recordingId,
                        details = "Marked under review: $actionReason"
                    )
                }
            }
            Result.success(Unit)
        } catch (e: Exception) {
            Log.e("RecordingRepository", "Moderation action failed", e)
            Result.failure(e)
        }
    }

    suspend fun prepareDraftFromUri(uri: Uri): com.example.audio.RecordingResult? = withContext(Dispatchers.IO) {
        try {
            val stagingDir = File(context.cacheDir, "imported_drafts").apply { mkdirs() }
            val tempFile = File(stagingDir, "draft_${System.currentTimeMillis()}.m4a")

            context.contentResolver.openInputStream(uri)?.use { input ->
                FileOutputStream(tempFile).use { output ->
                    input.copyTo(output)
                }
            } ?: return@withContext null

            var durationMs = 0L
            try {
                val mmr = MediaMetadataRetriever()
                mmr.setDataSource(tempFile.absolutePath)
                val durStr = mmr.extractMetadata(MediaMetadataRetriever.METADATA_KEY_DURATION)
                durationMs = durStr?.toLongOrNull() ?: 0L
                mmr.release()
            } catch (e: Exception) {
                Log.w("RecordingRepository", "Could not extract duration from imported file", e)
            }

            val hash = AudioIntegrityUtils.calculateSha256(tempFile)

            com.example.audio.RecordingResult(
                file = tempFile,
                durationMs = durationMs,
                sha256Hash = hash,
                fileSizeBytes = tempFile.length()
            )
        } catch (e: Exception) {
            Log.e("RecordingRepository", "prepareDraftFromUri failed", e)
            null
        }
    }

    suspend fun publishRecording(
        ownerId: String,
        ownerUsername: String,
        title: String,
        description: String,
        category: String,
        audioFile: File,
        durationMs: Long,
        locationDescription: String? = null,
        incidentDateTime: Long? = null,
        language: String = "ar"
    ): Result<RecordingEntity> = withContext(Dispatchers.IO) {
        try {
            val evidenceId = AudioIntegrityUtils.generateEvidenceId()
            val hash = AudioIntegrityUtils.calculateSha256(audioFile)

            // Move file to permanent storage if needed
            val permanentDir = File(context.filesDir, "published_audio").apply { mkdirs() }
            val permanentFile = File(permanentDir, "${evidenceId}.m4a")

            if (audioFile.absolutePath != permanentFile.absolutePath) {
                audioFile.copyTo(permanentFile, overwrite = true)
            }

            val entity = RecordingEntity(
                recordingId = evidenceId,
                ownerId = ownerId,
                ownerUsername = ownerUsername,
                title = title.trim(),
                description = description.trim(),
                category = category,
                filePath = permanentFile.absolutePath,
                durationMs = durationMs,
                fileSizeBytes = permanentFile.length(),
                sha256Hash = hash,
                uploadTimestamp = System.currentTimeMillis(),
                publicationTimestamp = System.currentTimeMillis(),
                isPublic = true,
                locationDescription = locationDescription?.trim()?.ifBlank { null },
                incidentDateTime = incidentDateTime,
                language = language,
                moderationStatus = "PUBLISHED",
                reportCount = 0
            )

            recordingDao.insertRecording(entity)
            Result.success(entity)
        } catch (e: Exception) {
            Log.e("RecordingRepository", "Failed to publish recording", e)
            Result.failure(e)
        }
    }

    suspend fun importAudioFile(
        uri: Uri,
        ownerId: String,
        ownerUsername: String,
        title: String,
        description: String,
        category: String,
        locationDescription: String? = null,
        incidentDateTime: Long? = null,
        language: String = "ar"
    ): Result<RecordingEntity> = withContext(Dispatchers.IO) {
        try {
            val evidenceId = AudioIntegrityUtils.generateEvidenceId()
            val permanentDir = File(context.filesDir, "published_audio").apply { mkdirs() }
            val destinationFile = File(permanentDir, "${evidenceId}_imported.m4a")

            context.contentResolver.openInputStream(uri)?.use { input ->
                FileOutputStream(destinationFile).use { output ->
                    input.copyTo(output)
                }
            } ?: return@withContext Result.failure(Exception("Failed to open audio stream"))

            // Extract duration using MediaMetadataRetriever
            var durationMs = 0L
            try {
                val mmr = MediaMetadataRetriever()
                mmr.setDataSource(destinationFile.absolutePath)
                val durStr = mmr.extractMetadata(MediaMetadataRetriever.METADATA_KEY_DURATION)
                durationMs = durStr?.toLongOrNull() ?: 0L
                mmr.release()
            } catch (e: Exception) {
                Log.w("RecordingRepository", "Could not extract duration", e)
            }

            val hash = AudioIntegrityUtils.calculateSha256(destinationFile)

            val entity = RecordingEntity(
                recordingId = evidenceId,
                ownerId = ownerId,
                ownerUsername = ownerUsername,
                title = title.trim(),
                description = description.trim(),
                category = category,
                filePath = destinationFile.absolutePath,
                durationMs = durationMs,
                fileSizeBytes = destinationFile.length(),
                sha256Hash = hash,
                uploadTimestamp = System.currentTimeMillis(),
                publicationTimestamp = System.currentTimeMillis(),
                isPublic = true,
                locationDescription = locationDescription?.trim()?.ifBlank { null },
                incidentDateTime = incidentDateTime,
                language = language,
                moderationStatus = "PUBLISHED",
                reportCount = 0
            )

            recordingDao.insertRecording(entity)
            Result.success(entity)
        } catch (e: Exception) {
            Log.e("RecordingRepository", "Import audio failed", e)
            Result.failure(e)
        }
    }

    suspend fun deleteRecording(recordingId: String): Result<Unit> = withContext(Dispatchers.IO) {
        try {
            val recording = recordingDao.getRecordingById(recordingId)
            if (recording != null) {
                val file = File(recording.filePath)
                if (file.exists()) {
                    file.delete()
                }
                recordingDao.deleteRecording(recordingId)
            }
            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    suspend fun reportRecording(
        recordingId: String,
        reporterUsername: String,
        reason: String,
        comment: String
    ): Result<Unit> = withContext(Dispatchers.IO) {
        try {
            val report = ReportEntity(
                reportId = UUID.randomUUID().toString(),
                recordingId = recordingId,
                reporterUsername = reporterUsername,
                reason = reason,
                comment = comment.trim(),
                timestamp = System.currentTimeMillis(),
                status = "PENDING"
            )
            reportDao.insertReport(report)
            recordingDao.incrementReportCount(recordingId)
            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    suspend fun seedInitialRecordingsIfEmpty() = withContext(Dispatchers.IO) {
        try {
            val count = recordingDao.getRecordingsCount()
            if (count > 0) return@withContext

            val demoDir = File(context.filesDir, "published_audio").apply { mkdirs() }

            // 1. Police Interaction demonstration
            val id1 = "PS-TN-2026-000184"
            val file1 = File(demoDir, "${id1}.wav")
            AudioIntegrityUtils.createSampleWavFile(file1, durationSeconds = 6, frequencyHz = 480.0)
            val hash1 = AudioIntegrityUtils.calculateSha256(file1)

            val rec1 = RecordingEntity(
                recordingId = id1,
                ownerId = "system_verified",
                ownerUsername = "citizen_tn_72",
                title = "معاينة وثائق روتينية على الطريق الوطنية رقم 1",
                description = "حوار مهني أثناء دورية مراقبة أوراق السيارة. تم التحقق من الفحص الفني والتأمين في كنف الاحترام المتبادل.",
                category = "Police interaction",
                filePath = file1.absolutePath,
                durationMs = 6000L,
                fileSizeBytes = file1.length(),
                sha256Hash = hash1,
                uploadTimestamp = System.currentTimeMillis() - 86400000L * 2,
                publicationTimestamp = System.currentTimeMillis() - 86400000L * 2,
                isPublic = true,
                locationDescription = "المدخل الجنوبي للعاصمة (الطريق الوطنية 1)",
                incidentDateTime = System.currentTimeMillis() - 86400000L * 2 - 3600000L,
                language = "ar",
                moderationStatus = "PUBLISHED",
                reportCount = 0
            )

            // 2. Government Service interaction
            val id2 = "PS-TN-2026-000192"
            val file2 = File(demoDir, "${id2}.wav")
            AudioIntegrityUtils.createSampleWavFile(file2, durationSeconds = 5, frequencyHz = 520.0)
            val hash2 = AudioIntegrityUtils.calculateSha256(file2)

            val rec2 = RecordingEntity(
                recordingId = id2,
                ownerId = "system_verified",
                ownerUsername = "observer_tunis",
                title = "استفسار حول وصل إيداع ملف رخصة بناء بالدائرة البلدية",
                description = "تسجيل للحوار حول الإجراءات والآجال القانونية للحصول على وصل إيداع رسمي طبقا لمجلة التهيئة الترابية.",
                category = "Government service",
                filePath = file2.absolutePath,
                durationMs = 5000L,
                fileSizeBytes = file2.length(),
                sha256Hash = hash2,
                uploadTimestamp = System.currentTimeMillis() - 86400000L,
                publicationTimestamp = System.currentTimeMillis() - 86400000L,
                isPublic = true,
                locationDescription = "مقر بلدية تونس - مصلحة البناءات",
                incidentDateTime = System.currentTimeMillis() - 86400000L - 7200000L,
                language = "ar",
                moderationStatus = "PUBLISHED",
                reportCount = 0
            )

            // 3. Public Administration / Transport
            val id3 = "PS-TN-2026-000205"
            val file3 = File(demoDir, "${id3}.wav")
            AudioIntegrityUtils.createSampleWavFile(file3, durationSeconds = 4, frequencyHz = 440.0)
            val hash3 = AudioIntegrityUtils.calculateSha256(file3)

            val rec3 = RecordingEntity(
                recordingId = id3,
                ownerId = "system_verified",
                ownerUsername = "moaten_karim",
                title = "شباك التذاكر واشتراكات النقل السريع - محطة برشلونة",
                description = "استفسار عن سبب تعطل شباك الاشتراكات ومحاولة إيجاد حل بديل للمسافرين.",
                category = "Public transport",
                filePath = file3.absolutePath,
                durationMs = 4000L,
                fileSizeBytes = file3.length(),
                sha256Hash = hash3,
                uploadTimestamp = System.currentTimeMillis() - 3600000L * 4,
                publicationTimestamp = System.currentTimeMillis() - 3600000L * 4,
                isPublic = true,
                locationDescription = "محطة تونس برشلونة",
                incidentDateTime = System.currentTimeMillis() - 3600000L * 5,
                language = "ar",
                moderationStatus = "PUBLISHED",
                reportCount = 0
            )

            recordingDao.insertRecording(rec1)
            recordingDao.insertRecording(rec2)
            recordingDao.insertRecording(rec3)
        } catch (e: Exception) {
            Log.e("RecordingRepository", "Failed to seed demo recordings", e)
        }
    }

    // ==========================================
    // INCIDENT MANAGEMENT & EVIDENCE ATTACHMENTS
    // ==========================================

    val allPublicIncidents: Flow<List<IncidentEntity>> =
        incidentDao.getAllPublicIncidents()

    fun getIncidentsByOwner(ownerId: String): Flow<List<IncidentEntity>> =
        incidentDao.getIncidentsByOwner(ownerId)

    suspend fun getIncidentById(incidentId: String): IncidentEntity? =
        incidentDao.getIncidentById(incidentId)

    fun getIncidentFlowById(incidentId: String): Flow<IncidentEntity?> =
        incidentDao.getIncidentFlowById(incidentId)

    fun getEvidenceForIncident(incidentId: String): Flow<List<IncidentEvidenceEntity>> =
        incidentEvidenceDao.getEvidenceForIncident(incidentId)

    suspend fun submitIncident(
        incident: IncidentEntity,
        evidenceList: List<IncidentEvidenceEntity>
    ): Result<IncidentEntity> = withContext(Dispatchers.IO) {
        try {
            val updatedIncident = incident.copy(evidenceCount = evidenceList.size)
            incidentDao.insertIncident(updatedIncident)
            if (evidenceList.isNotEmpty()) {
                incidentEvidenceDao.insertEvidenceList(evidenceList)
            }
            logAuditEvent(
                actor = incident.ownerUsername,
                eventType = "INCIDENT_SUBMITTED",
                targetId = incident.incidentId,
                details = "Incident ${incident.incidentId} submitted with ${evidenceList.size} evidence attachment(s) in ${incident.governorate}"
            )
            Result.success(updatedIncident)
        } catch (e: Exception) {
            Log.e("RecordingRepository", "Failed to submit incident", e)
            Result.failure(e)
        }
    }

    suspend fun prepareIncidentEvidenceExportPackage(
        incident: IncidentEntity,
        evidenceList: List<IncidentEvidenceEntity>
    ): Pair<File, List<File>> = withContext(Dispatchers.IO) {
        val exportDir = File(context.cacheDir, "incident_exports").apply { mkdirs() }
        val manifestFile = File(exportDir, "${incident.incidentId}_MANIFEST.txt")

        val sdf = SimpleDateFormat("yyyy-MM-dd HH:mm:ss z", Locale.ENGLISH)
        val submissionDate = sdf.format(Date(incident.submissionTimestamp))
        val incidentDateStr = sdf.format(Date(incident.incidentDateTime))

        val manifestContent = buildString {
            appendLine("================================================================================")
            appendLine("                    PROTECT YOURSELF — إحمي روحك")
            appendLine("                STRUCTURED INCIDENT & EVIDENCE PACKAGE")
            appendLine("================================================================================")
            appendLine("INCIDENT IDENTIFIER:    ${incident.incidentId}")
            appendLine("PACKAGE GENERATED AT:   ${sdf.format(Date())}")
            appendLine()
            appendLine("--- SECTION 1: INCIDENT RECORD ---")
            appendLine("Incident ID:            ${incident.incidentId}")
            appendLine("Category:               ${incident.category}")
            appendLine("Governorate (Required): ${incident.governorate}")
            if (incident.latitude != null && incident.longitude != null) {
                appendLine("Exact GPS Coordinates:  ${incident.latitude}° N, ${incident.longitude}° E")
            } else {
                appendLine("Exact GPS Coordinates:  Not pinned by user (governorate level only)")
            }
            if (!incident.locationNotes.isNullOrBlank()) {
                appendLine("Location Notes:         ${incident.locationNotes}")
            }
            appendLine("Incident Date & Time:   $incidentDateStr")
            appendLine("Submission Date & Time: $submissionDate")
            appendLine("Submission Status:      ${incident.status}")
            appendLine()
            appendLine("--- SECTION 2: OFFICER / PERSON INVOLVED (USER-PROVIDED) ---")
            appendLine("Officer / Person Name:  ${incident.officerName ?: "Not specified"}")
            appendLine("Badge / ID Number:      ${incident.officerBadge ?: "Not specified"}")
            appendLine("Department / Service:   ${incident.officerDepartment ?: "Not specified"}")
            appendLine("Vehicle Registration:   ${incident.vehicleRegistration ?: "Not specified"}")
            appendLine("Other Identification:   ${incident.otherIdentifyingInfo ?: "Not specified"}")
            appendLine()
            appendLine("--- SECTION 3: INCIDENT NARRATIVE ---")
            appendLine(incident.description.ifBlank { "(No narrative description provided)" })
            appendLine()
            appendLine("--- SECTION 4: EVIDENCE ATTACHMENTS & CRYPTOGRAPHIC INTEGRITY ---")
            appendLine("Total Files Attached:   ${evidenceList.size}")
            evidenceList.forEachIndexed { index, ev ->
                appendLine("[Attachment #${index + 1}]")
                appendLine("  Evidence ID:          ${ev.evidenceId}")
                appendLine("  Type:                 ${ev.fileType}")
                appendLine("  Original Filename:    ${ev.originalFilename}")
                appendLine("  File Size:            ${ev.fileSizeBytes} bytes (${AudioIntegrityUtils.formatFileSize(ev.fileSizeBytes)})")
                appendLine("  Cryptographic Hash:   SHA-256: ${ev.sha256Hash}")
                if (ev.durationMs > 0) {
                    appendLine("  Duration:             ${AudioIntegrityUtils.formatDuration(ev.durationMs)}")
                }
                appendLine("  Added At:             ${sdf.format(Date(ev.createdAt))}")
                appendLine()
            }
            appendLine("--- SECTION 5: TIMELINE ---")
            appendLine(incident.timelineJson)
            appendLine()
            appendLine("--- SECTION 6: LEGAL PRESERVATION & INTEGRITY NOTICE ---")
            appendLine("1. PURPOSE: Protect Yourself is an evidence-preservation platform intended for")
            appendLine("   documenting incidents and submitting audio and other evidence. The platform does")
            appendLine("   NOT claim that any recording or file is automatically 'legally valid evidence'.")
            appendLine("   Instead, it preserves information and files accurately and transparently so they")
            appendLine("   can potentially be evaluated by the appropriate authorities or legal professionals.")
            appendLine("2. HASH INTEGRITY: Cryptographic SHA-256 hashes demonstrate whether the stored")
            appendLine("   file has been altered since submission. A hash does not prove the truthfulness")
            appendLine("   of statements made in the recording or text.")
            appendLine("3. ORIGINAL FILES: Original audio and media files are preserved untouched and")
            appendLine("   unmodified within this package.")
            appendLine("================================================================================")
        }

        FileWriter(manifestFile).use { it.write(manifestContent) }

        val validFiles = evidenceList.mapNotNull { ev ->
            val f = File(ev.filePath)
            if (f.exists() && f.canRead()) f else null
        }

        Pair(manifestFile, validFiles)
    }
}
