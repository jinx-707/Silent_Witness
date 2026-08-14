package com.silentwitness.presentation.theme

import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color

private val DarkColorScheme = darkColorScheme(
    background = SwBg,
    surface = SwSurface,
    onBackground = SwText,
    onSurface = SwText,
    primary = SwAccent,
    onPrimary = SwText,
    secondary = SwAccent2,
    onSecondary = SwText,
    outline = SwBorder,
    surfaceVariant = SwCard
)

@Composable
fun SecureAppTheme(content: @Composable () -> Unit) {
    MaterialTheme(
        colorScheme = DarkColorScheme,
        typography = SecureTypography,
        content = content
    )
}
