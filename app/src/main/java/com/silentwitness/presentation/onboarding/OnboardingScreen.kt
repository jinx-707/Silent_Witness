package com.silentwitness.presentation.onboarding

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Button
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import com.silentwitness.presentation.theme.SwAccent
import com.silentwitness.presentation.theme.SwMuted

@Composable
fun OnboardingScreen(
    onComplete: (String) -> Unit,
    viewModel: OnboardingViewModel = hiltViewModel()
) {
    val uiState by viewModel.uiState.collectAsState()

    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(24.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Spacer(Modifier.height(48.dp))

        when (uiState.currentStep) {
            0 -> WelcomeStep(onNext = { viewModel.goToStep(1) })
            1 -> SetPinStep(
                pin = uiState.pin,
                error = uiState.error,
                onPinChange = viewModel::updatePin,
                onNext = {
                    viewModel.goToStep(2)
                }
            )
            2 -> DisguiseStep(
                pin = uiState.pin,
                confirmPin = uiState.confirmPin,
                error = uiState.error,
                onPinChange = viewModel::updatePin,
                onConfirmChange = viewModel::updateConfirmPin,
                onComplete = { viewModel.completeOnboarding(onComplete) }
            )
        }
    }
}

@Composable
private fun WelcomeStep(onNext: () -> Unit) {
    Column(horizontalAlignment = Alignment.CenterHorizontally) {
        Text("Silent Witness", style = MaterialTheme.typography.headlineLarge)
        Spacer(Modifier.height(16.dp))
        Text(
            "A private journal for documenting abuse — disguised as a simple calculator.",
            style = MaterialTheme.typography.bodyLarge,
            color = SwMuted,
            modifier = Modifier.fillMaxWidth()
        )
        Spacer(Modifier.height(48.dp))
        Button(onClick = onNext, modifier = Modifier.fillMaxWidth()) {
            Text("Get started")
        }
    }
}

@Composable
private fun SetPinStep(
    pin: String,
    error: String?,
    onPinChange: (String) -> Unit,
    onNext: () -> Unit
) {
    Column(horizontalAlignment = Alignment.CenterHorizontally) {
        Text("Set your PIN", style = MaterialTheme.typography.headlineMedium)
        Spacer(Modifier.height(8.dp))
        Text(
            "You'll use this PIN to unlock the app through the calculator.",
            style = MaterialTheme.typography.bodyMedium,
            color = SwMuted
        )
        Spacer(Modifier.height(32.dp))
        OutlinedTextField(
            value = pin,
            onValueChange = onPinChange,
            label = { Text("PIN (4-6 digits)") },
            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.NumberPassword),
            visualTransformation = PasswordVisualTransformation(),
            singleLine = true,
            modifier = Modifier.fillMaxWidth()
        )
        error?.let {
            Spacer(Modifier.height(8.dp))
            Text(it, style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.error)
        }
        Spacer(Modifier.height(24.dp))
        Button(
            onClick = onNext,
            enabled = pin.length >= 4,
            modifier = Modifier.fillMaxWidth()
        ) { Text("Continue") }
    }
}

@Composable
private fun DisguiseStep(
    pin: String,
    confirmPin: String,
    error: String?,
    onPinChange: (String) -> Unit,
    onConfirmChange: (String) -> Unit,
    onComplete: () -> Unit
) {
    Column(horizontalAlignment = Alignment.CenterHorizontally) {
        Text("Confirm your PIN", style = MaterialTheme.typography.headlineMedium)
        Spacer(Modifier.height(8.dp))
        Text(
            "Enter the same PIN again to confirm.",
            style = MaterialTheme.typography.bodyMedium,
            color = SwMuted
        )
        Spacer(Modifier.height(24.dp))
        OutlinedTextField(
            value = pin,
            onValueChange = onPinChange,
            label = { Text("PIN") },
            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.NumberPassword),
            visualTransformation = PasswordVisualTransformation(),
            singleLine = true,
            modifier = Modifier.fillMaxWidth()
        )
        Spacer(Modifier.height(12.dp))
        OutlinedTextField(
            value = confirmPin,
            onValueChange = onConfirmChange,
            label = { Text("Confirm PIN") },
            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.NumberPassword),
            visualTransformation = PasswordVisualTransformation(),
            singleLine = true,
            modifier = Modifier.fillMaxWidth()
        )
        error?.let {
            Spacer(Modifier.height(8.dp))
            Text(it, style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.error)
        }
        Spacer(Modifier.height(24.dp))
        Button(
            onClick = onComplete,
            enabled = pin.length >= 4 && confirmPin.length >= 4,
            modifier = Modifier.fillMaxWidth()
        ) { Text("Finish") }
        Spacer(Modifier.height(8.dp))
        OutlinedButton(
            onClick = { /* this is the final step */ },
            enabled = false,
            modifier = Modifier.fillMaxWidth()
        ) { Text("Disguise: the app opens as a calculator") }
    }
}
