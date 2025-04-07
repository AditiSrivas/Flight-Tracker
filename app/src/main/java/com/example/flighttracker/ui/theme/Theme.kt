package com.example.flighttracker.ui.theme

import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.ui.graphics.Color

// Light theme colors
private val LightPrimary = Color(0xFF2196F3)
private val LightOnPrimary = Color.White
private val LightPrimaryContainer = Color(0xFFBBDEFB)
private val LightOnPrimaryContainer = Color(0xFF0D47A1)
private val LightSecondary = Color(0xFF26C6DA)
private val LightOnSecondary = Color.White
private val LightTertiary = Color(0xFF4CAF50)
private val LightOnTertiary = Color.White
private val LightError = Color(0xFFE53935)
private val LightErrorContainer = Color(0xFFFFCDD2)
private val LightOnErrorContainer = Color(0xFFB71C1C)
private val LightBackground = Color(0xFFF5F5F5)
private val LightOnBackground = Color(0xFF212121)
private val LightSurface = Color.White
private val LightOnSurface = Color(0xFF212121)

// Dark theme colors
private val DarkPrimary = Color(0xFF42A5F5)
private val DarkOnPrimary = Color.Black
private val DarkPrimaryContainer = Color(0xFF0D47A1)
private val DarkOnPrimaryContainer = Color(0xFFBBDEFB)
private val DarkSecondary = Color(0xFF4DD0E1)
private val DarkOnSecondary = Color.Black
private val DarkTertiary = Color(0xFF66BB6A)
private val DarkOnTertiary = Color.Black
private val DarkError = Color(0xFFEF5350)
private val DarkErrorContainer = Color(0xFF8B0000)
private val DarkOnErrorContainer = Color(0xFFFFCDD2)
private val DarkBackground = Color(0xFF121212)
private val DarkOnBackground = Color.White
private val DarkSurface = Color(0xFF212121)
private val DarkOnSurface = Color.White

// Light color scheme
val LightColors = lightColorScheme(
    primary = LightPrimary,
    onPrimary = LightOnPrimary,
    primaryContainer = LightPrimaryContainer,
    onPrimaryContainer = LightOnPrimaryContainer,
    secondary = LightSecondary,
    onSecondary = LightOnSecondary,
    tertiary = LightTertiary,
    onTertiary = LightOnTertiary,
    error = LightError,
    errorContainer = LightErrorContainer,
    onErrorContainer = LightOnErrorContainer,
    background = LightBackground,
    onBackground = LightOnBackground,
    surface = LightSurface,
    onSurface = LightOnSurface
)

// Dark color scheme
val DarkColors = darkColorScheme(
    primary = DarkPrimary,
    onPrimary = DarkOnPrimary,
    primaryContainer = DarkPrimaryContainer,
    onPrimaryContainer = DarkOnPrimaryContainer,
    secondary = DarkSecondary,
    onSecondary = DarkOnSecondary,
    tertiary = DarkTertiary,
    onTertiary = DarkOnTertiary,
    error = DarkError,
    errorContainer = DarkErrorContainer,
    onErrorContainer = DarkOnErrorContainer,
    background = DarkBackground,
    onBackground = DarkOnBackground,
    surface = DarkSurface,
    onSurface = DarkOnSurface
)

