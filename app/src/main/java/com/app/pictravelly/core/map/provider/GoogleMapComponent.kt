package com.app.pictravelly.core.map.provider

import android.annotation.SuppressLint
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.rememberUpdatedState
import androidx.compose.runtime.snapshotFlow
import androidx.compose.ui.Modifier
import com.app.pictravelly.core.map.MapMarkerData
import com.google.android.gms.maps.CameraUpdateFactory
import com.google.android.gms.maps.model.CameraPosition
import com.google.android.gms.maps.model.LatLng
import com.google.maps.android.compose.GoogleMap
import com.google.maps.android.compose.MapUiSettings
import com.google.maps.android.compose.Marker
import com.google.maps.android.compose.MarkerState
import com.google.maps.android.compose.rememberCameraPositionState
import kotlinx.coroutines.flow.collectLatest
import kotlin.math.abs

/**
 * Componente de mapa baseado no Google Maps SDK.
 */
@SuppressLint("UnrememberedMutableState")
@Composable
fun GoogleMapComponent(
    latitude: Double,
    longitude: Double,
    zoom: Float,
    markers: List<MapMarkerData>,
    onMarkerClick: (MapMarkerData) -> Unit,
    modifier: Modifier = Modifier,
    isInteractive: Boolean = true,
    onMapClick: (() -> Unit)? = null,
    onLocationPick: ((Double, Double) -> Unit)? = null,
    onZoomChange: ((Float) -> Unit)? = null
) {
    // Blindagem de estado: Garante que as lambdas executadas nunca fiquem desatualizadas na memória
    val currentOnMapClick by rememberUpdatedState(onMapClick)
    val currentOnLocationPick by rememberUpdatedState(onLocationPick)
    val currentOnMarkerClick by rememberUpdatedState(onMarkerClick)
    val currentOnZoomChange by rememberUpdatedState(onZoomChange)

    val cameraPositionState = rememberCameraPositionState {
        position = CameraPosition.fromLatLngZoom(LatLng(latitude, longitude), zoom)
    }

    // Monitora mudanças de zoom feitas pelo usuário (gesto de pinça e toques duplos)
    LaunchedEffect(cameraPositionState) {
        snapshotFlow { cameraPositionState.position.zoom }
            .collectLatest { newZoom ->
                currentOnZoomChange?.invoke(newZoom)
            }
    }

    // CORREÇÃO: Anima a câmera suavemente quando a localização mudar (evita o teletransporte)
    LaunchedEffect(latitude, longitude) {
        cameraPositionState.animate(CameraUpdateFactory.newLatLng(LatLng(latitude, longitude)))
    }

    // Anima a câmera quando os botões customizados de zoom forem clicados na tela pai
    LaunchedEffect(zoom) {
        if (abs(cameraPositionState.position.zoom - zoom) > 0.1f) {
            cameraPositionState.animate(CameraUpdateFactory.zoomTo(zoom))
        }
    }

    GoogleMap(
        modifier = modifier,
        cameraPositionState = cameraPositionState,
        uiSettings = MapUiSettings(
            zoomControlsEnabled = false, // Desligado: Usamos nossos controles
            compassEnabled = isInteractive,
            myLocationButtonEnabled = false, // Desligado: Oculte a interface nativa para manter seu layout limpo
            scrollGesturesEnabled = isInteractive,
            zoomGesturesEnabled = isInteractive
        ),
        onMapClick = { latLng ->
            currentOnMapClick?.invoke()
            currentOnLocationPick?.invoke(latLng.latitude, latLng.longitude)
        }
    ) {
        markers.forEach { markerData ->
            Marker(
                state = MarkerState(position = LatLng(markerData.latitude, markerData.longitude)),
                title = markerData.title,
                snippet = markerData.snippet,
                onClick = {
                    currentOnMarkerClick(markerData)
                    true // Retorna true para desativar a centralização automática e InfoWindow nativa do Google
                }
            )
        }
    }
}