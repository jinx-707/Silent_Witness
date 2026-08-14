package com.silentwitness.presentation.settings

import androidx.lifecycle.ViewModel
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import javax.inject.Inject

@HiltViewModel
class SettingsViewModel @Inject constructor() : ViewModel() {

    private val _shakeEnabled = MutableStateFlow(true)
    val shakeEnabled: StateFlow<Boolean> = _shakeEnabled.asStateFlow()

    fun toggleShake() {
        _shakeEnabled.update { !it }
    }
}
