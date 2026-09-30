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

enum class DateField { START, END }

/**
 * ROTA: Hospeda os launchers do Android e o estado efêmero de UI (Dialogs).
 */
@Composable
fun TripFormRoute(
    viewModel: TripFormViewModel,
    onNavigateBack: () -> Unit,
    onTripSaved: (Long) -> Unit,
    modifier: Modifier = Modifier
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()

    // Controla se o calendário está aberto e qual campo ele afeta
    var openDateField by rememberSaveable { mutableStateOf<DateField?>(null) }

    val coverPickerLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.GetContent()
    ) { uri: Uri? ->
        uri?.let { viewModel.updateCoverImageUri(it.toString()) }
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