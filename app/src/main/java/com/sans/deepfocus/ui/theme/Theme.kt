package com.sans.deepfocus.ui.theme

import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color

private val DarkColorScheme = darkColorScheme(
    primary = Indigo500,
    onPrimary = Color.White,
    secondary = Slate400,
    onSecondary = Color.White,
    tertiary = Emerald500,
    onTertiary = Color.White,
    background = Color(0xFF090D16),
    onBackground = Slate50,
    surface = Color(0xFF131B2E),
    onSurface = Slate50,
    surfaceVariant = Color(0xFF1E293B),
    onSurfaceVariant = Slate300,
    outline = Color(0xFF2E3B52),
    error = Rose500,
    onError = Color.White
)

private val LightColorScheme = lightColorScheme(
    primary = Indigo600,
    onPrimary = Color.White,
    secondary = Slate500,
    onSecondary = Color.White,
    tertiary = Emerald500,
    onTertiary = Color.White,
    background = Color(0xFFF8FAFC),
    onBackground = Slate900,
    surface = Color.White,
    onSurface = Slate900,
    surfaceVariant = Slate100,
    onSurfaceVariant = Slate600,
    outline = Slate200,
    error = Rose500,
    onError = Color.White
)

@Composable
fun DeepfocusTheme(
    darkTheme: Boolean = isSystemInDarkTheme(),
    content: @Composable () -> Unit
) {
    val colorScheme = if (darkTheme) DarkColorScheme else LightColorScheme

    MaterialTheme(
        colorScheme = colorScheme,
        typography = Typography,
        shapes = Shapes,
        content = content
    )
}