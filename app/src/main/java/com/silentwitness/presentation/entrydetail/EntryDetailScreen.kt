package com.silentwitness.presentation.entrydetail

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.outlined.ArrowBack
import androidx.compose.material.icons.outlined.Edit
import androidx.compose.material.icons.outlined.ExpandLess
import androidx.compose.material.icons.outlined.ExpandMore
import androidx.compose.material.icons.outlined.LocationOn
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import com.silentwitness.presentation.theme.SwAccent
import com.silentwitness.presentation.theme.SwCard
import com.silentwitness.presentation.theme.SwMuted
import com.silentwitness.utils.categoryLabel
import com.silentwitness.utils.formatFull
import com.silentwitness.utils.formatTime

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun EntryDetailScreen(
    entryId: String,
    onEdit: (String) -> Unit,
    onBack: () -> Unit,
    viewModel: EntryDetailViewModel = hiltViewModel()
) {
    val entry by viewModel.entry.collectAsState()
    val showAudit by viewModel.showAudit.collectAsState()

    LaunchedEffect(entryId) { viewModel.loadEntry(entryId) }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Entry details") },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(Icons.AutoMirrored.Outlined.ArrowBack, contentDescription = "Back")
                    }
                },
                actions = {
                    entry?.let {
                        IconButton(onClick = { onEdit(it.id) }) {
                            Icon(Icons.Outlined.Edit, contentDescription = "Edit")
                        }
                    }
                }
            )
        }
    ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .verticalScroll(rememberScrollState())
                .padding(16.dp)
        ) {
            entry?.let { e ->
                Text(e.date.formatFull(), style = MaterialTheme.typography.titleMedium)
                Spacer(Modifier.height(4.dp))

                Text(
                    e.description,
                    style = MaterialTheme.typography.bodyLarge,
                    overflow = TextOverflow.Ellipsis
                )
                Spacer(Modifier.height(16.dp))

                Card(colors = CardDefaults.cardColors(containerColor = SwCard)) {
                    Column(Modifier.padding(12.dp)) {
                        Text("Category: ${categoryLabel(e.category)}", style = MaterialTheme.typography.bodyMedium)
                        Spacer(Modifier.height(8.dp))
                        Text(
                            "Photo: ${if (e.photoAttached) "Attached" else "None"}   |   Audio: ${if (e.audioAttached) "Attached" else "None"}",
                            style = MaterialTheme.typography.bodyMedium,
                            color = SwMuted
                        )
                    }
                }

                if (!e.location.isNullOrBlank()) {
                    Spacer(Modifier.height(12.dp))
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(Icons.Outlined.LocationOn, contentDescription = null, tint = SwMuted, modifier = Modifier.size(18.dp))
                        Spacer(Modifier.width(6.dp))
                        Text(e.location, style = MaterialTheme.typography.bodyMedium)
                    }
                }

                Spacer(Modifier.height(16.dp))
                Card(colors = CardDefaults.cardColors(containerColor = SwCard)) {
                    Column(Modifier.padding(12.dp)) {
                        Text("Injuries", style = MaterialTheme.typography.titleSmall)
                        Spacer(Modifier.height(4.dp))
                        Text(
                            e.injuryNotes ?: "No injuries recorded.",
                            style = MaterialTheme.typography.bodyMedium,
                            color = SwMuted
                        )
                    }
                }

                Spacer(Modifier.height(16.dp))
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text("Audit trail", style = MaterialTheme.typography.titleSmall)
                    Spacer(Modifier.weight(1f))
                    IconButton(onClick = { viewModel.toggleAudit() }) {
                        Icon(
                            if (showAudit) Icons.Outlined.ExpandLess else Icons.Outlined.ExpandMore,
                            contentDescription = "Toggle audit trail"
                        )
                    }
                }
                if (showAudit) {
                    HorizontalDivider()
                    e.auditTrail.forEach { audit ->
                        Row(
                            modifier = Modifier.padding(vertical = 6.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(
                                audit.timestamp.formatTime(),
                                style = MaterialTheme.typography.bodySmall,
                                color = SwMuted,
                                modifier = Modifier.width(72.dp)
                            )
                            Text(audit.action, style = MaterialTheme.typography.bodySmall)
                        }
                    }
                }

                Spacer(Modifier.height(24.dp))
                Button(
                    onClick = { viewModel.deleteEntry(onBack) },
                    colors = ButtonDefaults.buttonColors(
                        containerColor = MaterialTheme.colorScheme.errorContainer,
                        contentColor = MaterialTheme.colorScheme.onErrorContainer
                    ),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Text("Delete entry")
                }
                Spacer(Modifier.height(24.dp))
            } ?: run {
                Text("Loading…", style = MaterialTheme.typography.bodyMedium, color = SwMuted)
            }
        }
    }
}
