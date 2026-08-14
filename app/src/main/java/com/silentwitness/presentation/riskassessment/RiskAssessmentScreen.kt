package com.silentwitness.presentation.riskassessment

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
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.outlined.ArrowBack
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.RadioButton
import androidx.compose.material3.RadioButtonDefaults
import androidx.compose.material3.Scaffold
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
import com.silentwitness.presentation.theme.SwCard
import com.silentwitness.presentation.theme.SwMuted

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun RiskAssessmentScreen(
    onBack: () -> Unit,
    viewModel: RiskAssessmentViewModel = hiltViewModel()
) {
    val uiState by viewModel.uiState.collectAsState()

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Risk assessment") },
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
            LinearProgressIndicator(
                progress = { uiState.progress },
                modifier = Modifier.fillMaxWidth(),
                color = SwAccent
            )
            Spacer(Modifier.height(8.dp))
            Text(
                "${uiState.answers.size} of ${riskQuestions.size} answered",
                style = MaterialTheme.typography.labelMedium,
                color = SwMuted
            )
            Spacer(Modifier.height(16.dp))

            Text(
                "Based on the Danger Assessment screening tool. This is not a diagnosis — it helps identify risk factors.",
                style = MaterialTheme.typography.bodySmall,
                color = SwMuted
            )
            Spacer(Modifier.height(16.dp))

            riskQuestions.forEach { question ->
                Card(
                    colors = CardDefaults.cardColors(containerColor = SwCard),
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(vertical = 4.dp)
                ) {
                    Column(Modifier.padding(12.dp)) {
                        Text(question.text, style = MaterialTheme.typography.bodyLarge)
                        Spacer(Modifier.height(4.dp))
                        Row {
                            YesNoOption(
                                label = "Yes",
                                selected = uiState.answers[question.id] == true,
                                onClick = { viewModel.answer(question.id, true) }
                            )
                            YesNoOption(
                                label = "No",
                                selected = uiState.answers[question.id] == false,
                                onClick = { viewModel.answer(question.id, false) }
                            )
                        }
                    }
                }
            }

            Spacer(Modifier.height(20.dp))
            Card(
                colors = CardDefaults.cardColors(containerColor = SwCard),
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(Modifier.padding(16.dp)) {
                    Text("Result", style = MaterialTheme.typography.titleSmall)
                    Spacer(Modifier.height(8.dp))
                    Text(
                        "Risk level: ${uiState.band()} (${uiState.score} risk factors)",
                        style = MaterialTheme.typography.bodyLarge,
                        color = SwAccent
                    )
                    Spacer(Modifier.height(8.dp))
                    Text(
                        "If you feel unsafe, contact a trusted person or a helpline. Your plan and contacts are in the main app.",
                        style = MaterialTheme.typography.bodySmall,
                        color = SwMuted
                    )
                }
            }
            Spacer(Modifier.height(24.dp))
            Button(
                onClick = onBack,
                modifier = Modifier.fillMaxWidth(),
                colors = ButtonDefaults.buttonColors(containerColor = SwAccent)
            ) { Text("Done") }
            Spacer(Modifier.height(24.dp))
        }
    }
}

@Composable
private fun YesNoOption(label: String, selected: Boolean, onClick: () -> Unit) {
    Row(
        modifier = Modifier
            .clickable(onClick = onClick)
            .padding(vertical = 4.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        RadioButton(
            selected = selected,
            onClick = onClick,
            colors = RadioButtonDefaults.colors(selectedColor = SwAccent)
        )
        Spacer(Modifier.width(4.dp))
        Text(label, style = MaterialTheme.typography.bodyMedium)
        Spacer(Modifier.width(16.dp))
    }
}
