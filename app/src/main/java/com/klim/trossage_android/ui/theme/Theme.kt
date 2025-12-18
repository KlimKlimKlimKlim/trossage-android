package com.klim.trossage_android.ui.theme

import android.app.Activity
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.SideEffect
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.toArgb
import androidx.compose.ui.platform.LocalView
import androidx.core.view.WindowCompat

private val LightColorScheme = lightColorScheme(
    primary = GoldPrimary,
    onPrimary = Color.White,
    primaryContainer = GoldSecondary,
    onPrimaryContainer = OnGoldPrimary,

    secondary = GoldTertiary,
    onSecondary = Color.White,
    secondaryContainer = GoldSurfaceVariant,
    onSecondaryContainer = OnGoldPrimary,

    tertiary = GoldPrimaryDark,
    onTertiary = Color.White,

    background = GoldBackground,
    onBackground = OnGoldBackground,

    surface = GoldSurface,
    onSurface = OnGoldBackground,
    surfaceVariant = GoldSurfaceVariant,
    onSurfaceVariant = OnGoldPrimary,

    error = ErrorLight,
    onError = Color.White,

    outline = GoldTertiary,
    outlineVariant = GoldSecondary
)

private val DarkColorScheme = darkColorScheme(
    primary = GoldSecondary,
    onPrimary = OnGoldPrimary,
    primaryContainer = GoldPrimaryDark,
    onPrimaryContainer = GoldSecondary,

    secondary = GoldTertiary,
    onSecondary = Color.White,
    secondaryContainer = DarkGoldSurfaceVariant,
    onSecondaryContainer = GoldSecondary,

    tertiary = GoldPrimary,
    onTertiary = OnGoldPrimary,

    background = DarkGoldBackground,
    onBackground = OnGoldBackgroundDark,

    surface = DarkGoldSurface,
    onSurface = OnGoldBackgroundDark,
    surfaceVariant = DarkGoldSurfaceVariant,
    onSurfaceVariant = OnGoldBackgroundDark,

    error = ErrorDark,
    onError = Color(0xFF690005),

    outline = GoldTertiary,
    outlineVariant = DarkGoldSurfaceVariant
)

@Composable
fun TrossageTheme(
    darkTheme: Boolean = isSystemInDarkTheme(),
    content: @Composable () -> Unit
) {
    val colorScheme = if (darkTheme) DarkColorScheme else LightColorScheme

    val view = LocalView.current
    if (!view.isInEditMode) {
        SideEffect {
            val window = (view.context as Activity).window
            window.statusBarColor = colorScheme.background.toArgb()
            WindowCompat.getInsetsController(window, view).isAppearanceLightStatusBars = !darkTheme
        }
    }

    MaterialTheme(
        colorScheme = colorScheme,
        content = content
    )
}
