package com.app.pictravelly.core.ui

import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.runtime.Composable
import com.app.pictravelly.MainActivityUiState
import com.app.pictravelly.core.database.model.settings.AppTheme
import com.app.pictravelly.core.database.model.settings.AppTheme.LIGHT
import com.app.pictravelly.core.design.theme.PicTravellyTheme

@Composable
fun PicTravellyRoot(
    uiState: MainActivityUiState,
    pendingSharedImages: List<String>? = null,
    onConsumeSharedImages: () -> Unit = {}
) {
    val darkTheme = when (uiState) {
        is MainActivityUiState.Loading -> isSystemInDarkTheme()
        is MainActivityUiState.Success -> when (uiState.userData.theme) {
            LIGHT -> false
            AppTheme.DARK -> true
            AppTheme.SYSTEM -> isSystemInDarkTheme()
        }
    }

    PicTravellyTheme(darkTheme = darkTheme) {
        PicTravellyAppScreen(
            pendingSharedImages = pendingSharedImages,
            onConsumeSharedImages = onConsumeSharedImages
        )
    }
}