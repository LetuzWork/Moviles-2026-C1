package com.menusemana.core.designsystem.theme

import android.os.Build
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.dynamicDarkColorScheme
import androidx.compose.material3.dynamicLightColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext

val MenuSemanaLightColors = lightColorScheme(
    primary            = Persimmon500,
    onPrimary          = Neutral50,
    primaryContainer   = Persimmon100,
    onPrimaryContainer = Persimmon900,
    secondary             = Herb500,
    onSecondary           = Neutral50,
    secondaryContainer    = Herb100,
    onSecondaryContainer  = Herb900,
    tertiary              = Saffron500,
    onTertiary            = Neutral900,
    tertiaryContainer     = Saffron100,
    onTertiaryContainer   = Saffron900,
    background            = Neutral50,
    onBackground          = Neutral900,
    surface               = Neutral0,
    onSurface             = Neutral900,
    surfaceVariant        = Neutral100,
    onSurfaceVariant      = Neutral600,
    outline               = Neutral300,
    outlineVariant        = Neutral200,
    error                 = Danger500,
    onError               = Neutral50,
)

val MenuSemanaDarkColors = darkColorScheme(
    primary            = Persimmon400,
    onPrimary          = Neutral900,
    primaryContainer   = Color(0xFF4A2114),
    onPrimaryContainer = Persimmon100,
    secondary             = Herb300,
    onSecondary           = Neutral900,
    secondaryContainer    = Color(0xFF2A3818),
    onSecondaryContainer  = Herb100,
    tertiary              = Saffron300,
    background            = Neutral950,
    onBackground          = Color(0xFFF4EDE2),
    surface               = Neutral900,
    onSurface             = Color(0xFFF4EDE2),
    surfaceVariant        = Color(0xFF28231D),
    onSurfaceVariant      = Color(0xFFC7B9A1),
    outline               = Neutral600,
    error                 = Danger500,
)

@Composable
fun MenuSemanaTheme(
    darkTheme: Boolean = isSystemInDarkTheme(),
    dynamicColor: Boolean = true,
    content: @Composable () -> Unit,
) {
    val colorScheme = when {
        dynamicColor && Build.VERSION.SDK_INT >= Build.VERSION_CODES.S -> {
            val context = LocalContext.current
            if (darkTheme) dynamicDarkColorScheme(context) else dynamicLightColorScheme(context)
        }
        darkTheme -> MenuSemanaDarkColors
        else -> MenuSemanaLightColors
    }

    CompositionLocalProvider(LocalSpacing provides Spacing()) {
        MaterialTheme(
            colorScheme = colorScheme,
            typography  = MenuSemanaTypography,
            shapes      = MenuSemanaShapes,
            content     = content,
        )
    }
}
