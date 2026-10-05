package com.example.audio

import java.io.ByteArrayOutputStream
import java.io.File
import java.io.FileInputStream
import java.io.FileOutputStream
import java.security.MessageDigest
import java.util.Locale
import java.util.Random

object AudioIntegrityUtils {

    /**
     * Calculates the SHA-256 cryptographic hash of the provided file.
     */
    fun calculateSha256(file: File): String {
        return try {
            val digest = MessageDigest.getInstance("SHA-256")
            FileInputStream(file).use { fis ->
                val buffer = ByteArray(8192)
                var bytesRead: Int
                while (fis.read(buffer).also { bytesRead = it } != -1) {
                    digest.update(buffer, 0, bytesRead)
                }
            }
            bytesToHex(digest.digest())
        } catch (e: Exception) {
            "HASH_ERROR_${e.message?.take(8)}"
        }
    }

    fun calculateSha256(bytes: ByteArray): String {
        return try {
            val digest = MessageDigest.getInstance("SHA-256")
            val hash = digest.digest(bytes)
            bytesToHex(hash)
        } catch (e: Exception) {
            "HASH_ERROR_${e.message?.take(8)}"
        }
    }

    private fun bytesToHex(bytes: ByteArray): String {
        val hexChars = CharArray(bytes.size * 2)
        val hexArray = "0123456789abcdef".toCharArray()
        for (i in bytes.indices) {
            val v = bytes[i].toInt() and 0xFF
            hexChars[i * 2] = hexArray[v ushr 4]
            hexChars[i * 2 + 1] = hexArray[v and 0x0F]
        }
        return String(hexChars)
    }

    /**
     * Generates a formal Incident Identifier following the format:
     * TN-2026-000184
     */
    fun generateIncidentId(): String {
        val number = 100000 + Random().nextInt(900000)
        return "TN-2026-$number"
    }

    /**
     * Generates a formal Evidence Identifier following the format:
     * PS-TN-2026-XXXXXX
     */
    fun generateEvidenceId(): String {
        val number = 100000 + Random().nextInt(900000)
        return "EV-TN-2026-$number"
    }

    fun formatDuration(ms: Long): String {
        val totalSec = (ms / 1000).coerceAtLeast(0)
        val minutes = totalSec / 60
        val seconds = totalSec % 60
        return String.format(Locale.US, "%02d:%02d", minutes, seconds)
    }

    fun formatFileSize(bytes: Long): String {
        return when {
            bytes < 1024 -> "$bytes B"
            bytes < 1024 * 1024 -> String.format(Locale.US, "%.1f KB", bytes / 1024.0)
            else -> String.format(Locale.US, "%.2f MB", bytes / (1024.0 * 1024.0))
        }
    }

    /**
     * Generates a valid standard 16-bit PCM WAV audio file with standard RIFF header.
     * Useful for sample audio seeding and verified playback testing.
     */
    fun createSampleWavFile(destinationFile: File, durationSeconds: Int = 4, frequencyHz: Double = 440.0) {
        val sampleRate = 22050
        val numSamples = durationSeconds * sampleRate
        val pcmData = ByteArray(numSamples * 2) // 16-bit = 2 bytes per sample

        for (i in 0 until numSamples) {
            val angle = 2.0 * Math.PI * i / (sampleRate / frequencyHz)
            // Add slight harmonic variation for a natural chime tone
            val sampleValue = (Math.sin(angle) * 0.4 + Math.sin(angle * 1.5) * 0.2) * 32767
            val sampleInt = sampleValue.toInt().coerceIn(-32768, 32767)
            pcmData[i * 2] = (sampleInt and 0xFF).toByte()
            pcmData[i * 2 + 1] = ((sampleInt shr 8) and 0xFF).toByte()
        }

        val totalDataLen = pcmData.size + 36
        val byteRate = sampleRate * 1 * 2 // SampleRate * NumChannels * BitsPerSample/8

        val header = ByteArray(44)
        // RIFF chunk descriptor
        header[0] = 'R'.code.toByte(); header[1] = 'I'.code.toByte(); header[2] = 'F'.code.toByte(); header[3] = 'F'.code.toByte()
        header[4] = (totalDataLen and 0xff).toByte()
        header[5] = ((totalDataLen shr 8) and 0xff).toByte()
        header[6] = ((totalDataLen shr 16) and 0xff).toByte()
        header[7] = ((totalDataLen shr 24) and 0xff).toByte()
        header[8] = 'W'.code.toByte(); header[9] = 'A'.code.toByte(); header[10] = 'V'.code.toByte(); header[11] = 'E'.code.toByte()

        // fmt sub-chunk
        header[12] = 'f'.code.toByte(); header[13] = 'm'.code.toByte(); header[14] = 't'.code.toByte(); header[15] = ' '.code.toByte()
        header[16] = 16; header[17] = 0; header[18] = 0; header[19] = 0 // Subchunk1Size (16 for PCM)
        header[20] = 1; header[21] = 0 // AudioFormat (1 = PCM)
        header[22] = 1; header[23] = 0 // NumChannels (1 = Mono)
        header[24] = (sampleRate and 0xff).toByte()
        header[25] = ((sampleRate shr 8) and 0xff).toByte()
        header[26] = ((sampleRate shr 16) and 0xff).toByte()
        header[27] = ((sampleRate shr 24) and 0xff).toByte()
        header[28] = (byteRate and 0xff).toByte()
        header[29] = ((byteRate shr 8) and 0xff).toByte()
        header[30] = ((byteRate shr 16) and 0xff).toByte()
        header[31] = ((byteRate shr 24) and 0xff).toByte()
        header[32] = 2; header[33] = 0 // BlockAlign
        header[34] = 16; header[35] = 0 // BitsPerSample

        // data sub-chunk
        header[36] = 'd'.code.toByte(); header[37] = 'a'.code.toByte(); header[38] = 't'.code.toByte(); header[39] = 'a'.code.toByte()
        header[40] = (pcmData.size and 0xff).toByte()
        header[41] = ((pcmData.size shr 8) and 0xff).toByte()
        header[42] = ((pcmData.size shr 16) and 0xff).toByte()
        header[43] = ((pcmData.size shr 24) and 0xff).toByte()

        destinationFile.parentFile?.mkdirs()
        FileOutputStream(destinationFile).use { fos ->
            fos.write(header)
            fos.write(pcmData)
        }
    }
}
