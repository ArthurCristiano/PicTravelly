package com.app.pictravelly.core.map

import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import com.app.pictravelly.core.map.provider.GoogleMapComponent
import com.app.pictravelly.core.map.provider.OsmMapComponent

/**
 * Configuração global do motor de mapa ativo na aplicação.
 * Por padrão: OpenStreetMap (OSM) para desenvolvimento livre sem custos.
 */
object MapConfig {
    var activeEngine: MapEngineType = MapEngineType.OSM
}

/**
 * Componente Facade Universal de Mapa para o PicTravelly.
 * Isola completamente as telas da implementação da biblioteca de mapas.
 */
@Composable
fun PicTravellyMap(
    latitude: Double,
    longitude: Double,
    modifier: Modifier = Modifier,
    zoom: Double = 13.0,
    markers: List<MapMarkerData> = emptyList(),
    onMarkerClick: (MapMarkerData) -> Unit = {},
    isInteractive: Boolean = true,
    onMapClick: (() -> Unit)? = null,
    engine: MapEngineType = MapConfig.activeEngine
) {
    when (engine) {
        MapEngineType.OSM -> {
            OsmMapComponent(
                latitude = latitude,
                longitude = longitude,
                zoom = zoom,
                markers = markers,
                onMarkerClick = onMarkerClick,
                modifier = modifier,
                isInteractive = isInteractive,
                onMapClick = onMapClick
            )
        }
        MapEngineType.GOOGLE_MAPS -> {
            GoogleMapComponent(
                latitude = latitude,
                longitude = longitude,
                zoom = zoom,
                markers = markers,
                onMarkerClick = onMarkerClick,
                modifier = modifier,
                isInteractive = isInteractive,
                onMapClick = onMapClick
            )
        }
    }
}
