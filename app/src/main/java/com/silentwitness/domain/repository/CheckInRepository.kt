package com.silentwitness.domain.repository

import com.silentwitness.domain.models.CheckInSettings
import kotlinx.coroutines.flow.Flow

interface CheckInRepository {
    fun getCheckInSettings(): Flow<CheckInSettings>
    suspend fun saveCheckInSettings(settings: CheckInSettings)
}
