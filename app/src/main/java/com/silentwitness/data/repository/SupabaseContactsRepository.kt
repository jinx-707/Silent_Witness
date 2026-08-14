package com.silentwitness.data.repository

import com.silentwitness.data.encryption.CryptoManager
import com.silentwitness.data.network.models.ContactEntity
import com.silentwitness.data.network.models.EncryptedContactPayload
import com.silentwitness.domain.models.Contact
import com.silentwitness.domain.repository.AuthRepository
import com.silentwitness.domain.repository.ContactsRepository
import com.silentwitness.domain.repository.PartialContactUpdate
import io.github.jan.supabase.SupabaseClient
import io.github.jan.supabase.postgrest.postgrest
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.emitAll
import kotlinx.coroutines.flow.flow
import kotlinx.serialization.json.Json
import java.time.LocalDateTime
import java.util.UUID
import javax.inject.Singleton

@Singleton
class SupabaseContactsRepository(
    private val client: SupabaseClient,
    private val authRepository: AuthRepository,
    private val cryptoManager: CryptoManager,
    private val json: Json
) : ContactsRepository {

    private val _cache = MutableStateFlow<List<Contact>>(emptyList())

    override fun getAllContacts(): Flow<List<Contact>> = flow {
        refresh()
        emitAll(_cache)
    }

    override suspend fun addContact(contact: Contact): Contact {
        val newContact = contact.copy(
            id = if (contact.id.isBlank()) UUID.randomUUID().toString() else contact.id
        )
        _cache.value = _cache.value + newContact
        upsertRemote(newContact)
        return newContact
    }

    override suspend fun updateContact(id: String, updates: PartialContactUpdate) {
        val current = _cache.value.firstOrNull { it.id == id } ?: return
        val merged = current.copy(
            name = updates.name ?: current.name,
            contactMethod = updates.contactMethod ?: current.contactMethod,
            tier = updates.tier ?: current.tier
        )
        _cache.value = _cache.value.map { if (it.id == id) merged else it }
        upsertRemote(merged)
    }

    override suspend fun deleteContact(id: String) {
        _cache.value = _cache.value.filter { it.id != id }
        val uid = authRepository.ensureSignedIn()
        client.postgrest.from("contacts").delete {
            filter {
                eq("id", id)
                eq("user_id", uid)
            }
        }
    }

    private suspend fun refresh() {
        runCatching {
            val uid = authRepository.ensureSignedIn()
            val rows = client.postgrest.from("contacts").select {
                filter { eq("user_id", uid) }
            }.decodeList<ContactEntity>()
            _cache.value = rows.mapNotNull { it.toDomain() }.sortedBy { it.name }
        }.onFailure { /* offline / auth error -> keep stale cache */ }
    }

    private suspend fun upsertRemote(contact: Contact) {
        val uid = authRepository.ensureSignedIn()
        val payload = EncryptedContactPayload(
            id = contact.id,
            name = contact.name,
            contactMethod = contact.contactMethod
        )
        val blob = cryptoManager.encryptJson(
            json.encodeToString(EncryptedContactPayload.serializer(), payload)
        )
        val entity = ContactEntity(
            id = contact.id,
            userId = uid,
            encryptedData = blob.ciphertextB64,
            iv = blob.ivB64,
            salt = blob.saltB64,
            hash = blob.hash,
            tier = contact.tier,
            createdAt = LocalDateTime.now().toString(),
            updatedAt = LocalDateTime.now().toString()
        )
        client.postgrest.from("contacts").upsert(entity) {
            onConflict = "id"
        }
    }

    private fun ContactEntity.toDomain(): Contact? = runCatching {
        val plaintext = cryptoManager.decryptJson(
            CryptoManager.EncryptedBlob(
                ciphertextB64 = encryptedData,
                ivB64 = iv,
                saltB64 = salt,
                hash = hash
            )
        )
        val payload = json.decodeFromString<EncryptedContactPayload>(plaintext)
        Contact(
            id = id,
            name = payload.name,
            contactMethod = payload.contactMethod,
            tier = tier
        )
    }.getOrNull()
}
