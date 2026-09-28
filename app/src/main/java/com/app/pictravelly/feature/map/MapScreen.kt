package com.app.pictravelly.feature.map

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.app.pictravelly.core.design.theme.PicTravellyTheme
import com.app.pictravelly.core.map.MapMarkerData
import com.app.pictravelly.core.map.components.PicTravellyExpandedMapView
import com.app.pictravelly.core.map.components.PicTravellySelectedSpotFloatingCard
import com.app.pictravelly.feature.map.components.MapHeaderChip

@Composable
fun MapScreen(
    uiState: MapUiState,
    contentPadding: PaddingValues,
    onCloseSelection: () -> Unit,
    onMarkerClick: (MapMarkerData) -> Unit,
    onZoomChange: (Float) -> Unit,
    onNavigateToSpotDetail: (Long) -> Unit,
    modifier: Modifier = Modifier
) {
    Box(modifier = modifier.fillMaxSize()) {
        if (uiState.isLoadingSettings) {
            Box(
                modifier = Modifier.fillMaxSize(),
                contentAlignment = Alignment.Center
            ) {
                CircularProgressIndicator(color = MaterialTheme.colorScheme.primary)
            }
        } else {
            PicTravellyExpandedMapView(
                latitude = uiState.focusLatitude,
                longitude = uiState.focusLongitude,
                engine = uiState.mapEngine,
                zoom = uiState.mapZoom,
                markers = uiState.markers,
                onClose = onCloseSelection,
                onMarkerClick = onMarkerClick,
                onZoomChange = onZoomChange,
                showCloseButton = uiState.selectedSpot != null,
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
                        modifier = Modifier.align(Alignment.BottomCenter)
                    )
                }
            )
        }
    }
}

@Preview(showBackground = true)
@Composable
private fun MapScreenView() {
    PicTravellyTheme {
        MapScreen(
            uiState = MapUiState(),
            contentPadding = PaddingValues(0.dp),
            onCloseSelection = {},
            onMarkerClick = {},
            onZoomChange = {},
            onNavigateToSpotDetail = { Long },
            modifier = Modifier
        )
    }
}