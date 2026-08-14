package com.silentwitness.presentation.screenshotimporter

import android.net.Uri
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.silentwitness.domain.models.LogEntry
import com.silentwitness.domain.repository.LogEntryRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import javax.inject.Inject

data class ScreenshotImporterUiState(
    val uri: Uri? = null,
    val sender: String = "",
    val platform: String = "",
    val notes: String = "",
    val saving: Boolean = false
)

@HiltViewModel
class ScreenshotImporterViewModel @Inject constructor(
    private val repository: LogEntryRepository
) : ViewModel() {

    private val _uiState = MutableStateFlow(ScreenshotImporterUiState())
    val uiState: StateFlow<ScreenshotImporterUiState> = _uiState.asStateFlow()

    fun setUri(uri: Uri) { _uiState.update { it.copy(uri = uri) } }
    fun updateSender(value: String) { _uiState.update { it.copy(sender = value) } }
    fun updatePlatform(value: String) { _uiState.update { it.copy(platform = value) } }
    fun updateNotes(value: String) { _uiState.update { it.copy(notes = value) } }

    fun saveScreenshot(onSaved: (String) -> Unit) {
        viewModelScope.launch {
            val s = _uiState.value
            if (s.uri == null) return@launch
            _uiState.update { it.copy(saving = true) }

            val description = buildString {
                append(s.notes.ifBlank { "Screenshot evidence imported." })
                if (s.sender.isNotBlank()) append(" Sender: ${s.sender}")
                if (s.platform.isNotBlank()) append(" Platform: ${s.platform}")
            }

            val entry = repository.addEntry(
                LogEntry(
                    date = java.time.LocalDateTime.now(),
                    description = description,
                    category = "digital",
                    captureTimeUnknown = true,
                    photoAttached = true,
                    senderInfo = s.sender.ifBlank { null },
                    platform = s.platform.ifBlank { null }
                )
            )
            _uiState.update { it.copy(saving = false) }
            onSaved(entry.id)
        }
    }
}
