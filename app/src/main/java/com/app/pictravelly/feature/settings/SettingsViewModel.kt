package com.app.pictravelly.feature.settings

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.app.pictravelly.core.data.SettingsRepository
import com.app.pictravelly.core.database.model.settings.GoogleMapType
import com.app.pictravelly.core.database.model.settings.AppTheme
import com.app.pictravelly.core.database.model.settings.MapEngineType
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

data class SettingsUiState(
    val selectedTheme: AppTheme = AppTheme.SYSTEM,
    val selectedGoogleMapType: GoogleMapType = GoogleMapType.NORMAL,
    val selectedMapEngine: MapEngineType = MapEngineType.OSM
)

class SettingsViewModel(private val repository: SettingsRepository) : ViewModel(

) {

    // Lê o modelo puro, converte para estado de tela e garante reatividade contínua
    val uiState: StateFlow<SettingsUiState> =
        repository.userDataStream.map { userSettings ->
            SettingsUiState(
                selectedTheme = userSettings.theme,
                selectedGoogleMapType = userSettings.googleMapType,
                selectedMapEngine = userSettings.mapEngine
            )
        }.stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5_000), // Padrão recomendado pela Google
            initialValue = SettingsUiState()
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
}
