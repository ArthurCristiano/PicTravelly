package com.app.pictravelly.core.map.components.previewCard

import android.Manifest
import android.content.Intent
import android.provider.Settings
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.scaleIn
import androidx.compose.animation.scaleOut
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.GpsOff
import androidx.compose.material.icons.filled.Warning
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.app.pictravelly.core.database.model.settings.GoogleMapType
import com.app.pictravelly.core.database.model.settings.MapEngineType
import com.app.pictravelly.core.di.AppViewModelProvider
import com.app.pictravelly.core.location.model.LocationStatus
import com.app.pictravelly.core.location.rememberLocationStatus
import com.app.pictravelly.core.map.MapConfigViewModel
import com.app.pictravelly.core.map.model.MapMarkerData

@Composable
fun PicTravellySmartPreviewMapCard(
    currentLatitude: Double,
    currentLongitude: Double,
    markers: List<MapMarkerData>,
    onExpandClick: () -> Unit,
    onMarkerSelect: (Long) -> Unit,
    modifier: Modifier = Modifier,
    mapConfigViewModel: MapConfigViewModel = viewModel(factory = AppViewModelProvider.Factory),
    onFetchLocationRequested: () -> Unit = {}
) {

    val mapSettings by mapConfigViewModel.mapSettings.collectAsState()
    val locationStatus by rememberLocationStatus()
    val context = LocalContext.current

    val permissionLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.RequestMultiplePermissions()
    ) { permissions ->
        // O rememberLocationStatus() nota sozinho e muda para READY
    }

    LaunchedEffect(locationStatus) {
        if (locationStatus == LocationStatus.READY) {
            onFetchLocationRequested()
        }
    }

    if (mapSettings == null) {
        Box(
            modifier = modifier
                .fillMaxWidth()
                .height(260.dp),
            contentAlignment = Alignment.Center
        ) {
            CircularProgressIndicator()
        }
        return
    }

    // Usamos fillMaxWidth e a altura exata para conter o mapa e os elementos flutuantes
    Box(modifier = modifier
        .fillMaxWidth()
        .height(260.dp)) {

        PicTravellyPreviewMapCard(
            currentLatitude = currentLatitude,
            currentLongitude = currentLongitude,
            engine = mapSettings!!.mapEngine ?: MapEngineType.OSM,
            zoom = mapSettings!!.lastZoom ?: 13f,
            googleMapType = mapSettings!!.googleMapType ?: GoogleMapType.NORMAL,
            markers = markers,
            onExpandClick = onExpandClick,
            onMarkerSelect = onMarkerSelect,
            // Preenche o Box completamente
            modifier = Modifier.fillMaxSize()
        )

        // Chip de Aviso Minimalista injetado no Canto Superior Esquerdo
        CompactLocationWarningChip(
            status = locationStatus,
            onClick = {
                if (locationStatus == LocationStatus.GPS_DISABLED) {
                    context.startActivity(Intent(Settings.ACTION_LOCATION_SOURCE_SETTINGS))
                } else {
                    permissionLauncher.launch(
                        arrayOf(
                            Manifest.permission.ACCESS_FINE_LOCATION,
                            Manifest.permission.ACCESS_COARSE_LOCATION
                        )
                    )
                }
            },
            modifier = Modifier
                .align(Alignment.TopStart)
                .padding(12.dp) // Substitui o padding de 90dp pelo espaçamento padrão das bordas
        )
    }
}

/**
 * Componente minimalista para exibir alertas críticos sem poluir o Preview Card.
 */
@Composable
private fun CompactLocationWarningChip(
    status: LocationStatus,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    AnimatedVisibility(
        visible = status != LocationStatus.READY,
        enter = fadeIn() + scaleIn(),
        exit = fadeOut() + scaleOut(),
        modifier = modifier
    ) {
        Surface(
            onClick = onClick,
            shape = RoundedCornerShape(8.dp),
            color = MaterialTheme.colorScheme.errorContainer.copy(alpha = 0.95f),
            contentColor = MaterialTheme.colorScheme.onErrorContainer,
            shadowElevation = 4.dp
        ) {
            Row(
                modifier = Modifier.padding(horizontal = 10.dp, vertical = 6.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(6.dp)
            ) {
                Icon(
                    imageVector = if (status == LocationStatus.GPS_DISABLED) Icons.Default.GpsOff else Icons.Default.Warning,
                    contentDescription = null,
                    modifier = Modifier.size(16.dp)
                )
                Text(
                    text = if (status == LocationStatus.GPS_DISABLED) "GPS Desligado" else "Sem Permissão",
                    style = MaterialTheme.typography.labelMedium,
                    fontWeight = FontWeight.Bold
                )
            }
        }
    }
}