package com.app.pictravelly.feature.trips

import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp

/**
 * ROTA: Reponsável exclusiva por interligar o ViewModel e fornecer dados mastigados para a UI.
 */
@Composable
fun TripsRoute(
    viewModel: TripsViewModel,
    onNavigateToTripDetail: (Long) -> Unit,
    onNavigateToAllSpots: () -> Unit,
    modifier: Modifier = Modifier,
    contentPadding: PaddingValues = PaddingValues(0.dp)
) {
    val uiState by viewModel.uiState.collectAsState()

    TripsScreen(
        uiState = uiState,
        onNavigateToTripDetail = onNavigateToTripDetail,
        onNavigateToAllSpots = onNavigateToAllSpots,
        modifier = modifier,
        contentPadding = contentPadding
    )
}