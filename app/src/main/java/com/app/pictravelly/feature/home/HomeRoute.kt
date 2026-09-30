package com.app.pictravelly.feature.home

import androidx.activity.compose.BackHandler
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import com.app.pictravelly.core.di.AppViewModelProvider

@Composable
fun HomeRoute(
    onNavigateToDetail: (Long) -> Unit,
    onNavigateToSpots: () -> Unit,
    onNavigateToCreate: () -> Unit,
    modifier: Modifier = Modifier,
    viewModel: HomeViewModel = viewModel(factory = AppViewModelProvider.Factory),
    contentPadding: PaddingValues = PaddingValues(0.dp),
    onMapExpandedChange: ((Boolean) -> Unit)? = null
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    val context = LocalContext.current

    LaunchedEffect(uiState.isMapExpanded) {
        onMapExpandedChange?.invoke(uiState.isMapExpanded)
    }

    // Delegação do botão voltar do hardware para recolher o mapa
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
        onFetchLocationRequested = { viewModel.fetchCurrentLocation(context) },
        modifier = modifier
    )
}