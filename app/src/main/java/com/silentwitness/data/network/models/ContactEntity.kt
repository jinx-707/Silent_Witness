package com.silentwitness.data.network.models

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class ContactEntity(
    val id: String = "",
    @SerialName("user_id") val userId: String = "",
    @SerialName("encrypted_data") val encryptedData: String = "",
    val iv: String = "",
    val salt: String = "",
    val hash: String = "",
    val tier: Int = 1,
    @SerialName("created_at") val createdAt: String = "",
    @SerialName("updated_at") val updatedAt: String? = null
)
