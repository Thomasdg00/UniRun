package com.univpm.unirun.ui.theme

import androidx.compose.material3.Typography
import androidx.compose.ui.text.font.Font
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.sp
import com.univpm.unirun.R
import androidx.compose.ui.text.TextStyle

val Lexend = FontFamily(Font(R.font.lexend))
val Manrope = FontFamily(Font(R.font.manrope))
val SpaceGrotesk = FontFamily(Font(R.font.spacegrotesk))

val KineticTypography = Typography(
    displayLarge = TextStyle(fontFamily = Lexend, fontSize = 28.sp, fontWeight = FontWeight.Bold),
    bodyLarge = TextStyle(fontFamily = Manrope, fontSize = 16.sp),
    labelLarge = TextStyle(fontFamily = SpaceGrotesk, fontSize = 12.sp)
)
