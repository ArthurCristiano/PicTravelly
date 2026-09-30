package com.app.pictravelly.core.map.components.expandedMap

import android.Manifest
import android.content.Intent
import android.provider.Settings
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxScope
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.app.pictravelly.core.database.model.settings.GoogleMapType
import com.app.pictravelly.core.database.model.settings.MapEngineType
import com.app.pictravelly.core.design.components.DockDestination
import com.app.pictravelly.core.design.components.PicTravellyLocationWarningBanner
import com.app.pictravelly.core.di.AppViewModelProvider
import com.app.pictravelly.core.location.model.LocationStatus
import com.app.pictravelly.core.location.rememberLocationStatus
import com.app.pictravelly.core.map.MapConfigViewModel
import com.app.pictravelly.core.map.model.MapMarkerData

@Composable
fun PicTravellySmartMapView(
    latitude: Double,
    longitude: Double,
    markers: List<MapMarkerData>,
    onClose: () -> Unit,
    onMarkerClick: (MapMarkerData) -> Unit,
    modifier: Modifier = Modifier,
    showCloseButton: Boolean = true,
    topContent: @Composable (BoxScope.() -> Unit)? = null,
    bottomContent: @Composable (BoxScope.() -> Unit)? = null,
    onFetchLocationRequested: () -> Unit = {}, // <--- Callback para a ViewModel buscar a localização
    mapConfigViewModel: MapConfigViewModel = viewModel(factory = AppViewModelProvider.Factory)
) {
    val mapSettings by mapConfigViewModel.mapSettings.collectAsState()
    val centerTrigger by mapConfigViewModel.centerTrigger.collectAsState()

    // 1. Instancia o rastreador de estado do GPS
    val locationStatus by rememberLocationStatus()
    val context = LocalContext.current

    // 2. Launcher para pedir permissão no Android
    val permissionLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.RequestMultiplePermissions()
    ) { permissions ->
        // Se foi concedido, o rememberLocationStatus() vai notar sozinho e mudar para READY
    }

    // 3. O SEGREDO: Se o usuário acabou de ligar o GPS ou deu permissão, busca a coordenada real!
    LaunchedEffect(locationStatus) {
        if (locationStatus == LocationStatus.READY) {
            onFetchLocationRequested()
        }
    }

    if (mapSettings == null) {
        Box(modifier = modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
            CircularProgressIndicator()
        }
        return
    }

    Box(modifier = modifier.fillMaxSize()) {
        PicTravellyExpandedMapView(
            latitude = latitude,
            longitude = longitude,
            engine = mapSettings!!.mapEngine ?: MapEngineType.OSM,
            zoom = mapSettings!!.lastZoom ?: 13f,
            googleMapType = mapSettings!!.googleMapType ?: GoogleMapType.NORMAL,
            centerTrigger = centerTrigger,
            markers = markers,
            showCloseButton = showCloseButton,
            onClose = onClose,
            onMarkerClick = onMarkerClick,
            onZoomChange = mapConfigViewModel::updateZoom,
            onEngineChange = mapConfigViewModel::updateMapEngine,
            onMapTypeChange = mapConfigViewModel::updateGoogleMapType,
            onCenterOnUser = mapConfigViewModel::triggerCenterOnUser,
            topContent = topContent,
            bottomContent = bottomContent,
            modifier = Modifier.fillMaxSize()
        )

        // 4. Injeta o Banner no topo da tela (abaixo da status bar)
        PicTravellyLocationWarningBanner(
            status = locationStatus,
            onRequestPermission = {
                permissionLauncher.launch(
                    arrayOf(
                        Manifest.permission.ACCESS_FINE_LOCATION,
                        Manifest.permission.ACCESS_COARSE_LOCATION
                    )
                )
            },
            onOpenGpsSettings = {
                context.startActivity(Intent(Settings.ACTION_LOCATION_SOURCE_SETTINGS))
            },
            modifier = Modifier
                .align(Alignment.TopCenter)
                .padding(top = 90.dp) // Fica logo abaixo dos botões flutuantes de fechar/engrenagem
        )
    }
}