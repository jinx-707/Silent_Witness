package com.silentwitness.presentation.escalationtimeline

import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.IntrinsicSize
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.outlined.ArrowBack
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
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
import com.silentwitness.utils.formatDate
import com.silentwitness.utils.formatTime

@OptIn(ExperimentalFoundationApi::class, ExperimentalMaterial3Api::class)
@Composable
fun EscalationTimelineScreen(
    onEntryClick: (String) -> Unit,
    onBack: () -> Unit,
    viewModel: EscalationTimelineViewModel = hiltViewModel()
) {
    val groupedEntries by viewModel.groupedEntries.collectAsState()

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Escalation timeline") },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(Icons.AutoMirrored.Outlined.ArrowBack, contentDescription = "Back")
                    }
                }
            )
        }
    ) { padding ->
        if (groupedEntries.isEmpty()) {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(padding)
                    .padding(16.dp)
            ) {
                Text(
                    "No entries yet. Add incidents to build your timeline.",
                    style = MaterialTheme.typography.bodyMedium,
                    color = SwMuted
                )
            }
        } else {
            LazyColumn(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(padding),
                contentPadding = androidx.compose.foundation.layout.PaddingValues(16.dp)
            ) {
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
                        TimelineItem(
                            entry = monthEntries[index],
                            isFirst = index == 0,
                            onClick = { onEntryClick(monthEntries[index].id) }
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun TimelineItem(entry: LogEntry, isFirst: Boolean, onClick: () -> Unit) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .height(IntrinsicSize.Max)
    ) {
        Box(
            modifier = Modifier
                .width(2.dp)
                .fillMaxHeight()
                .background(SwAccent.copy(alpha = 0.4f))
        )
        Column(
            modifier = Modifier
                .weight(1f)
                .clickable(onClick = onClick)
                .padding(vertical = 8.dp)
        ) {
            Text(
                "${entry.date.formatDate()} ${entry.date.formatTime()}",
                style = MaterialTheme.typography.labelMedium,
                color = SwMuted
            )
            Spacer(Modifier.height(4.dp))
            Text(
                entry.description,
                style = MaterialTheme.typography.bodyLarge,
                maxLines = 2,
                overflow = TextOverflow.Ellipsis
            )
            Spacer(Modifier.height(4.dp))
            Text(
                "${categoryLabel(entry.category)} • ${if (entry.captureTimeUnknown) "capture time unknown" else "timestamped"}",
                style = MaterialTheme.typography.labelSmall,
                color = SwMuted
            )
        }
    }
}
