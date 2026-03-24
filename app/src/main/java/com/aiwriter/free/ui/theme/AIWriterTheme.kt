package com.aiwriter.free.ui.theme

import android.app.Activity
import android.os.Build
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.graphics.toArgb
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalView
import androidx.core.view.WindowCompat

private val AIWriterDarkScheme = darkColorScheme(
    primary = AIViolet80,
    secondary = NeuralCyan80,
    tertiary = InkAmber80,
    background = DarkAIBackground,
    surface = DarkAISurface,
    surfaceVariant = DarkAISurfaceVariant,
    onPrimary = AIViolet20,
    onBackground = AISurface,
    onSurface = AISurface,
)

private val AIWriterLightScheme = lightColorScheme(
    primary = AIViolet40,
    secondary = NeuralCyan40,
    tertiary = InkAmber40,
    background = AIBackground,
    surface = AISurface,
    surfaceVariant = AISurfaceVariant,
    onPrimary = AISurface,
    onBackground = AIViolet20,
    onSurface = AIViolet20,
)

@Composable
fun AIWriterTheme(
    darkTheme: Boolean = isSystemInDarkTheme(),
    dynamicColor: Boolean = true,
    content: @Composable () -> Unit
) {
    val colorScheme = when {
        dynamicColor && Build.VERSION.SDK_INT >= Build.VERSION_CODES.S -> {
            val context = LocalContext.current
            if (darkTheme) dynamicDarkColorScheme(context) else dynamicLightColorScheme(context)
        }
        darkTheme -> AIWriterDarkScheme
        else -> AIWriterLightScheme
    }
    val view = LocalView.current
    if (!view.isInEditMode) {
        SideEffect {
            val window = (view.context as Activity).window
            window.statusBarColor = colorScheme.primary.toArgb()
            WindowCompat.getInsetsController(window, view).isAppearanceLightStatusBars = !darkTheme
        }
    }
    MaterialTheme(colorScheme = colorScheme, content = content)
}
