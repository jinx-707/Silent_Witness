package com.silentwitness.presentation.paywall

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.outlined.ArrowBack
import androidx.compose.material.icons.outlined.Check
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import com.silentwitness.presentation.theme.SwAccent
import com.silentwitness.presentation.theme.SwCard
import com.silentwitness.presentation.theme.SwMuted

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun PaywallScreen(
    onBack: () -> Unit,
    viewModel: PaywallViewModel = hiltViewModel()
) {
    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Silent Witness Premium") },
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
                "Support development while keeping every feature private.",
                style = MaterialTheme.typography.bodyMedium,
                color = SwMuted
            )
            Spacer(Modifier.height(16.dp))

            PlanCard(
                title = "Free",
                price = "$0",
                description = "Everything you need to document safely.",
                features = listOf(
                    "Unlimited journal entries",
                    "Photo & audio evidence",
                    "Trusted contacts",
                    "Certificate generation"
                ),
                isHighlighted = false,
                action = { Text("Current plan") }
            )

            Spacer(Modifier.height(16.dp))

            PlanCard(
                title = "Premium",
                price = "$4.99/mo",
                description = "Extra peace of mind.",
                features = listOf(
                    "Proactive check-in reminders",
                    "Risk assessment insights",
                    "Priority support",
                    "Early access to new tools"
                ),
                isHighlighted = true,
                action = {
                    TextButton(onClick = { /* RevenueCat stub */ }) {
                        Text("Upgrade", color = SwAccent)
                    }
                }
            )

            Spacer(Modifier.height(24.dp))
            Text(
                "In-app purchases are not yet available. Premium plans will be added in a future update.",
                style = MaterialTheme.typography.bodySmall,
                color = SwMuted
            )
            Spacer(Modifier.height(24.dp))
        }
    }
}

@Composable
private fun PlanCard(
    title: String,
    price: String,
    description: String,
    features: List<String>,
    isHighlighted: Boolean,
    action: @Composable () -> Unit
) {
    Card(
        colors = CardDefaults.cardColors(
            containerColor = if (isHighlighted) SwAccent.copy(alpha = 0.15f) else SwCard
        )
    ) {
        Column(Modifier.padding(16.dp)) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Column(modifier = Modifier.weight(1f)) {
                    Text(title, style = MaterialTheme.typography.titleLarge)
                    Text(price, style = MaterialTheme.typography.titleMedium, color = if (isHighlighted) SwAccent else SwMuted)
                }
                action()
            }
            Spacer(Modifier.height(8.dp))
            Text(description, style = MaterialTheme.typography.bodyMedium, color = SwMuted)
            Spacer(Modifier.height(12.dp))
            features.forEach { feature ->
                Row(
                    modifier = Modifier.padding(vertical = 2.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Icon(
                        Icons.Outlined.Check,
                        contentDescription = null,
                        tint = if (isHighlighted) SwAccent else MaterialTheme.colorScheme.primary,
                        modifier = Modifier.width(20.dp)
                    )
                    Spacer(Modifier.width(6.dp))
                    Text(feature, style = MaterialTheme.typography.bodyMedium)
                }
            }
        }
    }
}
