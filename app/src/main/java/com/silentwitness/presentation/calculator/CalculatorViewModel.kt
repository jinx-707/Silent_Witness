package com.silentwitness.presentation.calculator

import androidx.lifecycle.ViewModel
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import javax.inject.Inject

data class CalculatorUiState(
    val display: String = "0",
    val firstOperand: Double? = null,
    val operator: String? = null,
    val waitingForSecond: Boolean = false,
    val pinAttempt: String = ""
)

sealed class CalculatorEvent {
    data class Digit(val digit: String) : CalculatorEvent()
    object Decimal : CalculatorEvent()
    data class Operator(val op: String) : CalculatorEvent()
    object Equals : CalculatorEvent()
    object Clear : CalculatorEvent()
    object ToggleSign : CalculatorEvent()
    object Percent : CalculatorEvent()
}

@HiltViewModel
class CalculatorViewModel @Inject constructor() : ViewModel() {
    private val _uiState = MutableStateFlow(CalculatorUiState())
    val uiState: StateFlow<CalculatorUiState> = _uiState.asStateFlow()

    fun onEvent(event: CalculatorEvent) {
        when (event) {
            is CalculatorEvent.Digit -> handleDigit(event.digit)
            CalculatorEvent.Decimal -> handleDecimal()
            is CalculatorEvent.Operator -> handleOperator(event.op)
            CalculatorEvent.Equals -> handleEquals()
            CalculatorEvent.Clear -> handleClear()
            CalculatorEvent.ToggleSign -> handleToggleSign()
            CalculatorEvent.Percent -> handlePercent()
        }
    }

    // Returns and clears the digits typed since the last reset (the PIN attempt).
    fun consumePinAttempt(): String {
        val attempt = _uiState.value.pinAttempt
        _uiState.update { it.copy(pinAttempt = "") }
        return attempt
    }

    private fun handleDigit(digit: String) {
        val newAttempt = (_uiState.value.pinAttempt + digit).take(8)
        _uiState.update {
            if (it.waitingForSecond) {
                it.copy(display = digit, waitingForSecond = false, pinAttempt = newAttempt)
            } else {
                val newDisplay = if (it.display == "0") digit else it.display + digit
                it.copy(display = newDisplay.take(10), pinAttempt = newAttempt)
            }
        }
    }

    private fun handleDecimal() {
        _uiState.update {
            if (it.waitingForSecond) {
                it.copy(display = "0.", waitingForSecond = false)
            } else if (!it.display.contains(".")) {
                it.copy(display = it.display + ".")
            } else it
        }
    }

    private fun handleOperator(op: String) {
        _uiState.update {
            val current = it.display.toDoubleOrNull() ?: 0.0
            if (it.firstOperand != null && !it.waitingForSecond) {
                val result = compute(it.firstOperand!!, current, it.operator!!)
                it.copy(
                    display = formatResult(result),
                    firstOperand = result,
                    operator = op,
                    waitingForSecond = true
                )
            } else {
                it.copy(firstOperand = current, operator = op, waitingForSecond = true)
            }
        }
    }

    private fun handleEquals() {
        _uiState.update {
            val current = it.display.toDoubleOrNull() ?: 0.0
            if (it.firstOperand != null && it.operator != null) {
                val result = compute(it.firstOperand!!, current, it.operator!!)
                it.copy(display = formatResult(result), firstOperand = null, operator = null, waitingForSecond = false)
            } else {
                it.copy(firstOperand = null, operator = null, waitingForSecond = false)
            }
        }
    }

    private fun handleClear() {
        _uiState.value = CalculatorUiState()
    }

    private fun handleToggleSign() {
        _uiState.update {
            it.copy(display = if (it.display.startsWith("-")) it.display.drop(1) else "-${it.display}")
        }
    }

    private fun handlePercent() {
        _uiState.update {
            val value = it.display.toDoubleOrNull() ?: 0.0
            it.copy(display = formatResult(value / 100))
        }
    }

    private fun compute(a: Double, b: Double, op: String): Double {
        return when (op) {
            "+" -> a + b
            "−" -> a - b
            "×" -> a * b
            "÷" -> if (b != 0.0) a / b else 0.0
            else -> b
        }
    }

    private fun formatResult(n: Double): String {
        val s = n.toString()
        return if (s.length > 10) n.toString().take(10) else s
    }
}
