package com.app.pictravelly.feature.home

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

private data class HomeTransientState(
    val isMapExpanded: Boolean = false,
    val selectedSpot: TouristSpotWithImages? = null,
    val currentLatitude: Double = LocationHelper.DEFAULT_LATITUDE,
    val currentLongitude: Double = LocationHelper.DEFAULT_LONGITUDE,
    val isLoadingLocation: Boolean = true
)

data class HomeUiState(
    val spots: List<TouristSpotWithImages> = emptyList(),
    val isMapExpanded: Boolean = false,
    val selectedSpot: TouristSpotWithImages? = null,
    val currentLatitude: Double = LocationHelper.DEFAULT_LATITUDE,
    val currentLongitude: Double = LocationHelper.DEFAULT_LONGITUDE,
    val isLoadingLocation: Boolean = true
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
    touristSpotRepository: TouristSpotRepository
) : ViewModel() {

    private val _transientState = MutableStateFlow(HomeTransientState())

    val uiState: StateFlow<HomeUiState> = combine(
        touristSpotRepository.getAllSpotsStream(),
        _transientState
    ) { spotsList, transient ->
        HomeUiState(
            spots = spotsList,
            isMapExpanded = transient.isMapExpanded,
            selectedSpot = transient.selectedSpot,
            currentLatitude = transient.currentLatitude,
            currentLongitude = transient.currentLongitude,
            isLoadingLocation = transient.isLoadingLocation
        )
    }.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5_000),
        initialValue = HomeUiState()
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

    fun setMapExpanded(expanded: Boolean) {
        _transientState.update { it.copy(isMapExpanded = expanded) }
    }

    fun selectSpot(spot: TouristSpotWithImages?) {
        _transientState.update { it.copy(selectedSpot = spot) }
    }
}