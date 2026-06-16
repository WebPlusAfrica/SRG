package com.example.ui.theme

import android.os.Build
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.dynamicDarkColorScheme
import androidx.compose.material3.dynamicLightColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext

private val DarkColorScheme =
  darkColorScheme(
    primary = ElegantPrimary,
    onPrimary = ElegantOnPrimary,
    primaryContainer = ElegantPrimaryContainer,
    onPrimaryContainer = ElegantOnPrimaryContainer,
    secondary = PurpleGrey80,
    background = ElegantBackground,
    surface = ElegantSurface,
    onBackground = ElegantOnBackground,
    onSurface = ElegantOnSurface,
    outline = ElegantOutline
  )

private val LightColorScheme =
  lightColorScheme(
    primary = Color(0xFF4A3780),         // Luxury deep indigo-violet
    onPrimary = Color.White,
    primaryContainer = Color(0xFFF4EFF4), // Soft lavender gray
    onPrimaryContainer = Color(0xFF1D0060),
    secondary = Color(0xFF625B71),
    background = Color(0xFFF9F9FB),      // Premium off-white base
    surface = Color(0xFFFFFFFF),         // Clean slate white surface
    onBackground = Color(0xFF131316),    // Dark rich charcoal
    onSurface = Color(0xFF131316),
    outline = Color(0xFFCAC4D0)
  )

@Composable
fun MyApplicationTheme(
  darkTheme: Boolean = true, // Defaults to state-driven or dark
  // Dynamic color is disabled by default to force the custom brand aesthetic
  dynamicColor: Boolean = false,
  content: @Composable () -> Unit,
) {
  val colorScheme =
    when {
      dynamicColor && Build.VERSION.SDK_INT >= Build.VERSION_CODES.S -> {
        val context = LocalContext.current
        if (darkTheme) dynamicDarkColorScheme(context) else dynamicLightColorScheme(context)
      }

      darkTheme -> DarkColorScheme
      else -> LightColorScheme
    }

  MaterialTheme(colorScheme = colorScheme, typography = Typography, content = content)
}
