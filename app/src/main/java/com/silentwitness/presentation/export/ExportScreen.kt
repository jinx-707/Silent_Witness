package com.silentwitness.presentation.export

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
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.outlined.ArrowBack
import androidx.compose.material3.Button
import androidx.compose.material3.Checkbox
import androidx.compose.material3.CheckboxDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBar
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
import com.silentwitness.presentation.theme.SwMuted
import com.silentwitness.presentation.theme.SwSurface
import com.silentwitness.utils.categoryLabel
import com.silentwitness.utils.formatFull

@OptIn(ExperimentalFoundationApi::class, ExperimentalMaterial3Api::class)
@Composable
fun ExportScreen(
    onGenerateCertificate: (List<String>) -> Unit,
    onBack: () -> Unit,
    viewModel: ExportViewModel = hiltViewModel()
) {
    val uiState by viewModel.uiState.collectAsState()
    val groupedEntries by viewModel.groupedEntries.collectAsState()

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Export") },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(Icons.AutoMirrored.Outlined.ArrowBack, contentDescription = "Back")
                    }
                }
            )
        },
        bottomBar = {
            Surface(color = SwSurface) {
                Column(Modifier.padding(12.dp)) {
                    Text(
                        "${uiState.selectedIds.size} of ${uiState.entries.size} entries selected",
                        style = MaterialTheme.typography.labelMedium,
                        color = SwMuted
                    )
                    Spacer(Modifier.height(8.dp))
                    Button(
                        onClick = { onGenerateCertificate(uiState.selectedIds.toList()) },
                        enabled = uiState.selectedIds.isNotEmpty(),
                        modifier = Modifier.fillMaxWidth()
                    ) { Text("Generate certificate") }
                }
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
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Text(
                        "Select entries to include",
                        style = MaterialTheme.typography.titleMedium,
                        modifier = Modifier.weight(1f)
                    )
                    TextButton(onClick = { viewModel.toggleSelectAll() }) {
                        Text(if (uiState.selectedIds.size == uiState.entries.size && uiState.entries.isNotEmpty()) "Select none" else "Select all")
                    }
                }
                Spacer(Modifier.height(8.dp))
            }

            if (groupedEntries.isEmpty()) {
                item {
                    Text(
                        "No entries to export yet.",
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
                        val entry = monthEntries[index]
                        ExportRow(
                            entry = entry,
                            selected = entry.id in uiState.selectedIds,
                            onClick = { viewModel.toggleSelection(entry.id) }
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun ExportRow(entry: LogEntry, selected: Boolean, onClick: () -> Unit) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .background(
                if (selected) SwAccent.copy(alpha = 0.15f) else SwSurface,
                androidx.compose.foundation.shape.RoundedCornerShape(10.dp)
            )
            .clickable(onClick = onClick)
            .padding(10.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Checkbox(
            checked = selected,
            onCheckedChange = { onClick() },
            colors = CheckboxDefaults.colors(checkedColor = SwAccent)
        )
        Spacer(Modifier.width(8.dp))
        Column(modifier = Modifier.weight(1f)) {
            Text(
                entry.description,
                style = MaterialTheme.typography.bodyMedium,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )
            Text(
                "${entry.date.formatFull()} • ${categoryLabel(entry.category)}",
                style = MaterialTheme.typography.labelSmall,
                color = SwMuted
            )
        }
    }
}
