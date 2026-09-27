package com.app.pictravelly.feature.map

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.app.pictravelly.core.data.TouristSpotRepository
import com.app.pictravelly.core.database.model.touristSpot.TouristSpotWithImages
import com.app.pictravelly.core.location.LocationHelper
import com.app.pictravelly.core.map.MapMarkerData
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

/**
 * Estado da aba Mapa, que reúne todos os pontos turísticos cadastrados.
 */
data class MapUiState(
    val spots: List<TouristSpotWithImages> = emptyList(),
    val selectedSpot: TouristSpotWithImages? = null,
    val currentLatitude: Double = LocationHelper.DEFAULT_LATITUDE,
    val currentLongitude: Double = LocationHelper.DEFAULT_LONGITUDE,
    val isLoading: Boolean = true
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

    /**
     * O mapa abre no ponto selecionado, no cadastro mais recente ou, sem nada
     * cadastrado, na posição atual do aparelho.
     */
    val focusLatitude: Double
        get() = selectedSpot?.spot?.latitude ?: spots.firstOrNull()?.spot?.latitude ?: currentLatitude

    val focusLongitude: Double
        get() = selectedSpot?.spot?.longitude ?: spots.firstOrNull()?.spot?.longitude ?: currentLongitude
}

/**
 * ViewModel da aba Mapa: observa os pontos e guarda o marcador selecionado.
 */
class MapViewModel(
    private val repository: TouristSpotRepository
) : ViewModel() {

    private val _uiState = MutableStateFlow(MapUiState())
    val uiState: StateFlow<MapUiState> = _uiState.asStateFlow()

    init {
        viewModelScope.launch {
            repository.getAllSpotsStream().collect { spotsList ->
                _uiState.update { it.copy(spots = spotsList, isLoading = false) }
            }
        }
    }

    fun updateCurrentLocation(lat: Double, lng: Double) {
        _uiState.update { it.copy(currentLatitude = lat, currentLongitude = lng) }
    }

    fun selectSpotById(spotId: Long) {
        val spot = _uiState.value.spots.firstOrNull { it.spot.id == spotId }
        _uiState.update { it.copy(selectedSpot = spot) }
    }

    fun clearSelection() {
        _uiState.update { it.copy(selectedSpot = null) }
    }
}
