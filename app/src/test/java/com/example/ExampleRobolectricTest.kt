package com.example

import android.content.Context
import androidx.room.Room
import androidx.test.core.app.ApplicationProvider
import com.example.data.local.AppDatabase
import com.example.data.model.IncidentEntity
import com.example.data.model.IncidentEvidenceEntity
import com.example.data.model.RecordingEntity
import com.example.data.model.TunisianGovernorates
import com.example.data.model.UserEntity
import com.example.data.repository.AuthRepository
import com.example.data.repository.AuthResult
import com.example.data.repository.RecordingRepository
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.runBlocking
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config

@RunWith(RobolectricTestRunner::class)
@Config(sdk = [34])
class ExampleRobolectricTest {

    private lateinit var db: AppDatabase
    private lateinit var context: Context

    @Before
    fun setup() {
        context = ApplicationProvider.getApplicationContext()
        db = Room.inMemoryDatabaseBuilder(context, AppDatabase::class.java)
            .allowMainThreadQueries()
            .build()
    }

    @After
    fun tearDown() {
        db.close()
    }

    @Test
    fun readStringFromContext_matchesAppName() {
        val appName = context.getString(R.string.app_name)
        assertEquals("Protect Yourself", appName)
    }

    @Test
    fun tunisianGovernorates_containsAll24Governorates() {
        assertEquals(24, TunisianGovernorates.all.size)
        val expected = listOf(
            "Tunis", "Ariana", "Ben Arous", "Manouba", "Nabeul", "Zaghouan",
            "Bizerte", "Béja", "Jendouba", "Kef", "Siliana", "Kairouan",
            "Kasserine", "Sidi Bouzid", "Sousse", "Monastir", "Mahdia", "Sfax",
            "Gabès", "Medenine", "Tataouine", "Gafsa", "Tozeur", "Kebili"
        )
        for (gov in expected) {
            assertNotNull("Governorate $gov must be present", TunisianGovernorates.findByName(gov))
        }
    }

    @Test
    fun incidentDao_insertsAndRetrievesStructuredIncident() = runBlocking {
        val incident = IncidentEntity(
            incidentId = "TN-2026-000184",
            ownerId = "citizen_42",
            ownerUsername = "ali_citizen",
            title = "Police Roadblock Verification",
            description = "Stopped at checkpoint, badge number verified",
            category = "Police / security incident",
            governorate = "Sousse",
            latitude = 35.8256,
            longitude = 10.6084,
            locationNotes = "GP1 Km 120 near Sousse entrance",
            incidentDateTime = System.currentTimeMillis() - 7200000L,
            submissionTimestamp = System.currentTimeMillis(),
            officerName = "Agent B.",
            officerBadge = "9482",
            officerDepartment = "Garde Nationale",
            evidenceCount = 2
        )

        db.incidentDao().insertIncident(incident)

        val retrieved = db.incidentDao().getIncidentById("TN-2026-000184")
        assertNotNull(retrieved)
        assertEquals("TN-2026-000184", retrieved?.incidentId)
        assertEquals("Sousse", retrieved?.governorate)
        assertEquals("Police / security incident", retrieved?.category)
        assertEquals("Agent B.", retrieved?.officerName)
        assertEquals(35.8256, retrieved?.latitude ?: 0.0, 0.0001)

        val evidence1 = IncidentEvidenceEntity(
            evidenceId = "EV-TN-2026-001",
            incidentId = "TN-2026-000184",
            fileType = "AUDIO",
            originalFilename = "checkpoint_audio.m4a",
            filePath = "/data/audio/rec1.m4a",
            fileSizeBytes = 409600L,
            sha256Hash = "8f434346648f6b96df89dda901c5176b10a6d83961dd3c1ac88b59b2dc327aa4",
            durationMs = 95000L
        )

        val evidence2 = IncidentEvidenceEntity(
            evidenceId = "EV-TN-2026-002",
            incidentId = "TN-2026-000184",
            fileType = "PHOTO",
            originalFilename = "checkpoint_photo.jpg",
            filePath = "/data/photo/photo1.jpg",
            fileSizeBytes = 1200000L,
            sha256Hash = "e3b0c44298fc1c149afbf4c8996fb92427ae41e4649b934ca495991b7852b855"
        )

        db.incidentEvidenceDao().insertEvidenceList(listOf(evidence1, evidence2))

        val evidenceList = db.incidentEvidenceDao().getEvidenceListForIncident("TN-2026-000184")
        assertEquals(2, evidenceList.size)
        assertEquals("AUDIO", evidenceList[0].fileType)
        assertEquals("PHOTO", evidenceList[1].fileType)
        assertEquals("8f434346648f6b96df89dda901c5176b10a6d83961dd3c1ac88b59b2dc327aa4", evidenceList[0].sha256Hash)
    }

    @Test
    fun userRegistrationAndLogin_worksCorrectly() = runBlocking {
        val authRepo = AuthRepository(db.userDao(), context)

        // 1. Register new user
        val regResult = authRepo.register("citizen_test", "securePass123")
        assertTrue(regResult is AuthResult.Success)

        // 2. Login with correct password
        val loginResult = authRepo.login("citizen_test", "securePass123")
        assertTrue(loginResult is AuthResult.Success)

        // 3. Login with wrong password
        val failResult = authRepo.login("citizen_test", "wrongPassword")
        assertTrue(failResult is AuthResult.Error)
    }

    @Test
    fun recordingDao_insertsAndRetrievesPublicRecordings() = runBlocking {
        val recording = RecordingEntity(
            recordingId = "PS-TN-2026-000999",
            ownerId = "user_123",
            ownerUsername = "test_user",
            title = "Test Incident at Police Station",
            description = "Detailed test context",
            category = "Police interaction",
            filePath = "/fake/path.m4a",
            durationMs = 45000L,
            fileSizeBytes = 102400L,
            sha256Hash = "e3b0c44298fc1c149afbf4c8996fb92427ae41e4649b934ca495991b7852b855",
            isPublic = true
        )

        db.recordingDao().insertRecording(recording)

        val retrieved = db.recordingDao().getRecordingById("PS-TN-2026-000999")
        assertNotNull(retrieved)
        assertEquals("Test Incident at Police Station", retrieved?.title)

        val publicList = db.recordingDao().getAllPublicRecordings().first()
        assertEquals(1, publicList.size)
        assertEquals("PS-TN-2026-000999", publicList[0].recordingId)
    }

    @Test
    fun savedRecordingDao_savesAndRetrievesSavedRecordings() = runBlocking {
        val recording = RecordingEntity(
            recordingId = "PS-TN-2026-SAVED-1",
            ownerId = "user_A",
            ownerUsername = "uploader_A",
            title = "Public Service Interaction",
            description = "Preserved public evidence",
            category = "Government service",
            filePath = "/fake/saved.m4a",
            durationMs = 12000L,
            fileSizeBytes = 50000L,
            sha256Hash = "abc123hash",
            isPublic = true
        )
        db.recordingDao().insertRecording(recording)

        val isSavedBefore = db.savedRecordingDao().isRecordingSaved("user_B", "PS-TN-2026-SAVED-1")
        assertEquals(false, isSavedBefore)

        db.savedRecordingDao().saveRecording(
            com.example.data.model.SavedRecordingEntity(
                userId = "user_B",
                recordingId = "PS-TN-2026-SAVED-1"
            )
        )

        val isSavedAfter = db.savedRecordingDao().isRecordingSaved("user_B", "PS-TN-2026-SAVED-1")
        assertEquals(true, isSavedAfter)

        val savedList = db.savedRecordingDao().getSavedRecordingsForUser("user_B").first()
        assertEquals(1, savedList.size)
        assertEquals("PS-TN-2026-SAVED-1", savedList[0].recordingId)

        // Unsave
        db.savedRecordingDao().unsaveRecording("user_B", "PS-TN-2026-SAVED-1")
        val isSavedFinal = db.savedRecordingDao().isRecordingSaved("user_B", "PS-TN-2026-SAVED-1")
        assertEquals(false, isSavedFinal)
    }

    @Test
    fun moderation_restrictsContentAndHidesFromPublicFeed() = runBlocking {
        val recording = RecordingEntity(
            recordingId = "PS-TN-2026-MOD-1",
            ownerId = "user_X",
            ownerUsername = "user_X",
            title = "Reported Content",
            description = "Potentially infringing description",
            category = "Other",
            filePath = "/fake/mod.m4a",
            durationMs = 10000L,
            fileSizeBytes = 40000L,
            sha256Hash = "modhash123",
            isPublic = true,
            moderationStatus = "PUBLISHED"
        )
        db.recordingDao().insertRecording(recording)

        // Initially in public feed
        val publicListBefore = db.recordingDao().getAllPublicRecordings().first()
        assertTrue(publicListBefore.any { it.recordingId == "PS-TN-2026-MOD-1" })

        // Restrict content
        db.recordingDao().updateModerationStatus("PS-TN-2026-MOD-1", "RESTRICTED")

        // Hidden from public feed
        val publicListAfter = db.recordingDao().getAllPublicRecordings().first()
        assertTrue(publicListAfter.none { it.recordingId == "PS-TN-2026-MOD-1" })
    }

    @Test
    fun auditLogDao_recordsAndRetrievesImmutableAuditLogs() = runBlocking {
        val log = com.example.data.model.AuditLogEntity(
            logId = "LOG-001",
            actorUsername = "moderator_admin",
            eventType = "CONTENT_RESTRICTED",
            targetId = "PS-TN-2026-MOD-1",
            details = "Content restricted under privacy policy"
        )
        db.auditLogDao().insertLog(log)

        val logs = db.auditLogDao().getRecentLogs().first()
        assertEquals(1, logs.size)
        assertEquals("CONTENT_RESTRICTED", logs[0].eventType)
        assertEquals("moderator_admin", logs[0].actorUsername)
    }
}
