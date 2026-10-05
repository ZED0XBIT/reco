package com.example.audio

import android.content.Context
import android.media.MediaRecorder
import android.os.Build
import android.util.Log
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.isActive
import kotlinx.coroutines.launch
import java.io.File

enum class RecorderState {
    IDLE,
    RECORDING,
    PAUSED,
    COMPLETED,
    ERROR
}

data class RecordingResult(
    val file: File,
    val durationMs: Long,
    val sha256Hash: String,
    val fileSizeBytes: Long
)

class AudioRecorderManager(private val context: Context) {

    private var mediaRecorder: MediaRecorder? = null
    private var currentOutputFile: File? = null
    private var startTimeMs: Long = 0L
    private var pausedAccumulatedMs: Long = 0L
    private var pauseStartTimeMs: Long = 0L

    private val _recorderState = MutableStateFlow(RecorderState.IDLE)
    val recorderState: StateFlow<RecorderState> = _recorderState.asStateFlow()

    private val _elapsedTimeMs = MutableStateFlow(0L)
    val elapsedTimeMs: StateFlow<Long> = _elapsedTimeMs.asStateFlow()

    private val _currentAmplitude = MutableStateFlow(0f) // Normalized 0f - 1f
    val currentAmplitude: StateFlow<Float> = _currentAmplitude.asStateFlow()

    private val _recordingResult = MutableStateFlow<RecordingResult?>(null)
    val recordingResult: StateFlow<RecordingResult?> = _recordingResult.asStateFlow()

    private val _errorMessage = MutableStateFlow<String?>(null)
    val errorMessage: StateFlow<String?> = _errorMessage.asStateFlow()

    private var tickerJob: Job? = null
    private val scope = CoroutineScope(Dispatchers.Default)

    fun startRecording(): Boolean {
        try {
            stopCurrentSessionSilently()

            val dir = File(context.filesDir, "recordings").apply { mkdirs() }
            val file = File(dir, "rec_${System.currentTimeMillis()}.m4a")
            currentOutputFile = file

            val recorder = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
                MediaRecorder(context)
            } else {
                @Suppress("DEPRECATION")
                MediaRecorder()
            }

            recorder.apply {
                setAudioSource(MediaRecorder.AudioSource.MIC)
                setOutputFormat(MediaRecorder.OutputFormat.MPEG_4)
                setAudioEncoder(MediaRecorder.AudioEncoder.AAC)
                setAudioEncodingBitRate(128000)
                setAudioSamplingRate(44100)
                setOutputFile(file.absolutePath)
                prepare()
                start()
            }

            mediaRecorder = recorder
            startTimeMs = System.currentTimeMillis()
            pausedAccumulatedMs = 0L
            _elapsedTimeMs.value = 0L
            _recorderState.value = RecorderState.RECORDING
            _errorMessage.value = null
            _recordingResult.value = null

            startTicker()
            return true
        } catch (e: Exception) {
            Log.e("AudioRecorderManager", "Error starting recording", e)
            _errorMessage.value = e.localizedMessage ?: "Failed to start recording"
            _recorderState.value = RecorderState.ERROR
            cleanup()
            return false
        }
    }

    fun pauseRecording() {
        if (_recorderState.value == RecorderState.RECORDING && Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
            try {
                mediaRecorder?.pause()
                pauseStartTimeMs = System.currentTimeMillis()
                _recorderState.value = RecorderState.PAUSED
            } catch (e: Exception) {
                Log.e("AudioRecorderManager", "Pause error", e)
            }
        }
    }

    fun resumeRecording() {
        if (_recorderState.value == RecorderState.PAUSED && Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
            try {
                mediaRecorder?.resume()
                pausedAccumulatedMs += (System.currentTimeMillis() - pauseStartTimeMs)
                _recorderState.value = RecorderState.RECORDING
            } catch (e: Exception) {
                Log.e("AudioRecorderManager", "Resume error", e)
            }
        }
    }

    fun stopRecording(): RecordingResult? {
        tickerJob?.cancel()
        val file = currentOutputFile
        val finalDuration = if (_recorderState.value == RecorderState.PAUSED) {
            (pauseStartTimeMs - startTimeMs - pausedAccumulatedMs).coerceAtLeast(0)
        } else {
            (System.currentTimeMillis() - startTimeMs - pausedAccumulatedMs).coerceAtLeast(0)
        }

        try {
            mediaRecorder?.apply {
                stop()
                reset()
                release()
            }
        } catch (e: Exception) {
            Log.e("AudioRecorderManager", "Stop error", e)
        } finally {
            mediaRecorder = null
        }

        if (file != null && file.exists() && file.length() > 0) {
            val hash = AudioIntegrityUtils.calculateSha256(file)
            val result = RecordingResult(
                file = file,
                durationMs = finalDuration,
                sha256Hash = hash,
                fileSizeBytes = file.length()
            )
            _recordingResult.value = result
            _recorderState.value = RecorderState.COMPLETED
            return result
        } else {
            _recorderState.value = RecorderState.IDLE
            return null
        }
    }

    fun cancelRecording() {
        tickerJob?.cancel()
        cleanup()
        currentOutputFile?.delete()
        currentOutputFile = null
        _recorderState.value = RecorderState.IDLE
        _elapsedTimeMs.value = 0L
        _recordingResult.value = null
    }

    private fun startTicker() {
        tickerJob?.cancel()
        tickerJob = scope.launch {
            while (isActive && _recorderState.value != RecorderState.COMPLETED) {
                if (_recorderState.value == RecorderState.RECORDING) {
                    val currentElapsed = System.currentTimeMillis() - startTimeMs - pausedAccumulatedMs
                    _elapsedTimeMs.value = currentElapsed.coerceAtLeast(0)

                    // Sample amplitude
                    val maxAmp = try {
                        mediaRecorder?.maxAmplitude ?: 0
                    } catch (e: Exception) {
                        0
                    }
                    _currentAmplitude.value = (maxAmp / 32767f).coerceIn(0f, 1f)
                }
                delay(100)
            }
        }
    }

    private fun stopCurrentSessionSilently() {
        tickerJob?.cancel()
        try {
            mediaRecorder?.stop()
        } catch (ignored: Exception) {}
        cleanup()
    }

    private fun cleanup() {
        try {
            mediaRecorder?.reset()
            mediaRecorder?.release()
        } catch (ignored: Exception) {}
        mediaRecorder = null
    }
}
