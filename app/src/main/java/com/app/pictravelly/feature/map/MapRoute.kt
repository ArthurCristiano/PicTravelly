package com.app.pictravelly.feature.map

import android.Manifest
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import com.app.pictravelly.core.location.LocationHelper
import com.app.pictravelly.core.location.LocationHelper.getCurrentLocation
import kotlinx.coroutines.launch

/**
 * ROTA: Responsável apenas pelas integrações com o SO (Permissões, GPS, ViewModel).
 */
@Composable
fun MapRoute(
    viewModel: MapViewModel,
    onNavigateToSpotDetail: (Long) -> Unit,
    modifier: Modifier = Modifier,
    contentPadding: PaddingValues = PaddingValues(0.dp)
) {
    val uiState by viewModel.uiState.collectAsState()
    val context = LocalContext.current
    val coroutineScope = rememberCoroutineScope()

    val permissionLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.RequestMultiplePermissions()
    ) { permissions ->
        val granted = permissions[Manifest.permission.ACCESS_FINE_LOCATION] == true ||
                permissions[Manifest.permission.ACCESS_COARSE_LOCATION] == true
        if (granted) {
            coroutineScope.launch {
                val (lat, lng) = getCurrentLocation(context)
                viewModel.updateCurrentLocation(lat, lng)
            }
        }
    }

    LaunchedEffect(Unit) {
        if (LocationHelper.hasLocationPermission(context)) {
            val (lat, lng) = getCurrentLocation(context)
            viewModel.updateCurrentLocation(lat, lng)
        } else {
            permissionLauncher.launch(
                arrayOf(
                    Manifest.permission.ACCESS_FINE_LOCATION,
                    Manifest.permission.ACCESS_COARSE_LOCATION
                )
            )
        }
    }

    // Delega os dados puros para a tela visual
    MapScreen(
        uiState = uiState,
        contentPadding = contentPadding,
        onCloseSelection = viewModel::clearSelection,
        onMarkerClick = { marker -> viewModel.selectSpotById(marker.id) },
        onZoomChange = viewModel::updateZoom,
        onNavigateToSpotDetail = onNavigateToSpotDetail,
        modifier = modifier
    )
}