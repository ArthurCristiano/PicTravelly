package com.app.pictravelly.feature.home

import android.Manifest
import androidx.activity.compose.BackHandler
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
import kotlinx.coroutines.launch

@Composable
fun HomeRoute(
    viewModel: HomeViewModel,
    onNavigateToDetail: (Long) -> Unit,
    onNavigateToSpots: () -> Unit,
    onNavigateToCreate: () -> Unit,
    modifier: Modifier = Modifier,
    contentPadding: PaddingValues = PaddingValues(0.dp)
) {
    val uiState by viewModel.uiState.collectAsState()
    val context = LocalContext.current
    val coroutineScope = rememberCoroutineScope()

    // Delegação do botão voltar do hardware
    BackHandler(enabled = uiState.isMapExpanded) {
        viewModel.setMapExpanded(false)
    }

    // Permissões do Android
    val permissionLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.RequestMultiplePermissions()
    ) { permissions ->
        val granted = permissions[Manifest.permission.ACCESS_FINE_LOCATION] == true ||
                permissions[Manifest.permission.ACCESS_COARSE_LOCATION] == true
        if (granted) {
            coroutineScope.launch {
                val (lat, lng) = LocationHelper.getCurrentLocation(context)
                viewModel.updateCurrentLocation(lat, lng)
            }
        }
    }

    LaunchedEffect(Unit) {
        if (LocationHelper.hasLocationPermission(context)) {
            val (lat, lng) = LocationHelper.getCurrentLocation(context)
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

    HomeScreen(
        uiState = uiState,
        contentPadding = contentPadding,
        onMapExpandedChange = viewModel::setMapExpanded,
        onSpotSelect = viewModel::selectSpot,
        onNavigateToDetail = onNavigateToDetail,
        onNavigateToSpots = onNavigateToSpots,
        onNavigateToCreate = onNavigateToCreate,
        modifier = modifier
    )
}