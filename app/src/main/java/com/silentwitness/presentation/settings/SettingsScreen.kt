package com.silentwitness.presentation.settings

import android.Manifest
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
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
import androidx.compose.material.icons.outlined.ChevronRight
import androidx.compose.material.icons.outlined.Fingerprint
import androidx.compose.material.icons.outlined.HealthAndSafety
import androidx.compose.material.icons.outlined.Lock
import androidx.compose.material.icons.outlined.Notifications
import androidx.compose.material.icons.outlined.Sos
import androidx.compose.material.icons.outlined.Star
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Switch
import androidx.compose.material3.SwitchDefaults
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import com.google.accompanist.permissions.ExperimentalPermissionsApi
import com.google.accompanist.permissions.isGranted
import com.google.accompanist.permissions.rememberPermissionState
import com.silentwitness.presentation.session.SessionViewModel
import com.silentwitness.presentation.sos.SosViewModel
import com.silentwitness.presentation.theme.SwAccent
import com.silentwitness.presentation.theme.SwMuted
import com.silentwitness.presentation.theme.SwSurface

@OptIn(ExperimentalMaterial3Api::class, ExperimentalPermissionsApi::class)
@Composable
fun SettingsScreen(
    sessionViewModel: SessionViewModel,
    onNavigateToRiskAssessment: () -> Unit,
    onNavigateToCheckIn: () -> Unit,
    onNavigateToPaywall: () -> Unit,
    onLock: () -> Unit,
    onBack: () -> Unit,
    viewModel: SettingsViewModel = hiltViewModel(),
    sosViewModel: SosViewModel = hiltViewModel()
) {
    PermissionManager()
    val smsPermissionState = rememberPermissionState(Manifest.permission.SEND_SMS)
    val microphonePermissionState = rememberPermissionState(Manifest.permission.RECORD_AUDIO)
    val shakeEnabled by viewModel.shakeEnabled.collectAsState()
    val sosShakeEnabled by sosViewModel.shakeEnabled.collectAsState()
    val sosVoiceEnabled by sosViewModel.voiceEnabled.collectAsState()
    var showChangePin by remember { mutableStateOf(false) }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Settings") },
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
            Text("Security", style = MaterialTheme.typography.titleSmall, color = SwMuted)
            SettingsRow(
                title = "Change PIN",
                subtitle = "Update your app PIN",
                icon = Icons.Outlined.Lock,
                onClick = { showChangePin = true }
            )
            SettingsRow(
                title = "Lock now",
                subtitle = "Return to the disguise screen",
                icon = Icons.Outlined.Fingerprint,
                onClick = onLock
            )
            SettingsRow(
                title = "Shake to disguise",
                subtitle = "Quickly hide the app",
                icon = Icons.Outlined.Star,
                onClick = { viewModel.toggleShake() },
                trailing = {
                    Switch(
                        checked = shakeEnabled,
                        onCheckedChange = { viewModel.toggleShake() },
                        colors = SwitchDefaults.colors(checkedThumbColor = SwAccent)
                    )
                }
            )

            Spacer(Modifier.height(24.dp))
            Text("Safety tools", style = MaterialTheme.typography.titleSmall, color = SwMuted)
            SettingsRow(
                title = "Risk assessment",
                subtitle = "Danger Assessment screening",
                icon = Icons.Outlined.HealthAndSafety,
                onClick = onNavigateToRiskAssessment
            )
            SettingsRow(
                title = "Check-in reminders",
                subtitle = "Discreet check-ins",
                icon = Icons.Outlined.Notifications,
                onClick = onNavigateToCheckIn
            )
            SettingsRow(
                title = "SOS shake detection",
                subtitle = "Shake the phone to send an SOS",
                icon = Icons.Outlined.Sos,
                onClick = {
                    if (!smsPermissionState.status.isGranted) {
                        smsPermissionState.launchPermissionRequest()
                    }
                    sosViewModel.toggleShake(!sosShakeEnabled)
                },
                trailing = {
                    Switch(
                        checked = sosShakeEnabled,
                        onCheckedChange = { enabled ->
                            if (enabled && !smsPermissionState.status.isGranted) {
                                smsPermissionState.launchPermissionRequest()
                            }
                            sosViewModel.toggleShake(enabled)
                        },
                        colors = SwitchDefaults.colors(checkedThumbColor = SwAccent)
                    )
                }
            )
            SettingsRow(
                title = "Voice SOS detection",
                subtitle = "Listen for \"help\" or \"SOS\" (uses microphone)",
                icon = Icons.Outlined.Sos,
                onClick = {
                    if (!microphonePermissionState.status.isGranted) {
                        microphonePermissionState.launchPermissionRequest()
                    }
                    sosViewModel.toggleVoice(!sosVoiceEnabled)
                },
                trailing = {
                    Switch(
                        checked = sosVoiceEnabled,
                        onCheckedChange = { enabled ->
                            if (enabled && !microphonePermissionState.status.isGranted) {
                                microphonePermissionState.launchPermissionRequest()
                            }
                            sosViewModel.toggleVoice(enabled)
                        },
                        colors = SwitchDefaults.colors(checkedThumbColor = SwAccent)
                    )
                }
            )
            SettingsRow(
                title = "Trigger SOS now",
                subtitle = "Send alert + evidence to tier-1 contacts",
                icon = Icons.Outlined.Sos,
                onClick = {
                    if (!smsPermissionState.status.isGranted) {
                        smsPermissionState.launchPermissionRequest()
                    }
                    sosViewModel.triggerSos()
                }
            )

            Spacer(Modifier.height(24.dp))
            Text("About", style = MaterialTheme.typography.titleSmall, color = SwMuted)
            SettingsRow(
                title = "Silent Witness Premium",
                subtitle = "Review plans",
                icon = Icons.Outlined.Star,
                onClick = onNavigateToPaywall
            )
            Spacer(Modifier.height(24.dp))
        }
    }

    if (showChangePin) {
        ChangePinDialog(
            sessionViewModel = sessionViewModel,
            onDismiss = { showChangePin = false }
        )
    }
}

@Composable
private fun SettingsRow(
    title: String,
    subtitle: String,
    icon: ImageVector,
    onClick: () -> Unit,
    trailing: (@Composable () -> Unit)? = null
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 4.dp)
            .background(SwSurface, RoundedCornerShape(12.dp))
            .clickable(onClick = onClick)
            .padding(horizontal = 12.dp, vertical = 12.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Icon(icon, contentDescription = null, tint = SwAccent, modifier = Modifier.width(28.dp))
        Spacer(Modifier.width(12.dp))
        Column(modifier = Modifier.weight(1f)) {
            Text(title, style = MaterialTheme.typography.bodyLarge)
            Text(subtitle, style = MaterialTheme.typography.bodySmall, color = SwMuted)
        }
        trailing?.invoke()
        if (trailing == null) {
            Icon(Icons.Outlined.ChevronRight, contentDescription = null, tint = SwMuted)
        }
    }
}

@OptIn(ExperimentalPermissionsApi::class)
@Composable
private fun PermissionManager() {
    val smsPermissionState = rememberPermissionState(Manifest.permission.SEND_SMS)
    val locationPermissionState = rememberPermissionState(Manifest.permission.ACCESS_FINE_LOCATION)

    LaunchedEffect(Unit) {
        if (!smsPermissionState.status.isGranted) {
            smsPermissionState.launchPermissionRequest()
        }
        if (!locationPermissionState.status.isGranted) {
            locationPermissionState.launchPermissionRequest()
        }
    }
}

@Composable
private fun ChangePinDialog(sessionViewModel: SessionViewModel, onDismiss: () -> Unit) {
    var oldPin by remember { mutableStateOf("") }
    var newPin by remember { mutableStateOf("") }
    var confirmPin by remember { mutableStateOf("") }
    var error by remember { mutableStateOf<String?>(null) }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Change PIN") },
        text = {
            Column {
                OutlinedTextField(
                    value = oldPin,
                    onValueChange = { oldPin = it },
                    label = { Text("Current PIN") },
                    singleLine = true
                )
                Spacer(Modifier.height(8.dp))
                OutlinedTextField(
                    value = newPin,
                    onValueChange = { newPin = it },
                    label = { Text("New PIN") },
                    singleLine = true
                )
                Spacer(Modifier.height(8.dp))
                OutlinedTextField(
                    value = confirmPin,
                    onValueChange = { confirmPin = it },
                    label = { Text("Confirm new PIN") },
                    singleLine = true
                )
                error?.let {
                    Spacer(Modifier.height(8.dp))
                    Text(it, style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.error)
                }
            }
        },
        confirmButton = {
            Button(
                onClick = {
                    when {
                        newPin.length < 4 -> error = "PIN must be at least 4 digits."
                        newPin != confirmPin -> error = "PINs do not match."
                        !sessionViewModel.changePin(oldPin, newPin) -> error = "Current PIN is incorrect."
                        else -> onDismiss()
                    }
                }
            ) { Text("Change") }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) { Text("Cancel") }
        }
    )
}
