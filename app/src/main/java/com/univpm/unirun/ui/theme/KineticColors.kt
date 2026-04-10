package com.univpm.unirun.ui.theme

import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.ColorScheme
import androidx.compose.ui.graphics.Color

val KineticBackground = Color(0xFF090E1C)
val KineticPrimary = Color(0xFFF3FFCA)
val KineticPrimaryContainer = Color(0xFFCAFD00)
val KineticSecondary = Color(0xFF00E3FD)
val KineticSurface = Color(0xFF0F1724)
val KineticOnPrimary = Color(0xFF000000)
val KineticOnBackground = Color(0xFFFFFFFF)

val KineticDarkColors: ColorScheme = darkColorScheme(
    primary = KineticPrimary,
    onPrimary = KineticOnPrimary,
    primaryContainer = KineticPrimaryContainer,
    secondary = KineticSecondary,
    background = KineticBackground,
    surface = KineticSurface,
    onBackground = KineticOnBackground,
    onSurface = KineticOnBackground
)
