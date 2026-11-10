package com.example.ui.theme

import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.runtime.Composable

private val CyberpunkDarkColorScheme = darkColorScheme(
    primary = NeonCyan,
    onPrimary = DarkBackground,
    primaryContainer = DarkCardElevated,
    onPrimaryContainer = NeonCyan,
    secondary = NeonGreen,
    onSecondary = DarkBackground,
    secondaryContainer = DarkCard,
    onSecondaryContainer = NeonGreen,
    tertiary = NeonPurple,
    onTertiary = TextPrimary,
    tertiaryContainer = DarkCard,
    onTertiaryContainer = NeonPurple,
    background = DarkBackground,
    onBackground = TextPrimary,
    surface = DarkSurface,
    onSurface = TextPrimary,
    surfaceVariant = DarkCard,
    onSurfaceVariant = TextSecondary,
    outline = DarkBorder,
    outlineVariant = DarkBorderSubtle,
    error = NeonRed,
    onError = TextPrimary,
    errorContainer = DarkCard,
    onErrorContainer = NeonRed
)

@Composable
fun CryptoSignalTheme(
    content: @Composable () -> Unit
) {
    MaterialTheme(
        colorScheme = CyberpunkDarkColorScheme,
        typography = Typography,
        content = content
    )
}

