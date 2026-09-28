package com.app.pictravelly.core.map

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Remove
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.IconButtonDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.app.pictravelly.core.database.model.settings.MapEngineType
import com.app.pictravelly.core.database.model.settings.GoogleMapType
import com.app.pictravelly.core.map.provider.GoogleMapComponent
import com.app.pictravelly.core.map.provider.OsmMapComponent
import kotlin.math.roundToInt

/**
 * Componente Facade Universal de Mapa para o PicTravelly.
 * STATELESS: Não possui dependência de banco de dados ou ViewModel.
 */
@Composable
fun PicTravellyMap(
    latitude: Double,
    longitude: Double,
    engine: MapEngineType, // Obrigatório
    zoom: Float, // Obrigatório
    modifier: Modifier = Modifier,
    markers: List<MapMarkerData> = emptyList(),
    onMarkerClick: (MapMarkerData) -> Unit = {},
    isInteractive: Boolean = true,
    onMapClick: (() -> Unit)? = null,
    onLocationPick: ((Double, Double) -> Unit)? = null,
    onZoomChange: ((Float) -> Unit)? = null,
    googleMapType: GoogleMapType,
    centerTrigger: Int = 0
) {
    Box(modifier = modifier) {
        // Renderização do motor de mapa
        when (engine) {
            MapEngineType.OSM -> {
                OsmMapComponent(
                    latitude = latitude,
                    longitude = longitude,
                    zoom = zoom,
                    markers = markers,
                    onMarkerClick = onMarkerClick,
                    modifier = Modifier.matchParentSize(),
                    isInteractive = isInteractive,
                    onMapClick = onMapClick,
                    onLocationPick = onLocationPick,
                    onZoomChange = onZoomChange,
                    googleMapType = googleMapType,
                    centerTrigger = centerTrigger
                )
            }

            MapEngineType.GOOGLE_MAPS -> {
                GoogleMapComponent(
                    latitude = latitude,
                    longitude = longitude,
                    zoom = zoom,
                    markers = markers,
                    onMarkerClick = onMarkerClick,
                    modifier = Modifier.matchParentSize(),
                    isInteractive = isInteractive,
                    onMapClick = onMapClick,
                    onLocationPick = onLocationPick,
                    onZoomChange = onZoomChange,
                    googleMapType = googleMapType,
                    centerTrigger = centerTrigger
                )
            }
        }

        // Camada de Interface Customizada (Zoom)
        if (isInteractive) {
            Column(
                modifier = Modifier
                    .align(Alignment.CenterEnd)
                    .padding(end = 16.dp),
                verticalArrangement = Arrangement.spacedBy(8.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                // Indicador de percentual
                val zoomPercentage = ((zoom / 20f) * 100).roundToInt().coerceIn(0, 100)

                Box(
                    modifier = Modifier
                        .clip(RoundedCornerShape(8.dp))
                        .background(MaterialTheme.colorScheme.surface.copy(alpha = 0.8f))
                        .padding(horizontal = 6.dp, vertical = 4.dp)
                ) {
                    Text(
                        text = "$zoomPercentage%",
                        style = MaterialTheme.typography.labelSmall.copy(
                            fontSize = 10.sp,
                            fontWeight = FontWeight.Bold
                        ),
                        color = MaterialTheme.colorScheme.onSurface
                    )
                }

                // Botões disparam evento com coerção, a UI não guarda o valor
                IconButton(
                    onClick = { onZoomChange?.invoke((zoom + 1f).coerceAtMost(20f)) },
                    modifier = Modifier
                        .size(40.dp)
                        .clip(CircleShape),
                    colors = IconButtonDefaults.iconButtonColors(
                        containerColor = MaterialTheme.colorScheme.primary,
                        contentColor = Color.White
                    )
                ) {
                    Icon(Icons.Default.Add, contentDescription = "Mais Zoom")
                }

                IconButton(
                    onClick = { onZoomChange?.invoke((zoom - 1f).coerceAtLeast(1f)) },
                    modifier = Modifier
                        .size(40.dp)
                        .clip(CircleShape),
                    colors = IconButtonDefaults.iconButtonColors(
                        containerColor = MaterialTheme.colorScheme.surface.copy(alpha = 0.9f),
                        contentColor = MaterialTheme.colorScheme.primary
                    )
                ) {
                    Icon(Icons.Default.Remove, contentDescription = "Menos Zoom")
                }
            }
        }
    }
}