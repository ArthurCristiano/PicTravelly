package com.app.pictravelly.feature.home

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

private data class HomeTransientState(
    val isMapExpanded: Boolean = false,
    val selectedSpot: TouristSpotWithImages? = null,
    val currentLatitude: Double = LocationHelper.DEFAULT_LATITUDE,
    val currentLongitude: Double = LocationHelper.DEFAULT_LONGITUDE,
    val isLoadingLocation: Boolean = true,
    val centerTrigger: Int = 0
)

data class HomeUiState(
    val spots: List<TouristSpotWithImages> = emptyList(),
    val isMapExpanded: Boolean = false,
    val selectedSpot: TouristSpotWithImages? = null,
    val currentLatitude: Double = LocationHelper.DEFAULT_LATITUDE,
    val currentLongitude: Double = LocationHelper.DEFAULT_LONGITUDE,
    val isLoadingLocation: Boolean = true,
    val mapEngine: MapEngineType = MapEngineType.OSM,
    val mapZoom: Float = 13f,
    val googleMapType: GoogleMapType = GoogleMapType.NORMAL,
    val isLoadingSettings: Boolean = true // <--- ADICIONADO: Controla se o DataStore já respondeu
) {
    val totalSpotsCount: Int get() = spots.size

    val travelerLevel: String
        get() = when (totalSpotsCount) {
            0 -> "Novato do Diário"
            in 1..3 -> "Explorador Curioso"
            in 4..9 -> "Aventureiro de Estrada"
            in 10..20 -> "Viajante Experiente"
            else -> "Mestre dos Horizontes"
        }

    val xpProgress: Float
        get() {
            val targetForNextLevel = when {
                totalSpotsCount < 3 -> 3
                totalSpotsCount < 10 -> 10
                totalSpotsCount < 25 -> 25
                else -> 50
            }
            return (totalSpotsCount.toFloat() / targetForNextLevel).coerceIn(0f, 1f)
        }
}

class HomeViewModel(
    touristSpotRepository: TouristSpotRepository,
    private val settingsRepository: SettingsRepository
) : ViewModel() {

    private val _transientState = MutableStateFlow(HomeTransientState())

    val uiState: StateFlow<HomeUiState> = combine(
        touristSpotRepository.getAllSpotsStream(),
        settingsRepository.userDataStream,
        _transientState
    ) { spotsList, settings, transient ->
        HomeUiState(
            spots = spotsList,
            isMapExpanded = transient.isMapExpanded,
            selectedSpot = transient.selectedSpot,
            currentLatitude = transient.currentLatitude,
            currentLongitude = transient.currentLongitude,
            isLoadingLocation = transient.isLoadingLocation,
            // Se 'settings' veio do DataStore, usamos ele. Se for null, mantemos o fallback mas marcamos como carregado se necessário.
            mapEngine = settings?.mapEngine ?: MapEngineType.OSM,
            mapZoom = settings?.lastZoom ?: 13f,
            googleMapType = settings?.googleMapType ?: GoogleMapType.NORMAL,
            isLoadingSettings = false // <--- ADICIONADO: Assim que o combine roda pela 1ª vez com o DataStore, fica false!
        )
    }.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5_000),
        initialValue = HomeUiState(isLoadingSettings = true) // <--- Começa true bloqueando a renderização cega
    )

    fun updateCurrentLocation(lat: Double, lng: Double) {
        _transientState.update {
            it.copy(currentLatitude = lat, currentLongitude = lng, isLoadingLocation = false)
        }
    }

    fun setMapExpanded(expanded: Boolean) {
        _transientState.update { it.copy(isMapExpanded = expanded) }
    }

    fun selectSpot(spot: TouristSpotWithImages?) {
        _transientState.update { it.copy(selectedSpot = spot) }
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