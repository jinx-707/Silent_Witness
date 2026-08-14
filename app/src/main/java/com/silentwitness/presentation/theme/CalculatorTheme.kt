package com.silentwitness.presentation.theme

import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable

@Composable
fun CalculatorTheme(content: @Composable () -> Unit) {
    MaterialTheme(
        colorScheme = MaterialTheme.colorScheme.copy(
            background = CalcBg,
            surface = CalcBg,
            onBackground = CalcDisplayText,
            onSurface = CalcDisplayText
        ),
        typography = CalculatorTypography,
        content = content
    )
}
