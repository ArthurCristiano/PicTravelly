package com.app.pictravelly.feature.settings

import androidx.lifecycle.ViewModel
import com.app.pictravelly.core.design.theme.ThemeController
import com.app.pictravelly.core.map.MapConfig
import com.app.pictravelly.core.map.MapEngineType
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update

enum class AppThemeSetting(val label: String) {
    SYSTEM("Padrão do Sistema"),
    LIGHT("Modo Claro"),
    DARK("Modo Escuro")
}

data class SettingsUiState(
    val selectedTheme: AppThemeSetting = ThemeController.selectedTheme.value,
    val selectedLanguage: String = "Português (Brasil)",
    val selectedMapEngine: MapEngineType = MapConfig.activeEngine
)
class SettingsViewModel : ViewModel() {

    private val _uiState = MutableStateFlow(SettingsUiState())
    val uiState: StateFlow<SettingsUiState> = _uiState.asStateFlow()

    fun setTheme(theme: AppThemeSetting) {
        ThemeController.setTheme(theme)
        _uiState.update { it.copy(selectedTheme = theme) }
    }

    fun setLanguage(language: String) {
        _uiState.update { it.copy(selectedLanguage = language) }
    }

    fun setMapEngine(engine: MapEngineType) {
        MapConfig.activeEngine = engine
        _uiState.update { it.copy(selectedMapEngine = engine) }
    }
}