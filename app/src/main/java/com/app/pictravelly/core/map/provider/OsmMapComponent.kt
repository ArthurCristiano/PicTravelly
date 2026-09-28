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
import com.app.pictravelly.core.database.model.settings.GoogleMapType
import com.app.pictravelly.core.map.MapMarkerData
import org.osmdroid.config.Configuration
import org.osmdroid.events.MapEventsReceiver
import org.osmdroid.events.MapListener
import org.osmdroid.events.ScrollEvent
import org.osmdroid.events.ZoomEvent
import org.osmdroid.tileprovider.tilesource.ITileSource
import org.osmdroid.tileprovider.tilesource.TileSourceFactory
import org.osmdroid.util.GeoPoint
import org.osmdroid.views.MapView
import org.osmdroid.views.overlay.FolderOverlay
import org.osmdroid.views.overlay.MapEventsOverlay
import org.osmdroid.views.overlay.Marker
import kotlin.math.abs

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
    onZoomChange: ((Float) -> Unit)? = null,
    googleMapType: GoogleMapType = GoogleMapType.NORMAL,
    centerTrigger: Int = 0
) {
    val context = LocalContext.current
    val lifecycleOwner = LocalLifecycleOwner.current

    val currentOnMapClick by rememberUpdatedState(onMapClick)
    val currentOnLocationPick by rememberUpdatedState(onLocationPick)
    val currentOnMarkerClick by rememberUpdatedState(onMarkerClick)
    val currentOnZoomChange by rememberUpdatedState(onZoomChange)

    remember {
        Configuration.getInstance().userAgentValue = context.packageName
        true
    }

    val markerFolder = remember { FolderOverlay() }

    // Mapeamento das camadas visuais (Tile Sources) para o OSM
    val tileSource: ITileSource = when (googleMapType) {
        GoogleMapType.NORMAL -> TileSourceFactory.MAPNIK
        GoogleMapType.SATELLITE, GoogleMapType.HYBRID -> TileSourceFactory.USGS_SAT
    }

    val mapView = remember {
        MapView(context).apply {
            setTileSource(tileSource)
            setBuiltInZoomControls(false)
            setMultiTouchControls(true)

            controller.setZoom(zoom.toDouble())
            controller.setCenter(GeoPoint(latitude, longitude))

            val eventsReceiver = object : MapEventsReceiver {
                override fun singleTapConfirmedHelper(p: GeoPoint?): Boolean {
                    currentOnMapClick?.invoke()
                    if (p != null) currentOnLocationPick?.invoke(p.latitude, p.longitude)
                    return true
                }

                override fun longPressHelper(p: GeoPoint?): Boolean = false
            }
            overlays.add(MapEventsOverlay(eventsReceiver))
            overlays.add(markerFolder)

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

    LaunchedEffect(latitude, longitude) {
        val currentCenter = mapView.mapCenter
        val latDiff = abs(currentCenter.latitude - latitude)
        val lonDiff = abs(currentCenter.longitude - longitude)

        if (latDiff > 0.0001 || lonDiff > 0.0001) {
            mapView.controller.animateTo(GeoPoint(latitude, longitude))
        }
    }

    LaunchedEffect(zoom) {
        if (abs(mapView.zoomLevelDouble - zoom.toDouble()) > 0.01) {
            mapView.controller.setZoom(zoom.toDouble())
        }
    }

    // Dispara a animação sempre que mudar lat/long OU o gatilho do botão for acionado
    LaunchedEffect(latitude, longitude, centerTrigger) {
        mapView.controller.animateTo(GeoPoint(latitude, longitude))
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
            // Atualiza a fonte do tile se o usuário mudou a configuração nas preferências
            if (map.tileProvider.tileSource.name() != tileSource.name()) {
                map.setTileSource(tileSource)
            }

            map.setMultiTouchControls(isInteractive)

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