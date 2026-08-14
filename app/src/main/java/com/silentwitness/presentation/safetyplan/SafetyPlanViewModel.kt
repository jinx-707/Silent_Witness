package com.silentwitness.presentation.safetyplan

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.silentwitness.domain.models.EmergencyItem
import com.silentwitness.domain.models.SafetyPlan
import com.silentwitness.domain.repository.SafetyPlanRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import javax.inject.Inject

data class SafetyPlanUiState(
    val plan: SafetyPlan = SafetyPlan(),
    val currentStep: Int = 0,
    val loaded: Boolean = false
)

@HiltViewModel
class SafetyPlanViewModel @Inject constructor(
    private val repository: SafetyPlanRepository
) : ViewModel() {

    private val _uiState = MutableStateFlow(SafetyPlanUiState())
    val uiState: StateFlow<SafetyPlanUiState> = _uiState.asStateFlow()

    init {
        viewModelScope.launch {
            repository.getSafetyPlan().collect { plan ->
                _uiState.update { it.copy(plan = plan ?: SafetyPlan(), loaded = true) }
            }
        }
    }

    fun goToStep(step: Int) {
        _uiState.update { it.copy(currentStep = step.coerceIn(0, 3)) }
    }

    fun updatePlan(plan: SafetyPlan) {
        _uiState.update { it.copy(plan = plan) }
    }

    fun savePlan(onSaved: () -> Unit) {
        viewModelScope.launch {
            val plan = _uiState.value.plan
            val cleaned = SafetyPlan(
                exitRoutes = plan.exitRoutes.filter { it.isNotBlank() }.map { it.trim() },
                emergencyItems = plan.emergencyItems.filter { it.item.isNotBlank() }.map { it.copy(item = it.item.trim()) },
                safePeople = plan.safePeople.filter { it.isNotBlank() }.map { it.trim() },
                safePlaces = plan.safePlaces.filter { it.isNotBlank() }.map { it.trim() },
                codeWord = plan.codeWord.trim()
            )
            repository.saveSafetyPlan(cleaned)
            onSaved()
        }
    }
}

private fun <T> List<T>.replaceAt(index: Int, value: T): List<T> =
    mapIndexed { i, item -> if (i == index) value else item }

private fun <T> List<T>.removeAt(index: Int): List<T> =
    filterIndexed { i, _ -> i != index }
