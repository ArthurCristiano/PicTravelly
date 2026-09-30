package com.app.pictravelly.feature.settings

import android.content.Context
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.app.pictravelly.core.data.SettingsRepository
import com.app.pictravelly.core.data.TouristSpotRepository
import com.app.pictravelly.core.database.model.settings.AppTheme
import com.app.pictravelly.core.database.model.settings.GoogleMapType
import com.app.pictravelly.core.database.model.settings.MapEngineType
import com.app.pictravelly.core.utils.ImageStorageManager
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.io.File

data class SettingsUiState(
    val selectedTheme: AppTheme = AppTheme.SYSTEM,
    val selectedGoogleMapType: GoogleMapType = GoogleMapType.NORMAL,
    val selectedMapEngine: MapEngineType = MapEngineType.OSM,
    val isLoading: Boolean = true,
    val isExporting: Boolean = false,
    val exportMessage: String? = null
)

class SettingsViewModel(
    private val repository: SettingsRepository,
    private val touristSpotRepository: TouristSpotRepository
) : ViewModel() {

    private val _isExporting = MutableStateFlow(false)
    private val _exportMessage = MutableStateFlow<String?>(null)

    val uiState: StateFlow<SettingsUiState> = combine(
        repository.userDataStream,
        _isExporting,
        _exportMessage
    ) { userSettings, isExporting, exportMessage ->
        SettingsUiState(
            selectedTheme = userSettings.theme,
            selectedGoogleMapType = userSettings.googleMapType,
            selectedMapEngine = userSettings.mapEngine,
            isLoading = false,
            isExporting = isExporting,
            exportMessage = exportMessage
        )
    }.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5_000),
        initialValue = SettingsUiState(isLoading = true)
    )

    fun setTheme(theme: AppTheme) = viewModelScope.launch {
        repository.setTheme(theme)
    }

    fun setMapType(googleMapType: GoogleMapType) = viewModelScope.launch {
        repository.setMapType(googleMapType)
    }

    fun setMapEngine(engine: MapEngineType) = viewModelScope.launch {
        repository.setMapEngine(engine)
    }

    fun exportDiaryPhotos(context: Context, onReadyToShare: (File) -> Unit) {
        if (_isExporting.value) return
        viewModelScope.launch {
            _isExporting.value = true
            _exportMessage.value = null
            try {
                val spots = touristSpotRepository.getAllSpotsStream().first()
                val imageUris = spots.flatMap { it.images }.map { it.imageUri }
                val zipFile = withContext(Dispatchers.IO) {
                    ImageStorageManager.createPhotosZipBackup(context.applicationContext, imageUris)
                }
                if (zipFile != null && zipFile.exists()) {
                    onReadyToShare(zipFile)
                } else {
                    _exportMessage.value = "Nenhuma foto encontrada para exportar."
                }
            } catch (e: Exception) {
                _exportMessage.value = "Erro ao exportar fotos: ${e.localizedMessage}"
            } finally {
                _isExporting.value = false
            }
        }
    }

    fun clearExportMessage() {
        _exportMessage.value = null
    }
}