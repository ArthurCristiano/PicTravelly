package com.app.pictravelly.feature.trips

import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import com.app.pictravelly.core.di.AppViewModelProvider

/**
 * ROTA: Responsável exclusiva por interligar o ViewModel e fornecer dados para a UI.
 */
@Composable
fun TripsRoute(
    onNavigateToTripDetail: (Long) -> Unit,
    onNavigateToAllSpots: () -> Unit,
    modifier: Modifier = Modifier,
    viewModel: TripsViewModel = viewModel(factory = AppViewModelProvider.Factory),
    contentPadding: PaddingValues = PaddingValues(0.dp)
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()

    TripsScreen(
        uiState = uiState,
        onNavigateToTripDetail = onNavigateToTripDetail,
        onNavigateToAllSpots = onNavigateToAllSpots,
        modifier = modifier,
        contentPadding = contentPadding
    )
}