package com.silentwitness.data.repository

import com.silentwitness.data.encryption.CryptoManager
import com.silentwitness.data.network.models.AuditPayload
import com.silentwitness.data.network.models.EncryptedEntryPayload
import com.silentwitness.data.network.models.EntryEntity
import com.silentwitness.domain.models.AuditEntry
import com.silentwitness.domain.models.LogEntry
import com.silentwitness.domain.repository.AuthRepository
import com.silentwitness.domain.repository.LogEntryRepository
import com.silentwitness.domain.repository.PartialLogEntryUpdate
import io.github.jan.supabase.SupabaseClient
import io.github.jan.supabase.postgrest.postgrest
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.emitAll
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.flowOn
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable
import kotlinx.serialization.json.Json
import java.time.LocalDateTime
import java.util.UUID
import javax.inject.Singleton

@Singleton
class SupabaseLogEntryRepository(
    private val client: SupabaseClient,
    private val authRepository: AuthRepository,
    private val cryptoManager: CryptoManager,
    private val json: Json
) : LogEntryRepository {

    private val _cache = MutableStateFlow<List<LogEntry>>(emptyList())

    override fun getAllEntries(): Flow<List<LogEntry>> = flow {
        refresh()
        emitAll(_cache)
    }.flowOn(Dispatchers.IO)

    override suspend fun getEntryById(id: String): LogEntry? =
        _cache.value.firstOrNull { it.id == id }

    override suspend fun addEntry(entry: LogEntry): LogEntry {
        val newEntry = entry.copy(
            id = if (entry.id.isBlank()) UUID.randomUUID().toString() else entry.id,
            auditTrail = if (entry.auditTrail.isEmpty()) {
                listOf(AuditEntry("Created", entry.createdAt))
            } else {
                entry.auditTrail
            }
        )
        _cache.value = listOf(newEntry) + _cache.value
        upsertRemote(newEntry)
        return newEntry
    }

    override suspend fun updateEntry(id: String, updates: PartialLogEntryUpdate) {
        val current = _cache.value.firstOrNull { it.id == id } ?: return
        val merged = current.copy(
            date = updates.date ?: current.date,
            description = updates.description ?: current.description,
            category = updates.category ?: current.category,
            injuryNotes = updates.injuryNotes ?: current.injuryNotes,
            location = updates.location ?: current.location,
            photoAttached = updates.photoAttached ?: current.photoAttached,
            audioAttached = updates.audioAttached ?: current.audioAttached,
            auditTrail = current.auditTrail + AuditEntry("Edited", LocalDateTime.now())
        )
        _cache.value = _cache.value.map { if (it.id == id) merged else it }
        upsertRemote(merged)
    }

    override suspend fun deleteEntry(id: String) {
        _cache.value = _cache.value.filter { it.id != id }
        val uid = authRepository.ensureSignedIn()
        client.postgrest.from("entries").update(EntrySoftDelete()) {
            filter {
                eq("id", id)
                eq("user_id", uid)
            }
        }
    }

    private suspend fun refresh() {
        runCatching {
            val uid = authRepository.ensureSignedIn()
            val rows = client.postgrest.from("entries").select {
                filter {
                    eq("user_id", uid)
                    eq("is_deleted", false)
                }
            }.decodeList<EntryEntity>()
            _cache.value = rows
                .mapNotNull { it.toDomain() }
                .sortedByDescending { it.date }
        }.onFailure { /* offline / auth error -> keep stale cache */ }
    }

    private suspend fun upsertRemote(entry: LogEntry) {
        val uid = authRepository.ensureSignedIn()
        val payload = entry.toPayload()
        val blob = cryptoManager.encryptJson(
            json.encodeToString(EncryptedEntryPayload.serializer(), payload)
        )
        val entity = EntryEntity(
            id = entry.id,
            userId = uid,
            encryptedData = blob.ciphertextB64,
            iv = blob.ivB64,
            salt = blob.saltB64,
            createdAt = entry.createdAt.toString(),
            updatedAt = LocalDateTime.now().toString(),
            hash = blob.hash,
            isDeleted = false
        )
        client.postgrest.from("entries").upsert(entity) {
            onConflict = "id"
        }
    }

    private fun EntryEntity.toDomain(): LogEntry? = runCatching {
        val plaintext = cryptoManager.decryptJson(
            CryptoManager.EncryptedBlob(
                ciphertextB64 = encryptedData,
                ivB64 = iv,
                saltB64 = salt,
                hash = hash
            )
        )
        val payload = json.decodeFromString<EncryptedEntryPayload>(plaintext)
        LogEntry(
            id = id,
            date = LocalDateTime.parse(payload.date),
            description = payload.description,
            category = payload.category,
            injuryNotes = payload.injuryNotes,
            location = payload.location,
            photoAttached = payload.photoAttached,
            audioAttached = payload.audioAttached,
            captureTimeUnknown = payload.captureTimeUnknown,
            senderInfo = payload.senderInfo,
            platform = payload.platform,
            createdAt = LocalDateTime.parse(payload.createdAt),
            auditTrail = payload.auditTrail.map {
                AuditEntry(it.action, LocalDateTime.parse(it.timestamp))
            }
        )
    }.getOrNull()

    private fun LogEntry.toPayload(): EncryptedEntryPayload = EncryptedEntryPayload(
        id = id,
        date = date.toString(),
        description = description,
        category = category,
        injuryNotes = injuryNotes,
        location = location,
        photoAttached = photoAttached,
        audioAttached = audioAttached,
        captureTimeUnknown = captureTimeUnknown,
        senderInfo = senderInfo,
        platform = platform,
        createdAt = createdAt.toString(),
        auditTrail = auditTrail.map { AuditPayload(it.action, it.timestamp.toString()) }
    )
}

@Serializable
private data class EntrySoftDelete(
    @SerialName("is_deleted") val isDeleted: Boolean = true
)
