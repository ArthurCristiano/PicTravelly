package com.app.pictravelly.feature.trips

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.app.pictravelly.core.data.TripRepository
import com.app.pictravelly.core.database.model.TouristSpotWithImages
import com.app.pictravelly.core.database.model.TripWithSpots
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn

/**
 * Estado da aba Diário, que lista as viagens registradas.
 */
data class TripsUiState(
    val trips: List<TripWithSpots> = emptyList(),
    val spotsWithoutTrip: List<TouristSpotWithImages> = emptyList(),
    val isLoading: Boolean = false
) {
    val totalTrips: Int get() = trips.size

    val totalSpots: Int
        get() = trips.sumOf { it.spotsCount } + spotsWithoutTrip.size

    val isEmpty: Boolean
        get() = trips.isEmpty() && spotsWithoutTrip.isEmpty()
}

/**
 * ViewModel que observa as viagens e os pontos que ainda não foram agrupados
 * em nenhuma delas.
 */
class TripsViewModel(
    private val repository: TripRepository
) : ViewModel() {

    val uiState: StateFlow<TripsUiState> = combine(
        repository.getAllTripsStream(),
        repository.getSpotsWithoutTripStream()
    ) { trips, looseSpots ->
        TripsUiState(
            trips = trips,
            spotsWithoutTrip = looseSpots,
            isLoading = false
        )
    }.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5000),
        initialValue = TripsUiState(isLoading = true)
    )
}
