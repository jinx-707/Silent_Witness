package com.silentwitness.presentation.screenshotimporter

import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.outlined.ArrowBack
import androidx.compose.material.icons.outlined.Image
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import coil.compose.AsyncImage
import com.silentwitness.presentation.theme.SwAccent
import com.silentwitness.presentation.theme.SwMuted
import com.silentwitness.presentation.theme.SwSurface

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ScreenshotImporterScreen(
    onSave: (String) -> Unit,
    onBack: () -> Unit,
    viewModel: ScreenshotImporterViewModel = hiltViewModel()
) {
    val uiState by viewModel.uiState.collectAsState()

    val pickImageLauncher = rememberLauncherForActivityResult(
        ActivityResultContracts.GetContent()
    ) { uri -> uri?.let { viewModel.setUri(it) } }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Import screenshot") },
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
            if (uiState.uri != null) {
                AsyncImage(
                    model = uiState.uri,
                    contentDescription = "Screenshot",
                    contentScale = ContentScale.Fit,
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(240.dp)
                )
                Spacer(Modifier.height(12.dp))

                OutlinedTextField(
                    value = uiState.sender,
                    onValueChange = viewModel::updateSender,
                    label = { Text("Sender (optional)") },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth()
                )
                Spacer(Modifier.height(12.dp))

                OutlinedTextField(
                    value = uiState.platform,
                    onValueChange = viewModel::updatePlatform,
                    label = { Text("Platform (e.g. WhatsApp, SMS)") },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth()
                )
                Spacer(Modifier.height(12.dp))

                OutlinedTextField(
                    value = uiState.notes,
                    onValueChange = viewModel::updateNotes,
                    label = { Text("Notes (optional)") },
                    modifier = Modifier.fillMaxWidth(),
                    minLines = 2
                )
                Spacer(Modifier.height(12.dp))

                Text(
                    "Screenshots are timestamped with capture time unknown.",
                    style = MaterialTheme.typography.bodySmall,
                    color = SwMuted
                )
                Spacer(Modifier.height(16.dp))

                Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                    OutlinedButton(
                        onClick = { pickImageLauncher.launch("image/*") },
                        modifier = Modifier.weight(1f)
                    ) { Text("Pick another") }
                    Button(
                        onClick = { viewModel.saveScreenshot(onSave) },
                        enabled = !uiState.saving,
                        modifier = Modifier.weight(1f)
                    ) { Text("Save entry") }
                }
            } else {
                Card(
                    colors = CardDefaults.cardColors(containerColor = SwSurface),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(24.dp),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        Icon(Icons.Outlined.Image, contentDescription = null, tint = SwAccent, modifier = Modifier.size(48.dp))
                        Spacer(Modifier.height(12.dp))
                        Text(
                            "Import a screenshot of threats, harassment, or financial abuse. The capture time is recorded as unknown.",
                            style = MaterialTheme.typography.bodyMedium,
                            color = SwMuted
                        )
                        Spacer(Modifier.height(16.dp))
                        Button(
                            onClick = { pickImageLauncher.launch("image/*") },
                            modifier = Modifier.fillMaxWidth()
                        ) { Text("Choose screenshot") }
                    }
                }
            }
        }
    }
}
