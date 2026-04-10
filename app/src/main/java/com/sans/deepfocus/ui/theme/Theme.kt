package com.sans.deepfocus.ui.theme

import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color

private val DarkColorScheme = darkColorScheme(
    primary = PrimaryIndigo80,
    secondary = PrimaryIndigoLight,
    tertiary = PrimaryIndigo,
    background = BackgroundDark,
    surface = SurfaceDark,
    onPrimary = PrimaryIndigoDark,
    onSecondary = PrimaryIndigoDark,
    onTertiary = Color.White,
    onBackground = TextPrimary,
    onSurface = TextPrimary,
    error = SemanticRed,
    onError = Color.White,
)

private val LightColorScheme = lightColorScheme(
    primary = PrimaryIndigo,
    secondary = PrimaryIndigoDark,
    tertiary = PrimaryIndigoLight,
    background = Color(0xFFFFFBFE),
    surface = Color(0xFFFFFBFE),
    onPrimary = Color.White,
    onSecondary = Color.White,
    onTertiary = Color.Black,
    onBackground = Color(0xFF1C1B1F),
    onSurface = Color(0xFF1C1B1F),
    error = SemanticRed,
    onError = Color.White,
)

@Composable
fun DeepfocusTheme(
    darkTheme: Boolean = isSystemInDarkTheme(),
    // Dynamic color is available on Android 12+
    dynamicColor: Boolean = false, // Always use our custom theme for consistent branding
    content: @Composable () -> Unit
) {
    val colorScheme = when {
        darkTheme -> DarkColorScheme
        else -> LightColorScheme
    }

    MaterialTheme(
        colorScheme = colorScheme,
        typography = Typography,
        content = content
    )
}