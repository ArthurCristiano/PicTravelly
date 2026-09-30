package com.app.pictravelly.feature.map

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.app.pictravelly.core.design.theme.PicTravellyTheme
import com.app.pictravelly.core.map.components.PicTravellySelectedSpotFloatingCard
import com.app.pictravelly.core.map.components.expandedMap.PicTravellySmartMapView
import com.app.pictravelly.core.map.model.MapMarkerData
import com.app.pictravelly.feature.map.components.MapHeaderChip

/**
 * TELA: 100% Visual. Não conhece a ViewModel, apenas estados e callbacks.
 */
@Composable
fun MapScreen(
    uiState: MapUiState,
    contentPadding: PaddingValues,
    onCloseSelection: () -> Unit,
    onMarkerClick: (MapMarkerData) -> Unit,
    onNavigateToSpotDetail: (Long) -> Unit,
    onFetchLocationRequested: () -> Unit,
    modifier: Modifier = Modifier
) {
    Box(modifier = modifier.fillMaxSize()) {
        PicTravellySmartMapView(
            latitude = uiState.focusLatitude,
            longitude = uiState.focusLongitude,
            markers = uiState.markers,
            onClose = onCloseSelection,
            onMarkerClick = onMarkerClick,
            showCloseButton = uiState.selectedSpot != null,
            onFetchLocationRequested = onFetchLocationRequested,
            // Injeção do Cabeçalho Superior
            topContent = {
                MapHeaderChip(
                    spotsCount = uiState.spots.size,
                    modifier = Modifier
                        .align(Alignment.TopCenter)
                        .padding(top = contentPadding.calculateTopPadding() + 12.dp)
                )
            },
            // Injeção do Card Flutuante via Slot API
            bottomContent = {
                PicTravellySelectedSpotFloatingCard(
                    selectedSpot = uiState.selectedSpot,
                    onNavigateToDetail = onNavigateToSpotDetail,
                    modifier = Modifier
                        .align(Alignment.BottomCenter)
                        .padding(bottom = contentPadding.calculateBottomPadding())
                )
            }
        )
    }
}

@Preview(showBackground = true)
@Composable
private fun MapScreenPreview() {
    PicTravellyTheme {
        MapScreen(
            uiState = MapUiState(),
            contentPadding = PaddingValues(0.dp),
            onCloseSelection = {},
            onMarkerClick = {},
            onNavigateToSpotDetail = {},
            modifier = Modifier,
            onFetchLocationRequested = {}
        )
    }
}