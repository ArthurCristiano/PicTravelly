package com.app.pictravelly.feature.map

import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import com.app.pictravelly.core.location.LocationHelper
import kotlinx.coroutines.launch

/**
 * ROTA: Responsável apenas pelas integrações com o SO (Permissões, GPS, ViewModel).
 */
@Composable
fun MapRoute(
    viewModel: MapViewModel,
    onNavigateToSpotDetail: (Long) -> Unit,
    modifier: Modifier = Modifier,
    contentPadding: PaddingValues = PaddingValues(0.dp)
) {
    val uiState by viewModel.uiState.collectAsState()
    val context = LocalContext.current
    val coroutineScope = rememberCoroutineScope()

    // Delega os dados puros para a tela visual
    MapScreen(
        uiState = uiState,
        contentPadding = contentPadding,
        onCloseSelection = viewModel::clearSelection,
        onMarkerClick = { marker -> viewModel.selectSpotById(marker.id) },
        onNavigateToSpotDetail = onNavigateToSpotDetail,
        onFetchLocationRequested = {
            coroutineScope.launch {
                val (lat, lng) = LocationHelper.getCurrentLocation(context)
                viewModel.updateCurrentLocation(lat, lng)
            }
        },
        modifier = modifier
    )
}