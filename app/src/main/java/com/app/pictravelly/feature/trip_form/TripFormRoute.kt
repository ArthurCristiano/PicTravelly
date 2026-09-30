package com.app.pictravelly.feature.trip_form

import android.net.Uri
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.lifecycle.compose.collectAsStateWithLifecycle

import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.platform.LocalContext
import com.app.pictravelly.core.utils.ImageStorageManager
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

enum class DateField { START, END }

@Composable
fun TripFormRoute(
    viewModel: TripFormViewModel,
    onNavigateBack: () -> Unit,
    onTripSaved: (Long) -> Unit,
    modifier: Modifier = Modifier
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    val context = LocalContext.current
    val coroutineScope = rememberCoroutineScope()

    var openDateField by rememberSaveable { mutableStateOf<DateField?>(null) }

    val coverPickerLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.GetContent()
    ) { uri: Uri? ->
        if (uri != null) {
            coroutineScope.launch(Dispatchers.IO) {
                val savedUri = ImageStorageManager.copyUriToInternalStorage(context, uri)
                withContext(Dispatchers.Main) {
                    if (savedUri != null) {
                        viewModel.updateCoverImageUri(savedUri.toString())
                    }
                }
            }
        }
    }

    TripFormScreen(
        uiState = uiState,
        openDateField = openDateField,
        onNavigateBack = onNavigateBack,
        onTitleChange = viewModel::updateTitle,
        onDescriptionChange = viewModel::updateDescription,
        onPickCover = { coverPickerLauncher.launch("image/*") },
        onRemoveCover = { viewModel.updateCoverImageUri(null) },
        onOpenDateField = { openDateField = it },
        onCloseDateField = { openDateField = null },
        onDateSelected = { millis ->
            when (openDateField) {
                DateField.START -> viewModel.updateStartDateFromUtc(millis)
                DateField.END -> viewModel.updateEndDateFromUtc(millis)
                null -> {}
            }
            openDateField = null
        },
        onClearEndDate = {
            viewModel.updateEndDateFromUtc(null)
            openDateField = null
        },
        onSaveTrip = { viewModel.saveTrip(onTripSaved) },
        modifier = modifier
    )
}