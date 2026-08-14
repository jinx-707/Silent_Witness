package com.silentwitness.domain.models

data class Contact(
    val id: String = java.util.UUID.randomUUID().toString(),
    val name: String,
    val contactMethod: String,
    val tier: ContactTier
)
