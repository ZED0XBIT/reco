package com.example.viewmodel

import android.app.Application
import android.content.Context
import android.content.Intent
import android.net.Uri
import androidx.core.content.FileProvider
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.audio.AudioIntegrityUtils
import com.example.audio.AudioPlayerManager
import com.example.audio.AudioRecorderManager
import com.example.audio.RecordingResult
import com.example.data.local.AppDatabase
import com.example.data.model.AuditLogEntity
import com.example.data.model.IncidentEntity
import com.example.data.model.IncidentEvidenceEntity
import com.example.data.model.RecordingEntity
import com.example.data.model.ReportEntity
import com.example.data.model.TimelineEntryItem
import com.example.data.model.UserEntity
import com.example.data.repository.AuthRepository
import com.example.data.repository.AuthResult
import com.example.data.repository.IntegrityVerificationResult
import com.example.data.repository.RecordingRepository
import com.example.i18n.AppLanguage
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.firstOrNull
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import java.io.File
import java.io.FileOutputStream
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

enum class NavTab {
    HOME,
    FEED,
    RECORD,
    MY_AUDIOS,
    ACCOUNT
}

data class PublishDraftState(
    val title: String = "",
    val description: String = "",
    val category: String = "Police interaction",
    val locationDescription: String = "",
    val incidentDate: Long? = null,
    val pendingFile: File? = null,
    val pendingDurationMs: Long = 0L,
    val pendingSha256: String = "",
    val pendingFileSize: Long = 0L
)

@OptIn(ExperimentalCoroutinesApi::class)
class MainViewModel(application: Application) : AndroidViewModel(application) {

    private val db = AppDatabase.getDatabase(application)
    val authRepository = AuthRepository(db.userDao(), application)
    val recordingRepository = RecordingRepository(
        db.recordingDao(),
        db.reportDao(),
        db.savedRecordingDao(),
        db.incidentDao(),
        db.incidentEvidenceDao(),
        db.auditLogDao(),
        application
    )
    val audioRecorderManager = AudioRecorderManager(application)
    val audioPlayerManager = AudioPlayerManager()

    private val prefs = application.getSharedPreferences("protect_app_prefs", Context.MODE_PRIVATE)

    // Legal Gate acceptance
    private val _legalGateAccepted = MutableStateFlow(prefs.getBoolean("legal_gate_accepted", false))
    val legalGateAccepted: StateFlow<Boolean> = _legalGateAccepted.asStateFlow()

    // Language state
    private val savedLangCode = prefs.getString("selected_lang", AppLanguage.ARABIC.code) ?: AppLanguage.ARABIC.code
    private val _currentLanguage = MutableStateFlow(
        AppLanguage.values().firstOrNull { it.code == savedLangCode } ?: AppLanguage.ARABIC
    )
    val currentLanguage: StateFlow<AppLanguage> = _currentLanguage.asStateFlow()

    // Theme Mode
    private val savedTheme = prefs.getString("selected_theme", "LIGHT") ?: "LIGHT"
    private val _themeMode = MutableStateFlow(
        try { com.example.ui.theme.AppThemeMode.valueOf(savedTheme) } catch(e: Exception) { com.example.ui.theme.AppThemeMode.LIGHT }
    )
    val themeMode: StateFlow<com.example.ui.theme.AppThemeMode> = _themeMode.asStateFlow()

    fun setThemeMode(mode: com.example.ui.theme.AppThemeMode) {
        _themeMode.value = mode
        prefs.edit().putString("selected_theme", mode.name).apply()
    }

    // Navigation
    private val _currentTab = MutableStateFlow(NavTab.HOME)
    val currentTab: StateFlow<NavTab> = _currentTab.asStateFlow()

    // Selected recording for detail view
    private val _selectedRecording = MutableStateFlow<RecordingEntity?>(null)
    val selectedRecording: StateFlow<RecordingEntity?> = _selectedRecording.asStateFlow()

    // UI Dialogs & Sheets
    private val _showLegalSheet = MutableStateFlow(false)
    val showLegalSheet: StateFlow<Boolean> = _showLegalSheet.asStateFlow()

    private val _showPublicWarningSheet = MutableStateFlow(false)
    val showPublicWarningSheet: StateFlow<Boolean> = _showPublicWarningSheet.asStateFlow()

    private val _reportingRecordingId = MutableStateFlow<String?>(null)
    val reportingRecordingId: StateFlow<String?> = _reportingRecordingId.asStateFlow()

    private val _snackbarMessage = MutableStateFlow<String?>(null)
    val snackbarMessage: StateFlow<String?> = _snackbarMessage.asStateFlow()

    // QR Evidence Card preview dialog
    private val _showQrEvidenceCard = MutableStateFlow<RecordingEntity?>(null)
    val showQrEvidenceCard: StateFlow<RecordingEntity?> = _showQrEvidenceCard.asStateFlow()

    fun openQrEvidenceCard(rec: RecordingEntity) {
        _showQrEvidenceCard.value = rec
    }

    fun closeQrEvidenceCard() {
        _showQrEvidenceCard.value = null
    }

    // Integrity Check state
    private val _integrityVerificationState = MutableStateFlow<IntegrityVerificationResult?>(null)
    val integrityVerificationState: StateFlow<IntegrityVerificationResult?> = _integrityVerificationState.asStateFlow()

    private val _isVerifyingIntegrity = MutableStateFlow(false)
    val isVerifyingIntegrity: StateFlow<Boolean> = _isVerifyingIntegrity.asStateFlow()

    fun verifyIntegrity(recordingId: String) {
        viewModelScope.launch {
            _isVerifyingIntegrity.value = true
            _integrityVerificationState.value = null
            val result = recordingRepository.verifyIntegrity(recordingId)
            _integrityVerificationState.value = result
            _isVerifyingIntegrity.value = false
        }
    }

    fun clearIntegrityResult() {
        _integrityVerificationState.value = null
    }

    // Duplicate upload warning
    private val _duplicateWarning = MutableStateFlow<RecordingEntity?>(null)
    val duplicateWarning: StateFlow<RecordingEntity?> = _duplicateWarning.asStateFlow()

    fun clearDuplicateWarning() {
        _duplicateWarning.value = null
    }

    // Search and filter for public feed
    val searchQuery = MutableStateFlow("")
    val selectedCategoryFilter = MutableStateFlow<String?>(null) // null = all
    val selectedDateFilter = MutableStateFlow("ALL") // ALL, TODAY, WEEK, MONTH

    // Draft for review & publication
    private val _publishDraft = MutableStateFlow(PublishDraftState())
    val publishDraft: StateFlow<PublishDraftState> = _publishDraft.asStateFlow()

    private val _isPublishing = MutableStateFlow(false)
    val isPublishing: StateFlow<Boolean> = _isPublishing.asStateFlow()

    // Filtered public recordings
    val filteredPublicRecordings: StateFlow<List<RecordingEntity>> = combine(
        recordingRepository.allPublicRecordings,
        searchQuery,
        selectedCategoryFilter,
        selectedDateFilter
    ) { recordings, query, catFilter, dateFilter ->
        val now = System.currentTimeMillis()
        recordings.filter { rec ->
            val matchesQuery = if (query.isBlank()) true else {
                rec.title.contains(query, ignoreCase = true) ||
                rec.description.contains(query, ignoreCase = true) ||
                rec.category.contains(query, ignoreCase = true) ||
                rec.recordingId.contains(query, ignoreCase = true) ||
                rec.ownerUsername.contains(query, ignoreCase = true)
            }
            val matchesCat = if (catFilter == null) true else rec.category == catFilter
            val matchesDate = when (dateFilter) {
                "TODAY" -> (now - rec.publicationTimestamp) < 86400000L
                "WEEK" -> (now - rec.publicationTimestamp) < 86400000L * 7
                "MONTH" -> (now - rec.publicationTimestamp) < 86400000L * 30
                else -> true
            }
            matchesQuery && matchesCat && matchesDate
        }
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    // Saved recordings for current user
    val savedRecordings: StateFlow<List<RecordingEntity>> = authRepository.currentUser.flatMapLatest { user ->
        if (user != null) {
            recordingRepository.getSavedRecordingsForUser(user.userId)
        } else {
            flowOf(emptyList())
        }
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    fun toggleSaveRecording(recordingId: String) {
        val user = authRepository.currentUser.value
        if (user == null) {
            _snackbarMessage.value = when (_currentLanguage.value) {
                AppLanguage.ARABIC -> "يرجى تسجيل الدخول لحفظ الأدلة في قائمتك الخاصة"
                AppLanguage.FRENCH -> "Connectez-vous pour enregistrer cette preuve"
                AppLanguage.ENGLISH -> "Sign in to save evidence privately"
            }
            return
        }
        viewModelScope.launch {
            val saved = recordingRepository.toggleSaveRecording(user.userId, recordingId)
            _snackbarMessage.value = if (saved) {
                when (_currentLanguage.value) {
                    AppLanguage.ARABIC -> "تم حفظ الدليل في قائمتك الخاصة"
                    AppLanguage.FRENCH -> "Enregistrement sauvegardé"
                    AppLanguage.ENGLISH -> "Saved to your private evidence"
                }
            } else {
                when (_currentLanguage.value) {
                    AppLanguage.ARABIC -> "تمت إزالة الدليل من قائمتك الخاصة"
                    AppLanguage.FRENCH -> "Supprimé des sauvegardes"
                    AppLanguage.ENGLISH -> "Removed from saved evidence"
                }
            }
        }
    }

    // Moderation & Audit
    val pendingReports: StateFlow<List<ReportEntity>> = recordingRepository.getPendingReports()
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val auditLogs: StateFlow<List<AuditLogEntity>> = recordingRepository.getRecentAuditLogs()
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    fun takeModerationAction(reportId: String, recordingId: String, action: String, reason: String) {
        val user = authRepository.currentUser.value
        val modName = user?.username ?: "system_moderator"
        viewModelScope.launch {
            val res = recordingRepository.takeModerationAction(reportId, recordingId, action, modName, reason)
            res.onSuccess {
                _snackbarMessage.value = "Moderation action recorded: $action"
            }.onFailure {
                _snackbarMessage.value = "Moderation action failed: ${it.localizedMessage}"
            }
        }
    }

    // Current user's recordings
    val myRecordings: StateFlow<List<RecordingEntity>> = combine(
        authRepository.currentUser,
        recordingRepository.allPublicRecordings
    ) { user, recordings ->
        if (user == null) emptyList()
        else recordings.filter { it.ownerId == user.userId }
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    // ==========================================
    // STRUCTURED INCIDENT REPORTING FLOW
    // ==========================================
    private val _incidentDraft = MutableStateFlow(
        IncidentDraftState(incidentId = AudioIntegrityUtils.generateIncidentId())
    )
    val incidentDraft: StateFlow<IncidentDraftState> = _incidentDraft.asStateFlow()

    private val _selectedIncident = MutableStateFlow<IncidentEntity?>(null)
    val selectedIncident: StateFlow<IncidentEntity?> = _selectedIncident.asStateFlow()

    val selectedIncidentEvidence: StateFlow<List<IncidentEvidenceEntity>> = _selectedIncident.flatMapLatest { inc ->
        if (inc != null) recordingRepository.getEvidenceForIncident(inc.incidentId)
        else flowOf(emptyList())
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val allPublicIncidents: StateFlow<List<IncidentEntity>> = recordingRepository.allPublicIncidents
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val myIncidents: StateFlow<List<IncidentEntity>> = combine(
        authRepository.currentUser,
        recordingRepository.allPublicIncidents
    ) { user, incidents ->
        if (user == null) emptyList()
        else incidents.filter { it.ownerId == user.userId }
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    fun startNewIncident() {
        val newId = AudioIntegrityUtils.generateIncidentId()
        _incidentDraft.value = IncidentDraftState(
            incidentId = newId,
            incidentDateTime = System.currentTimeMillis()
        )
    }

    fun setIncidentTerms(rightToSubmit: Boolean, responsible: Boolean, storageUnderstood: Boolean) {
        _incidentDraft.value = _incidentDraft.value.copy(
            termsRightToSubmitConfirmed = rightToSubmit,
            termsResponsibleConfirmed = responsible,
            termsStorageUnderstoodConfirmed = storageUnderstood,
            termsAcceptedAt = System.currentTimeMillis()
        )
    }

    fun setIncidentDateTime(timestamp: Long) {
        _incidentDraft.value = _incidentDraft.value.copy(
            incidentDateTime = timestamp
        )
    }

    fun setIncidentGovernorate(gov: String) {
        _incidentDraft.value = _incidentDraft.value.copy(
            governorate = gov
        )
    }

    fun setIncidentCoordinates(lat: Double?, lng: Double?) {
        _incidentDraft.value = _incidentDraft.value.copy(
            latitude = lat,
            longitude = lng
        )
    }

    fun setIncidentLocationNotes(notes: String) {
        _incidentDraft.value = _incidentDraft.value.copy(
            locationNotes = notes
        )
    }

    fun setIncidentCategory(category: String) {
        _incidentDraft.value = _incidentDraft.value.copy(
            category = category
        )
    }

    fun setIncidentTitle(title: String) {
        _incidentDraft.value = _incidentDraft.value.copy(
            title = title
        )
    }

    fun setIncidentDescription(desc: String) {
        _incidentDraft.value = _incidentDraft.value.copy(
            description = desc
        )
    }

    fun setIncidentOfficerInfo(name: String, badge: String, dept: String, vehicle: String, other: String) {
        _incidentDraft.value = _incidentDraft.value.copy(
            officerName = name,
            officerBadge = badge,
            officerDepartment = dept,
            vehicleRegistration = vehicle,
            otherIdentifyingInfo = other
        )
    }

    fun setIncidentStep(step: Int) {
        _incidentDraft.value = _incidentDraft.value.copy(
            currentStep = step
        )
    }

    fun attachRecordedAudioToIncident(result: RecordingResult) {
        val incidentId = _incidentDraft.value.incidentId.ifBlank { AudioIntegrityUtils.generateIncidentId() }
        val permanentDir = File(getApplication<Application>().filesDir, "incident_evidence").apply { mkdirs() }
        val destFile = File(permanentDir, "${incidentId}_AUDIO_${System.currentTimeMillis()}.m4a")
        result.file.copyTo(destFile, overwrite = true)

        val hash = AudioIntegrityUtils.calculateSha256(destFile)
        val evidence = IncidentEvidenceEntity(
            evidenceId = AudioIntegrityUtils.generateEvidenceId(),
            incidentId = incidentId,
            fileType = "AUDIO",
            originalFilename = "original_recording_${System.currentTimeMillis()}.m4a",
            filePath = destFile.absolutePath,
            fileSizeBytes = destFile.length(),
            sha256Hash = hash,
            durationMs = result.durationMs,
            mimeType = "audio/mp4"
        )

        val newTimeline = _incidentDraft.value.timeline + TimelineEntryItem(
            timestamp = System.currentTimeMillis(),
            label = "Audio recording attached (${AudioIntegrityUtils.formatDuration(result.durationMs)})",
            eventType = "RECORDING_ENDED"
        )

        _incidentDraft.value = _incidentDraft.value.copy(
            incidentId = incidentId,
            attachments = _incidentDraft.value.attachments + evidence,
            timeline = newTimeline
        )

        setDraftFromRecording(result)
    }

    fun attachFileFromUri(uri: Uri, fileType: String, originalFilename: String) {
        viewModelScope.launch {
            try {
                val context = getApplication<Application>()
                val incidentId = _incidentDraft.value.incidentId.ifBlank { AudioIntegrityUtils.generateIncidentId() }
                val stagingDir = File(context.filesDir, "incident_evidence").apply { mkdirs() }
                val safeFilename = "${incidentId}_${fileType}_${System.currentTimeMillis()}_${originalFilename.filter { it.isLetterOrDigit() || it == '.' }}"
                val destFile = File(stagingDir, safeFilename)

                context.contentResolver.openInputStream(uri)?.use { input ->
                    FileOutputStream(destFile).use { output ->
                        input.copyTo(output)
                    }
                }

                val hash = AudioIntegrityUtils.calculateSha256(destFile)
                var durationMs = 0L
                if (fileType == "AUDIO" || fileType == "VIDEO") {
                    try {
                        val mmr = android.media.MediaMetadataRetriever()
                        mmr.setDataSource(destFile.absolutePath)
                        durationMs = mmr.extractMetadata(android.media.MediaMetadataRetriever.METADATA_KEY_DURATION)?.toLongOrNull() ?: 0L
                        mmr.release()
                    } catch (e: Exception) { }
                }

                val evidence = IncidentEvidenceEntity(
                    evidenceId = AudioIntegrityUtils.generateEvidenceId(),
                    incidentId = incidentId,
                    fileType = fileType,
                    originalFilename = originalFilename,
                    filePath = destFile.absolutePath,
                    fileSizeBytes = destFile.length(),
                    sha256Hash = hash,
                    durationMs = durationMs,
                    mimeType = context.contentResolver.getType(uri) ?: ""
                )

                val eventType = when(fileType) {
                    "PHOTO" -> "PHOTO_ADDED"
                    "VIDEO" -> "VIDEO_ADDED"
                    "DOCUMENT" -> "DOCUMENT_ADDED"
                    else -> "AUDIO_ADDED"
                }

                val newTimeline = _incidentDraft.value.timeline + TimelineEntryItem(
                    timestamp = System.currentTimeMillis(),
                    label = "$fileType evidence attached: $originalFilename",
                    eventType = eventType
                )

                _incidentDraft.value = _incidentDraft.value.copy(
                    incidentId = incidentId,
                    attachments = _incidentDraft.value.attachments + evidence,
                    timeline = newTimeline
                )

                _snackbarMessage.value = when (_currentLanguage.value) {
                    AppLanguage.ARABIC -> "تم إرفاق الدليل وحفظ البصمة الرقمية بنجاح"
                    AppLanguage.FRENCH -> "Preuve attachée et empreinte SHA-256 enregistrée"
                    AppLanguage.ENGLISH -> "Evidence attached and SHA-256 hash recorded"
                }
            } catch (e: Exception) {
                _snackbarMessage.value = "Failed to attach file: ${e.localizedMessage}"
            }
        }
    }

    fun removeIncidentAttachment(evidenceId: String) {
        _incidentDraft.value = _incidentDraft.value.copy(
            attachments = _incidentDraft.value.attachments.filterNot { it.evidenceId == evidenceId }
        )
    }

    fun submitIncident() {
        val draft = _incidentDraft.value
        val user = authRepository.currentUser.value
        val ownerId = user?.userId ?: "citizen_local"
        val ownerUsername = user?.username ?: "citizen_reporter"

        if (!draft.areTermsAccepted) {
            _snackbarMessage.value = when (_currentLanguage.value) {
                AppLanguage.ARABIC -> "يجب الموافقة على الشروط المطلوبة قبل الإرسال"
                AppLanguage.FRENCH -> "Veuillez accepter les conditions requises"
                AppLanguage.ENGLISH -> "Please accept required terms before submission"
            }
            return
        }

        if (draft.governorate.isBlank()) {
            _snackbarMessage.value = when (_currentLanguage.value) {
                AppLanguage.ARABIC -> "يرجى تحديد الولاية التونسية (مطلوب)"
                AppLanguage.FRENCH -> "Veuillez sélectionner le gouvernorat (requis)"
                AppLanguage.ENGLISH -> "Please select the governorate (required)"
            }
            return
        }

        if (draft.attachments.isEmpty()) {
            _snackbarMessage.value = when (_currentLanguage.value) {
                AppLanguage.ARABIC -> "يرجى إرفاق دليل واحد على الأقل (تسجيل صوتي، صور، أو وثائق)"
                AppLanguage.FRENCH -> "Veuillez attacher au moins une preuve"
                AppLanguage.ENGLISH -> "Please attach at least one piece of evidence"
            }
            return
        }

        viewModelScope.launch {
            _incidentDraft.value = _incidentDraft.value.copy(isSubmitting = true)

            val sdf = SimpleDateFormat("yyyy-MM-dd HH:mm", Locale.ENGLISH)
            val timelineEntries = mutableListOf<String>()
            timelineEntries.add("${sdf.format(Date(draft.incidentDateTime))} — Incident occurred")
            draft.timeline.forEach {
                timelineEntries.add("${sdf.format(Date(it.timestamp))} — ${it.label}")
            }
            timelineEntries.add("${sdf.format(Date())} — Evidence package submitted to platform")

            val timelineJson = timelineEntries.joinToString(separator = "\n")

            val titleToUse = draft.title.ifBlank {
                "${draft.category} - ${draft.governorate}"
            }

            val incident = IncidentEntity(
                incidentId = draft.incidentId,
                ownerId = ownerId,
                ownerUsername = ownerUsername,
                title = titleToUse,
                description = draft.description,
                category = draft.category,
                governorate = draft.governorate,
                latitude = draft.latitude,
                longitude = draft.longitude,
                locationNotes = draft.locationNotes.ifBlank { null },
                incidentDateTime = draft.incidentDateTime,
                submissionTimestamp = System.currentTimeMillis(),
                officerName = draft.officerName.ifBlank { null },
                officerBadge = draft.officerBadge.ifBlank { null },
                officerDepartment = draft.officerDepartment.ifBlank { null },
                vehicleRegistration = draft.vehicleRegistration.ifBlank { null },
                otherIdentifyingInfo = draft.otherIdentifyingInfo.ifBlank { null },
                termsAcceptedAt = draft.termsAcceptedAt,
                timelineJson = timelineJson,
                status = "SUBMITTED",
                evidenceCount = draft.attachments.size,
                isPublic = true
            )

            val res = recordingRepository.submitIncident(incident, draft.attachments)
            res.onSuccess { submitted ->
                _incidentDraft.value = _incidentDraft.value.copy(
                    isSubmitting = false,
                    submittedIncident = submitted,
                    currentStep = 7 // Confirmation step
                )

                // If audio attachment exists, also insert into recordings so feed & player can access
                val audioAtt = draft.attachments.firstOrNull { it.fileType == "AUDIO" }
                if (audioAtt != null) {
                    try {
                        val recEntity = RecordingEntity(
                            recordingId = AudioIntegrityUtils.generateEvidenceId(),
                            ownerId = ownerId,
                            ownerUsername = ownerUsername,
                            title = titleToUse,
                            description = draft.description,
                            category = draft.category,
                            filePath = audioAtt.filePath,
                            durationMs = audioAtt.durationMs,
                            fileSizeBytes = audioAtt.fileSizeBytes,
                            sha256Hash = audioAtt.sha256Hash,
                            uploadTimestamp = System.currentTimeMillis(),
                            publicationTimestamp = System.currentTimeMillis(),
                            isPublic = true,
                            locationDescription = "${draft.governorate}${if (draft.locationNotes.isNotBlank()) " - ${draft.locationNotes}" else ""}",
                            incidentDateTime = draft.incidentDateTime,
                            language = _currentLanguage.value.code,
                            moderationStatus = "PUBLISHED",
                            reportCount = 0,
                            incidentId = submitted.incidentId
                        )
                        db.recordingDao().insertRecording(recEntity)
                    } catch (e: Exception) { }
                }

                _snackbarMessage.value = when (_currentLanguage.value) {
                    AppLanguage.ARABIC -> "تم توثيق الواقعة بنجاح برقم: ${submitted.incidentId}"
                    AppLanguage.FRENCH -> "Incident enregistré avec succès : ${submitted.incidentId}"
                    AppLanguage.ENGLISH -> "Incident submitted successfully: ${submitted.incidentId}"
                }
            }.onFailure { err ->
                _incidentDraft.value = _incidentDraft.value.copy(isSubmitting = false)
                _snackbarMessage.value = "Failed to submit incident: ${err.localizedMessage}"
            }
        }
    }

    fun openIncidentDetail(incident: IncidentEntity) {
        _selectedIncident.value = incident
    }

    fun closeIncidentDetail() {
        _selectedIncident.value = null
    }

    fun exportIncidentEvidencePackage(incident: IncidentEntity, context: Context) {
        viewModelScope.launch {
            try {
                val evidenceList = recordingRepository.getEvidenceForIncident(incident.incidentId).firstOrNull() ?: emptyList()
                val (manifestFile, files) = recordingRepository.prepareIncidentEvidenceExportPackage(incident, evidenceList)
                val uris = arrayListOf<Uri>()

                val manifestUri = FileProvider.getUriForFile(
                    context,
                    "${context.packageName}.fileprovider",
                    manifestFile
                )
                uris.add(manifestUri)

                files.forEach { f ->
                    val u = FileProvider.getUriForFile(
                        context,
                        "${context.packageName}.fileprovider",
                        f
                    )
                    uris.add(u)
                }

                val shareIntent = Intent(Intent.ACTION_SEND_MULTIPLE).apply {
                    type = "*/*"
                    putParcelableArrayListExtra(Intent.EXTRA_STREAM, uris)
                    addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
                    putExtra(Intent.EXTRA_SUBJECT, "Protect Yourself Incident Package: ${incident.incidentId}")
                    putExtra(
                        Intent.EXTRA_TEXT,
                        "Structured Civic Incident Evidence Package preserved via Protect Yourself — إحمي روحك.\n" +
                        "Incident ID: ${incident.incidentId}\n" +
                        "Governorate: ${incident.governorate}\n" +
                        "Category: ${incident.category}\n" +
                        "Total Evidence Files: ${evidenceList.size}\n" +
                        "Notice: Platform preserves evidence transparently; it does not claim automatic legal admissibility."
                    )
                }

                val chooser = Intent.createChooser(shareIntent, "Export Evidence Package")
                chooser.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
                context.startActivity(chooser)
            } catch (e: Exception) {
                _snackbarMessage.value = "Failed to export incident package: ${e.localizedMessage}"
            }
        }
    }

    init {
        viewModelScope.launch {
            recordingRepository.seedInitialRecordingsIfEmpty()
        }
    }

    fun setLanguage(lang: AppLanguage) {
        _currentLanguage.value = lang
        prefs.edit().putString("selected_lang", lang.code).apply()
    }

    fun acceptLegalGate() {
        _legalGateAccepted.value = true
        prefs.edit().putBoolean("legal_gate_accepted", true).apply()
    }

    fun navigateToTab(tab: NavTab) {
        _currentTab.value = tab
        _selectedRecording.value = null
    }

    fun openRecordingDetail(recording: RecordingEntity) {
        _selectedRecording.value = recording
    }

    fun closeRecordingDetail() {
        _selectedRecording.value = null
        audioPlayerManager.stop()
    }

    fun openLegalSheet() {
        _showLegalSheet.value = true
    }

    fun closeLegalSheet() {
        _showLegalSheet.value = false
    }

    fun showPublicWarning() {
        _showPublicWarningSheet.value = true
    }

    fun dismissPublicWarning() {
        _showPublicWarningSheet.value = false
    }

    fun startReporting(recordingId: String) {
        _reportingRecordingId.value = recordingId
    }

    fun dismissReporting() {
        _reportingRecordingId.value = null
    }

    private val _correctingRecordingId = MutableStateFlow<String?>(null)
    val correctingRecordingId: StateFlow<String?> = _correctingRecordingId.asStateFlow()

    fun startSuggestingCorrection(recordingId: String) {
        _correctingRecordingId.value = recordingId
    }

    fun dismissCorrection() {
        _correctingRecordingId.value = null
    }

    fun submitCorrection(field: String, suggestion: String) {
        val recordingId = _correctingRecordingId.value ?: return
        val user = authRepository.currentUser.value
        val reporter = user?.username ?: "citizen_reviewer"

        viewModelScope.launch {
            recordingRepository.reportRecording(
                recordingId = recordingId,
                reporterUsername = reporter,
                reason = "CORRECTION_SUGGESTED",
                comment = "[Correction for $field]: $suggestion"
            )
            recordingRepository.logAuditEvent(
                actor = reporter,
                eventType = "CORRECTION_SUGGESTED",
                targetId = recordingId,
                details = "Suggested correction for field '$field': $suggestion"
            )
            _correctingRecordingId.value = null
            _snackbarMessage.value = when (_currentLanguage.value) {
                AppLanguage.ARABIC -> "تم استلام اقتراح التصحيح وإحالته إلى التدقيق الإداري."
                AppLanguage.FRENCH -> "Suggestion de correction transmise à la modération."
                AppLanguage.ENGLISH -> "Correction suggestion received and routed to moderation."
            }
        }
    }

    fun submitReport(reason: String, comment: String) {
        val recordingId = _reportingRecordingId.value ?: return
        val user = authRepository.currentUser.value
        val reporter = user?.username ?: "anonymous_reporter"

        viewModelScope.launch {
            recordingRepository.reportRecording(recordingId, reporter, reason, comment)
            _reportingRecordingId.value = null
            _snackbarMessage.value = "Report received and queued for compliance review"
        }
    }

    fun clearSnackbar() {
        _snackbarMessage.value = null
    }

    // Audio Recording Flow
    fun startRecording(): Boolean {
        audioPlayerManager.stop()
        return audioRecorderManager.startRecording()
    }

    fun pauseRecording() = audioRecorderManager.pauseRecording()
    fun resumeRecording() = audioRecorderManager.resumeRecording()

    fun stopRecordingAndPrepareDraft() {
        val result = audioRecorderManager.stopRecording()
        if (result != null) {
            setDraftFromRecording(result)
        }
    }

    fun cancelRecording() {
        audioRecorderManager.cancelRecording()
        _publishDraft.value = PublishDraftState()
    }

    fun setDraftFromRecording(result: RecordingResult) {
        _publishDraft.value = PublishDraftState(
            pendingFile = result.file,
            pendingDurationMs = result.durationMs,
            pendingSha256 = result.sha256Hash,
            pendingFileSize = result.fileSizeBytes
        )
        viewModelScope.launch {
            val duplicate = recordingRepository.checkDuplicateHash(result.sha256Hash)
            _duplicateWarning.value = duplicate
        }
    }

    fun exportEvidencePackage(recording: RecordingEntity, context: Context) {
        viewModelScope.launch {
            try {
                val (manifestFile, audioFile) = recordingRepository.prepareEvidenceExportPackage(recording)
                val uris = arrayListOf<Uri>()

                val manifestUri = FileProvider.getUriForFile(
                    context,
                    "${context.packageName}.fileprovider",
                    manifestFile
                )
                uris.add(manifestUri)

                if (audioFile != null && audioFile.exists()) {
                    val audioUri = FileProvider.getUriForFile(
                        context,
                        "${context.packageName}.fileprovider",
                        audioFile
                    )
                    uris.add(audioUri)
                }

                val shareIntent = Intent(Intent.ACTION_SEND_MULTIPLE).apply {
                    type = "*/*"
                    putParcelableArrayListExtra(Intent.EXTRA_STREAM, uris)
                    addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
                    putExtra(Intent.EXTRA_SUBJECT, "Protect Yourself Evidence Package: ${recording.recordingId}")
                    putExtra(
                        Intent.EXTRA_TEXT,
                        "Civic evidence manifest and audio recording preserved via Protect Yourself — إحمي روحك.\n" +
                        "Evidence ID: ${recording.recordingId}\n" +
                        "Cryptographic Hash: SHA-256: ${recording.sha256Hash}\n" +
                        "Platform: https://protect-yourself.tn/evidence/${recording.recordingId}"
                    )
                }
                val chooser = Intent.createChooser(shareIntent, "Export Evidence Package")
                chooser.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
                context.startActivity(chooser)
            } catch (e: Exception) {
                _snackbarMessage.value = "Failed to export evidence package: ${e.localizedMessage}"
            }
        }
    }

    fun updateDraftTitle(title: String) {
        _publishDraft.value = _publishDraft.value.copy(title = title)
    }

    fun updateDraftDescription(desc: String) {
        _publishDraft.value = _publishDraft.value.copy(description = desc)
    }

    fun updateDraftCategory(category: String) {
        _publishDraft.value = _publishDraft.value.copy(category = category)
    }

    fun updateDraftLocation(location: String) {
        _publishDraft.value = _publishDraft.value.copy(locationDescription = location)
    }

    fun importAudioFile(uri: Uri) {
        viewModelScope.launch {
            val res = recordingRepository.prepareDraftFromUri(uri)
            if (res != null) {
                setDraftFromRecording(res)
                _snackbarMessage.value = when (_currentLanguage.value) {
                    AppLanguage.ARABIC -> "تم تحميل الملف بنجاح. يرجى مراجعة البيانات وتأكيد النشر."
                    AppLanguage.FRENCH -> "Fichier audio importé. Veuillez vérifier les informations avant publication."
                    AppLanguage.ENGLISH -> "Audio imported. Review metadata and confirm publication."
                }
            } else {
                _snackbarMessage.value = when (_currentLanguage.value) {
                    AppLanguage.ARABIC -> "تعذر قراءة الملف الصوتي المحدد."
                    AppLanguage.FRENCH -> "Échec de lecture du fichier audio."
                    AppLanguage.ENGLISH -> "Failed to read selected audio file."
                }
            }
        }
    }

    fun confirmAndPublishPublicly() {
        val user = authRepository.currentUser.value
        if (user == null) {
            _snackbarMessage.value = "Account required to publish. Please sign in or register."
            _showPublicWarningSheet.value = false
            _currentTab.value = NavTab.ACCOUNT
            return
        }

        val draft = _publishDraft.value
        val file = draft.pendingFile
        if (file == null || !file.exists()) {
            _snackbarMessage.value = "No audio file to publish. Please record audio first."
            _showPublicWarningSheet.value = false
            return
        }

        if (draft.title.isBlank()) {
            _snackbarMessage.value = "Please provide a title for the recording."
            _showPublicWarningSheet.value = false
            return
        }

        _isPublishing.value = true
        viewModelScope.launch {
            val result = recordingRepository.publishRecording(
                ownerId = user.userId,
                ownerUsername = user.username,
                title = draft.title,
                description = draft.description,
                category = draft.category,
                audioFile = file,
                durationMs = draft.pendingDurationMs,
                locationDescription = draft.locationDescription,
                incidentDateTime = System.currentTimeMillis(),
                language = _currentLanguage.value.code
            )

            _isPublishing.value = false
            _showPublicWarningSheet.value = false

            result.onSuccess { publishedRec ->
                _publishDraft.value = PublishDraftState()
                _selectedRecording.value = publishedRec
                _currentTab.value = NavTab.FEED
                _snackbarMessage.value = "Your recording is now public: ${publishedRec.recordingId}"
            }.onFailure { e ->
                _snackbarMessage.value = "Publication failed: ${e.localizedMessage}"
            }
        }
    }

    fun deleteRecording(recordingId: String) {
        viewModelScope.launch {
            val res = recordingRepository.deleteRecording(recordingId)
            res.onSuccess {
                if (_selectedRecording.value?.recordingId == recordingId) {
                    closeRecordingDetail()
                }
                _snackbarMessage.value = "Recording deleted successfully"
            }.onFailure {
                _snackbarMessage.value = "Deletion failed"
            }
        }
    }

    override fun onCleared() {
        super.onCleared()
        audioPlayerManager.stop()
        audioRecorderManager.cancelRecording()
    }
}
