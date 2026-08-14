package com.silentwitness.data.repository

import com.silentwitness.domain.models.Contact
import com.silentwitness.domain.repository.ContactsRepository
import com.silentwitness.domain.repository.PartialContactUpdate
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow

class FakeContactsRepository : ContactsRepository {
    private val _contacts = MutableStateFlow<List<Contact>>(
        listOf(
            Contact("1", "Maya Chen", "555-0142", 1),
            Contact("2", "Dr. Sarah Okafor", "sokafor@clinic.org", 2)
        )
    )
    override fun getAllContacts(): Flow<List<Contact>> = _contacts.asStateFlow()

    override suspend fun addContact(contact: Contact): Contact {
        val new = contact.copy(id = java.util.UUID.randomUUID().toString())
        _contacts.value = _contacts.value + new
        return new
    }

    override suspend fun updateContact(id: String, updates: PartialContactUpdate) {
        _contacts.value = _contacts.value.map {
            if (it.id == id) {
                it.copy(
                    name = updates.name ?: it.name,
                    contactMethod = updates.contactMethod ?: it.contactMethod,
                    tier = updates.tier ?: it.tier
                )
            } else it
        }
    }

    override suspend fun deleteContact(id: String) {
        _contacts.value = _contacts.value.filter { it.id != id }
    }
}
