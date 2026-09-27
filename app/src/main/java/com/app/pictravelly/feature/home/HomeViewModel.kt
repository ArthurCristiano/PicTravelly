package com.app.pictravelly.feature.home

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.app.pictravelly.core.data.TouristSpotRepository
import com.app.pictravelly.core.database.model.touristSpot.TouristSpotWithImages
import com.app.pictravelly.core.location.LocationHelper
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.flow.update

/**
 * Estado dinâmico provisório (Ações do usuário na tela).
 */
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
    repository: TouristSpotRepository
) : ViewModel() {

    // Guarda apenas os estados transitórios (cliques, expansões e GPS)
    private val _transientState = MutableStateFlow(HomeTransientState())

    // A Mágica: Funde o banco de dados e os estados transitórios automaticamente.
    val uiState: StateFlow<HomeUiState> = combine(
        repository.getAllSpotsStream(),
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
        initialValue = HomeUiState() // Estado inicial vazio
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
}