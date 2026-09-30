package com.app.pictravelly.feature.settings

import android.widget.Toast
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import com.app.pictravelly.core.di.AppViewModelProvider
import com.app.pictravelly.core.utils.ImageStorageManager

@Composable
fun SettingsRoute(
    modifier: Modifier = Modifier,
    viewModel: SettingsViewModel = viewModel(factory = AppViewModelProvider.Factory),
    contentPadding: PaddingValues = PaddingValues(0.dp)
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    val context = LocalContext.current

    LaunchedEffect(uiState.exportMessage) {
        uiState.exportMessage?.let { message ->
            Toast.makeText(context, message, Toast.LENGTH_SHORT).show()
            viewModel.clearExportMessage()
        }
    }

    SettingsScreen(
        uiState = uiState,
        contentPadding = contentPadding,
        modifier = modifier,
        onThemeChange = viewModel::setTheme,
        onMapEngineChange = viewModel::setMapEngine,
        onMapTypeChange = viewModel::setMapType,
        onExportPhotos = {
            viewModel.exportDiaryPhotos(context) { zipFile ->
                ImageStorageManager.shareZipFile(context, zipFile)
            }
        }
    )
}