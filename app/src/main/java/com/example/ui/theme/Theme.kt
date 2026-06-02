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
    primary = CbePurple,
    secondary = CbeGold,
    tertiary = CbePurpleDark,
    background = Color(0xFF130913),
    surface = Color(0xFF1E101E),
    onPrimary = Color.White,
    onSecondary = Color.White,
    onBackground = Color.White,
    onSurface = Color.White
  )

private val LightColorScheme =
  lightColorScheme(
    primary = CbePurple,
    secondary = CbeGold,
    tertiary = CbePurpleDark,
    background = CbeBackground,
    surface = CbeSurface,
    onPrimary = Color.White,
    onSecondary = Color.White,
    onBackground = CbeTextDark,
    onSurface = CbeTextDark,
    error = CbeDebitRed
  )

@Composable
fun MyApplicationTheme(
  darkTheme: Boolean = false, // CBE mobile app is primarily themed with light/purple
  dynamicColor: Boolean = false, // Force brand colors
  content: @Composable () -> Unit,
) {
  val colorScheme = if (darkTheme) DarkColorScheme else LightColorScheme

  MaterialTheme(colorScheme = colorScheme, typography = Typography, content = content)
}
