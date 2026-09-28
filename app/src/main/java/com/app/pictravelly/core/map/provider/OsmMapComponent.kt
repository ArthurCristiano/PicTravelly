package com.app.pictravelly.core.map.provider

import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberUpdatedState
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.viewinterop.AndroidView
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleEventObserver
import androidx.lifecycle.compose.LocalLifecycleOwner
import com.app.pictravelly.core.map.MapMarkerData
import org.osmdroid.config.Configuration
import org.osmdroid.events.MapEventsReceiver
import org.osmdroid.events.MapListener
import org.osmdroid.events.ScrollEvent
import org.osmdroid.events.ZoomEvent
import org.osmdroid.tileprovider.tilesource.TileSourceFactory
import org.osmdroid.util.GeoPoint
import org.osmdroid.views.MapView
import org.osmdroid.views.overlay.FolderOverlay
import org.osmdroid.views.overlay.MapEventsOverlay
import org.osmdroid.views.overlay.Marker

@Composable
fun OsmMapComponent(
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
    val context = LocalContext.current
    val lifecycleOwner = LocalLifecycleOwner.current

    // Garante que lambdas atualizadas sejam chamadas dentro dos listeners do OSM
    val currentOnMapClick by rememberUpdatedState(onMapClick)
    val currentOnLocationPick by rememberUpdatedState(onLocationPick)
    val currentOnMarkerClick by rememberUpdatedState(onMarkerClick)
    val currentOnZoomChange by rememberUpdatedState(onZoomChange)

    remember {
        Configuration.getInstance().userAgentValue = context.packageName
        true
    }

    // Pasta isolada para gerenciar os pinos sem corromper as camadas base do mapa
    val markerFolder = remember { FolderOverlay() }

    val mapView = remember {
        MapView(context).apply {
            setTileSource(TileSourceFactory.MAPNIK)
            setBuiltInZoomControls(false) // Desliga botões feios nativos
            setMultiTouchControls(true)   // Liga a pinça do usuário

            controller.setZoom(zoom.toDouble())
            controller.setCenter(GeoPoint(latitude, longitude))

            // Interceptador de toques no mapa instanciado UMA ÚNICA VEZ
            val eventsReceiver = object : MapEventsReceiver {
                override fun singleTapConfirmedHelper(p: GeoPoint?): Boolean {
                    currentOnMapClick?.invoke()
                    if (p != null) currentOnLocationPick?.invoke(p.latitude, p.longitude)
                    return true
                }

                override fun longPressHelper(p: GeoPoint?): Boolean = false
            }
            overlays.add(MapEventsOverlay(eventsReceiver))
            overlays.add(markerFolder) // Adiciona a pasta de pinos ao mapa

            addMapListener(object : MapListener {
                override fun onScroll(event: ScrollEvent?): Boolean = false
                override fun onZoom(event: ZoomEvent?): Boolean {
                    event?.let {
                        currentOnZoomChange?.invoke(it.zoomLevel.toFloat())
                    }
                    return true
                }
            })
        }
    }

    // Controle inteligente da Câmera (Posição)
    LaunchedEffect(latitude, longitude) {
        // Move o mapa apenas se a instrução vier de fora (ex: botão de GPS), animando suavemente
        mapView.controller.animateTo(GeoPoint(latitude, longitude))
    }

    // Controle inteligente do Zoom (Botões customizados)
    LaunchedEffect(zoom) {
        // Previne loop infinito se o zoom nativo do OSM já atingiu o valor
        if (mapView.zoomLevelDouble != zoom.toDouble()) {
            mapView.controller.zoomTo(zoom.toDouble())
        }
    }

    DisposableEffect(lifecycleOwner) {
        val observer = LifecycleEventObserver { _, event ->
            when (event) {
                Lifecycle.Event.ON_RESUME -> mapView.onResume()
                Lifecycle.Event.ON_PAUSE -> mapView.onPause()
                else -> Unit
            }
        }
        lifecycleOwner.lifecycle.addObserver(observer)
        onDispose {
            lifecycleOwner.lifecycle.removeObserver(observer)
            mapView.onDetach()
        }
    }

    AndroidView(
        factory = { mapView },
        modifier = modifier,
        update = { map ->
            // Apenas liga/desliga a interatividade sem resetar o mapa
            map.setMultiTouchControls(isInteractive)

            // O update cuida EXCLUSIVAMENTE de desenhar os pinos dentro da pasta blindada
            markerFolder.items.clear()

            markers.forEach { markerData ->
                val marker = Marker(map).apply {
                    position = GeoPoint(markerData.latitude, markerData.longitude)
                    title = markerData.title
                    snippet = markerData.snippet
                    setAnchor(Marker.ANCHOR_CENTER, Marker.ANCHOR_BOTTOM)
                    setOnMarkerClickListener { _, _ ->
                        currentOnMarkerClick(markerData)
                        true
                    }
                }
                markerFolder.add(marker)
            }

            map.invalidate()
        }
    )
}