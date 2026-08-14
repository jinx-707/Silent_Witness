package com.silentwitness.presentation.onboarding

import androidx.lifecycle.ViewModel
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import javax.inject.Inject

data class OnboardingUiState(
    val currentStep: Int = 0, // 0 = Welcome, 1 = Set PIN, 2 = Disguise
    val pin: String = "",
    val confirmPin: String = "",
    val error: String? = null
)

@HiltViewModel
class OnboardingViewModel @Inject constructor() : ViewModel() {

    private val _uiState = MutableStateFlow(OnboardingUiState())
    val uiState: StateFlow<OnboardingUiState> = _uiState.asStateFlow()

    fun goToStep(step: Int) {
        _uiState.update { it.copy(currentStep = step, error = null) }
    }

    fun updatePin(value: String) {
        _uiState.update {
            it.copy(pin = value.filter { c -> c.isDigit() }.take(6), error = null)
        }
    }

    fun updateConfirmPin(value: String) {
        _uiState.update {
            it.copy(confirmPin = value.filter { c -> c.isDigit() }.take(6), error = null)
        }
    }

    fun validatePin(): Boolean {
        val state = _uiState.value
        return when {
            state.pin.length < 4 -> {
                _uiState.update { it.copy(error = "PIN must be at least 4 digits.") }
                false
            }
            state.pin != state.confirmPin -> {
                _uiState.update { it.copy(error = "PINs don't match. Try again.") }
                false
            }
            else -> true
        }
    }

    fun completeOnboarding(onComplete: (String) -> Unit) {
        if (validatePin()) {
            onComplete(_uiState.value.pin)
        }
    }
}
