package com.silentwitness.data.repository

import com.silentwitness.domain.models.CheckInSettings
import com.silentwitness.domain.repository.CheckInRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow

class FakeCheckInRepository : CheckInRepository {
    private val _settings = MutableStateFlow(CheckInSettings())
    override fun getCheckInSettings(): Flow<CheckInSettings> = _settings.asStateFlow()

    override suspend fun saveCheckInSettings(settings: CheckInSettings) {
        _settings.value = settings
    }
}
