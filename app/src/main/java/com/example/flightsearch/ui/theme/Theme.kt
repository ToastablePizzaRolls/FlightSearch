package com.example.flightsearch.ui.theme

import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color

private val LightColorScheme = lightColorScheme(
    primary = Color(0xFF1A6FC4),
    onPrimary = Color.White,
    primaryContainer = Color(0xFFD6E8FF),
    secondary = Color(0xFF2D9CDB),
    tertiary = Color(0xFF27AE60),
    background = Color(0xFFF8FAFC),
    surface = Color.White,
    surfaceVariant = Color(0xFFF0F4F8),
)

@Composable
fun FlightSearchTheme(content: @Composable () -> Unit) {
    MaterialTheme(
        colorScheme = LightColorScheme,
        content = content
    )
}
