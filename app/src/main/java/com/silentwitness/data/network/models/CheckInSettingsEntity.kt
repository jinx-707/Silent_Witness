package com.silentwitness.data.network.models

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class CheckInSettingsEntity(
    @SerialName("user_id") val userId: String = "",
    val enabled: Boolean = false,
    @SerialName("interval_hours") val intervalHours: Int = 4,
    @SerialName("notify_tier") val notifyTier: Int = 1,
    @SerialName("release_on_missed") val releaseOnMissed: Boolean = false,
    @SerialName("grace_period_minutes") val gracePeriodMinutes: Int = 15,
    @SerialName("updated_at") val updatedAt: String = ""
)
