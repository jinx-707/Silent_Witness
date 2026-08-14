package com.silentwitness.presentation.dashboard

import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.background
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
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.Add
import androidx.compose.material.icons.outlined.Contacts
import androidx.compose.material.icons.outlined.IosShare
import androidx.compose.material.icons.outlined.PhotoLibrary
import androidx.compose.material.icons.outlined.Shield
import androidx.compose.material.icons.outlined.Timeline
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import com.silentwitness.domain.models.LogEntry
import com.silentwitness.presentation.theme.SwAccent
import com.silentwitness.presentation.theme.SwCard
import com.silentwitness.presentation.theme.SwMuted
import com.silentwitness.presentation.theme.SwSurface
import com.silentwitness.utils.categoryLabel
import com.silentwitness.utils.formatFull

@OptIn(ExperimentalFoundationApi::class)
@Composable
fun DashboardScreen(
    onNavigateToAddEntry: () -> Unit,
    onNavigateToEntryDetail: (String) -> Unit,
    onNavigateToScreenshotImporter: () -> Unit,
    onNavigateToExport: () -> Unit,
    onNavigateToContacts: () -> Unit,
    onNavigateToSafetyPlan: () -> Unit,
    onNavigateToTimeline: () -> Unit,
    onNavigateToSettings: () -> Unit,
    viewModel: DashboardViewModel = hiltViewModel()
) {
    val entries by viewModel.entries.collectAsState()
    val groupedEntries by viewModel.groupedEntries.collectAsState()
    val isLoading by viewModel.isLoading.collectAsState()

    Box(modifier = Modifier.fillMaxSize()) {
        LazyColumn(
            modifier = Modifier.fillMaxSize(),
            contentPadding = androidx.compose.foundation.layout.PaddingValues(
                start = 16.dp,
                end = 16.dp,
                top = 16.dp,
                bottom = 96.dp
            )
        ) {
            item {
                Text("Dashboard", style = MaterialTheme.typography.headlineMedium)
                Spacer(Modifier.height(4.dp))
                Text(
                    "Incidents and quick actions.",
                    style = MaterialTheme.typography.bodyMedium
                )
                Spacer(Modifier.height(20.dp))
            }

            item {
                Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                    QuickActionCard(
                        title = "Add Entry",
                        subtitle = "Log an incident",
                        icon = Icons.Outlined.Add,
                        modifier = Modifier.weight(1f),
                        onClick = onNavigateToAddEntry
                    )
                    QuickActionCard(
                        title = "Import",
                        subtitle = "From gallery",
                        icon = Icons.Outlined.PhotoLibrary,
                        modifier = Modifier.weight(1f),
                        onClick = onNavigateToScreenshotImporter
                    )
                }
                Spacer(Modifier.height(12.dp))
                Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                    QuickActionCard(
                        title = "Export",
                        subtitle = "Logs & certificate",
                        icon = Icons.Outlined.IosShare,
                        modifier = Modifier.weight(1f),
                        onClick = onNavigateToExport
                    )
                    QuickActionCard(
                        title = "Contacts",
                        subtitle = "Trusted people",
                        icon = Icons.Outlined.Contacts,
                        modifier = Modifier.weight(1f),
                        onClick = onNavigateToContacts
                    )
                }
                Spacer(Modifier.height(12.dp))
                Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                    QuickActionCard(
                        title = "Safety Plan",
                        subtitle = "Exit & plan",
                        icon = Icons.Outlined.Shield,
                        modifier = Modifier.weight(1f),
                        onClick = onNavigateToSafetyPlan
                    )
                    QuickActionCard(
                        title = "Timeline",
                        subtitle = "Escalation view",
                        icon = Icons.Outlined.Timeline,
                        modifier = Modifier.weight(1f),
                        onClick = onNavigateToTimeline
                    )
                }
            }

            item {
                Spacer(Modifier.height(24.dp))
                Text("Recent activity", style = MaterialTheme.typography.titleMedium)
            }

            if (isLoading) {
                item {
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(vertical = 48.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        CircularProgressIndicator()
                    }
                }
            } else if (entries.isEmpty()) {
                item {
                    Spacer(Modifier.height(16.dp))
                    Text(
                        "No entries yet. Tap “Add Entry” to record your first incident.",
                        style = MaterialTheme.typography.bodyMedium,
                        color = SwMuted
                    )
                }
            } else {
                groupedEntries.forEach { (month, monthEntries) ->
                    stickyHeader(key = month) {
                        Text(
                            month,
                            style = MaterialTheme.typography.labelLarge,
                            color = SwMuted,
                            modifier = Modifier
                                .fillMaxWidth()
                                .background(SwSurface)
                                .padding(vertical = 8.dp)
                        )
                    }
                    items(monthEntries.size, key = { monthEntries[it].id }) { index ->
                        EntryCard(
                            entry = monthEntries[index],
                            onClick = { onNavigateToEntryDetail(monthEntries[index].id) }
                        )
                    }
                }
            }
        }

        FloatingActionButton(
            onClick = onNavigateToAddEntry,
            containerColor = SwAccent,
            modifier = Modifier
                .align(Alignment.BottomEnd)
                .padding(24.dp)
        ) {
            Icon(Icons.Outlined.Add, contentDescription = "Add entry")
        }
    }
}

@Composable
private fun EntryCard(entry: LogEntry, onClick: () -> Unit) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 6.dp)
            .clickable(onClick = onClick),
        colors = CardDefaults.cardColors(containerColor = SwCard),
        shape = RoundedCornerShape(12.dp)
    ) {
        Column(Modifier.padding(12.dp)) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                FilterChip(
                    selected = false,
                    onClick = {},
                    label = { Text(categoryLabel(entry.category), style = MaterialTheme.typography.labelSmall) },
                    enabled = false
                )
                Spacer(Modifier.width(8.dp))
                Text(
                    entry.date.formatFull(),
                    style = MaterialTheme.typography.labelSmall,
                    color = SwMuted
                )
            }
            Spacer(Modifier.height(8.dp))
            Text(
                entry.description,
                style = MaterialTheme.typography.bodyLarge,
                maxLines = 2,
                overflow = TextOverflow.Ellipsis
            )
        }
    }
}

@Composable
private fun QuickActionCard(
    title: String,
    subtitle: String,
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    modifier: Modifier = Modifier,
    onClick: () -> Unit
) {
    Card(
        modifier = modifier.clickable(onClick = onClick),
        colors = CardDefaults.cardColors(containerColor = SwCard),
        shape = RoundedCornerShape(12.dp)
    ) {
        Column(Modifier.padding(16.dp)) {
            Icon(icon, contentDescription = null, tint = SwAccent)
            Spacer(Modifier.height(12.dp))
            Text(title, style = MaterialTheme.typography.titleMedium)
            Text(subtitle, style = MaterialTheme.typography.bodyMedium, color = SwMuted)
        }
    }
}
