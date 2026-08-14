package com.silentwitness.presentation.addedit

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import com.silentwitness.domain.models.LogEntry
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import java.time.LocalDateTime
import java.time.format.DateTimeFormatter
import javax.inject.Inject

data class DraftEntryState(
    val dateTime: String = LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm")),
    val category: String = "verbal",
    val description: String = "",
    val injuryNotes: String = "",
    val location: String = "",
    val photoUri: String? = null,
    val audioUri: String? = null,
    val photoAttached: Boolean = false,
    val audioAttached: Boolean = false,
)

@HiltViewModel
class DraftEntryViewModel @Inject constructor(
    private val savedStateHandle: SavedStateHandle
) : ViewModel() {

    private val _state = MutableStateFlow(DraftEntryState())
    val state: StateFlow<DraftEntryState> = _state.asStateFlow()

    fun updateDateTime(value: String) { _state.update { it.copy(dateTime = value) } }
    fun updateCategory(value: String) { _state.update { it.copy(category = value) } }
    fun updateDescription(value: String) { _state.update { it.copy(description = value) } }
    fun updateInjuryNotes(value: String) { _state.update { it.copy(injuryNotes = value) } }
    fun updateLocation(value: String) { _state.update { it.copy(location = value) } }

    fun setPhotoUri(uri: String?) {
        _state.update { it.copy(photoUri = uri, photoAttached = uri != null) }
    }

    fun setAudioUri(uri: String?) {
        _state.update { it.copy(audioUri = uri, audioAttached = uri != null) }
    }

    fun clearPhoto() {
        _state.update { it.copy(photoUri = null, photoAttached = false) }
    }

    fun clearAudio() {
        _state.update { it.copy(audioUri = null, audioAttached = false) }
    }

    fun reset() {
        _state.value = DraftEntryState()
    }

    fun loadFromEntry(entry: LogEntry) {
        _state.value = DraftEntryState(
            dateTime = entry.date.format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm")),
            category = entry.category,
            description = entry.description,
            injuryNotes = entry.injuryNotes ?: "",
            location = entry.location ?: "",
            // LogEntry does not persist media URIs yet, only the attached flags.
            photoUri = null,
            audioUri = null,
            photoAttached = entry.photoAttached,
            audioAttached = entry.audioAttached,
        )
    }
}
