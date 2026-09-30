package com.app.pictravelly.feature.map

import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import com.app.pictravelly.core.di.AppViewModelProvider

/**
 * ROTA: Responsável apenas pelas integrações com o SO (Permissões, GPS, ViewModel).
 */
@Composable
fun MapRoute(
    onNavigateToSpotDetail: (Long) -> Unit,
    modifier: Modifier = Modifier,
    viewModel: MapViewModel = viewModel(factory = AppViewModelProvider.Factory),
    contentPadding: PaddingValues = PaddingValues(0.dp)
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    val context = LocalContext.current

    // Delega os dados puros para a tela visual
    MapScreen(
        uiState = uiState,
        contentPadding = contentPadding,
        onCloseSelection = viewModel::clearSelection,
        onMarkerClick = { marker -> viewModel.selectSpotById(marker.id) },
        onNavigateToSpotDetail = onNavigateToSpotDetail,
        onFetchLocationRequested = { viewModel.fetchCurrentLocation(context) },
        modifier = modifier
    )
}