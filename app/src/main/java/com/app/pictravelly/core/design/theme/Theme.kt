package com.app.pictravelly.core.design.theme

import android.os.Build
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.dynamicDarkColorScheme
import androidx.compose.material3.dynamicLightColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.platform.LocalContext

private val LightColorScheme = lightColorScheme(
    primary = PrimaryBrown,
    onPrimary = OnPrimaryBrown,
    primaryContainer = PrimaryContainerBrown,
    onPrimaryContainer = OnPrimaryContainerBrown,
    secondary = SecondaryTerracotta,
    onSecondary = OnSecondaryTerracotta,
    secondaryContainer = SecondaryContainerTerracotta,
    onSecondaryContainer = OnSecondaryContainerTerracotta,
    background = BackgroundLight,
    onBackground = TextPrimaryLight,
    surface = SurfaceLight,
    onSurface = TextPrimaryLight,
    surfaceVariant = SurfaceLight,
    onSurfaceVariant = TextSecondaryLight,
    outline = OutlineLight,
    outlineVariant = OutlineVariantLight,
    error = ErrorRed,
    errorContainer = ErrorContainerLight,
    onErrorContainer = OnErrorContainerLight
)

private val DarkColorScheme = darkColorScheme(
    primary = PrimaryBrownDark,
    onPrimary = OnPrimaryBrownDark,
    primaryContainer = PrimaryContainerBrownDark,
    onPrimaryContainer = OnPrimaryContainerBrownDark,
    secondary = SecondaryTerracottaDark,
    onSecondary = OnSecondaryTerracottaDark,
    secondaryContainer = SecondaryContainerTerracottaDark,
    onSecondaryContainer = OnSecondaryContainerTerracottaDark,
    background = BackgroundDark,
    onBackground = TextPrimaryDark,
    surface = SurfaceDark,
    onSurface = TextPrimaryDark,
    surfaceVariant = SurfaceDark,
    onSurfaceVariant = TextSecondaryDark,
    outline = OutlineDark,
    outlineVariant = OutlineVariantDark,
    error = ErrorRedDark,
    errorContainer = ErrorContainerDark,
    onErrorContainer = OnErrorContainerDark
)

@Composable
fun PicTravellyTheme(
    darkTheme: Boolean = isSystemInDarkTheme(),
    dynamicColor: Boolean = false,
    content: @Composable () -> Unit
) {
    val colorScheme = when {
        dynamicColor && Build.VERSION.SDK_INT >= Build.VERSION_CODES.S -> {
            val context = LocalContext.current
            if (darkTheme) dynamicDarkColorScheme(context) else dynamicLightColorScheme(context)
        }

        darkTheme -> DarkColorScheme
        else -> LightColorScheme
    }

    MaterialTheme(
        colorScheme = colorScheme,
        typography = Typography,
        content = content
    )
}