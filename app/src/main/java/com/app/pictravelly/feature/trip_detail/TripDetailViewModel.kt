package com.app.pictravelly.feature.trip_detail

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.app.pictravelly.core.data.TouristSpotRepository
import com.app.pictravelly.core.data.TripRepository
import com.app.pictravelly.core.database.model.touristSpot.TouristSpotWithImages
import com.app.pictravelly.core.navigation.DestinationScreen
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

/**
 * Modelo pré-formatado (sem lógica) que a Tela consome.
 */
data class TripDetailHeaderUiModel(
    val id: Long,
    val title: String,
    val description: String,
    val coverImageUri: String?,
    val formattedPeriod: String
)

/**
 * Estado provisório (Ações do Usuário).
 */
private data class TripDetailTransientState(
    val wasDeleted: Boolean = false
)

/**
 * Estado da tela de detalhe de uma viagem.
 */
data class TripDetailUiState(
    val tripHeader: TripDetailHeaderUiModel? = null,
    val spots: List<TouristSpotWithImages> = emptyList(),
    val isLoading: Boolean = true,
    val isLooseGroup: Boolean = false,
    val wasDeleted: Boolean = false
) {
    val displayTitle: String
        get() = if (isLooseGroup) "Pontos sem viagem" else tripHeader?.title.orEmpty()

    val spotsCount: Int get() = spots.size
}

@OptIn(ExperimentalCoroutinesApi::class)
class TripDetailViewModel(
    private val tripRepository: TripRepository,
    private val spotRepository: TouristSpotRepository,
    private val tripId: Long
) : ViewModel() {

    private val isLooseGroup = tripId == DestinationScreen.NO_TRIP_ID

    private val dateFormat = SimpleDateFormat("dd 'de' MMM, yyyy", Locale.forLanguageTag("pt-BR"))
    private val shortDateFormat = SimpleDateFormat("dd 'de' MMM", Locale.forLanguageTag("pt-BR"))

    private val _transientState = MutableStateFlow(TripDetailTransientState())

    // A Mágica: Decide automaticamente qual fluxo escutar baseado se é "LooseGroup" ou não.
    private val _databaseFlow = if (isLooseGroup) {
        tripRepository.getSpotsWithoutTripStream().flatMapLatest { spots ->
            flowOf(null to spots) // Retorna header nulo + lista de spots
        }
    } else {
        tripRepository.getTripStream(tripId).flatMapLatest { tripWithSpots ->
            flowOf(tripWithSpots?.trip to tripWithSpots?.spots.orEmpty())
        }
    }

    val uiState: StateFlow<TripDetailUiState> = combine(
        _databaseFlow,
        _transientState
    ) { (tripEntity, spotsList), transient ->

        // Verifica se a viagem foi apagada no banco
        val isDeletedInDb = !isLooseGroup && tripEntity == null

        val headerUiModel = tripEntity?.let { trip ->
            TripDetailHeaderUiModel(
                id = trip.id,
                title = trip.title,
                description = trip.description,
                coverImageUri = trip.coverImageUri,
                formattedPeriod = formatPeriod(trip.startDate, trip.endDate)
            )
        }

        TripDetailUiState(
            tripHeader = headerUiModel,
            spots = spotsList,
            isLoading = false,
            isLooseGroup = isLooseGroup,
            wasDeleted = transient.wasDeleted || isDeletedInDb // Une deleção via botão com deleção via DB
        )
    }.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5_000),
        initialValue = TripDetailUiState(isLoading = true, isLooseGroup = isLooseGroup)
    )

    private fun formatPeriod(startDate: Long, endDate: Long?): String {
        val start = Date(startDate)
        if (endDate == null) return "${dateFormat.format(start)} · em andamento"
        val end = Date(endDate)
        if (startDate == endDate) return dateFormat.format(start)
        return "${shortDateFormat.format(start)} - ${dateFormat.format(end)}"
    }

    fun deleteTrip() {
        if (isLooseGroup) return
        viewModelScope.launch {
            tripRepository.deleteTrip(tripId)
            _transientState.update { it.copy(wasDeleted = true) }
        }
    }

    fun removeSpotFromTrip(spotId: Long) {
        viewModelScope.launch { tripRepository.assignSpotToTrip(spotId, null) }
    }

    fun deleteSpot(spotId: Long) {
        viewModelScope.launch { spotRepository.deleteSpot(spotId) }
    }
}