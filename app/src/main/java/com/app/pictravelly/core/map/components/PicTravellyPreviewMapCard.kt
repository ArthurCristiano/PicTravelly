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
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.app.pictravelly.core.database.model.settings.MapEngineType
import com.app.pictravelly.core.design.components.PicTravellyCard
import com.app.pictravelly.core.design.theme.PicTravellyTheme
import com.app.pictravelly.core.map.MapMarkerData
import com.app.pictravelly.core.map.PicTravellyMap

/**
 * Card de mapa genérico reutilizável em todo o aplicativo.
 * A persistência do zoom e a escolha da engine são tratadas internamente pelo PicTravellyMap caso não fornecidas.
STATELESS: Recebe engine e zoom estritamente da tela que o hospeda.
 */
@Composable
fun PicTravellyPreviewMapCard(
    currentLatitude: Double,
    currentLongitude: Double,
    engine: MapEngineType,
    zoom: Float,
    markers: List<MapMarkerData>,
    onExpandClick: () -> Unit,
    onMarkerSelect: (Long) -> Unit,
    modifier: Modifier = Modifier
) {
    PicTravellyCard(
        modifier = modifier
            .fillMaxWidth()
            .height(260.dp),
        containerColor = MaterialTheme.colorScheme.surface
    ) {
        Box(modifier = Modifier.fillMaxSize()) {
            PicTravellyMap(
                latitude = currentLatitude,
                longitude = currentLongitude,
                zoom = zoom,
                engine = engine,
                markers = markers,
                onMarkerClick = { marker ->
                    onMarkerSelect(marker.id)
                },
                modifier = Modifier.fillMaxSize(),
                isInteractive = false, // CORREÇÃO: Miniaturas em listas NUNCA devem roubar o scroll da tela principal
                onMapClick = onExpandClick
            )

            // Botão de expandir no topo direito
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

            // Tag de exploração inferior
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

@Preview(showBackground = true)
@Composable
private fun PicTravellyPreviewMapCardPreview() {
    PicTravellyTheme {
        val sampleMarkers = listOf(
            MapMarkerData(
                id = 1L,
                title = "Ponto Turístico Central",
                snippet = "Um belo lugar para visitar",
                latitude = -23.55052,
                longitude = -46.633308
            )
        )
        PicTravellyPreviewMapCard(
            currentLatitude = -23.55052,
            currentLongitude = -46.633308,
            engine = MapEngineType.OSM, // Mock obrigatório para o Preview
            zoom = 14f,                 // Mock obrigatório para o Preview
            markers = sampleMarkers,
            onExpandClick = {},
            onMarkerSelect = {},
            modifier = Modifier.padding(16.dp)
        )
    }
}