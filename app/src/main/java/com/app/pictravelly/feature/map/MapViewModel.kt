package com.app.pictravelly.feature.map

import android.content.Context
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.app.pictravelly.core.data.TouristSpotRepository
import com.app.pictravelly.core.database.model.touristSpot.TouristSpotWithImages
import com.app.pictravelly.core.location.LocationHelper
import com.app.pictravelly.core.map.model.MapMarkerData
import com.app.pictravelly.core.map.model.MarkerType
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

private data class MapTransientState(
    val selectedSpot: TouristSpotWithImages? = null,
    val currentLatitude: Double = LocationHelper.DEFAULT_LATITUDE,
    val currentLongitude: Double = LocationHelper.DEFAULT_LONGITUDE,
    val isLoadingLocation: Boolean = true,
)

data class MapUiState(
    val spots: List<TouristSpotWithImages> = emptyList(),
    val selectedSpot: TouristSpotWithImages? = null,
    val currentLatitude: Double = LocationHelper.DEFAULT_LATITUDE,
    val currentLongitude: Double = LocationHelper.DEFAULT_LONGITUDE,
    val isLoading: Boolean = true
) {
    val markers: List<MapMarkerData>
        get() {
            val spotMarkers = spots.map { spotWithImages ->
                MapMarkerData(
                    id = spotWithImages.spot.id,
                    title = spotWithImages.spot.title,
                    snippet = spotWithImages.spot.locationName,
                    latitude = spotWithImages.spot.latitude,
                    longitude = spotWithImages.spot.longitude,
                    type = MarkerType.TOURIST_SPOT
                )
            }

            // ID negativo para evitar colisão com IDs do banco de dados
            val userMarker = MapMarkerData(
                id = -1L,
                title = "Você está aqui",
                snippet = "Sua localização atual",
                latitude = currentLatitude,
                longitude = currentLongitude,
                type = MarkerType.USER_LOCATION
            )

            return spotMarkers + userMarker
        }

    val focusLatitude: Double
        get() = selectedSpot?.spot?.latitude ?: currentLatitude

    val focusLongitude: Double
        get() = selectedSpot?.spot?.longitude ?: currentLongitude
}

class MapViewModel(
    touristSpotRepository: TouristSpotRepository,
) : ViewModel() {

    private val _transientState = MutableStateFlow(MapTransientState())

    val uiState: StateFlow<MapUiState> = combine(
        touristSpotRepository.getAllSpotsStream(),
        _transientState
    ) { spotsList, transient ->
        MapUiState(
            spots = spotsList,
            selectedSpot = transient.selectedSpot,
            currentLatitude = transient.currentLatitude,
            currentLongitude = transient.currentLongitude,
            isLoading = false
        )
    }.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5_000),
        initialValue = MapUiState()
    )

    fun updateCurrentLocation(lat: Double, lng: Double) {
        _transientState.update {
            it.copy(currentLatitude = lat, currentLongitude = lng, isLoadingLocation = false)
        }
    }

    fun fetchCurrentLocation(context: Context) {
        viewModelScope.launch {
            val (lat, lng) = LocationHelper.getCurrentLocation(context.applicationContext)
            updateCurrentLocation(lat, lng)
        }
    }

    fun selectSpotById(spotId: Long) {
        val spot = uiState.value.spots.firstOrNull { it.spot.id == spotId }
        _transientState.update { it.copy(selectedSpot = spot) }
    }

    fun clearSelection() {
        _transientState.update { it.copy(selectedSpot = null) }
    }
}