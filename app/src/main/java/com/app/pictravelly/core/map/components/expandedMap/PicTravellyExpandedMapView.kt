package com.app.pictravelly.core.map.components.expandedMap

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxScope
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.MyLocation
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import androidx.compose.ui.zIndex
import com.app.pictravelly.core.database.model.settings.GoogleMapType
import com.app.pictravelly.core.database.model.settings.MapEngineType
import com.app.pictravelly.core.map.PicTravellyMap
import com.app.pictravelly.core.map.components.MapSettingsDialog
import com.app.pictravelly.core.map.components.mapLegend.MapLegendCard
import com.app.pictravelly.core.map.model.MapMarkerData

@Composable
fun PicTravellyExpandedMapView(
    latitude: Double,
    longitude: Double,
    engine: MapEngineType,
    zoom: Float,
    markers: List<MapMarkerData>,
    onClose: () -> Unit,
    onMarkerClick: (MapMarkerData) -> Unit,
    onZoomChange: (Float) -> Unit,
    googleMapType: GoogleMapType,
    onEngineChange: ((MapEngineType) -> Unit)? = null,
    onMapTypeChange: ((GoogleMapType) -> Unit)? = null,
    onCenterOnUser: (() -> Unit)? = null,
    centerTrigger: Int = 0,
    modifier: Modifier = Modifier,
    showCloseButton: Boolean = true,
    onMapClick: (() -> Unit)? = null,
    topContent: @Composable (BoxScope.() -> Unit)? = null,
    bottomContent: @Composable (BoxScope.() -> Unit)? = null,
    locationWarningBanner: @Composable (() -> Unit)? = null
) {
    var ephemeralZoom by remember(zoom) { mutableFloatStateOf(zoom) }
    var showSettingsDialog by remember { mutableStateOf(false) }

    DisposableEffect(Unit) {
        onDispose {
            if (ephemeralZoom != zoom) {
                onZoomChange(ephemeralZoom)
            }
        }
    }

    Box(
        modifier = modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background)
    ) {
        PicTravellyMap(
            latitude = latitude,
            longitude = longitude,
            zoom = ephemeralZoom,
            markers = markers,
            onMarkerClick = onMarkerClick,
            onMapClick = onMapClick,
            onZoomChange = { newZoom -> ephemeralZoom = newZoom },
            modifier = Modifier.fillMaxSize(),
            isInteractive = true,
            engine = engine,
            googleMapType = googleMapType,
            centerTrigger = centerTrigger
        )

        MapLegendCard(
            markers = markers,
            onMarkerClick = onMarkerClick,
            onCenterOnUser = onCenterOnUser,
            modifier = Modifier
                .align(Alignment.TopStart)
                .statusBarsPadding()
                .padding(16.dp)
                .zIndex(20f)
        )

        Row(
            modifier = Modifier
                .align(Alignment.TopEnd)
                .statusBarsPadding()
                .padding(16.dp)
                .zIndex(10f),
            horizontalArrangement = Arrangement.spacedBy(8.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            if (onCenterOnUser != null) {
                Box(
                    modifier = Modifier
                        .size(48.dp)
                        .clip(CircleShape)
                        .background(MaterialTheme.colorScheme.surface.copy(alpha = 0.90f))
                        .border(1.dp, Color.White.copy(alpha = 0.3f), CircleShape)
                        .clickable(onClick = onCenterOnUser),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = Icons.Default.MyLocation,
                        contentDescription = "Centralizar na minha localização",
                        tint = MaterialTheme.colorScheme.primary
                    )
                }
            }

            if (onEngineChange != null && onMapTypeChange != null) {
                Box(
                    modifier = Modifier
                        .size(48.dp)
                        .clip(CircleShape)
                        .background(MaterialTheme.colorScheme.surface.copy(alpha = 0.90f))
                        .border(1.dp, Color.White.copy(alpha = 0.3f), CircleShape)
                        .clickable(onClick = { showSettingsDialog = true }),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = Icons.Default.Settings,
                        contentDescription = "Configurações do Mapa",
                        tint = MaterialTheme.colorScheme.onSurface
                    )
                }
            }

            if (showCloseButton) {
                Box(
                    modifier = Modifier
                        .size(48.dp)
                        .clip(CircleShape)
                        .background(MaterialTheme.colorScheme.surface.copy(alpha = 0.90f))
                        .border(1.dp, Color.White.copy(alpha = 0.3f), CircleShape)
                        .clickable(onClick = {
                            if (ephemeralZoom != zoom) {
                                onZoomChange(ephemeralZoom)
                            }
                            onClose()
                        }),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = Icons.Default.Close,
                        contentDescription = "Fechar",
                        tint = MaterialTheme.colorScheme.onSurface
                    )
                }
            }
        }

        Column(
            modifier = Modifier
                .align(Alignment.TopCenter)
                .fillMaxWidth()
                .statusBarsPadding()
                .padding(top = 72.dp, start = 16.dp, end = 16.dp)
                .zIndex(5f),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            topContent?.let { content ->
                Box(contentAlignment = Alignment.Center) {
                    content()
                }
            }
            locationWarningBanner?.invoke()
        }

        bottomContent?.let { it() }
    }

    if (showSettingsDialog && onEngineChange != null && onMapTypeChange != null) {
        MapSettingsDialog(
            currentEngine = engine,
            currentMapType = googleMapType,
            onEngineChanged = onEngineChange,
            onMapTypeChanged = onMapTypeChange,
            onDismiss = { showSettingsDialog = false }
        )
    }
}
