package com.silentwitness.data.repository

import com.silentwitness.data.encryption.CryptoManager
import com.silentwitness.data.network.models.EmergencyItemPayload
import com.silentwitness.data.network.models.EncryptedSafetyPlanPayload
import com.silentwitness.data.network.models.SafetyPlanEntity
import com.silentwitness.domain.models.EmergencyItem
import com.silentwitness.domain.models.SafetyPlan
import com.silentwitness.domain.repository.AuthRepository
import com.silentwitness.domain.repository.SafetyPlanRepository
import io.github.jan.supabase.SupabaseClient
import io.github.jan.supabase.postgrest.postgrest
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.emitAll
import kotlinx.coroutines.flow.flow
import kotlinx.serialization.json.Json
import java.time.LocalDateTime
import javax.inject.Singleton

@Singleton
class SupabaseSafetyPlanRepository(
    private val client: SupabaseClient,
    private val authRepository: AuthRepository,
    private val cryptoManager: CryptoManager,
    private val json: Json
) : SafetyPlanRepository {

    private val _cache = MutableStateFlow<SafetyPlan?>(null)

    override fun getSafetyPlan(): Flow<SafetyPlan?> = flow {
        refresh()
        emitAll(_cache)
    }

    override suspend fun saveSafetyPlan(plan: SafetyPlan) {
        _cache.value = plan
        val uid = authRepository.ensureSignedIn()
        val payload = EncryptedSafetyPlanPayload(
            exitRoutes = plan.exitRoutes,
            emergencyItems = plan.emergencyItems.map { EmergencyItemPayload(it.item, it.packed) },
            safePeople = plan.safePeople,
            safePlaces = plan.safePlaces,
            codeWord = plan.codeWord
        )
        val blob = cryptoManager.encryptJson(
            json.encodeToString(EncryptedSafetyPlanPayload.serializer(), payload)
        )
        val entity = SafetyPlanEntity(
            userId = uid,
            encryptedData = blob.ciphertextB64,
            iv = blob.ivB64,
            salt = blob.saltB64,
            hash = blob.hash,
            updatedAt = LocalDateTime.now().toString()
        )
        client.postgrest.from("safety_plans").upsert(entity) {
            onConflict = "user_id"
        }
    }

    private suspend fun refresh() {
        runCatching {
            val uid = authRepository.ensureSignedIn()
            val rows = client.postgrest.from("safety_plans").select {
                filter { eq("user_id", uid) }
            }.decodeList<SafetyPlanEntity>()
            _cache.value = rows.firstOrNull()?.toDomain()
        }.onFailure { /* offline / auth error -> keep stale cache */ }
    }

    private fun SafetyPlanEntity.toDomain(): SafetyPlan? = runCatching {
        val plaintext = cryptoManager.decryptJson(
            CryptoManager.EncryptedBlob(
                ciphertextB64 = encryptedData,
                ivB64 = iv,
                saltB64 = salt,
                hash = hash
            )
        )
        val payload = json.decodeFromString<EncryptedSafetyPlanPayload>(plaintext)
        SafetyPlan(
            exitRoutes = payload.exitRoutes,
            emergencyItems = payload.emergencyItems.map { EmergencyItem(it.item, it.packed) },
            safePeople = payload.safePeople,
            safePlaces = payload.safePlaces,
            codeWord = payload.codeWord
        )
    }.getOrNull()
}
