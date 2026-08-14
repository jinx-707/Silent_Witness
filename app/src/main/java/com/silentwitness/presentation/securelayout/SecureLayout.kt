package com.silentwitness.presentation.securelayout

import android.Manifest
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.Lock
import androidx.compose.material.icons.outlined.Settings
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.rememberNavController
import com.google.accompanist.permissions.ExperimentalPermissionsApi
import com.google.accompanist.permissions.isGranted
import com.google.accompanist.permissions.rememberPermissionState
import com.silentwitness.presentation.navigation.SecureGraph
import com.silentwitness.presentation.navigation.Settings
import com.silentwitness.presentation.navigation.secureGraph
import com.silentwitness.presentation.session.SessionViewModel
import com.silentwitness.presentation.theme.SwSurface
import com.silentwitness.presentation.theme.SwText
import com.silentwitness.presentation.theme.SwMuted

@OptIn(ExperimentalMaterial3Api::class, ExperimentalPermissionsApi::class)
@Composable
fun SecureLayout(
    outerNavController: NavHostController,
    sessionViewModel: SessionViewModel,
    onLock: () -> Unit
) {
    val innerNavController = rememberNavController()

    val smsPermissionState = rememberPermissionState(Manifest.permission.SEND_SMS)
    val locationPermissionState = rememberPermissionState(Manifest.permission.ACCESS_FINE_LOCATION)
    val microphonePermissionState = rememberPermissionState(Manifest.permission.RECORD_AUDIO)

    LaunchedEffect(Unit) {
        if (!smsPermissionState.status.isGranted) {
            smsPermissionState.launchPermissionRequest()
        }
        if (!locationPermissionState.status.isGranted) {
            locationPermissionState.launchPermissionRequest()
        }
        if (!microphonePermissionState.status.isGranted) {
            microphonePermissionState.launchPermissionRequest()
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Silent Witness") },
                actions = {
                    IconButton(onClick = { innerNavController.navigate(Settings) }) {
                        Icon(Icons.Outlined.Settings, contentDescription = "Settings")
                    }
                    IconButton(onClick = onLock) {
                        Icon(Icons.Outlined.Lock, contentDescription = "Lock")
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = SwSurface,
                    titleContentColor = SwText,
                    actionIconContentColor = SwMuted
                )
            )
        }
    ) { paddingValues ->
        NavHost(
            navController = innerNavController,
            startDestination = SecureGraph,
            modifier = Modifier
                .padding(paddingValues)
        ) {
            secureGraph(
                navController = innerNavController,
                sessionViewModel = sessionViewModel,
                onLock = onLock
            )
        }
    }
}
