package com.app.pictravelly.feature.spot_form.components

import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.MyLocation
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.filled.TouchApp
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.app.pictravelly.core.design.components.PicTravellyCard
import com.app.pictravelly.core.map.MapMarkerData
import com.app.pictravelly.core.map.PicTravellyMap
import com.app.pictravelly.feature.spot_form.SpotFormUiState


@Composable
fun SpotLocationCard(
    uiState: SpotFormUiState,
    onUseMyLocation: () -> Unit,
    onSearchAddress: () -> Unit,
    onPickOnMap: (Double, Double) -> Unit
) {
    PicTravellyCard(
        modifier = Modifier.fillMaxWidth(),
        containerColor = MaterialTheme.colorScheme.surface
    ) {
        Column(modifier = Modifier.padding(14.dp)) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(
                    Icons.Default.MyLocation,
                    contentDescription = null,
                    tint = MaterialTheme.colorScheme.primary,
                    modifier = Modifier.size(18.dp)
                )
                Spacer(modifier = Modifier.width(8.dp))
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        "Localização do Ponto",
                        style = MaterialTheme.typography.titleSmall,
                        fontWeight = FontWeight.Bold
                    )
                    Text(
                        "Lat: %.5f, Lng: %.5f".format(uiState.latitude, uiState.longitude),
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
                if (uiState.isLocatingDevice) {
                    CircularProgressIndicator(
                        modifier = Modifier.size(20.dp),
                        strokeWidth = 2.dp,
                        color = MaterialTheme.colorScheme.primary
                    )
                }
            }

            Spacer(modifier = Modifier.height(12.dp))

            // Controle local de Zoom do card. Não precisa persistir no global.
            var formMapZoom by remember(uiState.isUsingFallbackLocation) {
                mutableFloatStateOf(if (uiState.isUsingFallbackLocation) 11f else 16f)
            }

            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(200.dp)
                    .clip(RoundedCornerShape(14.dp))
                    .border(
                        1.dp,
                        MaterialTheme.colorScheme.outlineVariant,
                        RoundedCornerShape(14.dp)
                    )
            ) {
                PicTravellyMap(
                    latitude = uiState.latitude,
                    longitude = uiState.longitude,
                    zoom = formMapZoom,
                    engine = uiState.mapEngine,
                    markers = listOf(
                        MapMarkerData(
                            id = 0L,
                            title = uiState.title.ifBlank { "Ponto escolhido" },
                            snippet = uiState.locationName,
                            latitude = uiState.latitude,
                            longitude = uiState.longitude
                        )
                    ),
                    onMarkerClick = {},
                    isInteractive = true,
                    onLocationPick = onPickOnMap,
                    onZoomChange = {
                        formMapZoom = it
                    }, // Atualiza o zoom local para os botões do mapa funcionarem
                    modifier = Modifier.fillMaxSize()
                )
            }

            Spacer(modifier = Modifier.height(8.dp))

            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(
                    Icons.Default.TouchApp,
                    contentDescription = null,
                    tint = MaterialTheme.colorScheme.outline,
                    modifier = Modifier.size(14.dp)
                )
                Spacer(modifier = Modifier.width(4.dp))
                Text(
                    "Toque no mapa para ajustar o ponto exato",
                    style = MaterialTheme.typography.labelSmall,
                    color = MaterialTheme.colorScheme.outline
                )
            }

            Spacer(modifier = Modifier.height(8.dp))

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(4.dp)
            ) {
                TextButton(onClick = onUseMyLocation, enabled = !uiState.isLocatingDevice) {
                    Icon(
                        Icons.Default.MyLocation,
                        contentDescription = null,
                        modifier = Modifier.size(16.dp)
                    )
                    Spacer(modifier = Modifier.width(6.dp))
                    Text("Minha posição")
                }
                TextButton(onClick = onSearchAddress, enabled = !uiState.isResolvingAddress) {
                    Icon(
                        Icons.Default.Search,
                        contentDescription = null,
                        modifier = Modifier.size(16.dp)
                    )
                    Spacer(modifier = Modifier.width(6.dp))
                    Text("Buscar endereço")
                }
            }
        }
    }
}