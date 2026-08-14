package com.silentwitness.presentation.audiorecording

import android.content.Context
import android.media.MediaRecorder
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import java.io.File
import java.util.Timer
import java.util.TimerTask
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import javax.inject.Inject

data class AudioRecordingUiState(
    val state: AudioState = AudioState.Idle,
    val filePath: String? = null,
    val durationSeconds: Int = 0
)

enum class AudioState { Idle, Recording, Done }

@HiltViewModel
class AudioRecordingViewModel @Inject constructor() : ViewModel() {

    private val _uiState = MutableStateFlow(AudioRecordingUiState())
    val uiState: StateFlow<AudioRecordingUiState> = _uiState.asStateFlow()

    private var recorder: MediaRecorder? = null
    private var currentFile: File? = null
    private var timer: Timer? = null

    fun startRecording(context: Context) {
        if (_uiState.value.state == AudioState.Recording) return

        val file = File(context.cacheDir, "audio_${System.currentTimeMillis()}.3gp")
        val r = MediaRecorder()
        try {
            r.setAudioSource(MediaRecorder.AudioSource.MIC)
            r.setOutputFormat(MediaRecorder.OutputFormat.THREE_GPP)
            r.setAudioEncoder(MediaRecorder.AudioEncoder.AMR_NB)
            r.setOutputFile(file.absolutePath)
            r.prepare()
            r.start()
            recorder = r
            currentFile = file
            _uiState.update { it.copy(state = AudioState.Recording, durationSeconds = 0, filePath = null) }

            timer = Timer().apply {
                scheduleAtFixedRate(object : TimerTask() {
                    override fun run() {
                        viewModelScope.launch {
                            _uiState.update { s ->
                                if (s.state == AudioState.Recording) s.copy(durationSeconds = s.durationSeconds + 1) else s
                            }
                        }
                    }
                }, 0, 1000)
            }
        } catch (_: Exception) {
            runCatching { r.release() }
            recorder = null
        }
    }

    fun stopRecording() {
        if (_uiState.value.state != AudioState.Recording) return
        timer?.cancel()
        timer = null
        runCatching { recorder?.stop() }
        runCatching { recorder?.release() }
        recorder = null
        val path = currentFile?.absolutePath
        _uiState.update { it.copy(state = AudioState.Done, filePath = path) }
    }

    fun reset() {
        timer?.cancel()
        timer = null
        _uiState.value = AudioRecordingUiState()
    }

    override fun onCleared() {
        timer?.cancel()
        runCatching { recorder?.stop() }
        runCatching { recorder?.release() }
        super.onCleared()
    }
}

fun formatDuration(seconds: Int): String {
    val m = seconds / 60
    val s = seconds % 60
    return String.format("%02d:%02d", m, s)
}
