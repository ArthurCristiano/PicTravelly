package com.app.pictravelly.feature.spots

import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp

/**
 * ROTA: Responsável apenas pela injeção da ViewModel e delegação de eventos.
 */
@Composable
fun SpotsRoute(
    viewModel: SpotsViewModel,
    onNavigateToDetail: (Long) -> Unit,
    modifier: Modifier = Modifier,
    contentPadding: PaddingValues = PaddingValues(0.dp)
) {
    val uiState by viewModel.uiState.collectAsState()

    SpotsScreen(
        uiState = uiState,
        onSearchQueryChanged = viewModel::onSearchQueryChanged,
        onNavigateToDetail = onNavigateToDetail,
        modifier = modifier,
        contentPadding = contentPadding
    )
}