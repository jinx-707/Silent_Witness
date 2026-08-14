package com.silentwitness.presentation.sos

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.silentwitness.services.SosService
import com.silentwitness.services.SosTriggerManager
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class SosViewModel @Inject constructor(
    application: Application,
    private val sosService: SosService,
    private val sosTriggerManager: SosTriggerManager
) : AndroidViewModel(application) {

    val shakeEnabled: StateFlow<Boolean> = sosTriggerManager.shakeEnabled
    val voiceEnabled: StateFlow<Boolean> = sosTriggerManager.voiceEnabled

    private val _isTriggeringSos = MutableStateFlow(false)
    val isTriggeringSos: StateFlow<Boolean> = _isTriggeringSos.asStateFlow()

    fun toggleShake(enable: Boolean) {
        sosTriggerManager.setShakeEnabled(enable)
    }

    fun toggleVoice(enable: Boolean) {
        sosTriggerManager.setVoiceEnabled(enable)
    }

    fun triggerSos() {
        if (_isTriggeringSos.value) return
        _isTriggeringSos.value = true
        viewModelScope.launch {
            runCatching { sosService.triggerSos() }
            _isTriggeringSos.value = false
        }
    }
}
