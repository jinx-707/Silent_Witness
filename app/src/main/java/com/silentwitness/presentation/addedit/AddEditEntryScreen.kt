package com.silentwitness.presentation.addedit

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
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.outlined.ArrowBack
import androidx.compose.material.icons.outlined.AudioFile
import androidx.compose.material.icons.outlined.PhotoCamera
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FilterChip
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import coil.compose.AsyncImage
import com.silentwitness.presentation.theme.SwAccent
import com.silentwitness.presentation.theme.SwBg
import com.silentwitness.presentation.theme.SwMuted
import com.silentwitness.presentation.theme.SwSurface
import com.silentwitness.presentation.theme.SwText
import com.silentwitness.utils.categoryLabel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AddEditEntryScreen(
    entryId: String? = null,
    onSave: (String) -> Unit,
    onBack: () -> Unit,
    onNavigateToPhotoCapture: () -> Unit,
    onNavigateToAudioRecording: () -> Unit,
    viewModel: AddEditEntryViewModel = hiltViewModel(),
    draftViewModel: DraftEntryViewModel = hiltViewModel()
) {
    val uiState by draftViewModel.state.collectAsState()
    val categories = listOf("physical", "verbal", "financial", "digital", "other")

    // Load existing entry if editing
    LaunchedEffect(entryId) {
        if (entryId != null) {
            val entry = viewModel.loadEntry(entryId)
            if (entry != null) {
                draftViewModel.loadFromEntry(entry)
            }
        } else {
            draftViewModel.reset()
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text(if (entryId != null) "Edit entry" else "Add entry", color = SwText) },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(Icons.AutoMirrored.Outlined.ArrowBack, contentDescription = "Back", tint = SwMuted)
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(containerColor = SwSurface)
            )
        },
        containerColor = SwBg
    ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .verticalScroll(rememberScrollState())
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            // Date/Time
            OutlinedTextField(
                value = uiState.dateTime,
                onValueChange = { draftViewModel.updateDateTime(it) },
                label = { Text("Date & time") },
                singleLine = true,
                modifier = Modifier.fillMaxWidth(),
                colors = OutlinedTextFieldDefaults.colors(
                    focusedBorderColor = SwAccent,
                    focusedLabelColor = SwAccent
                )
            )

            // Category chips
            Column {
                Text("Category", style = MaterialTheme.typography.labelMedium, color = SwMuted)
                Spacer(Modifier.height(8.dp))
                Row(
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    categories.forEach { cat ->
                        FilterChip(
                            selected = uiState.category == cat,
                            onClick = { draftViewModel.updateCategory(cat) },
                            label = { Text(categoryLabel(cat), style = MaterialTheme.typography.labelSmall) },
                            modifier = Modifier.weight(1f)
                        )
                    }
                }
            }

            // Description
            OutlinedTextField(
                value = uiState.description,
                onValueChange = { draftViewModel.updateDescription(it) },
                label = { Text("What happened?") },
                modifier = Modifier.fillMaxWidth(),
                minLines = 4,
                colors = OutlinedTextFieldDefaults.colors(
                    focusedBorderColor = SwAccent,
                    focusedLabelColor = SwAccent
                )
            )

            // Injury notes (only for physical)
            if (uiState.category == "physical") {
                OutlinedTextField(
                    value = uiState.injuryNotes,
                    onValueChange = { draftViewModel.updateInjuryNotes(it) },
                    label = { Text("Injury notes (optional)") },
                    modifier = Modifier.fillMaxWidth(),
                    minLines = 2,
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedBorderColor = SwAccent,
                        focusedLabelColor = SwAccent
                    )
                )
            }

            // Location
            OutlinedTextField(
                value = uiState.location,
                onValueChange = { draftViewModel.updateLocation(it) },
                label = { Text("Location (optional)") },
                singleLine = true,
                modifier = Modifier.fillMaxWidth(),
                colors = OutlinedTextFieldDefaults.colors(
                    focusedBorderColor = SwAccent,
                    focusedLabelColor = SwAccent
                )
            )

            // Attachments
            Column {
                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    OutlinedButton(
                        onClick = onNavigateToPhotoCapture,
                        modifier = Modifier.weight(1f),
                        colors = ButtonDefaults.outlinedButtonColors(contentColor = SwAccent)
                    ) {
                        Icon(Icons.Outlined.PhotoCamera, contentDescription = null, modifier = Modifier.size(18.dp))
                        Spacer(Modifier.size(6.dp))
                        Text("Photo" + if (uiState.photoAttached) " ✓" else "")
                    }
                    OutlinedButton(
                        onClick = onNavigateToAudioRecording,
                        modifier = Modifier.weight(1f),
                        colors = ButtonDefaults.outlinedButtonColors(contentColor = SwAccent)
                    ) {
                        Icon(Icons.Outlined.AudioFile, contentDescription = null, modifier = Modifier.size(18.dp))
                        Spacer(Modifier.size(6.dp))
                        Text("Audio" + if (uiState.audioAttached) " ✓" else "")
                    }
                }

                // Photo thumbnail preview
                if (uiState.photoUri != null) {
                    Spacer(Modifier.height(12.dp))
                    AsyncImage(
                        model = uiState.photoUri,
                        contentDescription = "Photo preview",
                        modifier = Modifier
                            .size(80.dp)
                            .clip(RoundedCornerShape(8.dp)),
                        contentScale = ContentScale.Crop
                    )
                }
            }

            Button(
                onClick = {
                    viewModel.saveEntry(entryId, uiState) { savedId ->
                        draftViewModel.reset()
                        onSave(savedId)
                    }
                },
                enabled = uiState.description.isNotBlank(),
                modifier = Modifier.fillMaxWidth(),
                colors = ButtonDefaults.buttonColors(containerColor = SwAccent)
            ) {
                Text(if (entryId != null) "Save changes" else "Save entry", color = SwText)
            }

            Text(
                "Entry saved locally on this device only.",
                style = MaterialTheme.typography.bodySmall,
                color = SwMuted
            )
        }
    }
}
