package com.app.pictravelly.core.map.components

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Explore
import androidx.compose.material.icons.filled.Fullscreen
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import com.app.pictravelly.core.database.model.touristSpot.TouristSpotWithImages
import com.app.pictravelly.core.design.components.PicTravellyCard
import com.app.pictravelly.core.map.MapMarkerData
import com.app.pictravelly.core.map.PicTravellyMap
import com.app.pictravelly.feature.home.HomeUiState


/**
 * Card contendo o Google Map recolhido, com pins e centrado na localização atual.
 */
@Composable
fun HomeMapCard(
    uiState: HomeUiState,
    onExpandClick: () -> Unit,
    onSpotSelect: (TouristSpotWithImages) -> Unit,
    modifier: Modifier = Modifier
) {
    val markers = remember(uiState.spots) {
        uiState.spots.map { spotWithImages ->
            MapMarkerData(
                id = spotWithImages.spot.id,
                title = spotWithImages.spot.title,
                snippet = spotWithImages.spot.locationName,
                latitude = spotWithImages.spot.latitude,
                longitude = spotWithImages.spot.longitude
            )
        }
    }

    PicTravellyCard(
        modifier = modifier
            .fillMaxWidth()
            .height(260.dp),
        containerColor = MaterialTheme.colorScheme.surface
    ) {
        Box(modifier = Modifier.fillMaxSize()) {
            PicTravellyMap(
                latitude = uiState.currentLatitude,
                longitude = uiState.currentLongitude,
                zoom = 12.0,
                markers = markers,
                onMarkerClick = { marker ->
                    val selected = uiState.spots.firstOrNull { it.spot.id == marker.id }
                    if (selected != null) onSpotSelect(selected)
                },
                modifier = Modifier.fillMaxSize(),
                isInteractive = false,
                onMapClick = onExpandClick
            )

            // Botão One UI flutuante de expandir no canto superior direito
            Box(
                modifier = Modifier
                    .align(Alignment.TopEnd)
                    .padding(12.dp)
                    .clip(CircleShape)
                    .background(MaterialTheme.colorScheme.surface.copy(alpha = 0.88f))
                    .border(1.dp, Color.White.copy(alpha = 0.3f), CircleShape)
            ) {
                IconButton(onClick = onExpandClick) {
                    Icon(
                        imageVector = Icons.Default.Fullscreen,
                        contentDescription = "Expandir Mapa",
                        tint = MaterialTheme.colorScheme.primary
                    )
                }
            }

            // Tag indicativa inferior
            Box(
                modifier = Modifier
                    .align(Alignment.BottomStart)
                    .padding(12.dp)
                    .clip(RoundedCornerShape(8.dp))
                    .background(MaterialTheme.colorScheme.surface.copy(alpha = 0.85f))
                    .padding(horizontal = 10.dp, vertical = 6.dp)
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(
                        imageVector = Icons.Default.Explore,
                        contentDescription = null,
                        modifier = Modifier.size(16.dp),
                        tint = MaterialTheme.colorScheme.primary
                    )
                    Spacer(modifier = Modifier.width(6.dp))
                    Text(
                        text = "Toque para explorar o mapa",
                        style = MaterialTheme.typography.labelSmall,
                        color = MaterialTheme.colorScheme.onSurface
                    )
                }
            }
        }
    }
}