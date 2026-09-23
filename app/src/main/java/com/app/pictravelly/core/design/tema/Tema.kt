package com.app.pictravelly.core.design.tema

import android.os.Build
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.dynamicDarkColorScheme
import androidx.compose.material3.dynamicLightColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.platform.LocalContext

private val EsquemaClaro = lightColorScheme(
    primary = PrimaryBlue,
    onPrimary = OnPrimaryBlue,
    primaryContainer = PrimaryContainerBlue,
    onPrimaryContainer = OnPrimaryContainerBlue,
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
    error = ErrorRed
)

private val EsquemaEscuro = darkColorScheme(
    primary = PrimaryBlueDark,
    onPrimary = OnPrimaryBlueDark,
    primaryContainer = PrimaryContainerBlueDark,
    onPrimaryContainer = OnPrimaryContainerBlueDark,
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
    error = ErrorRedDark
)

@Composable
fun PicTravellyTema(
    temaEscuro: Boolean = isSystemInDarkTheme(),
    corDinamica: Boolean = false,
    content: @Composable () -> Unit
) {
    val esquemaDeCores = when {
        corDinamica && Build.VERSION.SDK_INT >= Build.VERSION_CODES.S -> {
            val context = LocalContext.current
            if (temaEscuro) dynamicDarkColorScheme(context) else dynamicLightColorScheme(context)
        }

        temaEscuro -> EsquemaEscuro
        else -> EsquemaClaro
    }

    MaterialTheme(
        colorScheme = esquemaDeCores,
        typography = Tipografia,
        content = content
    )
}