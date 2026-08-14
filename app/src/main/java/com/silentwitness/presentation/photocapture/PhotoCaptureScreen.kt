package com.silentwitness.presentation.photocapture

import android.Manifest
import android.net.Uri
import android.widget.Toast
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.ArrowBack
import androidx.compose.material.icons.outlined.CameraAlt
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import com.google.accompanist.permissions.ExperimentalPermissionsApi
import com.google.accompanist.permissions.isGranted
import com.google.accompanist.permissions.rememberPermissionState
import com.google.accompanist.permissions.shouldShowRationale
import com.silentwitness.presentation.addedit.DraftEntryViewModel
import com.silentwitness.presentation.theme.*

@OptIn(ExperimentalPermissionsApi::class, ExperimentalMaterial3Api::class)
@Composable
fun PhotoCaptureScreen(
    onSave: () -> Unit,
    onBack: () -> Unit,
    viewModel: PhotoCaptureViewModel = hiltViewModel(),
    draftViewModel: DraftEntryViewModel = hiltViewModel()
) {
    val context = LocalContext.current
    val uiState by viewModel.uiState.collectAsState()

    // Camera permission state
    val cameraPermissionState = rememberPermissionState(Manifest.permission.CAMERA)

    // Launcher for taking a photo
    val launcher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.TakePicture()
    ) { success ->
        if (success) {
            viewModel.setCapturedUri(uiState.tempUri)
        } else {
            Toast.makeText(context, "Photo capture failed", Toast.LENGTH_SHORT).show()
        }
    }

    // Function to trigger camera capture
    fun launchCamera() {
        if (cameraPermissionState.status.isGranted) {
            val uri = viewModel.createTempImageUri(context)
            launcher.launch(uri)
        } else {
            // Request permission
            cameraPermissionState.launchPermissionRequest()
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Add photo", color = SwText) },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(Icons.Outlined.ArrowBack, contentDescription = "Back", tint = SwMuted)
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
                .padding(16.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            // Show permission denied message if needed
            if (!cameraPermissionState.status.isGranted && cameraPermissionState.status.shouldShowRationale) {
                Surface(
                    shape = MaterialTheme.shapes.small,
                    color = SwCard,
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Column(modifier = Modifier.padding(16.dp)) {
                        Text(
                            "Camera permission is needed to take photos.",
                            style = MaterialTheme.typography.bodyMedium,
                            color = SwText
                        )
                        Spacer(modifier = Modifier.height(8.dp))
                        Button(
                            onClick = { cameraPermissionState.launchPermissionRequest() },
                            colors = ButtonDefaults.buttonColors(containerColor = SwAccent)
                        ) {
                            Text("Grant permission", color = SwText)
                        }
                    }
                }
            }

            if (uiState.capturedUri != null) {
                // Show captured image preview
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .aspectRatio(4f / 3f)
                        .background(SwCard),
                    contentAlignment = Alignment.Center
                ) {
                    // In a real implementation, you'd display the image using Coil or similar
                    Text("Image captured ✓", color = SwText)
                }
                Row(
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Button(
                        onClick = { viewModel.clearImage() },
                        modifier = Modifier.weight(1f),
                        colors = ButtonDefaults.buttonColors(containerColor = SwSurface)
                    ) {
                        Text("Retake", color = SwMuted)
                    }
                    Button(
                        onClick = {
                            uiState.capturedUri?.let { draftViewModel.setPhotoUri(it.toString()) }
                            onSave()
                        },
                        modifier = Modifier.weight(1f),
                        colors = ButtonDefaults.buttonColors(containerColor = SwAccent)
                    ) {
                        Text("Use photo", color = SwText)
                    }
                }
            } else {
                // Capture area
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .aspectRatio(4f / 3f)
                        .background(SwCard)
                        .clickable {
                            launchCamera()
                        },
                    contentAlignment = Alignment.Center
                ) {
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Icon(
                            Icons.Outlined.CameraAlt,
                            contentDescription = "Take photo",
                            tint = SwMuted,
                            modifier = Modifier.size(48.dp)
                        )
                        Text(
                            if (cameraPermissionState.status.isGranted) "Tap to capture" else "Permission required",
                            color = SwMuted
                        )
                    }
                }

                // Gallery import option
                TextButton(
                    onClick = {
                        // TODO: implement gallery import
                        Toast.makeText(context, "Gallery import coming soon", Toast.LENGTH_SHORT).show()
                    }
                ) {
                    Text("Import from library (capture time unknown)", color = SwMuted)
                }
                Text(
                    text = "Library imports flag the capture timestamp as unknown, since the original metadata may not reflect when the event occurred.",
                    style = MaterialTheme.typography.bodySmall,
                    color = SwMuted
                )
            }
        }
    }
}
