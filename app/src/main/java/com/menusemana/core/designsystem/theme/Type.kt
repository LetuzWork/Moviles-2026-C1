package com.menusemana.core.designsystem.theme

import androidx.compose.material3.Typography
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.Font
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.sp
import com.menusemana.R

val Bricolage = FontFamily(
    Font(R.font.bricolage_grotesque_600, FontWeight.SemiBold),
    Font(R.font.bricolage_grotesque_700, FontWeight.Bold),
)

val Manrope = FontFamily(
    Font(R.font.manrope_400, FontWeight.Normal),
    Font(R.font.manrope_600, FontWeight.SemiBold),
    Font(R.font.manrope_700, FontWeight.Bold),
)

val JetBrainsMono = FontFamily(
    Font(R.font.jetbrains_mono_500, FontWeight.Medium),
)

val MenuSemanaTypography = Typography(
    displayLarge  = TextStyle(fontFamily = Bricolage, fontWeight = FontWeight.SemiBold, fontSize = 56.sp, lineHeight = 60.sp, letterSpacing = (-1.1).sp),
    displayMedium = TextStyle(fontFamily = Bricolage, fontWeight = FontWeight.SemiBold, fontSize = 44.sp, lineHeight = 48.sp, letterSpacing = (-0.9).sp),
    displaySmall  = TextStyle(fontFamily = Bricolage, fontWeight = FontWeight.SemiBold, fontSize = 36.sp, lineHeight = 40.sp),
    headlineLarge = TextStyle(fontFamily = Bricolage, fontWeight = FontWeight.SemiBold, fontSize = 30.sp, lineHeight = 36.sp),
    headlineMedium= TextStyle(fontFamily = Bricolage, fontWeight = FontWeight.SemiBold, fontSize = 26.sp, lineHeight = 32.sp),
    headlineSmall = TextStyle(fontFamily = Bricolage, fontWeight = FontWeight.SemiBold, fontSize = 22.sp, lineHeight = 28.sp),
    titleLarge    = TextStyle(fontFamily = Manrope,   fontWeight = FontWeight.Bold,     fontSize = 20.sp, lineHeight = 27.sp),
    titleMedium   = TextStyle(fontFamily = Manrope,   fontWeight = FontWeight.Bold,     fontSize = 16.sp, lineHeight = 22.sp),
    titleSmall    = TextStyle(fontFamily = Manrope,   fontWeight = FontWeight.Bold,     fontSize = 14.sp, lineHeight = 20.sp),
    bodyLarge     = TextStyle(fontFamily = Manrope,   fontWeight = FontWeight.Normal,   fontSize = 16.sp, lineHeight = 24.sp),
    bodyMedium    = TextStyle(fontFamily = Manrope,   fontWeight = FontWeight.Normal,   fontSize = 14.sp, lineHeight = 21.sp),
    bodySmall     = TextStyle(fontFamily = Manrope,   fontWeight = FontWeight.Normal,   fontSize = 12.sp, lineHeight = 17.sp),
    labelLarge    = TextStyle(fontFamily = Manrope,   fontWeight = FontWeight.SemiBold, fontSize = 14.sp, lineHeight = 19.sp),
    labelMedium   = TextStyle(fontFamily = Manrope,   fontWeight = FontWeight.SemiBold, fontSize = 12.sp, lineHeight = 16.sp, letterSpacing = 0.5.sp),
    labelSmall    = TextStyle(fontFamily = Manrope,   fontWeight = FontWeight.Bold,     fontSize = 11.sp, lineHeight = 14.sp, letterSpacing = 0.7.sp),
)
