package com.example.data.model

import androidx.room.Entity
import androidx.room.Index
import androidx.room.PrimaryKey

@Entity(
    tableName = "users",
    indices = [Index(value = ["username"], unique = true)]
)
data class UserEntity(
    @PrimaryKey val userId: String,
    val username: String,
    val passwordHash: String,
    val salt: String,
    val createdAt: Long = System.currentTimeMillis(),
    val status: String = "ACTIVE",
    val role: String = "USER" // USER, MODERATOR, ADMIN
)

@Entity(
    tableName = "recordings",
    indices = [
        Index(value = ["ownerId"]),
        Index(value = ["category"]),
        Index(value = ["uploadTimestamp"]),
        Index(value = ["sha256Hash"])
    ]
)
data class RecordingEntity(
    @PrimaryKey val recordingId: String, // e.g. "PS-TN-2026-000184"
    val ownerId: String,
    val ownerUsername: String,
    val title: String,
    val description: String,
    val category: String,
    val filePath: String,
    val durationMs: Long,
    val fileSizeBytes: Long,
    val sha256Hash: String,
    val uploadTimestamp: Long = System.currentTimeMillis(),
    val publicationTimestamp: Long = System.currentTimeMillis(),
    val isPublic: Boolean = true,
    val locationDescription: String? = null,
    val incidentDateTime: Long? = null,
    val language: String = "ar",
    val moderationStatus: String = "PUBLISHED", // PUBLISHED, FLAGGED, UNDER_REVIEW, RESTRICTED, REMOVED
    val reportCount: Int = 0,
    val incidentId: String? = null
)

@Entity(
    tableName = "saved_recordings",
    primaryKeys = ["userId", "recordingId"],
    indices = [Index(value = ["userId"]), Index(value = ["recordingId"])]
)
data class SavedRecordingEntity(
    val userId: String,
    val recordingId: String,
    val savedAt: Long = System.currentTimeMillis()
)

@Entity(
    tableName = "incidents",
    indices = [
        Index(value = ["ownerId"]),
        Index(value = ["governorate"]),
        Index(value = ["category"]),
        Index(value = ["submissionTimestamp"])
    ]
)
data class IncidentEntity(
    @PrimaryKey val incidentId: String, // e.g. "TN-2026-000184"
    val ownerId: String,
    val ownerUsername: String,
    val title: String,
    val description: String = "",
    val category: String, // Police / security incident, Road / traffic incident, etc.
    val governorate: String, // Required: 1 of 24 Tunisian governorates
    val latitude: Double? = null,
    val longitude: Double? = null,
    val locationNotes: String? = null,
    val incidentDateTime: Long, // When the incident happened
    val submissionTimestamp: Long = System.currentTimeMillis(), // When submitted
    val officerName: String? = null,
    val officerBadge: String? = null,
    val officerDepartment: String? = null,
    val vehicleRegistration: String? = null,
    val otherIdentifyingInfo: String? = null,
    val termsAcceptedAt: Long = System.currentTimeMillis(),
    val timelineJson: String = "[]", // JSON array of TimelineEntryItem
    val status: String = "SUBMITTED", // SUBMITTED, UNDER_REVIEW, RESTRICTED
    val evidenceCount: Int = 0,
    val isPublic: Boolean = true
)

@Entity(
    tableName = "incident_evidence",
    indices = [
        Index(value = ["incidentId"]),
        Index(value = ["sha256Hash"])
    ]
)
data class IncidentEvidenceEntity(
    @PrimaryKey val evidenceId: String, // e.g. "EV-TN-2026-XXXXXX"
    val incidentId: String,
    val fileType: String, // "AUDIO", "PHOTO", "VIDEO", "DOCUMENT"
    val originalFilename: String,
    val filePath: String,
    val fileSizeBytes: Long,
    val sha256Hash: String,
    val durationMs: Long = 0L,
    val createdAt: Long = System.currentTimeMillis(),
    val mimeType: String = ""
)

data class TimelineEntryItem(
    val timestamp: Long,
    val label: String,
    val eventType: String // "INCIDENT_OCCURRED", "RECORDING_STARTED", "RECORDING_ENDED", "PHOTO_ADDED", "VIDEO_ADDED", "DOCUMENT_ADDED", "EVIDENCE_SUBMITTED"
)

@Entity(
    tableName = "audit_logs",
    indices = [Index(value = ["timestamp"])]
)
data class AuditLogEntity(
    @PrimaryKey val logId: String,
    val actorUsername: String,
    val eventType: String, // CONTENT_PUBLISHED, REPORT_SUBMITTED, MODERATION_ACTION, CONTENT_RESTRICTED, PASSWORD_CHANGED, ACCOUNT_DELETED
    val targetId: String,
    val details: String,
    val timestamp: Long = System.currentTimeMillis()
)

@Entity(
    tableName = "reports",
    indices = [Index(value = ["recordingId"])]
)
data class ReportEntity(
    @PrimaryKey val reportId: String,
    val recordingId: String,
    val reporterUsername: String,
    val reason: String,
    val comment: String,
    val timestamp: Long = System.currentTimeMillis(),
    val status: String = "PENDING", // PENDING, UNDER_REVIEW, ACTION_TAKEN, NO_VIOLATION, CLOSED
    val actionNote: String? = null
)
