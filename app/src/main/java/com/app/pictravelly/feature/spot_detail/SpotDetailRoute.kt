package com.app.pictravelly.feature.spot_detail

import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.lifecycle.compose.collectAsStateWithLifecycle

@Composable
fun SpotDetailRoute(
    viewModel: SpotDetailViewModel,
    onNavigateBack: () -> Unit,
    onEditSpot: (Long) -> Unit,
    modifier: Modifier = Modifier
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    var showDeleteDialog by remember { mutableStateOf(false) }
    var isMapExpanded by remember { mutableStateOf(false) }

    SpotDetailScreen(
        uiState = uiState,
        showDeleteDialog = showDeleteDialog,
        isMapExpanded = isMapExpanded,
        onNavigateBack = onNavigateBack,
        onEditClick = { uiState.spotWithImages?.let { onEditSpot(it.spot.id) } },
        onDeleteClick = { showDeleteDialog = true },
        onDeleteConfirm = {
            showDeleteDialog = false
            viewModel.deleteSpot(onNavigateBack)
        },
        onDeleteDismiss = { showDeleteDialog = false },
        onMapExpandedChange = { isMapExpanded = it },
        onZoomChange = viewModel::updateZoom,
        onEngineChange = viewModel::updateMapEngine,
        onMapTypeChange = viewModel::updateGoogleMapType,
        modifier = modifier
    )
}