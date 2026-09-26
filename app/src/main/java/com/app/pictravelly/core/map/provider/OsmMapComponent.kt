package com.app.pictravelly.core.map.provider

import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.viewinterop.AndroidView
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleEventObserver
import androidx.lifecycle.compose.LocalLifecycleOwner
import com.app.pictravelly.core.map.MapMarkerData
import org.osmdroid.config.Configuration
import org.osmdroid.events.MapEventsReceiver
import org.osmdroid.tileprovider.tilesource.TileSourceFactory
import org.osmdroid.util.GeoPoint
import org.osmdroid.views.MapView
import org.osmdroid.views.overlay.MapEventsOverlay
import org.osmdroid.views.overlay.Marker

/**
 * Componente de mapa baseado no OpenStreetMap (osmdroid).
 * 100% livre, gratuito, sem exigência de chave de API e nativamente offline-friendly.
 */
@Composable
fun OsmMapComponent(
    latitude: Double,
    longitude: Double,
    zoom: Double,
    markers: List<MapMarkerData>,
    onMarkerClick: (MapMarkerData) -> Unit,
    modifier: Modifier = Modifier,
    isInteractive: Boolean = true,
    onMapClick: (() -> Unit)? = null,
    onLocationPick: ((Double, Double) -> Unit)? = null
) {
    val context = LocalContext.current
    val lifecycleOwner = LocalLifecycleOwner.current

    // Configuração obrigatória do User-Agent da aplicação para os servidores OpenStreetMap
    remember {
        Configuration.getInstance().userAgentValue = context.packageName
        true
    }

    val mapView = remember {
        MapView(context).apply {
            setTileSource(TileSourceFactory.MAPNIK)
            setMultiTouchControls(isInteractive)
            controller.setZoom(zoom)
            controller.setCenter(GeoPoint(latitude, longitude))
        }
    }

    // Tratamento correto do ciclo de vida do MapView
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
            map.setMultiTouchControls(isInteractive)
            map.controller.setCenter(GeoPoint(latitude, longitude))

            // Limpa overlays anteriores para sincronizar marcadores
            map.overlays.clear()

            // Listener de toque no mapa: expande o card e/ou escolhe a coordenada
            if (onMapClick != null || onLocationPick != null) {
                val eventsReceiver = object : MapEventsReceiver {
                    override fun singleTapConfirmedHelper(p: GeoPoint?): Boolean {
                        onMapClick?.invoke()
                        if (p != null) onLocationPick?.invoke(p.latitude, p.longitude)
                        return true
                    }
                    override fun longPressHelper(p: GeoPoint?): Boolean = false
                }
                map.overlays.add(MapEventsOverlay(eventsReceiver))
            }

            // Renderiza os marcadores dos locais cadastrados
            markers.forEach { markerData ->
                val marker = Marker(map).apply {
                    position = GeoPoint(markerData.latitude, markerData.longitude)
                    title = markerData.title
                    snippet = markerData.snippet
                    setAnchor(Marker.ANCHOR_CENTER, Marker.ANCHOR_BOTTOM)
                    setOnMarkerClickListener { _, _ ->
                        onMarkerClick(markerData)
                        true
                    }
                }
                map.overlays.add(marker)
            }

            map.invalidate()
        }
    )
}
