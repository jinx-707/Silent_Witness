package com.silentwitness.presentation.export

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.silentwitness.domain.models.LogEntry
import com.silentwitness.domain.repository.LogEntryRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import javax.inject.Inject

data class ExportUiState(
    val entries: List<LogEntry> = emptyList(),
    val selectedIds: Set<String> = emptySet()
)

@HiltViewModel
class ExportViewModel @Inject constructor(
    private val repository: LogEntryRepository
) : ViewModel() {

    private val _uiState = MutableStateFlow(ExportUiState())
    val uiState: StateFlow<ExportUiState> = _uiState.asStateFlow()

    val groupedEntries: StateFlow<Map<String, List<LogEntry>>> = _uiState
        .map { it.entries }
        .map { list ->
            list
                .sortedByDescending { it.date }
                .groupBy { it.date.format(java.time.format.DateTimeFormatter.ofPattern("MMMM yyyy")) }
        }
        .stateIn(viewModelScope, SharingStarted.Eagerly, emptyMap())

    init {
        viewModelScope.launch {
            repository.getAllEntries().collect { entries ->
                _uiState.update {
                    it.copy(
                        entries = entries.sortedByDescending { it.date },
                        selectedIds = it.selectedIds.intersect(entries.map { e -> e.id }.toSet())
                    )
                }
            }
        }
    }

    fun toggleSelection(id: String) {
        _uiState.update {
            val ids = if (id in it.selectedIds) it.selectedIds - id else it.selectedIds + id
            it.copy(selectedIds = ids)
        }
    }

    fun toggleSelectAll() {
        _uiState.update {
            val allSelected = it.selectedIds.size == it.entries.size
            it.copy(selectedIds = if (allSelected) emptySet() else it.entries.map { e -> e.id }.toSet())
        }
    }

    fun selectedEntries(): List<LogEntry> =
        _uiState.value.entries.filter { it.id in _uiState.value.selectedIds }
}
