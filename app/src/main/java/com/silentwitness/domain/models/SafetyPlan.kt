package com.silentwitness.domain.models

data class EmergencyItem(
    val item: String,
    val packed: Boolean
)

data class SafetyPlan(
    val exitRoutes: List<String> = emptyList(),
    val emergencyItems: List<EmergencyItem> = emptyList(),
    val safePeople: List<String> = emptyList(),
    val safePlaces: List<String> = emptyList(),
    val codeWord: String = ""
)
