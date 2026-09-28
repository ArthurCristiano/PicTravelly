package com.app.pictravelly.feature.spot_form


import android.Manifest
import android.net.Uri
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.core.content.FileProvider
import com.app.pictravelly.core.location.GeocodingHelper
import com.app.pictravelly.core.location.LocationHelper
import kotlinx.coroutines.launch
import java.io.File

@Composable
fun SpotFormRoute(
    viewModel: SpotFormViewModel,
    onNavigateBack: () -> Unit,
    onSpotSaved: (Long) -> Unit,
    modifier: Modifier = Modifier
) {
    val uiState by viewModel.uiState.collectAsState()
    val context = LocalContext.current
    val coroutineScope = rememberCoroutineScope()

    var tempPhotoUri by remember { mutableStateOf<Uri?>(null) }

    val photoPickerLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.GetMultipleContents()
    ) { uris: List<Uri> ->
        uris.forEach { uri -> viewModel.addImageUri(uri.toString()) }
    }

    val cameraLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.TakePicture()
    ) { success: Boolean ->
        if (success) tempPhotoUri?.let { uri -> viewModel.addImageUri(uri.toString()) }
    }

    fun launchCamera() {
        try {
            val photoFile = File.createTempFile(
                "spot_photo_${System.currentTimeMillis()}",
                ".jpg",
                context.cacheDir
            )
            val uri = FileProvider.getUriForFile(
                context,
                "${context.packageName}.fileprovider",
                photoFile
            )
            tempPhotoUri = uri
            cameraLauncher.launch(uri)
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    suspend fun resolveAddress(lat: Double, lng: Double, overwrite: Boolean) {
        if (!overwrite && uiState.locationName.isNotBlank()) return
        viewModel.setResolvingAddress(true)
        val address = GeocodingHelper.getAddressFromCoordinates(context, lat, lng)
        viewModel.setResolvingAddress(false)

        if (address != null) {
            viewModel.updateLocationName(address)
        } else if (overwrite) {
            viewModel.setErrorMessage("Não foi possível obter o endereço. Você pode digitá-lo manualmente.")
        }
    }

    suspend fun captureLocation(overwriteAddress: Boolean) {
        viewModel.setLocatingDevice(true)
        val (lat, lng) = LocationHelper.getCurrentLocation(context)
        viewModel.setLocatingDevice(false)

        val isFallback =
            lat == LocationHelper.DEFAULT_LATITUDE && lng == LocationHelper.DEFAULT_LONGITUDE
        viewModel.updateCoordinates(lat, lng, isFallback = isFallback)

        if (isFallback) {
            if (overwriteAddress) viewModel.setErrorMessage("Não foi possível obter a sua posição.")
            return
        }
        resolveAddress(lat, lng, overwriteAddress)
    }

    val locationPermissionLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.RequestMultiplePermissions()
    ) { permissions ->
        if (permissions[Manifest.permission.ACCESS_FINE_LOCATION] == true || permissions[Manifest.permission.ACCESS_COARSE_LOCATION] == true) {
            coroutineScope.launch { captureLocation(false) }
        } else {
            viewModel.setErrorMessage("Sem permissão de localização. Toque no mapa para marcar o ponto.")
        }
    }

    LaunchedEffect(Unit) {
        if (LocationHelper.hasLocationPermission(context)) {
            captureLocation(overwriteAddress = false)
        } else {
            locationPermissionLauncher.launch(
                arrayOf(
                    Manifest.permission.ACCESS_FINE_LOCATION,
                    Manifest.permission.ACCESS_COARSE_LOCATION
                )
            )
        }
    }

    SpotFormScreen(
        uiState = uiState,
        onNavigateBack = onNavigateBack,
        onTitleChange = viewModel::updateTitle,
        onDescriptionChange = viewModel::updateDescription,
        onLocationNameChange = viewModel::updateLocationName,
        onTripIdChange = viewModel::updateTripId,
        onPickOnMap = { lat, lng ->
            viewModel.updateCoordinates(lat, lng)
            coroutineScope.launch { resolveAddress(lat, lng, true) }
        },
        onUseMyLocation = {
            if (LocationHelper.hasLocationPermission(context)) {
                coroutineScope.launch { captureLocation(true) }
            } else {
                locationPermissionLauncher.launch(
                    arrayOf(
                        Manifest.permission.ACCESS_FINE_LOCATION,
                        Manifest.permission.ACCESS_COARSE_LOCATION
                    )
                )
            }
        },
        onSearchAddress = {
            coroutineScope.launch {
                resolveAddress(
                    uiState.latitude,
                    uiState.longitude,
                    true
                )
            }
        },
        onAddPhotoCamera = ::launchCamera,
        onAddPhotoGallery = { photoPickerLauncher.launch("image/*") },
        onRemovePhoto = viewModel::removeImageUri,
        onSaveClick = { viewModel.saveSpot(onSpotSaved) },
        modifier = modifier
    )
}