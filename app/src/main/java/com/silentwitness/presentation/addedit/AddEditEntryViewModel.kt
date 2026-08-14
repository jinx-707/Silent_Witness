package com.silentwitness.presentation.addedit

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.silentwitness.domain.models.LogEntry
import com.silentwitness.domain.repository.LogEntryRepository
import com.silentwitness.domain.repository.PartialLogEntryUpdate
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.launch
import java.time.LocalDateTime
import java.time.format.DateTimeFormatter
import javax.inject.Inject

@HiltViewModel
class AddEditEntryViewModel @Inject constructor(
    private val repository: LogEntryRepository
) : ViewModel() {

    suspend fun loadEntry(id: String): LogEntry? = repository.getEntryById(id)

    fun saveEntry(entryId: String?, draft: DraftEntryState, onSaved: (String) -> Unit) {
        viewModelScope.launch {
            if (draft.description.isBlank()) return@launch

            val date = runCatching {
                LocalDateTime.parse(draft.dateTime, DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm"))
            }.getOrElse { LocalDateTime.now() }

            if (entryId != null) {
                repository.updateEntry(
                    entryId,
                    PartialLogEntryUpdate(
                        date = date,
                        description = draft.description,
                        category = draft.category,
                        injuryNotes = draft.injuryNotes.takeIf { it.isNotBlank() },
                        location = draft.location.takeIf { it.isNotBlank() },
                        photoAttached = draft.photoAttached,
                        audioAttached = draft.audioAttached
                    )
                )
                onSaved(entryId)
            } else {
                val newEntry = repository.addEntry(
                    LogEntry(
                        date = date,
                        description = draft.description,
                        category = draft.category,
                        injuryNotes = draft.injuryNotes.takeIf { it.isNotBlank() },
                        location = draft.location.takeIf { it.isNotBlank() },
                        photoAttached = draft.photoAttached,
                        audioAttached = draft.audioAttached
                    )
                )
                onSaved(newEntry.id)
            }
        }
    }
}
