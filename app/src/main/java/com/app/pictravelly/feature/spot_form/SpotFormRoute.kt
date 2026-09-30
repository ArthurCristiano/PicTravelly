package com.app.pictravelly.feature.spot_form

import android.Manifest
import android.net.Uri
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.core.content.FileProvider
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.app.pictravelly.core.location.LocationHelper
import java.io.File

/**
 * ROTA: Responsável apenas por interligar a ViewModel, gerenciar launchers
 * de Activity/permissões e repassar dados e eventos para a UI (SpotFormScreen).
 */
@Composable
fun SpotFormRoute(
    viewModel: SpotFormViewModel,
    onNavigateBack: () -> Unit,
    onSpotSaved: (Long) -> Unit,
    modifier: Modifier = Modifier
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    val context = LocalContext.current

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

    val locationPermissionLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.RequestMultiplePermissions()
    ) { permissions ->
        if (permissions[Manifest.permission.ACCESS_FINE_LOCATION] == true ||
            permissions[Manifest.permission.ACCESS_COARSE_LOCATION] == true
        ) {
            viewModel.captureLocation(context, overwriteAddress = false)
        } else {
            viewModel.onLocationPermissionDenied()
        }
    }

    LaunchedEffect(Unit) {
        if (LocationHelper.hasLocationPermission(context)) {
            viewModel.captureLocation(context, overwriteAddress = false)
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
        onPickOnMap = { lat, lng -> viewModel.onPickOnMap(context, lat, lng) },
        onUseMyLocation = {
            if (LocationHelper.hasLocationPermission(context)) {
                viewModel.captureLocation(context, overwriteAddress = true)
            } else {
                locationPermissionLauncher.launch(
                    arrayOf(
                        Manifest.permission.ACCESS_FINE_LOCATION,
                        Manifest.permission.ACCESS_COARSE_LOCATION
                    )
                )
            }
        },
        onSearchAddress = { viewModel.resolveAddress(context, overwrite = true) },
        onAddPhotoCamera = ::launchCamera,
        onAddPhotoGallery = { photoPickerLauncher.launch("image/*") },
        onRemovePhoto = viewModel::removeImageUri,
        onSaveClick = { viewModel.saveSpot(onSpotSaved) },
        modifier = modifier
    )
}