package com.app.pictravelly.feature.home

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.app.pictravelly.core.data.TouristSpotRepository
import com.app.pictravelly.core.database.model.TouristSpotWithImages
import com.app.pictravelly.core.location.LocationHelper
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

/**
 * Estado da UI da Tela Inicial (Home).
 */
data class HomeUiState(
    val spots: List<TouristSpotWithImages> = emptyList(),
    val isMapExpanded: Boolean = false,
    val selectedSpot: TouristSpotWithImages? = null,
    val currentLatitude: Double = LocationHelper.DEFAULT_LATITUDE,
    val currentLongitude: Double = LocationHelper.DEFAULT_LONGITUDE,
    val isLoadingLocation: Boolean = true
) {
    val totalSpotsCount: Int get() = spots.size

    // Nível e XP calculados com base na quantidade de locais explorados (Gamificação)
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

/**
 * ViewModel responsável pela lógica de negócios da Home e do Mapa Interativo.
 */
class HomeViewModel(
    private val repository: TouristSpotRepository
) : ViewModel() {

    private val _uiState = MutableStateFlow(HomeUiState())
    val uiState: StateFlow<HomeUiState> = _uiState.asStateFlow()

    init {
        // Observa em tempo real os locais cadastrados no banco
        viewModelScope.launch {
            repository.getAllSpotsStream().collect { spotsList ->
                _uiState.update { it.copy(spots = spotsList) }
            }
        }
    }

    fun updateCurrentLocation(lat: Double, lng: Double) {
        _uiState.update {
            it.copy(
                currentLatitude = lat,
                currentLongitude = lng,
                isLoadingLocation = false
            )
        }
    }

    fun toggleMapExpansion() {
        _uiState.update { it.copy(isMapExpanded = !it.isMapExpanded) }
    }

    fun setMapExpanded(expanded: Boolean) {
        _uiState.update { it.copy(isMapExpanded = expanded) }
    }

    fun selectSpot(spot: TouristSpotWithImages?) {
        _uiState.update { it.copy(selectedSpot = spot) }
    }
}
