package com.example.audio

import android.media.MediaPlayer
import android.media.PlaybackParams
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

class AudioPlayerManager {

    private var mediaPlayer: MediaPlayer? = null
    private var progressJob: Job? = null
    private val scope = CoroutineScope(Dispatchers.Main)

    private val _isPlaying = MutableStateFlow(false)
    val isPlaying: StateFlow<Boolean> = _isPlaying.asStateFlow()

    private val _currentPositionMs = MutableStateFlow(0L)
    val currentPositionMs: StateFlow<Long> = _currentPositionMs.asStateFlow()

    private val _durationMs = MutableStateFlow(0L)
    val durationMs: StateFlow<Long> = _durationMs.asStateFlow()

    private val _currentRecordingId = MutableStateFlow<String?>(null)
    val currentRecordingId: StateFlow<String?> = _currentRecordingId.asStateFlow()

    private val _playbackSpeed = MutableStateFlow(1.0f)
    val playbackSpeed: StateFlow<Float> = _playbackSpeed.asStateFlow()

    fun loadAndPlay(recordingId: String, filePath: String) {
        if (_currentRecordingId.value == recordingId && mediaPlayer != null) {
            if (_isPlaying.value) {
                pause()
            } else {
                play()
            }
            return
        }

        stop()

        val file = File(filePath)
        if (!file.exists() || file.length() == 0L) {
            Log.e("AudioPlayerManager", "Audio file does not exist: $filePath")
            return
        }

        try {
            val player = MediaPlayer().apply {
                setDataSource(filePath)
                prepare()
                setOnCompletionListener {
                    _isPlaying.value = false
                    _currentPositionMs.value = 0L
                    stopProgressTracker()
                }
            }

            mediaPlayer = player
            _currentRecordingId.value = recordingId
            _durationMs.value = player.duration.toLong().coerceAtLeast(0L)
            _currentPositionMs.value = 0L

            applySpeed(_playbackSpeed.value)
            play()
        } catch (e: Exception) {
            Log.e("AudioPlayerManager", "Failed to load audio file", e)
            stop()
        }
    }

    fun play() {
        mediaPlayer?.let { player ->
            try {
                player.start()
                _isPlaying.value = true
                startProgressTracker()
            } catch (e: Exception) {
                Log.e("AudioPlayerManager", "Play failed", e)
            }
        }
    }

    fun pause() {
        mediaPlayer?.let { player ->
            try {
                if (player.isPlaying) {
                    player.pause()
                }
                _isPlaying.value = false
                stopProgressTracker()
            } catch (e: Exception) {
                Log.e("AudioPlayerManager", "Pause failed", e)
            }
        }
    }

    fun seekTo(positionMs: Long) {
        mediaPlayer?.let { player ->
            try {
                val clamped = positionMs.coerceIn(0L, _durationMs.value)
                player.seekTo(clamped.toInt())
                _currentPositionMs.value = clamped
            } catch (e: Exception) {
                Log.e("AudioPlayerManager", "Seek failed", e)
            }
        }
    }

    fun setPlaybackSpeed(speed: Float) {
        _playbackSpeed.value = speed
        applySpeed(speed)
    }

    private fun applySpeed(speed: Float) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            mediaPlayer?.let { player ->
                try {
                    val wasPlaying = player.isPlaying
                    val params = player.playbackParams ?: PlaybackParams()
                    params.speed = speed
                    player.playbackParams = params
                    if (!wasPlaying) {
                        player.pause()
                    }
                } catch (e: Exception) {
                    Log.e("AudioPlayerManager", "Speed adjustment failed", e)
                }
            }
        }
    }

    fun stop() {
        stopProgressTracker()
        try {
            mediaPlayer?.apply {
                if (isPlaying) stop()
                reset()
                release()
            }
        } catch (ignored: Exception) {}
        mediaPlayer = null
        _isPlaying.value = false
        _currentPositionMs.value = 0L
        _currentRecordingId.value = null
    }

    private fun startProgressTracker() {
        stopProgressTracker()
        progressJob = scope.launch {
            while (isActive && _isPlaying.value) {
                mediaPlayer?.let { player ->
                    try {
                        if (player.isPlaying) {
                            _currentPositionMs.value = player.currentPosition.toLong()
                        }
                    } catch (ignored: Exception) {}
                }
                delay(150)
            }
        }
    }

    private fun stopProgressTracker() {
        progressJob?.cancel()
        progressJob = null
    }
}
