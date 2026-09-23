package com.app.pictravelly.feature.settings

import androidx.lifecycle.ViewModel
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
    val selectedTheme: AppThemeSetting = AppThemeSetting.SYSTEM,
    val selectedLanguage: String = "Português (Brasil)",
    val selectedMapEngine: MapEngineType = MapConfig.activeEngine
)

/**
 * ViewModel que gerencia as preferências do usuário, incluindo o motor de mapas.
 */
class SettingsViewModel : ViewModel() {

    private val _uiState = MutableStateFlow(SettingsUiState())
    val uiState: StateFlow<SettingsUiState> = _uiState.asStateFlow()

    fun setTheme(theme: AppThemeSetting) {
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
