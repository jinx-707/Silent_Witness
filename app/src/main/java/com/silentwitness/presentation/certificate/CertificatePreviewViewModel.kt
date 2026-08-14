package com.silentwitness.presentation.certificate

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.silentwitness.domain.models.LogEntry
import com.silentwitness.domain.repository.LogEntryRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import java.time.LocalDateTime
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import javax.inject.Inject

data class CertificatePreviewUiState(
    val certificateId: String = "",
    val generatedAt: LocalDateTime = LocalDateTime.now(),
    val entries: List<LogEntry> = emptyList(),
    val loaded: Boolean = false
)

@HiltViewModel
class CertificatePreviewViewModel @Inject constructor(
    private val repository: LogEntryRepository,
    savedStateHandle: SavedStateHandle
) : ViewModel() {

    private val _uiState = MutableStateFlow(CertificatePreviewUiState())
    val uiState: StateFlow<CertificatePreviewUiState> = _uiState.asStateFlow()

    private val selectedIds: List<String> =
        savedStateHandle.get<List<String>>("selectedIds") ?: emptyList()

    init {
        viewModelScope.launch {
            val entries = repository.getAllEntries().first().sortedBy { it.date }
            val filtered = if (selectedIds.isEmpty()) entries else entries.filter { it.id in selectedIds }
            _uiState.update {
                it.copy(
                    certificateId = generateCertificateId(),
                    entries = filtered,
                    loaded = true
                )
            }
        }
    }
}

private fun generateCertificateId(): String {
    val now = LocalDateTime.now()
    val stamp = now.format(java.time.format.DateTimeFormatter.ofPattern("yyyyMMddHHmmss"))
    return "SW-$stamp-${(1000..9999).random()}"
}
