package com.silentwitness.data.network.models

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class SafetyPlanEntity(
    @SerialName("user_id") val userId: String = "",
    @SerialName("encrypted_data") val encryptedData: String = "",
    val iv: String = "",
    val salt: String = "",
    val hash: String = "",
    @SerialName("updated_at") val updatedAt: String = ""
)
