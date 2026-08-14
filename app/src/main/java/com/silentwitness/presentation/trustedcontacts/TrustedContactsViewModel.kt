package com.silentwitness.presentation.trustedcontacts

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.silentwitness.domain.models.Contact
import com.silentwitness.domain.repository.ContactsRepository
import com.silentwitness.domain.repository.PartialContactUpdate
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

data class TrustedContactsUiState(
    val contacts: List<Contact> = emptyList()
)

@HiltViewModel
class TrustedContactsViewModel @Inject constructor(
    private val repository: ContactsRepository
) : ViewModel() {

    private val _uiState = MutableStateFlow(TrustedContactsUiState())
    val uiState: StateFlow<TrustedContactsUiState> = _uiState.asStateFlow()

    init {
        viewModelScope.launch {
            repository.getAllContacts().collect { contacts ->
                _uiState.value = TrustedContactsUiState(
                    contacts = contacts.sortedBy { it.tier }
                )
            }
        }
    }

    fun saveContact(id: String?, name: String, contactMethod: String, tier: Int) {
        if (name.isBlank()) return
        viewModelScope.launch {
            if (id != null) {
                repository.updateContact(
                    id,
                    PartialContactUpdate(
                        name = name.trim(),
                        contactMethod = contactMethod.trim(),
                        tier = tier
                    )
                )
            } else {
                repository.addContact(
                    Contact(name = name.trim(), contactMethod = contactMethod.trim(), tier = tier)
                )
            }
        }
    }

    fun deleteContact(id: String) {
        viewModelScope.launch { repository.deleteContact(id) }
    }
}
