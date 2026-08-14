package com.silentwitness.presentation.session

import androidx.lifecycle.ViewModel
import com.silentwitness.data.encryption.CryptoManager
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import javax.inject.Inject

@HiltViewModel
class SessionViewModel @Inject constructor(
    private val cryptoManager: CryptoManager
) : ViewModel() {
    // PIN persists (encrypted) so onboarding runs only once across app restarts.
    // The `unlocked` state is intentionally NOT persisted: every fresh launch starts
    // at the calculator (Locked) until the PIN is re-entered.
    private var storedPin: String? = cryptoManager.loadPin()

    private val _state = MutableStateFlow<SessionState>(
        if (storedPin == null) SessionState.Onboarding else SessionState.Locked
    )
    val state: StateFlow<SessionState> = _state.asStateFlow()

    fun setupPin(pin: String) {
        storedPin = pin
        cryptoManager.savePin(pin)
        _state.value = SessionState.Locked
    }

    fun unlock(attempt: String): Boolean {
        if (attempt == storedPin) {
            _state.value = SessionState.Unlocked
            return true
        }
        return false
    }

    fun lock() {
        _state.value = SessionState.Locked
    }

    fun changePin(oldPin: String, newPin: String): Boolean {
        if (oldPin == storedPin) {
            storedPin = newPin
            cryptoManager.savePin(newPin)
            return true
        }
        return false
    }

    fun isPinSet(): Boolean = storedPin != null

    fun completeOnboarding(pin: String) {
        setupPin(pin)
    }
}
