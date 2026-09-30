package com.app.pictravelly.feature.trips

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.app.pictravelly.core.data.TripRepository
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

data class TripItemUiModel(
    val id: Long,
    val title: String,
    val description: String,
    val period: String,
    val coverUri: String?,
    val spotsCount: Int
)

data class TripsUiState(
    val trips: List<TripItemUiModel> = emptyList(),
    val looseSpotsCount: Int = 0,
    val looseSpotsCoverUri: String? = null,
    val isLoading: Boolean = true
) {
    val totalTrips: Int get() = trips.size
    val totalSpots: Int get() = trips.sumOf { it.spotsCount } + looseSpotsCount
    val isEmpty: Boolean get() = trips.isEmpty() && looseSpotsCount == 0
}

class TripsViewModel(
    private val repository: TripRepository
) : ViewModel() {

    private val dateFormat = SimpleDateFormat("dd 'de' MMM, yyyy", Locale.forLanguageTag("pt-BR"))
    private val shortDateFormat = SimpleDateFormat("dd 'de' MMM", Locale.forLanguageTag("pt-BR"))

    val uiState: StateFlow<TripsUiState> = combine(
        repository.getAllTripsStream(),
        repository.getSpotsWithoutTripStream()
    ) { trips, looseSpots ->

        val mappedTrips = trips.map { tripWithSpots ->
            val trip = tripWithSpots.trip
            TripItemUiModel(
                id = trip.id,
                title = trip.title,
                description = trip.description,
                period = formatPeriod(trip.startDate, trip.endDate),
                coverUri = tripWithSpots.displayCoverUri,
                spotsCount = tripWithSpots.spotsCount
            )
        }

        TripsUiState(
            trips = mappedTrips,
            looseSpotsCount = looseSpots.size,
            looseSpotsCoverUri = looseSpots.firstNotNullOfOrNull { it.coverImageUri },
            isLoading = false
        )
    }.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5000),
        initialValue = TripsUiState(isLoading = true)
    )

    private fun formatPeriod(startDate: Long, endDate: Long?): String {
        val start = Date(startDate)
        if (endDate == null) return "${dateFormat.format(start)} · em andamento"
        val end = Date(endDate)
        if (startDate == endDate) return dateFormat.format(start)
        return "${shortDateFormat.format(start)} - ${dateFormat.format(end)}"
    }
}