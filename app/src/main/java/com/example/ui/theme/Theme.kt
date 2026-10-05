package com.example.ui.theme

import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color

private val JarvisColorScheme = darkColorScheme(
    primary = JarvisCyan,
    onPrimary = Color(0xFF00222B),
    primaryContainer = JarvisDeepBlue,
    onPrimaryContainer = JarvisCyan,
    secondary = JarvisBlue,
    onSecondary = Color(0xFF001F3F),
    secondaryContainer = JarvisSurfaceVariant,
    onSecondaryContainer = JarvisTextPrimary,
    tertiary = JarvisEmerald,
    onTertiary = Color(0xFF003824),
    background = JarvisBackground,
    onBackground = JarvisTextPrimary,
    surface = JarvisSurface,
    onSurface = JarvisTextPrimary,
    surfaceVariant = JarvisSurfaceVariant,
    onSurfaceVariant = JarvisTextSecondary,
    outline = JarvisCardBorder,
    error = JarvisCrimson,
    onError = Color.White
)

@Composable
fun MyApplicationTheme(
    content: @Composable () -> Unit
) {
    MaterialTheme(
        colorScheme = JarvisColorScheme,
        typography = Typography,
        content = content
    )
}
