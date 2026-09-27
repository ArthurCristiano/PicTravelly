package com.app.pictravelly.core.ui

import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.runtime.Composable
import com.app.pictravelly.MainActivityUiState
import com.app.pictravelly.core.database.model.settings.AppTheme
import com.app.pictravelly.core.database.model.settings.AppTheme.LIGHT
import com.app.pictravelly.core.design.theme.PicTravellyTheme

/**
 * Raiz da árvore do Jetpack Compose.
 * Responsável por aplicar o Design System antes de injetar as telas.
 */
@Composable
fun PicTravellyRoot(
    uiState: MainActivityUiState
) {
    // 1. Calcula o tema baseado no estado assíncrono
    val darkTheme = when (uiState) {
        is MainActivityUiState.Loading -> isSystemInDarkTheme() // Fallback seguro
        is MainActivityUiState.Success -> when (uiState.userData.theme) {
            LIGHT -> false
            AppTheme.DARK -> true
            AppTheme.SYSTEM -> isSystemInDarkTheme()
        }
    }

    // 2. Envolve a aplicação com o tema e chama as rotas
    PicTravellyTheme(darkTheme = darkTheme) {
        PicTravellyAppScreen()
    }
}