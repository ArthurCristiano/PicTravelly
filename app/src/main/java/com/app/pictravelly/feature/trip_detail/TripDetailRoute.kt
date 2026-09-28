package com.app.pictravelly.feature.trip_detail

import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier

/**
 * ROTA: Responsável pelo Contexto, ViewModels e navegação.
 */
@Composable
fun TripDetailRoute(
    viewModel: TripDetailViewModel,
    onNavigateBack: () -> Unit,
    onEditTrip: (Long) -> Unit,
    onAddSpot: () -> Unit,
    onNavigateToSpotDetail: (Long) -> Unit,
    modifier: Modifier = Modifier
) {
    val uiState by viewModel.uiState.collectAsState()

    // Sobrevive à rotação de tela do aparelho
    var showDeleteTripDialog by rememberSaveable { mutableStateOf(false) }

    LaunchedEffect(uiState.wasDeleted) {
        if (uiState.wasDeleted) onNavigateBack()
    }

    TripDetailScreen(
        uiState = uiState,
        showDeleteTripDialog = showDeleteTripDialog,
        onNavigateBack = onNavigateBack,
        onEditTripClick = { uiState.tripHeader?.let { onEditTrip(it.id) } },
        onAddSpotClick = onAddSpot,
        onDeleteTripClick = { showDeleteTripDialog = true },
        onDeleteTripConfirm = {
            showDeleteTripDialog = false
            viewModel.deleteTrip()
        },
        onDeleteTripDismiss = { showDeleteTripDialog = false },
        onSpotClick = onNavigateToSpotDetail,
        onSpotUnlink = viewModel::removeSpotFromTrip,
        onSpotDelete = viewModel::deleteSpot,
        modifier = modifier
    )
}