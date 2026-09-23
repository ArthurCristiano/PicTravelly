package com.app.pictravelly.core.map.provider

import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.ui.Modifier
import com.app.pictravelly.core.map.MapMarkerData
import com.google.android.gms.maps.model.CameraPosition
import com.google.android.gms.maps.model.LatLng
import com.google.maps.android.compose.GoogleMap
import com.google.maps.android.compose.MapUiSettings
import com.google.maps.android.compose.Marker
import com.google.maps.android.compose.MarkerState
import com.google.maps.android.compose.rememberCameraPositionState

/**
 * Componente de mapa baseado no Google Maps SDK.
 */
@Composable
fun GoogleMapComponent(
    latitude: Double,
    longitude: Double,
    zoom: Double,
    markers: List<MapMarkerData>,
    onMarkerClick: (MapMarkerData) -> Unit,
    modifier: Modifier = Modifier,
    isInteractive: Boolean = true,
    onMapClick: (() -> Unit)? = null
) {
    val initialLatLng = LatLng(latitude, longitude)
    val cameraPositionState = rememberCameraPositionState {
        position = CameraPosition.fromLatLngZoom(initialLatLng, zoom.toFloat())
    }

    LaunchedEffect(latitude, longitude) {
        cameraPositionState.position = CameraPosition.fromLatLngZoom(
            LatLng(latitude, longitude),
            zoom.toFloat()
        )
    }

    GoogleMap(
        modifier = modifier,
        cameraPositionState = cameraPositionState,
        uiSettings = MapUiSettings(
            zoomControlsEnabled = isInteractive,
            compassEnabled = isInteractive,
            myLocationButtonEnabled = isInteractive,
            scrollGesturesEnabled = isInteractive,
            zoomGesturesEnabled = isInteractive
        ),
        onMapClick = {
            onMapClick?.invoke()
        }
    ) {
        markers.forEach { markerData ->
            Marker(
                state = MarkerState(position = LatLng(markerData.latitude, markerData.longitude)),
                title = markerData.title,
                snippet = markerData.snippet,
                onClick = {
                    onMarkerClick(markerData)
                    true
                }
            )
        }
    }
}
