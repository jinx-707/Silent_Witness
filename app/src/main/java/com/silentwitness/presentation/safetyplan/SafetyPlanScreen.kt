package com.silentwitness.presentation.safetyplan

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.outlined.ArrowBack
import androidx.compose.material.icons.outlined.Close
import androidx.compose.material3.Button
import androidx.compose.material3.Checkbox
import androidx.compose.material3.CheckboxDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import com.silentwitness.domain.models.EmergencyItem
import com.silentwitness.domain.models.SafetyPlan
import com.silentwitness.presentation.theme.SwAccent
import com.silentwitness.presentation.theme.SwMuted
import com.silentwitness.presentation.theme.SwSurface

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SafetyPlanScreen(
    onBack: () -> Unit,
    viewModel: SafetyPlanViewModel = hiltViewModel()
) {
    val uiState by viewModel.uiState.collectAsState()
    val plan = uiState.plan

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Safety plan") },
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
                "Step ${uiState.currentStep + 1} of 4",
                style = MaterialTheme.typography.labelLarge,
                color = SwMuted
            )
            Spacer(Modifier.height(8.dp))

            val steps = listOf(
                "Exit routes",
                "Emergency items",
                "Safe people & places",
                "Code word"
            )
            Row(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                steps.forEachIndexed { index, _ ->
                    Box(
                        modifier = Modifier
                            .weight(1f)
                            .height(6.dp)
                            .background(
                                if (index <= uiState.currentStep) SwAccent else SwSurface,
                                RoundedCornerShape(3.dp)
                            )
                    )
                }
            }
            Spacer(Modifier.height(16.dp))

            when (uiState.currentStep) {
                0 -> ExitRoutesStep(plan = plan, onUpdate = viewModel::updatePlan)
                1 -> EmergencyItemsStep(plan = plan, onUpdate = viewModel::updatePlan)
                2 -> SafePeoplePlacesStep(plan = plan, onUpdate = viewModel::updatePlan)
                3 -> CodeWordStep(plan = plan, onUpdate = viewModel::updatePlan)
            }

            Spacer(Modifier.height(24.dp))
            Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                OutlinedButton(
                    onClick = { viewModel.goToStep(uiState.currentStep - 1) },
                    enabled = uiState.currentStep > 0,
                    modifier = Modifier.weight(1f)
                ) { Text("Back") }
                if (uiState.currentStep < 3) {
                    Button(
                        onClick = { viewModel.goToStep(uiState.currentStep + 1) },
                        modifier = Modifier.weight(1f)
                    ) { Text("Next") }
                } else {
                    Button(
                        onClick = { viewModel.savePlan(onBack) },
                        modifier = Modifier.weight(1f)
                    ) { Text("Save plan") }
                }
            }
        }
    }
}

@Composable
private fun ExitRoutesStep(plan: SafetyPlan, onUpdate: (SafetyPlan) -> Unit) {
    Text("Ways out of your home (doors, windows, balcony).", style = MaterialTheme.typography.bodyMedium)
    Spacer(Modifier.height(12.dp))
    plan.exitRoutes.forEachIndexed { index, route ->
        EditableTextRow(
            value = route,
            placeholder = "e.g. back door through kitchen",
            onChange = { value ->
                onUpdate(plan.copy(exitRoutes = plan.exitRoutes.replaceAt(index, value)))
            },
            onRemove = {
                onUpdate(plan.copy(exitRoutes = plan.exitRoutes.removeAt(index)))
            }
        )
        Spacer(Modifier.height(8.dp))
    }
    TextButton(onClick = {
        onUpdate(plan.copy(exitRoutes = plan.exitRoutes + ""))
    }) { Text("+ Add route") }
}

@Composable
private fun EmergencyItemsStep(plan: SafetyPlan, onUpdate: (SafetyPlan) -> Unit) {
    Text("Items to pack when leaving.", style = MaterialTheme.typography.bodyMedium)
    Spacer(Modifier.height(12.dp))
    plan.emergencyItems.forEachIndexed { index, item ->
        Row(
            verticalAlignment = Alignment.CenterVertically,
            modifier = Modifier.fillMaxWidth()
        ) {
            Checkbox(
                checked = item.packed,
                onCheckedChange = { checked ->
                    onUpdate(
                        plan.copy(
                            emergencyItems = plan.emergencyItems.mapIndexed { i, e ->
                                if (i == index) e.copy(packed = checked) else e
                            }
                        )
                    )
                },
                colors = CheckboxDefaults.colors(checkedColor = SwAccent)
            )
            OutlinedTextField(
                value = item.item,
                onValueChange = { value ->
                    onUpdate(
                        plan.copy(
                            emergencyItems = plan.emergencyItems.mapIndexed { i, e ->
                                if (i == index) e.copy(item = value) else e
                            }
                        )
                    )
                },
                placeholder = { Text("e.g. phone charger") },
                modifier = Modifier.weight(1f)
            )
            IconButton(onClick = {
                onUpdate(plan.copy(emergencyItems = plan.emergencyItems.removeAt(index)))
            }) {
                Icon(Icons.Outlined.Close, contentDescription = "Remove")
            }
        }
    }
    TextButton(onClick = {
        onUpdate(plan.copy(emergencyItems = plan.emergencyItems + EmergencyItem("", false)))
    }) { Text("+ Add item") }
}

@Composable
private fun SafePeoplePlacesStep(plan: SafetyPlan, onUpdate: (SafetyPlan) -> Unit) {
    Text("People to call", style = MaterialTheme.typography.titleSmall)
    plan.safePeople.forEachIndexed { index, person ->
        EditableTextRow(
            value = person,
            placeholder = "Name",
            onChange = { value -> onUpdate(plan.copy(safePeople = plan.safePeople.replaceAt(index, value))) },
            onRemove = { onUpdate(plan.copy(safePeople = plan.safePeople.removeAt(index))) }
        )
        Spacer(Modifier.height(8.dp))
    }
    TextButton(onClick = { onUpdate(plan.copy(safePeople = plan.safePeople + "")) }) { Text("+ Add person") }

    Spacer(Modifier.height(16.dp))
    Text("Safe places to go", style = MaterialTheme.typography.titleSmall)
    plan.safePlaces.forEachIndexed { index, place ->
        EditableTextRow(
            value = place,
            placeholder = "e.g. neighbor's house",
            onChange = { value -> onUpdate(plan.copy(safePlaces = plan.safePlaces.replaceAt(index, value))) },
            onRemove = { onUpdate(plan.copy(safePlaces = plan.safePlaces.removeAt(index))) }
        )
        Spacer(Modifier.height(8.dp))
    }
    TextButton(onClick = { onUpdate(plan.copy(safePlaces = plan.safePlaces + "")) }) { Text("+ Add place") }
}

@Composable
private fun CodeWordStep(plan: SafetyPlan, onUpdate: (SafetyPlan) -> Unit) {
    Text(
        "A word only you and a trusted person understand, signalling you need help.",
        style = MaterialTheme.typography.bodyMedium
    )
    Spacer(Modifier.height(12.dp))
    OutlinedTextField(
        value = plan.codeWord,
        onValueChange = { value -> onUpdate(plan.copy(codeWord = value)) },
        label = { Text("Code word") },
        singleLine = true,
        modifier = Modifier.fillMaxWidth()
    )
}

@Composable
private fun EditableTextRow(
    value: String,
    placeholder: String,
    onChange: (String) -> Unit,
    onRemove: () -> Unit
) {
    Row(verticalAlignment = Alignment.CenterVertically) {
        OutlinedTextField(
            value = value,
            onValueChange = onChange,
            placeholder = { Text(placeholder) },
            singleLine = true,
            modifier = Modifier.weight(1f)
        )
        IconButton(onClick = onRemove) {
            Icon(Icons.Outlined.Close, contentDescription = "Remove")
        }
    }
}

private fun <T> List<T>.replaceAt(index: Int, value: T): List<T> =
    mapIndexed { i, item -> if (i == index) value else item }

private fun <T> List<T>.removeAt(index: Int): List<T> =
    filterIndexed { i, _ -> i != index }
