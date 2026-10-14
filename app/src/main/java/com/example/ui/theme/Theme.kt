package com.example.ui.theme

import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.runtime.Composable

private val SerenimeColorScheme =
  darkColorScheme(
    primary = Primary,
    secondary = Accent,
    background = BackgroundDark,
    surface = SurfaceDark,
    onPrimary = OnPrimary,
    onBackground = OnBackground,
    onSurface = OnBackground,
  )

@Composable
fun SerenimeTheme(
  content: @Composable () -> Unit,
) {
  MaterialTheme(colorScheme = SerenimeColorScheme, typography = Typography, content = content)
}
