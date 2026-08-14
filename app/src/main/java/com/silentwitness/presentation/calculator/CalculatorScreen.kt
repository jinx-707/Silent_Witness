package com.silentwitness.presentation.calculator

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.systemBarsPadding
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.compose.material3.Text
import com.silentwitness.presentation.session.SessionViewModel

private val CalculatorBg = Color(0xFF1C1C1E)
private val OperatorColor = Color(0xFF607D8B)
private val FunctionColor = Color(0xFFA5A5A5)
private val NumberColor = Color(0xFF333333)

@Composable
fun CalculatorScreen(
    sessionViewModel: SessionViewModel,
    onUnlock: () -> Unit,
    viewModel: CalculatorViewModel = hiltViewModel()
) {
    val uiState by viewModel.uiState.collectAsState()

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(CalculatorBg)
            .systemBarsPadding(),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Box(
            modifier = Modifier
                .weight(1f)
                .fillMaxWidth()
                .padding(start = 16.dp, end = 16.dp, top = 32.dp, bottom = 16.dp),
            contentAlignment = Alignment.BottomEnd
        ) {
            Text(
                text = uiState.display,
                fontSize = if (uiState.display.length > 9) 52.sp else 72.sp,
                fontWeight = FontWeight.Light,
                color = Color.White,
                textAlign = TextAlign.End,
                letterSpacing = 0.5.sp,
                maxLines = 1,
                modifier = Modifier.fillMaxWidth()
            )
        }

        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 12.dp, vertical = 4.dp)
        ) {
            val buttonRows = listOf(
                listOf("C", "±", "%", "÷"),
                listOf("7", "8", "9", "×"),
                listOf("4", "5", "6", "−"),
                listOf("1", "2", "3", "+"),
                listOf("0", ".", "=")
            )

            buttonRows.forEach { row ->
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(vertical = 6.dp),
                    horizontalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    row.forEach { label ->
                        val isOperator = label in listOf("÷", "×", "−", "+", "=")
                        val isFunction = label in listOf("C", "±", "%")
                        val isZero = label == "0"
                        val active = when (label) {
                            "÷", "×", "−", "+" -> uiState.operator == label && uiState.waitingForSecond
                            else -> false
                        }
                        CalculatorButton(
                            label = label,
                            isOperator = isOperator,
                            isFunction = isFunction,
                            active = active,
                            modifier = Modifier
                                .weight(if (isZero) 2f else 1f)
                                .aspectRatio(1f),
                            onClick = {
                                when (label) {
                                    "C" -> viewModel.onEvent(CalculatorEvent.Clear)
                                    "±" -> viewModel.onEvent(CalculatorEvent.ToggleSign)
                                    "%" -> viewModel.onEvent(CalculatorEvent.Percent)
                                    "÷", "×", "−", "+" -> viewModel.onEvent(CalculatorEvent.Operator(label))
                                    "=" -> {
                                        val attempt = viewModel.consumePinAttempt()
                                        if (attempt.isNotEmpty() && sessionViewModel.unlock(attempt)) {
                                            viewModel.onEvent(CalculatorEvent.Equals)
                                            onUnlock()
                                        } else {
                                            viewModel.onEvent(CalculatorEvent.Clear)
                                        }
                                    }
                                    "." -> viewModel.onEvent(CalculatorEvent.Decimal)
                                    else -> viewModel.onEvent(CalculatorEvent.Digit(label))
                                }
                            }
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun CalculatorButton(
    label: String,
    isOperator: Boolean,
    isFunction: Boolean,
    active: Boolean,
    modifier: Modifier = Modifier,
    onClick: () -> Unit
) {
    val backgroundColor = when {
        active && isOperator -> Color.White
        isOperator -> OperatorColor
        isFunction -> FunctionColor
        else -> NumberColor
    }
    val textColor = when {
        active && isOperator -> OperatorColor
        isFunction -> Color.Black
        else -> Color.White
    }

    Box(
        modifier = modifier
            .clip(CircleShape)
            .background(backgroundColor)
            .clickable { onClick() },
        contentAlignment = Alignment.Center
    ) {
        Text(
            text = label,
            fontSize = if (label == "±") 24.sp else 30.sp,
            fontWeight = FontWeight.Normal,
            color = textColor
        )
    }
}
