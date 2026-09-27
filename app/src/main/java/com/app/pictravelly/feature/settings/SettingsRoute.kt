package com.app.pictravelly.feature.settings

import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp

// 1. STATEFUL ROUTE
@Composable
fun SettingsRoute(
    viewModel: SettingsViewModel,
    modifier: Modifier = Modifier,
    contentPadding: PaddingValues = PaddingValues(0.dp)
) {
    val uiState by viewModel.uiState.collectAsState()

    SettingsScreen(
        uiState = uiState,
        contentPadding = contentPadding,
        modifier = modifier,
        onThemeChange = viewModel::setTheme,
        onMapEngineChange = viewModel::setMapEngine,
        onLanguageChange = viewModel::setLanguage
    )
}