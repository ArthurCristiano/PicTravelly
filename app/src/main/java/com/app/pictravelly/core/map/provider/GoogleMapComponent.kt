package com.app.pictravelly.core.map.provider

import android.annotation.SuppressLint
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.rememberUpdatedState
import androidx.compose.ui.Modifier
import com.app.pictravelly.core.database.model.settings.GoogleMapType
import com.app.pictravelly.core.map.MapMarkerData
import com.google.android.gms.maps.CameraUpdateFactory
import com.google.android.gms.maps.model.CameraPosition
import com.google.android.gms.maps.model.LatLng
import com.google.maps.android.compose.GoogleMap
import com.google.maps.android.compose.MapProperties
import com.google.maps.android.compose.MapType
import com.google.maps.android.compose.MapUiSettings
import com.google.maps.android.compose.Marker
import com.google.maps.android.compose.MarkerState
import com.google.maps.android.compose.rememberCameraPositionState
import kotlin.math.abs

/**
 * Componente de mapa baseado no Google Maps SDK otimizado para gestos de pinça e arraste.
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
    onZoomChange: ((Float) -> Unit)? = null,
    googleMapType: GoogleMapType = GoogleMapType.NORMAL,
    centerTrigger: Int = 0,
) {
    val currentOnMapClick by rememberUpdatedState(onMapClick)
    val currentOnLocationPick by rememberUpdatedState(onLocationPick)
    val currentOnMarkerClick by rememberUpdatedState(onMarkerClick)
    val currentOnZoomChange by rememberUpdatedState(onZoomChange)

    val cameraPositionState = rememberCameraPositionState {
        position = CameraPosition.fromLatLngZoom(LatLng(latitude, longitude), zoom)
    }

    // CORREÇÃO CRUCIAL PARA A PINÇA:
    // Sincroniza o zoom externo (botões ou estado pai) sem brigar com o gesto do usuário na tela.
    LaunchedEffect(zoom) {
        if (!cameraPositionState.isMoving && abs(cameraPositionState.position.zoom - zoom) > 0.01f) {
            cameraPositionState.animate(CameraUpdateFactory.zoomTo(zoom))
        }
    }

    // Atualiza a posição central apenas se as coordenadas mudarem externamente
    LaunchedEffect(latitude, longitude) {
        val currentTarget = cameraPositionState.position.target
        if (abs(currentTarget.latitude - latitude) > 0.0001 || abs(currentTarget.longitude - longitude) > 0.0001) {
            if (!cameraPositionState.isMoving) {
                cameraPositionState.animate(
                    CameraUpdateFactory.newLatLngZoom(
                        LatLng(latitude, longitude),
                        cameraPositionState.position.zoom
                    )
                )
            }
        }
    }

    // LANNED EFFECT ATUALIZADO: Dispara tanto se mudar as coordenadas NORMAIS
    // quanto se o usuário clicar no botão de centralizar (centerTrigger muda).
    LaunchedEffect(latitude, longitude, centerTrigger) {
        if (!cameraPositionState.isMoving) {
            cameraPositionState.animate(
                CameraUpdateFactory.newLatLngZoom(
                    LatLng(latitude, longitude),
                    cameraPositionState.position.zoom
                )
            )
        }
    }

    // Mapeia o seu Enum customizado para o MapType oficial do Google Maps Compose
    val sdkGoogleMapType: MapType = when (googleMapType) {
        GoogleMapType.NORMAL -> MapType.NORMAL
        GoogleMapType.SATELLITE -> MapType.SATELLITE
        GoogleMapType.HYBRID -> MapType.HYBRID
    }

    GoogleMap(
        modifier = modifier,
        cameraPositionState = cameraPositionState,
        uiSettings = MapUiSettings(
            zoomControlsEnabled = false,
            compassEnabled = isInteractive,
            myLocationButtonEnabled = false,
            scrollGesturesEnabled = isInteractive,
            zoomGesturesEnabled = isInteractive // Garante explicitamente que a pinça está ativa
        ),
        onMapClick = { latLng ->
            currentOnMapClick?.invoke()
            currentOnLocationPick?.invoke(latLng.latitude, latLng.longitude)
        },
        properties = MapProperties(
            mapType = sdkGoogleMapType, // <--- APLICA O TIPO DO MAPA AQUI
        ),
    ) {
        markers.forEach { markerData ->
            Marker(
                state = MarkerState(position = LatLng(markerData.latitude, markerData.longitude)),
                title = markerData.title,
                snippet = markerData.snippet,
                onClick = {
                    currentOnMarkerClick(markerData)
                    true
                }
            )
        }
    }
}