package com.app.pictravelly.feature.spot_detail

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.app.pictravelly.core.data.SettingsRepository
import com.app.pictravelly.core.data.TouristSpotRepository
import com.app.pictravelly.core.database.model.settings.GoogleMapType
import com.app.pictravelly.core.database.model.settings.MapEngineType
import com.app.pictravelly.core.database.model.touristSpot.TouristSpotWithImages
import com.app.pictravelly.core.location.LocationHelper
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

private data class SpotDetailTransientState(
    val isDeleted: Boolean = false,
    val currentLatitude: Double = LocationHelper.DEFAULT_LATITUDE,
    val currentLongitude: Double = LocationHelper.DEFAULT_LONGITUDE,
    val spotWithImages: TouristSpotWithImages? = null,
    val centerTrigger: Int = 0
)

data class SpotDetailUiState(
    val spotWithImages: TouristSpotWithImages? = null,
    val isLoading: Boolean = true,
    val isDeleted: Boolean = false,
    val mapEngine: MapEngineType = MapEngineType.OSM,
    val mapZoom: Float = 13f,
    val googleMapType: GoogleMapType = GoogleMapType.NORMAL,
    val centerTrigger: Int = 0
)

class SpotDetailViewModel(
    private val touristSpotRepository: TouristSpotRepository,
    private val settingsRepository: SettingsRepository,
    private val spotId: Long
) : ViewModel() {

    private val _transientState = MutableStateFlow(SpotDetailTransientState())

    val uiState: StateFlow<SpotDetailUiState> = combine(
        touristSpotRepository.getSpotStream(spotId),
        settingsRepository.userDataStream,
        _transientState
    ) { spot, userSettings, transient ->
        SpotDetailUiState(
            spotWithImages = spot,
            isLoading = false,
            isDeleted = transient.isDeleted,
            mapEngine = userSettings?.mapEngine ?: MapEngineType.OSM,
            mapZoom = userSettings?.lastZoom ?: 13f,
            googleMapType = userSettings?.googleMapType ?: GoogleMapType.NORMAL
        )
    }.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5_000),
        initialValue = SpotDetailUiState(isLoading = true)
    )

    fun deleteSpot(onDeleted: () -> Unit) {
        viewModelScope.launch {
            touristSpotRepository.deleteSpot(spotId)
            _transientState.update { it.copy(isDeleted = true) }
            onDeleted()
        }
    }

    fun updateZoom(newZoom: Float) {
        viewModelScope.launch {
            settingsRepository.setLastZoom(newZoom)
        }
    }

    fun updateMapEngine(newEngine: MapEngineType) {
        viewModelScope.launch {
            settingsRepository.setMapEngine(newEngine)
        }
    }

    fun updateGoogleMapType(newMapType: GoogleMapType) {
        viewModelScope.launch {
            settingsRepository.setMapType(newMapType)
        }
    }

}