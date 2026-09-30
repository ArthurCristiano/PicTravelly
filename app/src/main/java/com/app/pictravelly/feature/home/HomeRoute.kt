package com.app.pictravelly.feature.home

import androidx.activity.compose.BackHandler
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

@Composable
fun HomeRoute(
    viewModel: HomeViewModel,
    onNavigateToDetail: (Long) -> Unit,
    onNavigateToSpots: () -> Unit,
    onNavigateToCreate: () -> Unit,
    modifier: Modifier = Modifier,
    contentPadding: PaddingValues = PaddingValues(0.dp)
) {
    val uiState by viewModel.uiState.collectAsState()
    val context = LocalContext.current
    val coroutineScope = rememberCoroutineScope()

    // Delegação do botão voltar do hardware
    BackHandler(enabled = uiState.isMapExpanded) {
        viewModel.setMapExpanded(false)
    }

    HomeScreen(
        uiState = uiState,
        contentPadding = contentPadding,
        onMapExpandedChange = viewModel::setMapExpanded,
        onSpotSelect = viewModel::selectSpot,
        onNavigateToDetail = onNavigateToDetail,
        onNavigateToSpots = onNavigateToSpots,
        onNavigateToCreate = onNavigateToCreate,

        // CONEXÃO REATIVA: A tela chama isso quando o GPS ligar ou a permissão for dada
        onFetchLocationRequested = {
            coroutineScope.launch {
                val (lat, lng) = LocationHelper.getCurrentLocation(context)
                viewModel.updateCurrentLocation(lat, lng)
            }
        },
        modifier = modifier
    )
}