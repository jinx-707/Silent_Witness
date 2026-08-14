package com.silentwitness.data.network.models

import kotlinx.serialization.Serializable

/**
 * Plaintext JSON that is encrypted client-side (AES-256-GCM) before upload.
 * Dates are ISO-8601 strings so kotlinx-serialization works without extra datetime libs.
 */
@Serializable
data class EncryptedEntryPayload(
    val id: String,
    val date: String,
    val description: String,
    val category: String,
    val injuryNotes: String? = null,
    val location: String? = null,
    val photoAttached: Boolean = false,
    val audioAttached: Boolean = false,
    val captureTimeUnknown: Boolean = false,
    val senderInfo: String? = null,
    val platform: String? = null,
    val createdAt: String,
    val auditTrail: List<AuditPayload> = emptyList()
)

@Serializable
data class AuditPayload(
    val action: String,
    val timestamp: String
)

@Serializable
data class EncryptedContactPayload(
    val id: String,
    val name: String,
    val contactMethod: String
)

@Serializable
data class EncryptedSafetyPlanPayload(
    val exitRoutes: List<String> = emptyList(),
    val emergencyItems: List<EmergencyItemPayload> = emptyList(),
    val safePeople: List<String> = emptyList(),
    val safePlaces: List<String> = emptyList(),
    val codeWord: String = ""
)

@Serializable
data class EmergencyItemPayload(
    val item: String,
    val packed: Boolean
)
