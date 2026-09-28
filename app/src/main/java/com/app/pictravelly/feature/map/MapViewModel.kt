package com.app.pictravelly.feature.map

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.app.pictravelly.core.data.SettingsRepository
import com.app.pictravelly.core.data.TouristSpotRepository
import com.app.pictravelly.core.database.model.settings.GoogleMapType
import com.app.pictravelly.core.database.model.settings.MapEngineType
import com.app.pictravelly.core.database.model.touristSpot.TouristSpotWithImages
import com.app.pictravelly.core.location.LocationHelper
import com.app.pictravelly.core.map.MapMarkerData
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

/**
 * Estado dinâmico provisório (Ações do usuário na tela).
 */
private data class MapTransientState(
    val selectedSpot: TouristSpotWithImages? = null,
    val currentLatitude: Double = LocationHelper.DEFAULT_LATITUDE,
    val currentLongitude: Double = LocationHelper.DEFAULT_LONGITUDE,
    val isLoadingLocation: Boolean = true,
    val centerTrigger: Int = 0 // <--- Essencial para disparar o gatilho infinitas vezes
)

/**
 * Estado consolidado da aba Mapa.
 */
data class MapUiState(
    val spots: List<TouristSpotWithImages> = emptyList(),
    val selectedSpot: TouristSpotWithImages? = null,
    val currentLatitude: Double = LocationHelper.DEFAULT_LATITUDE,
    val currentLongitude: Double = LocationHelper.DEFAULT_LONGITUDE,
    val isLoading: Boolean = true,
    val mapEngine: MapEngineType = MapEngineType.OSM,
    val mapZoom: Float = 13f,
    val isLoadingSettings: Boolean = true,
    val googleMapType: GoogleMapType = GoogleMapType.NORMAL,
    val centerTrigger: Int = 0 // <--- Repassado para a UI
) {
    val markers: List<MapMarkerData>
        get() = spots.map { spotWithImages ->
            MapMarkerData(
                id = spotWithImages.spot.id,
                title = spotWithImages.spot.title,
                snippet = spotWithImages.spot.locationName,
                latitude = spotWithImages.spot.latitude,
                longitude = spotWithImages.spot.longitude
            )
        }

    val focusLatitude: Double
        get() = selectedSpot?.spot?.latitude ?: spots.firstOrNull()?.spot?.latitude
        ?: currentLatitude

    val focusLongitude: Double
        get() = selectedSpot?.spot?.longitude ?: spots.firstOrNull()?.spot?.longitude
        ?: currentLongitude
}

/**
 * ViewModel reativa da aba Mapa.
 */
class MapViewModel(
    touristSpotRepository: TouristSpotRepository,
    private val settingsRepository: SettingsRepository
) : ViewModel() {

    private val _transientState = MutableStateFlow(MapTransientState())

    val uiState: StateFlow<MapUiState> = combine(
        touristSpotRepository.getAllSpotsStream(),
        settingsRepository.userDataStream,
        _transientState
    ) { spotsList, userSettings, transient ->
        MapUiState(
            spots = spotsList,
            selectedSpot = transient.selectedSpot,
            currentLatitude = transient.currentLatitude,
            currentLongitude = transient.currentLongitude,
            isLoading = false,
            mapEngine = userSettings?.mapEngine ?: MapEngineType.OSM,
            mapZoom = userSettings?.lastZoom ?: 13f,
            isLoadingSettings = false,
            googleMapType = userSettings?.googleMapType ?: GoogleMapType.NORMAL,
            centerTrigger = transient.centerTrigger // <--- Mapeia o gatilho da transient state para a UiState
        )
    }.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5_000),
        initialValue = MapUiState(isLoadingSettings = true)
    )

    fun updateCurrentLocation(lat: Double, lng: Double) {
        _transientState.update {
            it.copy(currentLatitude = lat, currentLongitude = lng, isLoadingLocation = false)
        }
    }

    fun selectSpotById(spotId: Long) {
        val spot = uiState.value.spots.firstOrNull { it.spot.id == spotId }
        _transientState.update { it.copy(selectedSpot = spot) }
    }

    fun clearSelection() {
        _transientState.update { it.copy(selectedSpot = null) }
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

    /**
     * Incrementa o trigger e limpa a seleção para forçar o mapa a animar para a localização do usuário.
     */
    fun centerOnUserLocation() {
        _transientState.update {
            it.copy(
                selectedSpot = null,
                centerTrigger = it.centerTrigger + 1
            )
        }
    }
}