package com.silentwitness.presentation.session

sealed class SessionState {
    object Onboarding : SessionState()
    object Locked : SessionState()
    object Unlocked : SessionState()
}
