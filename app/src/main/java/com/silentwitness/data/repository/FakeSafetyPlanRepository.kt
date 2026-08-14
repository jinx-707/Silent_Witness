package com.silentwitness.data.repository

import com.silentwitness.domain.models.EmergencyItem
import com.silentwitness.domain.models.SafetyPlan
import com.silentwitness.domain.repository.SafetyPlanRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow

class FakeSafetyPlanRepository : SafetyPlanRepository {
    private val _plan = MutableStateFlow<SafetyPlan?>(
        SafetyPlan(
            exitRoutes = listOf(
                "Front door to Maya's car (she knows to be ready)",
                "Back door through the yard to Oak Street"
            ),
            emergencyItems = listOf(
                EmergencyItem("ID and passport", true),
                EmergencyItem("Medications (2-week supply)", false),
                EmergencyItem("Phone charger", true),
                EmergencyItem("Emergency cash (\$300)", false),
                EmergencyItem("Children's documents", false),
                EmergencyItem("Change of clothes", true),
                EmergencyItem("Important documents (lease, bank)", false)
            ),
            safePeople = listOf("Maya Chen — 555-0142", "Aunt Rosa — 555-0298"),
            safePlaces = listOf("Maya's apartment (23 Elm St)", "Oak Street Public Library", "Community center on 5th Ave"),
            codeWord = "tulip"
        )
    )
    override fun getSafetyPlan(): Flow<SafetyPlan?> = _plan.asStateFlow()

    override suspend fun saveSafetyPlan(plan: SafetyPlan) {
        _plan.value = plan
    }
}
