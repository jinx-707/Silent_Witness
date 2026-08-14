package com.silentwitness.domain.repository

import com.silentwitness.domain.models.SafetyPlan
import kotlinx.coroutines.flow.Flow

interface SafetyPlanRepository {
    fun getSafetyPlan(): Flow<SafetyPlan?>
    suspend fun saveSafetyPlan(plan: SafetyPlan)
}
