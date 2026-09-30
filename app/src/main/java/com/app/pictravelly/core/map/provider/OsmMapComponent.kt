package com.app.pictravelly.core.map.provider

import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberUpdatedState
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.toArgb
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.viewinterop.AndroidView
import androidx.core.content.ContextCompat
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleEventObserver
import androidx.lifecycle.compose.LocalLifecycleOwner
import com.app.pictravelly.core.database.model.settings.GoogleMapType
import com.app.pictravelly.core.map.model.MapMarkerData
import com.app.pictravelly.core.map.model.MarkerType
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

    val primaryColor = MaterialTheme.colorScheme.primary.toArgb()
    val secondaryColor = MaterialTheme.colorScheme.secondary.toArgb()

    remember {
        Configuration.getInstance().userAgentValue = context.packageName
        true
    }

    var activeMapView by remember { mutableStateOf<MapView?>(null) }

    // O OpenStreetMap opera com sua camada vetorial padrão mundial oficial (MAPNIK)
    val tileSource: ITileSource = TileSourceFactory.MAPNIK

    LaunchedEffect(zoom) {
        val map = activeMapView ?: return@LaunchedEffect
        if (abs(map.zoomLevelDouble - zoom.toDouble()) > 0.1) {
            map.controller.setZoom(zoom.toDouble())
        }
    }

    // ÚNICO GATILHO DE MOVIMENTO:
    // Dispara a animação sempre que mudar lat/long externamente OU o gatilho do botão for acionado
    LaunchedEffect(latitude, longitude, centerTrigger) {
        val map = activeMapView ?: return@LaunchedEffect
        val currentCenter = map.mapCenter
        val latDiff = abs(currentCenter.latitude - latitude)
        val lonDiff = abs(currentCenter.longitude - longitude)

        // Evita chamadas de animação desnecessárias se o mapa já estiver no lugar certo
        // (mas força a ida se o trigger de centralizar tiver sido acionado)
        if (latDiff > 0.0001 || lonDiff > 0.0001 || centerTrigger > 0) {
            map.controller.animateTo(GeoPoint(latitude, longitude))
        }
    }

    DisposableEffect(lifecycleOwner) {
        val observer = LifecycleEventObserver { _, event ->
            when (event) {
                Lifecycle.Event.ON_RESUME -> activeMapView?.onResume()
                Lifecycle.Event.ON_PAUSE -> activeMapView?.onPause()
                else -> Unit
            }
        }
        lifecycleOwner.lifecycle.addObserver(observer)
        onDispose {
            lifecycleOwner.lifecycle.removeObserver(observer)
        }
    }

    AndroidView(
        factory = { ctx ->
            MapView(ctx).apply {
                setTileSource(tileSource)
                setBuiltInZoomControls(false)
                setMultiTouchControls(isInteractive)

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

                addMapListener(object : MapListener {
                    override fun onScroll(event: ScrollEvent?): Boolean = false
                    override fun onZoom(event: ZoomEvent?): Boolean {
                        event?.let {
                            val currentZoom = zoomLevelDouble
                            val newZoom = it.zoomLevel
                            if (abs(currentZoom - newZoom) > 0.15) {
                                currentOnZoomChange?.invoke(newZoom.toFloat())
                            }
                        }
                        return true
                    }
                })

                activeMapView = this
            }
        },
        modifier = modifier,
        update = { map ->
            // Atualiza a fonte do tile se o usuário mudou a configuração nas preferências
            if (map.tileProvider.tileSource.name() != tileSource.name()) {
                map.setTileSource(tileSource)
            }

            map.setMultiTouchControls(isInteractive)

            // Remove marcadores anteriores de forma limpa
            map.overlays.removeAll { it is Marker }

            markers.forEach { markerData ->
                val marker = Marker(map).apply {
                    position = GeoPoint(markerData.latitude, markerData.longitude)
                    title = markerData.title
                    snippet = markerData.snippet
                    setAnchor(Marker.ANCHOR_CENTER, Marker.ANCHOR_BOTTOM)

                    // Customização Visual do OSM
                    when (markerData.type) {
                        MarkerType.USER_LOCATION -> {
                            val defaultIcon = ContextCompat.getDrawable(
                                context,
                                org.osmdroid.library.R.drawable.marker_default
                            )?.mutate()
                            defaultIcon?.setTint(primaryColor)
                            icon = defaultIcon
                        }

                        MarkerType.TOURIST_SPOT -> {
                            val defaultIcon = ContextCompat.getDrawable(
                                context,
                                org.osmdroid.library.R.drawable.marker_default
                            )?.mutate()
                            defaultIcon?.setTint(secondaryColor)
                            icon = defaultIcon
                        }
                    }

                    setOnMarkerClickListener { _, _ ->
                        if (markerData.type == MarkerType.TOURIST_SPOT) {
                            currentOnMarkerClick(markerData)
                        }
                        true
                    }
                }
                map.overlays.add(marker)
            }
            map.invalidate()
        },
        onRelease = { map ->
            activeMapView = null
            runCatching {
                map.onPause()
                map.onDetach()
            }
        }
    )
}