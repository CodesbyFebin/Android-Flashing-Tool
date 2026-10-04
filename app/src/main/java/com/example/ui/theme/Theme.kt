package com.example.ui.theme

import android.app.Activity
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.SideEffect
import androidx.compose.ui.graphics.toArgb
import androidx.compose.ui.platform.LocalView
import androidx.core.view.WindowCompat

private val ClearanceDarkColorScheme = darkColorScheme(
    primary = CyanNeon,
    onPrimary = ObsidianBackground,
    primaryContainer = SurfaceElevated,
    onPrimaryContainer = CyanNeon,
    secondary = SolanaPurple,
    onSecondary = TextPrimary,
    secondaryContainer = SurfaceElevated,
    onSecondaryContainer = SolanaPurple,
    tertiary = SolanaGreen,
    onTertiary = ObsidianBackground,
    tertiaryContainer = SurfaceElevated,
    onTertiaryContainer = SolanaGreen,
    background = ObsidianBackground,
    onBackground = TextPrimary,
    surface = SurfaceDark,
    onSurface = TextPrimary,
    surfaceVariant = SurfaceElevated,
    onSurfaceVariant = TextSecondary,
    outline = SurfaceBorder,
    error = RedDanger,
    onError = TextPrimary
)

@Composable
fun ClearanceTheme(
    darkTheme: Boolean = true, // Force modern cyber dark mode for Solana Seeker identity
    content: @Composable () -> Unit
) {
    val view = LocalView.current
    if (!view.isInEditMode) {
        SideEffect {
            val window = (view.context as? Activity)?.window
            if (window != null) {
                window.statusBarColor = ObsidianBackground.toArgb()
                window.navigationBarColor = SurfaceDark.toArgb()
                WindowCompat.getInsetsController(window, view).isAppearanceLightStatusBars = false
                WindowCompat.getInsetsController(window, view).isAppearanceLightNavigationBars = false
            }
        }
    }

    MaterialTheme(
        colorScheme = ClearanceDarkColorScheme,
        typography = Typography,
        content = content
    )
}
