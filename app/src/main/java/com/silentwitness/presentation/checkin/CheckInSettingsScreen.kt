package com.silentwitness.presentation.checkin

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.outlined.ArrowBack
import androidx.compose.material3.Button
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.RadioButton
import androidx.compose.material3.RadioButtonDefaults
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Switch
import androidx.compose.material3.SwitchDefaults
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import com.silentwitness.presentation.theme.SwAccent
import com.silentwitness.presentation.theme.SwMuted
import com.silentwitness.presentation.theme.SwSurface

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CheckInSettingsScreen(
    onBack: () -> Unit,
    viewModel: CheckInSettingsViewModel = hiltViewModel()
) {
    val uiState by viewModel.uiState.collectAsState()

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Check-in reminders") },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(Icons.AutoMirrored.Outlined.ArrowBack, contentDescription = "Back")
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
            Text(
                "Silent reminders prompt you to check in so your contacts know you're safe — without drawing attention.",
                style = MaterialTheme.typography.bodyMedium,
                color = SwMuted
            )
            Spacer(Modifier.height(16.dp))

            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(SwSurface, RoundedCornerShape(12.dp))
                    .clickable { viewModel.toggleEnabled() }
                    .padding(16.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column(modifier = Modifier.weight(1f)) {
                    Text("Enable check-in reminders", style = MaterialTheme.typography.bodyLarge)
                    Text(
                        "Discreet notifications on your schedule",
                        style = MaterialTheme.typography.bodySmall,
                        color = SwMuted
                    )
                }
                Switch(
                    checked = uiState.enabled,
                    onCheckedChange = { viewModel.toggleEnabled() },
                    colors = SwitchDefaults.colors(
                        checkedThumbColor = SwAccent,
                        checkedTrackColor = SwAccent.copy(alpha = 0.4f)
                    )
                )
            }
            Spacer(Modifier.height(20.dp))

            Text("Check-in interval", style = MaterialTheme.typography.titleSmall)
            Spacer(Modifier.height(8.dp))
            listOf(2, 4, 8).forEach { hours ->
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clickable { viewModel.updateInterval(hours) }
                        .padding(vertical = 6.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    RadioButton(
                        selected = uiState.intervalHours == hours,
                        onClick = { viewModel.updateInterval(hours) },
                        colors = RadioButtonDefaults.colors(selectedColor = SwAccent)
                    )
                    Spacer(Modifier.width(8.dp))
                    Text("Every $hours hours", style = MaterialTheme.typography.bodyLarge)
                }
            }

            Spacer(Modifier.height(20.dp))
            Text("Notify contacts from tier", style = MaterialTheme.typography.titleSmall)
            Spacer(Modifier.height(8.dp))
            listOf(1, 2, 3).forEach { tier ->
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clickable { viewModel.updateNotifyTier(tier) }
                        .padding(vertical = 6.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    RadioButton(
                        selected = uiState.notifyTier == tier,
                        onClick = { viewModel.updateNotifyTier(tier) },
                        colors = RadioButtonDefaults.colors(selectedColor = SwAccent)
                    )
                    Spacer(Modifier.width(8.dp))
                    Text("Tier $tier and above", style = MaterialTheme.typography.bodyLarge)
                }
            }

            Spacer(Modifier.height(20.dp))
            Text("If a check-in is missed", style = MaterialTheme.typography.titleSmall)
            Spacer(Modifier.height(8.dp))
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(SwSurface, RoundedCornerShape(12.dp))
                    .clickable { viewModel.updateReleaseOnMissed(!uiState.releaseOnMissed) }
                    .padding(16.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column(modifier = Modifier.weight(1f)) {
                    Text("Auto-SOS after a missed check-in", style = MaterialTheme.typography.bodyLarge)
                    Text(
                        "Alerts your contacts if you stop checking in",
                        style = MaterialTheme.typography.bodySmall,
                        color = SwMuted
                    )
                }
                Switch(
                    checked = uiState.releaseOnMissed,
                    onCheckedChange = { viewModel.updateReleaseOnMissed(it) },
                    colors = SwitchDefaults.colors(
                        checkedThumbColor = SwAccent,
                        checkedTrackColor = SwAccent.copy(alpha = 0.4f)
                    )
                )
            }

            Spacer(Modifier.height(16.dp))
            Text("Grace period before SOS", style = MaterialTheme.typography.titleSmall)
            Spacer(Modifier.height(8.dp))
            listOf(5, 15, 30).forEach { minutes ->
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clickable { viewModel.updateGracePeriod(minutes) }
                        .padding(vertical = 6.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    RadioButton(
                        selected = uiState.gracePeriodMinutes == minutes,
                        onClick = { viewModel.updateGracePeriod(minutes) },
                        colors = RadioButtonDefaults.colors(selectedColor = SwAccent)
                    )
                    Spacer(Modifier.width(8.dp))
                    Text("$minutes minutes", style = MaterialTheme.typography.bodyLarge)
                }
            }

            Spacer(Modifier.height(24.dp))
            Button(
                onClick = { viewModel.saveSettings() },
                modifier = Modifier.fillMaxWidth()
            ) { Text("Save settings") }
            Spacer(Modifier.height(16.dp))
        }
    }
}
