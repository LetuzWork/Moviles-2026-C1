package com.menusemana.core.designsystem.theme

import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.staticCompositionLocalOf
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp

data class Spacing(
    val xxs: Dp  = 4.dp,
    val xs: Dp   = 8.dp,
    val sm: Dp   = 12.dp,
    val md: Dp   = 16.dp,
    val lg: Dp   = 20.dp,
    val xl: Dp   = 24.dp,
    val xxl: Dp  = 32.dp,
    val xxxl: Dp = 40.dp,
    val huge: Dp = 56.dp,
    val giant: Dp = 72.dp,
)

val LocalSpacing = staticCompositionLocalOf { Spacing() }

val MaterialTheme.spacing: Spacing
    @Composable get() = LocalSpacing.current
