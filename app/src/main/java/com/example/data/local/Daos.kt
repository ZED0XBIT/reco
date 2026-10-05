package com.example.data.local

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update
import com.example.data.model.AuditLogEntity
import com.example.data.model.IncidentEntity
import com.example.data.model.IncidentEvidenceEntity
import com.example.data.model.RecordingEntity
import com.example.data.model.ReportEntity
import com.example.data.model.SavedRecordingEntity
import com.example.data.model.UserEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface UserDao {
    @Query("SELECT * FROM users WHERE username = :username LIMIT 1")
    suspend fun getUserByUsername(username: String): UserEntity?

    @Query("SELECT * FROM users WHERE userId = :userId LIMIT 1")
    suspend fun getUserById(userId: String): UserEntity?

    @Insert(onConflict = OnConflictStrategy.ABORT)
    suspend fun insertUser(user: UserEntity)

    @Query("UPDATE users SET passwordHash = :newHash, salt = :newSalt WHERE userId = :userId")
    suspend fun updatePassword(userId: String, newHash: String, newSalt: String)

    @Query("DELETE FROM users WHERE userId = :userId")
    suspend fun deleteUser(userId: String)
}

@Dao
interface RecordingDao {
    @Query("SELECT * FROM recordings WHERE isPublic = 1 AND moderationStatus != 'RESTRICTED' ORDER BY publicationTimestamp DESC")
    fun getAllPublicRecordings(): Flow<List<RecordingEntity>>

    @Query("SELECT * FROM recordings WHERE ownerId = :ownerId ORDER BY uploadTimestamp DESC")
    fun getRecordingsByOwner(ownerId: String): Flow<List<RecordingEntity>>

    @Query("SELECT * FROM recordings WHERE recordingId = :recordingId LIMIT 1")
    suspend fun getRecordingById(recordingId: String): RecordingEntity?

    @Query("SELECT * FROM recordings WHERE recordingId = :recordingId LIMIT 1")
    fun getRecordingFlowById(recordingId: String): Flow<RecordingEntity?>

    @Query("SELECT * FROM recordings WHERE sha256Hash = :hash LIMIT 1")
    suspend fun getRecordingByHash(hash: String): RecordingEntity?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertRecording(recording: RecordingEntity)

    @Query("UPDATE recordings SET moderationStatus = :status WHERE recordingId = :recordingId")
    suspend fun updateModerationStatus(recordingId: String, status: String)

    @Query("DELETE FROM recordings WHERE recordingId = :recordingId")
    suspend fun deleteRecording(recordingId: String)

    @Query("DELETE FROM recordings WHERE ownerId = :ownerId")
    suspend fun deleteRecordingsByOwner(ownerId: String)

    @Query("UPDATE recordings SET reportCount = reportCount + 1 WHERE recordingId = :recordingId")
    suspend fun incrementReportCount(recordingId: String)

    @Query("SELECT COUNT(*) FROM recordings WHERE isPublic = 1 AND moderationStatus != 'RESTRICTED'")
    suspend fun getRecordingsCount(): Int

    @Query("SELECT COUNT(*) FROM recordings WHERE ownerId = :ownerId")
    fun getOwnerRecordingsCountFlow(ownerId: String): Flow<Int>

    @Query("SELECT * FROM recordings WHERE incidentId = :incidentId ORDER BY uploadTimestamp ASC")
    fun getRecordingsForIncident(incidentId: String): Flow<List<RecordingEntity>>
}

@Dao
interface ReportDao {
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertReport(report: ReportEntity)

    @Query("SELECT * FROM reports WHERE recordingId = :recordingId ORDER BY timestamp DESC")
    fun getReportsForRecording(recordingId: String): Flow<List<ReportEntity>>

    @Query("SELECT * FROM reports ORDER BY timestamp DESC")
    fun getAllReports(): Flow<List<ReportEntity>>

    @Query("SELECT * FROM reports WHERE status = 'PENDING' ORDER BY timestamp DESC")
    fun getPendingReports(): Flow<List<ReportEntity>>

    @Query("UPDATE reports SET status = :newStatus, actionNote = :note WHERE reportId = :reportId")
    suspend fun updateReportStatus(reportId: String, newStatus: String, note: String?)
}

@Dao
interface SavedRecordingDao {
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun saveRecording(saved: SavedRecordingEntity)

    @Query("DELETE FROM saved_recordings WHERE userId = :userId AND recordingId = :recordingId")
    suspend fun unsaveRecording(userId: String, recordingId: String)

    @Query("SELECT EXISTS(SELECT 1 FROM saved_recordings WHERE userId = :userId AND recordingId = :recordingId)")
    fun isRecordingSavedFlow(userId: String, recordingId: String): Flow<Boolean>

    @Query("SELECT EXISTS(SELECT 1 FROM saved_recordings WHERE userId = :userId AND recordingId = :recordingId)")
    suspend fun isRecordingSaved(userId: String, recordingId: String): Boolean

    @Query("SELECT r.* FROM recordings r INNER JOIN saved_recordings s ON r.recordingId = s.recordingId WHERE s.userId = :userId ORDER BY s.savedAt DESC")
    fun getSavedRecordingsForUser(userId: String): Flow<List<RecordingEntity>>
}

@Dao
interface IncidentDao {
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertIncident(incident: IncidentEntity)

    @Query("SELECT * FROM incidents WHERE ownerId = :ownerId ORDER BY submissionTimestamp DESC")
    fun getIncidentsByOwner(ownerId: String): Flow<List<IncidentEntity>>

    @Query("SELECT * FROM incidents ORDER BY submissionTimestamp DESC")
    fun getAllPublicIncidents(): Flow<List<IncidentEntity>>

    @Query("SELECT * FROM incidents WHERE incidentId = :incidentId LIMIT 1")
    suspend fun getIncidentById(incidentId: String): IncidentEntity?

    @Query("SELECT * FROM incidents WHERE incidentId = :incidentId LIMIT 1")
    fun getIncidentFlowById(incidentId: String): Flow<IncidentEntity?>

    @Query("DELETE FROM incidents WHERE incidentId = :incidentId")
    suspend fun deleteIncident(incidentId: String)
}

@Dao
interface IncidentEvidenceDao {
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertEvidence(evidence: IncidentEvidenceEntity)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertEvidenceList(evidenceList: List<IncidentEvidenceEntity>)

    @Query("SELECT * FROM incident_evidence WHERE incidentId = :incidentId ORDER BY createdAt ASC")
    fun getEvidenceForIncident(incidentId: String): Flow<List<IncidentEvidenceEntity>>

    @Query("SELECT * FROM incident_evidence WHERE incidentId = :incidentId ORDER BY createdAt ASC")
    suspend fun getEvidenceListForIncident(incidentId: String): List<IncidentEvidenceEntity>

    @Query("DELETE FROM incident_evidence WHERE evidenceId = :evidenceId")
    suspend fun deleteEvidence(evidenceId: String)

    @Query("DELETE FROM incident_evidence WHERE incidentId = :incidentId")
    suspend fun deleteEvidenceForIncident(incidentId: String)
}

@Dao
interface AuditLogDao {
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertLog(log: AuditLogEntity)

    @Query("SELECT * FROM audit_logs ORDER BY timestamp DESC LIMIT :limit")
    fun getRecentLogs(limit: Int = 100): Flow<List<AuditLogEntity>>
}
