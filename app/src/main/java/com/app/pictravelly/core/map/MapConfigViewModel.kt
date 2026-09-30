package com.app.pictravelly.core.map

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.app.pictravelly.core.data.SettingsRepository
import com.app.pictravelly.core.database.model.settings.GoogleMapType
import com.app.pictravelly.core.database.model.settings.MapEngineType
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

class MapConfigViewModel(
    private val settingsRepository: SettingsRepository
) : ViewModel() {

    val mapSettings = settingsRepository.userDataStream.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5_000),
        initialValue = null
    )

    private val _centerTrigger = MutableStateFlow(0)
    val centerTrigger: StateFlow<Int> = _centerTrigger.asStateFlow()

    fun updateZoom(newZoom: Float) = viewModelScope.launch {
        settingsRepository.setLastZoom(newZoom)
    }

    fun updateMapEngine(newEngine: MapEngineType) = viewModelScope.launch {
        settingsRepository.setMapEngine(newEngine)
    }

    fun updateGoogleMapType(newMapType: GoogleMapType) = viewModelScope.launch {
        settingsRepository.setMapType(newMapType)
    }

    fun triggerCenterOnUser() {
        _centerTrigger.update { it + 1 }
    }
}