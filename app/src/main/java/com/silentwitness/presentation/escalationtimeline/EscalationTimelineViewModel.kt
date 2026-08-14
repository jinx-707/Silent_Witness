package com.silentwitness.presentation.escalationtimeline

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.silentwitness.domain.models.LogEntry
import com.silentwitness.domain.repository.LogEntryRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import java.time.format.DateTimeFormatter
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class EscalationTimelineViewModel @Inject constructor(
    private val repository: LogEntryRepository
) : ViewModel() {

    private val _entries = MutableStateFlow<List<LogEntry>>(emptyList())
    val entries: StateFlow<List<LogEntry>> = _entries.asStateFlow()

    val groupedEntries: StateFlow<Map<String, List<LogEntry>>> = _entries
        .map { list ->
            list
                .sortedByDescending { it.date }
                .groupBy { it.date.format(DateTimeFormatter.ofPattern("MMMM yyyy")) }
        }
        .stateIn(viewModelScope, SharingStarted.Eagerly, emptyMap())

    init {
        viewModelScope.launch {
            repository.getAllEntries().collect { list ->
                _entries.value = list.sortedByDescending { it.date }
            }
        }
    }
}
