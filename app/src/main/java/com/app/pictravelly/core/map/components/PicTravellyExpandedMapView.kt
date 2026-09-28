package com.app.pictravelly.core.map.components

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxScope
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Close
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import com.app.pictravelly.core.database.model.settings.MapEngineType
import com.app.pictravelly.core.map.MapMarkerData
import com.app.pictravelly.core.map.PicTravellyMap

/**
 * Visualização do mapa em tela cheia (Expandido).
 * 100% Agnostico: Usa Slot API para receber cards customizados de outras telas.
 */
@Composable
fun PicTravellyExpandedMapView(
    latitude: Double,
    longitude: Double,
    engine: MapEngineType, // OBRIGATÓRIO
    zoom: Float,           // OBRIGATÓRIO
    markers: List<MapMarkerData>,
    onClose: () -> Unit,
    onMarkerClick: (MapMarkerData) -> Unit,
    onZoomChange: (Float) -> Unit, // OBRIGATÓRIO para funcionar os botões
    modifier: Modifier = Modifier,
    showCloseButton: Boolean = true,
    topContent: @Composable (BoxScope.() -> Unit)? = null,
    bottomContent: @Composable (BoxScope.() -> Unit)? = null // SLOT API
) {
    Box(
        modifier = modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background)
    ) {
        PicTravellyMap(
            latitude = latitude,
            longitude = longitude,
            zoom = zoom,
            engine = engine,
            markers = markers,
            onMarkerClick = onMarkerClick,
            onZoomChange = onZoomChange,
            modifier = Modifier.fillMaxSize(),
            isInteractive = true
        )

        // Injeta conteúdo customizado no topo (se existir)
        topContent?.let { it() }

        if (showCloseButton) {
            Box(
                modifier = Modifier
                    .align(Alignment.TopEnd)
                    .statusBarsPadding()
                    .padding(16.dp)
                    .size(48.dp)
                    .clip(CircleShape)
                    .background(MaterialTheme.colorScheme.surface.copy(alpha = 0.90f))
                    .border(1.dp, Color.White.copy(alpha = 0.3f), CircleShape)
                    .clickable(onClick = onClose),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = Icons.Default.Close,
                    contentDescription = "Recolher Mapa",
                    tint = MaterialTheme.colorScheme.onSurface
                )
            }
        }

        // Injeta conteúdo customizado na base do mapa (se existir)
        bottomContent?.let { it() }
    }
}