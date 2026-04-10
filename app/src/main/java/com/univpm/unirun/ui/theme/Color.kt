package com.univpm.unirun.ui.theme

import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.ui.graphics.Color

val SurfaceBackgroundLight = Color(0xFFFFFFFF)
val SurfaceCardLight = Color(0xFFF5F5F5)
val SurfaceDashboardLight = Color(0xFFE0E0E0)
val SurfaceControlLight = Color(0xFFEEEEEE)
val TextPrimaryLight = Color(0xFF212121)
val TextSecondaryLight = Color(0xFF757575)

val KineticBackground = Color(0xFF090E1C)
val KineticSurfaceContainerLow = Color(0xFF0D1323)
val KineticSurfaceContainerLowest = Color(0xFF000000)
val KineticSurfaceContainer = Color(0xFF13192B)
val KineticSurfaceContainerHigh = Color(0xFF181F33)
val KineticSurfaceContainerHighest = Color(0xFF1E253B)
val KineticSurfaceBright = Color(0xFF242B43)
val KineticPrimary = Color(0xFFF3FFCA)
val KineticPrimaryContainer = Color(0xFFCAFD00)
val KineticPrimaryDim = Color(0xFFBEEE00)
val KineticOnPrimaryFixed = Color(0xFF3A4A00)
val KineticSecondary = Color(0xFF00E3FD)
val KineticOutline = Color(0xFF707588)
val KineticOutlineVariant = Color(0xFF434759)
val KineticOnSurface = Color(0xFFE1E4FA)
val KineticOnSurfaceVariant = Color(0xFFA6AABF)
val KineticError = Color(0xFFFF7351)

val AccentRed = Color(0xFFEE4B2B)
val AccentGreen = Color(0xFF4CAF50)
val AccentBlue = Color(0xFF2196F3)

val UniRunLightColorScheme = lightColorScheme(
    primary = AccentBlue,
    onPrimary = Color.White,
    primaryContainer = KineticPrimaryContainer,
    onPrimaryContainer = KineticOnPrimaryFixed,
    secondary = AccentGreen,
    onSecondary = Color.White,
    tertiary = AccentRed,
    background = SurfaceBackgroundLight,
    onBackground = TextPrimaryLight,
    surface = SurfaceCardLight,
    onSurface = TextPrimaryLight,
    surfaceVariant = SurfaceDashboardLight,
    onSurfaceVariant = TextSecondaryLight,
    surfaceContainerLow = SurfaceCardLight,
    surfaceContainer = SurfaceDashboardLight,
    surfaceContainerHigh = SurfaceControlLight,
    surfaceContainerHighest = Color(0xFFE7E7E7),
    outline = KineticOutline,
    outlineVariant = KineticOutlineVariant,
    error = AccentRed,
    onError = Color.White
)

val UniRunDarkColorScheme = darkColorScheme(
    primary = KineticPrimary,
    onPrimary = Color.Black,
    primaryContainer = KineticPrimaryContainer,
    onPrimaryContainer = KineticOnPrimaryFixed,
    secondary = KineticSecondary,
    onSecondary = Color.Black,
    tertiary = AccentGreen,
    background = KineticBackground,
    onBackground = KineticOnSurface,
    surface = KineticSurfaceContainer,
    onSurface = KineticOnSurface,
    surfaceVariant = KineticSurfaceContainerHighest,
    onSurfaceVariant = KineticOnSurfaceVariant,
    surfaceContainerLowest = KineticSurfaceContainerLowest,
    surfaceContainerLow = KineticSurfaceContainerLow,
    surfaceContainer = KineticSurfaceContainer,
    surfaceContainerHigh = KineticSurfaceContainerHigh,
    surfaceContainerHighest = KineticSurfaceContainerHighest,
    surfaceBright = KineticSurfaceBright,
    outline = KineticOutline,
    outlineVariant = KineticOutlineVariant,
    error = KineticError,
    onError = Color.Black
)

val KineticDarkColors = UniRunDarkColorScheme
val KineticSurface = KineticSurfaceContainer
val KineticOnBackground = KineticOnSurface
