package com.silentwitness.presentation.entrydetail

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.silentwitness.domain.models.LogEntry
import com.silentwitness.domain.repository.LogEntryRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class EntryDetailViewModel @Inject constructor(
    private val repository: LogEntryRepository
) : ViewModel() {

    private val _entry = MutableStateFlow<LogEntry?>(null)
    val entry: StateFlow<LogEntry?> = _entry.asStateFlow()

    private val _showAudit = MutableStateFlow(false)
    val showAudit: StateFlow<Boolean> = _showAudit.asStateFlow()

    fun loadEntry(id: String) {
        viewModelScope.launch {
            _entry.value = repository.getEntryById(id)
        }
    }

    fun toggleAudit() {
        _showAudit.value = !_showAudit.value
    }

    fun deleteEntry(onDeleted: () -> Unit) {
        viewModelScope.launch {
            val current = _entry.value ?: return@launch
            repository.deleteEntry(current.id)
            onDeleted()
        }
    }
}
