package com.silentwitness.presentation.trustedcontacts

import android.content.Intent
import android.net.Uri
import android.widget.Toast
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.outlined.ArrowBack
import androidx.compose.material.icons.outlined.Add
import androidx.compose.material.icons.outlined.Phone
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import com.silentwitness.domain.models.Contact
import com.silentwitness.presentation.theme.SwAccent
import com.silentwitness.presentation.theme.SwCard
import com.silentwitness.presentation.theme.SwMuted
import com.silentwitness.presentation.theme.SwText

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun TrustedContactsScreen(
    onBack: () -> Unit,
    viewModel: TrustedContactsViewModel = hiltViewModel()
) {
    val context = LocalContext.current
    val uiState by viewModel.uiState.collectAsState()
    var showForm by remember { mutableStateOf(false) }
    var editingContact by remember { mutableStateOf<Contact?>(null) }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Trusted contacts") },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(Icons.AutoMirrored.Outlined.ArrowBack, contentDescription = "Back")
                    }
                }
            )
        },
        floatingActionButton = {
            FloatingActionButton(
                onClick = { editingContact = null; showForm = true },
                containerColor = SwAccent
            ) {
                Icon(Icons.Outlined.Add, contentDescription = "Add contact")
            }
        }
    ) { padding ->
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding),
            contentPadding = androidx.compose.foundation.layout.PaddingValues(16.dp)
        ) {
            item {
                Text(
                    "People you could reach in an emergency. Tier 1 = first to contact, tier 3 = last resort.",
                    style = MaterialTheme.typography.bodySmall,
                    color = SwMuted
                )
                Spacer(Modifier.height(12.dp))
            }

            (1..3).forEach { tier ->
                val tierContacts = uiState.contacts.filter { it.tier == tier }
                item {
                    Text(
                        "Tier $tier",
                        style = MaterialTheme.typography.titleSmall,
                        modifier = Modifier.padding(top = 8.dp, bottom = 4.dp)
                    )
                }
                items(tierContacts) { contact ->
                    val phoneNumber = contact.contactMethod.replace(Regex("[^0-9+]"), "")
                    val canCall = isPhoneNumber(contact.contactMethod)

                    ContactCard(
                        contact = contact,
                        onEdit = {
                            editingContact = contact
                            showForm = true
                        },
                        onDelete = { viewModel.deleteContact(contact.id) },
                        onCallClick = {
                            if (canCall) {
                                val intent = Intent(Intent.ACTION_DIAL, Uri.parse("tel:$phoneNumber"))
                                context.startActivity(intent)
                            } else {
                                Toast.makeText(context, "Not a valid phone number", Toast.LENGTH_SHORT).show()
                            }
                        }
                    )
                }
            }

            if (uiState.contacts.isEmpty()) {
                item {
                    Spacer(Modifier.height(16.dp))
                    Text(
                        "No contacts yet. Add at least one trusted person.",
                        style = MaterialTheme.typography.bodyMedium,
                        color = SwMuted
                    )
                }
            }
        }
    }

    if (showForm) {
        ContactForm(
            initial = editingContact,
            onSave = { id, name, method, tier ->
                viewModel.saveContact(id, name, method, tier)
                showForm = false
            },
            onDismiss = { showForm = false }
        )
    }
}

private fun isPhoneNumber(text: String): Boolean {
    return text.replace(Regex("[^0-9+]"), "").length >= 7
}

@Composable
fun ContactCard(
    contact: Contact,
    onEdit: () -> Unit,
    onDelete: () -> Unit,
    onCallClick: () -> Unit
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .clickable { onCallClick() },
        colors = CardDefaults.cardColors(containerColor = SwCard)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Text(contact.name, style = MaterialTheme.typography.titleMedium, color = SwText)
                    Spacer(modifier = Modifier.width(4.dp))
                    if (isPhoneNumber(contact.contactMethod)) {
                        Icon(
                            Icons.Outlined.Phone,
                            contentDescription = "Call",
                            tint = SwAccent,
                            modifier = Modifier.size(16.dp)
                        )
                    }
                }
                Text(contact.contactMethod, style = MaterialTheme.typography.bodyMedium, color = SwMuted)
            }
            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                TextButton(onClick = onEdit) { Text("Edit", color = SwAccent) }
                TextButton(onClick = onDelete) { Text("Remove", color = SwMuted) }
            }
        }
    }
}

@Composable
private fun ContactForm(
    initial: Contact?,
    onSave: (String?, String, String, Int) -> Unit,
    onDismiss: () -> Unit
) {
    var name by remember { mutableStateOf(initial?.name ?: "") }
    var method by remember { mutableStateOf(initial?.contactMethod ?: "") }
    var tier by remember { mutableStateOf(initial?.tier ?: 1) }

    androidx.compose.material3.AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text(if (initial != null) "Edit contact" else "Add contact") },
        text = {
            Column {
                OutlinedTextField(
                    value = name,
                    onValueChange = { name = it },
                    label = { Text("Name") },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth()
                )
                Spacer(Modifier.height(12.dp))
                OutlinedTextField(
                    value = method,
                    onValueChange = { method = it },
                    label = { Text("Phone / method") },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth()
                )
                Spacer(Modifier.height(12.dp))
                Text("Tier", style = MaterialTheme.typography.titleSmall)
                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    (1..3).forEach { t ->
                        androidx.compose.material3.FilterChip(
                            selected = tier == t,
                            onClick = { tier = t },
                            label = { Text("$t") }
                        )
                    }
                }
            }
        },
        confirmButton = {
            Button(
                onClick = { onSave(initial?.id, name, method, tier) },
                enabled = name.isNotBlank()
            ) { Text("Save") }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) { Text("Cancel") }
        }
    )
}
