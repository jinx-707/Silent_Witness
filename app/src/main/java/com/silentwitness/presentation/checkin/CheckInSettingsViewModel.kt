package com.silentwitness.presentation.checkin

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.silentwitness.domain.models.CheckInSettings
import com.silentwitness.domain.repository.CheckInRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class CheckInSettingsViewModel @Inject constructor(
    private val repository: CheckInRepository
) : ViewModel() {

    private val _uiState = MutableStateFlow(CheckInSettings())
    val uiState: StateFlow<CheckInSettings> = _uiState.asStateFlow()

    init {
        viewModelScope.launch {
            repository.getCheckInSettings().collect { settings ->
                _uiState.value = settings
            }
        }
    }

    fun toggleEnabled() {
        _uiState.update { it.copy(enabled = !it.enabled) }
    }

    fun updateInterval(hours: Int) {
        _uiState.update { it.copy(intervalHours = hours) }
    }

    fun updateNotifyTier(tier: Int) {
        _uiState.update { it.copy(notifyTier = tier) }
    }

    fun updateReleaseOnMissed(enabled: Boolean) {
        _uiState.update { it.copy(releaseOnMissed = enabled) }
    }

    fun updateGracePeriod(minutes: Int) {
        _uiState.update { it.copy(gracePeriodMinutes = minutes) }
    }

    fun saveSettings() {
        viewModelScope.launch {
            repository.saveCheckInSettings(_uiState.value)
        }
    }
}
