package com.silentwitness.data.repository

import android.content.Context
import com.silentwitness.data.encryption.CryptoManager
import com.silentwitness.data.network.models.CheckInSettingsEntity
import com.silentwitness.domain.models.CheckInSettings
import com.silentwitness.domain.repository.AuthRepository
import com.silentwitness.domain.repository.CheckInRepository
import com.silentwitness.notification.CheckInScheduler
import dagger.hilt.android.qualifiers.ApplicationContext
import io.github.jan.supabase.SupabaseClient
import io.github.jan.supabase.postgrest.postgrest
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.emitAll
import kotlinx.coroutines.flow.flow
import java.time.LocalDateTime
import javax.inject.Singleton

@Singleton
class SupabaseCheckInRepository(
    private val client: SupabaseClient,
    private val authRepository: AuthRepository,
    private val cryptoManager: CryptoManager,
    @ApplicationContext private val appContext: Context
) : CheckInRepository {

    private val _cache = MutableStateFlow(CheckInSettings())

    override fun getCheckInSettings(): Flow<CheckInSettings> = flow {
        refresh()
        emitAll(_cache)
    }

    override suspend fun saveCheckInSettings(settings: CheckInSettings) {
        _cache.value = settings
        val uid = authRepository.ensureSignedIn()
        val entity = CheckInSettingsEntity(
            userId = uid,
            enabled = settings.enabled,
            intervalHours = settings.intervalHours,
            notifyTier = settings.notifyTier,
            releaseOnMissed = settings.releaseOnMissed,
            gracePeriodMinutes = settings.gracePeriodMinutes,
            updatedAt = LocalDateTime.now().toString()
        )
        client.postgrest.from("check_in_settings").upsert(entity) {
            onConflict = "user_id"
        }
        if (settings.enabled) {
            CheckInScheduler.schedule(appContext, settings.intervalHours)
        } else {
            CheckInScheduler.cancel(appContext)
        }
    }

    private suspend fun refresh() {
        runCatching {
            val uid = authRepository.ensureSignedIn()
            val rows = client.postgrest.from("check_in_settings").select {
                filter { eq("user_id", uid) }
            }.decodeList<CheckInSettingsEntity>()
            _cache.value = rows.firstOrNull()?.let {
                CheckInSettings(
                    enabled = it.enabled,
                    intervalHours = it.intervalHours,
                    notifyTier = it.notifyTier,
                    releaseOnMissed = it.releaseOnMissed,
                    gracePeriodMinutes = it.gracePeriodMinutes
                )
            } ?: CheckInSettings()
        }.onFailure { /* offline / auth error -> keep stale cache */ }
    }
}
