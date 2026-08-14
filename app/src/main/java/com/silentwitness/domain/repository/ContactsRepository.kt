package com.silentwitness.domain.repository

import com.silentwitness.domain.models.Contact
import com.silentwitness.domain.models.ContactTier
import kotlinx.coroutines.flow.Flow

interface ContactsRepository {
    fun getAllContacts(): Flow<List<Contact>>
    suspend fun addContact(contact: Contact): Contact
    suspend fun updateContact(id: String, updates: PartialContactUpdate)
    suspend fun deleteContact(id: String)
}

data class PartialContactUpdate(
    val name: String? = null,
    val contactMethod: String? = null,
    val tier: ContactTier? = null
)
