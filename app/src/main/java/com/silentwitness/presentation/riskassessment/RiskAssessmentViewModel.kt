package com.silentwitness.presentation.riskassessment

import androidx.lifecycle.ViewModel
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import javax.inject.Inject

data class RiskQuestion(
    val id: String,
    val text: String
)

data class RiskUiState(
    val answers: MutableMap<String, Boolean> = mutableMapOf()
) {
    val score: Int get() = answers.values.count { it }
    val progress: Float get() = (answers.size.toFloat() / riskQuestions.size).coerceIn(0f, 1f)

    fun band(): String = when {
        score in 0..3 -> "Variable"
        score in 4..7 -> "Increased"
        score in 8..11 -> "Severe"
        else -> "Extreme"
    }
}

val riskQuestions = listOf(
    RiskQuestion("q1", "Has the physical abuse increased in frequency or severity over the past year?"),
    RiskQuestion("q2", "Has your partner ever used a weapon against you or threatened you with one?"),
    RiskQuestion("q3", "Does your partner threaten to kill you or someone you love?"),
    RiskQuestion("q4", "Does your partner try to choke/strangle you or stop you from breathing?"),
    RiskQuestion("q5", "Is your partner violently and constantly jealous of you?"),
    RiskQuestion("q6", "Has your partner ever beaten you while you were pregnant or after childbirth?"),
    RiskQuestion("q7", "Has your partner ever forced you to have sex?"),
    RiskQuestion("q8", "Does your partner try to control your daily activities (money, work, friends)?"),
    RiskQuestion("q9", "Has your partner ever threatened or harmed your pets or belongings?"),
    RiskQuestion("q10", "Has your partner ever threatened or attempted suicide?"),
    RiskQuestion("q11", "Does your partner have access to a gun or other firearm?"),
    RiskQuestion("q12", "Have you tried to leave in the past and been stopped or threatened?"),
    RiskQuestion("q13", "Does your partner follow you, track you, or check up on you constantly?"),
    RiskQuestion("q14", "Has your partner abused you in front of children?"),
    RiskQuestion("q15", "Do you believe your partner is capable of killing you?")
)

@HiltViewModel
class RiskAssessmentViewModel @Inject constructor() : ViewModel() {

    private val _uiState = MutableStateFlow(RiskUiState())
    val uiState: StateFlow<RiskUiState> = _uiState.asStateFlow()

    fun answer(questionId: String, value: Boolean) {
        _uiState.update { it.copy(answers = it.answers.also { m -> m[questionId] = value }) }
    }
}
