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
import androidx.compose.runtime.rememberCoroutineScope
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.app.pictravelly.core.location.LocationHelper
import com.app.pictravelly.core.utils.ImageStorageManager
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.io.File

@Composable
fun SpotFormRoute(
    viewModel: SpotFormViewModel,
    onNavigateBack: () -> Unit,
    onSpotSaved: (Long, Boolean) -> Unit,
    modifier: Modifier = Modifier,
    initialSharedImages: List<String>? = null,
    onConsumeSharedImages: () -> Unit = {}
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    val context = LocalContext.current
    val coroutineScope = rememberCoroutineScope()

    LaunchedEffect(initialSharedImages) {
        if (!initialSharedImages.isNullOrEmpty()) {
            viewModel.addImageUris(initialSharedImages)
            onConsumeSharedImages()
        }
    }

    var currentPhotoFile by remember { mutableStateOf<File?>(null) }
    var currentPhotoUri by remember { mutableStateOf<Uri?>(null) }

    val photoPickerLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.GetMultipleContents()
    ) { uris: List<Uri> ->
        coroutineScope.launch(Dispatchers.IO) {
            val savedUris = uris.mapNotNull { uri ->
                ImageStorageManager.copyUriToInternalStorage(context, uri)
            }
            withContext(Dispatchers.Main) {
                savedUris.forEach { uri -> viewModel.addImageUri(uri.toString()) }
            }
        }
    }

    val cameraLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.TakePicture()
    ) { success: Boolean ->
        if (success) {
            currentPhotoUri?.let { uri -> viewModel.addImageUri(uri.toString()) }
        } else {
            currentPhotoFile?.delete()
        }
    }

    fun launchCamera() {
        try {
            val (file, uri) = ImageStorageManager.createCameraImageFile(context)
            currentPhotoFile = file
            currentPhotoUri = uri
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

    LaunchedEffect(uiState.isEditing) {
        if (!uiState.isEditing) {
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
        onSaveClick = {
            viewModel.saveSpot { savedId ->
                onSpotSaved(savedId, uiState.isEditing)
            }
        },
        modifier = modifier
    )
}