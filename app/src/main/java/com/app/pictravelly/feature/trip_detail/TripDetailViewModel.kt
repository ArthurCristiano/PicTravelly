package com.app.pictravelly.feature.trip_detail

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.app.pictravelly.core.data.TouristSpotRepository
import com.app.pictravelly.core.data.TripRepository
import com.app.pictravelly.core.database.model.touristSpot.TouristSpotWithImages
import com.app.pictravelly.core.database.model.trip.TripEntity
import com.app.pictravelly.core.navigation.DestinationScreen
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

/**
 * Estado da tela de detalhe de uma viagem.
 */
data class TripDetailUiState(
    val trip: TripEntity? = null,
    val spots: List<TouristSpotWithImages> = emptyList(),
    val isLoading: Boolean = true,
    /** True quando a tela está exibindo o grupo "Pontos sem viagem". */
    val isLooseGroup: Boolean = false,
    val wasDeleted: Boolean = false
) {
    val displayTitle: String
        get() = if (isLooseGroup) "Pontos sem viagem" else trip?.title.orEmpty()

    val spotsCount: Int get() = spots.size
}

/**
 * ViewModel do detalhe da viagem: observa a viagem e os pontos turísticos
 * cadastrados dentro dela.
 *
 * Quando recebe [DestinationScreen.NO_TRIP_ID], passa a observar os pontos que
 * não pertencem a nenhuma viagem.
 */
class TripDetailViewModel(
    private val tripRepository: TripRepository,
    private val spotRepository: TouristSpotRepository,
    private val tripId: Long
) : ViewModel() {

    private val _uiState = MutableStateFlow(TripDetailUiState())
    val uiState: StateFlow<TripDetailUiState> = _uiState.asStateFlow()

    private val isLooseGroup = tripId == DestinationScreen.NO_TRIP_ID

    init {
        if (isLooseGroup) {
            observeLooseSpots()
        } else {
            observeTrip()
        }
    }

    private fun observeLooseSpots() {
        _uiState.update { it.copy(isLooseGroup = true) }
        viewModelScope.launch {
            tripRepository.getSpotsWithoutTripStream().collect { spots ->
                _uiState.update {
                    it.copy(spots = spots, isLoading = false)
                }
            }
        }
    }

    private fun observeTrip() {
        viewModelScope.launch {
            tripRepository.getTripStream(tripId).collect { tripWithSpots ->
                _uiState.update {
                    it.copy(
                        trip = tripWithSpots?.trip,
                        spots = tripWithSpots?.spots.orEmpty(),
                        isLoading = false,
                        // A viagem sai do banco quando é apagada; a tela então se
                        // fecha. Um id inválido cai no mesmo caminho.
                        wasDeleted = tripWithSpots == null
                    )
                }
            }
        }
    }

    fun deleteTrip() {
        if (isLooseGroup) return
        viewModelScope.launch {
            tripRepository.deleteTrip(tripId)
            _uiState.update { it.copy(wasDeleted = true) }
        }
    }

    /** Solta o ponto da viagem sem apagar o registro. */
    fun removeSpotFromTrip(spotId: Long) {
        viewModelScope.launch {
            tripRepository.assignSpotToTrip(spotId, null)
        }
    }

    fun deleteSpot(spotId: Long) {
        viewModelScope.launch {
            spotRepository.deleteSpot(spotId)
        }
    }
}
