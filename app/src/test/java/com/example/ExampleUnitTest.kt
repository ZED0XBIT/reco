package com.example

import com.example.audio.AudioIntegrityUtils
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertTrue
import org.junit.Test
import java.io.File

class ExampleUnitTest {

    @Test
    fun evidenceId_hasCorrectFormat() {
        val evidenceId = AudioIntegrityUtils.generateEvidenceId()
        assertTrue(evidenceId.startsWith("EV-TN-2026-"))
        assertEquals(17, evidenceId.length) // "EV-TN-2026-" (11) + 6 digits (6)
    }

    @Test
    fun incidentId_hasCorrectFormat() {
        val incidentId = AudioIntegrityUtils.generateIncidentId()
        assertTrue(incidentId.startsWith("TN-2026-"))
        assertEquals(14, incidentId.length) // "TN-2026-" (8) + 6 digits (6)
    }

    @Test
    fun sha256_calculatesConsistentHash() {
        val testBytes = "PROTECT_YOURSELF_CITIZEN_EVIDENCE".toByteArray(Charsets.UTF_8)
        val hash1 = AudioIntegrityUtils.calculateSha256(testBytes)
        val hash2 = AudioIntegrityUtils.calculateSha256(testBytes)

        assertEquals(64, hash1.length)
        assertEquals(hash1, hash2)
    }

    @Test
    fun formatDuration_formatsMinutesAndSeconds() {
        assertEquals("00:00", AudioIntegrityUtils.formatDuration(0L))
        assertEquals("01:05", AudioIntegrityUtils.formatDuration(65000L))
        assertEquals("03:42", AudioIntegrityUtils.formatDuration(222000L))
    }

    @Test
    fun sampleWavFile_generatesValidRiffHeader() {
        val tempFile = File.createTempFile("test_audio", ".wav")
        try {
            AudioIntegrityUtils.createSampleWavFile(tempFile, durationSeconds = 1, frequencyHz = 440.0)
            assertTrue(tempFile.exists())
            assertTrue(tempFile.length() > 44L) // Header + PCM data

            val header = ByteArray(4)
            tempFile.inputStream().use { it.read(header) }
            assertEquals("RIFF", String(header))
        } finally {
            tempFile.delete()
        }
    }
}
