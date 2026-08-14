package com.silentwitness.data.network.models

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

/**
 * DB row for the `entries` table. All sensitive fields are stored as base64-encoded
 * AES-256-GCM ciphertext (`encryptedData`, `iv`, `salt`) plus a plaintext SHA-256
 * integrity hash. `createdAt`/`updatedAt` are ISO-8601 strings to avoid extra
 * datetime serializers. Column names are snake_case and must match the SQL schema.
 */
@Serializable
data class EntryEntity(
    val id: String = "",
    @SerialName("user_id") val userId: String = "",
    @SerialName("encrypted_data") val encryptedData: String = "",
    val iv: String = "",
    val salt: String = "",
    @SerialName("created_at") val createdAt: String = "",
    @SerialName("updated_at") val updatedAt: String? = null,
    val hash: String = "",
    @SerialName("photo_hash") val photoHash: String? = null,
    @SerialName("audio_hash") val audioHash: String? = null,
    @SerialName("is_deleted") val isDeleted: Boolean = false,
    @SerialName("sync_version") val syncVersion: Long = 1L
)
