package com.silentwitness.domain.models

data class CheckInSettings(
    val enabled: Boolean = false,
    val intervalHours: Int = 4,
    val notifyTier: ContactTier = 1,
    val releaseOnMissed: Boolean = false,
    val gracePeriodMinutes: Int = 15
)
