package com.app.pictravelly.core.map.provider

import android.annotation.SuppressLint
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberUpdatedState
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import com.app.pictravelly.core.database.model.settings.GoogleMapType
import com.app.pictravelly.core.map.model.MapMarkerData
import com.app.pictravelly.core.map.model.MarkerType
import com.google.android.gms.maps.CameraUpdateFactory
import com.google.android.gms.maps.model.BitmapDescriptorFactory
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
    zoom: Float, // <--- NECESSÁRIO para botões e estado inicial
    markers: List<MapMarkerData>,
    onMarkerClick: (MapMarkerData) -> Unit,
    modifier: Modifier = Modifier,
    isInteractive: Boolean = true,
    onMapClick: (() -> Unit)? = null,
    onLocationPick: ((Double, Double) -> Unit)? = null,
    onZoomChange: ((Float) -> Unit)? = null, // <--- ADICIONADO PARA SALVAR A PINÇA
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

    // TRAVA DE SEGURANÇA: Impede que o cache inicial do Google Maps sobrescreva o DataStore
    var skipFirstSync by remember {
        mutableStateOf(
            true
        )
    }

    // 1. PAI -> FILHO (Botões +/- ou Banco de Dados): Anima se a ViewModel mandou
    LaunchedEffect(zoom) {
        if (abs(cameraPositionState.position.zoom - zoom) > 0.1f) {
            cameraPositionState.animate(CameraUpdateFactory.zoomTo(zoom))
        }
    }

    // 2. FILHO -> PAI (Pinça do Usuário): Salva ao final do movimento
    LaunchedEffect(cameraPositionState.isMoving) {
        // Ignora a montagem inicial da tela para não bugar ao trocar de abas
        if (skipFirstSync) {
            skipFirstSync = false
            return@LaunchedEffect
        }

        // Só salva quando a câmera PARAR completamente
        if (!cameraPositionState.isMoving) {
            val currentCameraZoom = cameraPositionState.position.zoom
            if (abs(currentCameraZoom - zoom) > 0.1f) {
                currentOnZoomChange?.invoke(currentCameraZoom)
            }
        }
    }

    // 3. Centralização da Câmera (Coordenadas ou Botão de Foco)
    LaunchedEffect(latitude, longitude, centerTrigger) {
        val currentTarget = cameraPositionState.position.target
        val latDiff = abs(currentTarget.latitude - latitude)
        val lonDiff = abs(currentTarget.longitude - longitude)

        if (latDiff > 0.0001 || lonDiff > 0.0001 || centerTrigger > 0) {
            cameraPositionState.animate(
                CameraUpdateFactory.newLatLngZoom(
                    LatLng(latitude, longitude),
                    cameraPositionState.position.zoom
                )
            )
        }
    }

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
            zoomGesturesEnabled = isInteractive
        ),
        onMapClick = { latLng ->
            currentOnMapClick?.invoke()
            currentOnLocationPick?.invoke(latLng.latitude, latLng.longitude)
        },
        properties = MapProperties(
            mapType = sdkGoogleMapType,
        ),
    ) {
        markers.forEach { markerData ->

            // CORREÇÃO CRUCIAL DAS CORES: Usando constantes HUE do próprio Google Maps
            val icon = when (markerData.type) {
                MarkerType.USER_LOCATION -> BitmapDescriptorFactory.defaultMarker(
                    BitmapDescriptorFactory.HUE_AZURE
                )

                MarkerType.TOURIST_SPOT -> BitmapDescriptorFactory.defaultMarker(
                    BitmapDescriptorFactory.HUE_RED
                )
            }

            Marker(
                state = MarkerState(position = LatLng(markerData.latitude, markerData.longitude)),
                title = markerData.title,
                snippet = markerData.snippet,
                icon = icon,
                onClick = {
                    if (markerData.type == MarkerType.TOURIST_SPOT) {
                        currentOnMarkerClick(markerData)
                    }
                    true
                }
            )
        }
    }
}