package com.app.pictravelly.feature.trip_form

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.app.pictravelly.core.data.TripRepository
import com.app.pictravelly.core.database.model.trip.TripEntity
import com.app.pictravelly.core.navigation.DestinationScreen
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import java.text.SimpleDateFormat
import java.time.Instant
import java.time.ZoneId
import java.time.ZoneOffset
import java.util.Date
import java.util.Locale

/** Estado transitório puro contendo os valores originais. */
private data class TripFormTransientState(
    val title: String = "",
    val description: String = "",
    val startDate: Long = System.currentTimeMillis(),
    val endDate: Long? = null,
    val coverImageUri: String? = null,
    val isEditing: Boolean = false,
    val isLoading: Boolean = false,
    val isSaving: Boolean = false,
    val errorMessage: String? = null
)

/** Estado da UI formatado e pronto para renderização. */
data class TripFormUiState(
    val title: String = "",
    val description: String = "",
    val coverImageUri: String? = null,
    val isEditing: Boolean = false,
    val isLoading: Boolean = false,
    val isSaving: Boolean = false,
    val errorMessage: String? = null,
    val formattedStartDate: String = "",
    val formattedEndDate: String = "",
    val startDateUtcMillis: Long = 0L,
    val endDateUtcMillis: Long = 0L,
    val isValid: Boolean = false
)

class TripFormViewModel(
    private val repository: TripRepository,
    private val tripId: Long = DestinationScreen.NO_TRIP_ID
) : ViewModel() {

    private val isEditing = tripId != DestinationScreen.NO_TRIP_ID
    private val dateFormat = SimpleDateFormat("dd 'de' MMMM, yyyy", Locale.forLanguageTag("pt-BR"))

    // Mantemos os valores brutos protegidos aqui
    private val _transientState = MutableStateFlow(TripFormTransientState(isEditing = isEditing))

    // A UI consome apenas este fluxo com os dados já mastigados
    val uiState: StateFlow<TripFormUiState> = _transientState.map { state ->
        val hasValidPeriod = state.endDate == null || state.endDate >= state.startDate

        TripFormUiState(
            title = state.title,
            description = state.description,
            coverImageUri = state.coverImageUri,
            isEditing = state.isEditing,
            isLoading = state.isLoading,
            isSaving = state.isSaving,
            errorMessage = state.errorMessage,
            formattedStartDate = dateFormat.format(Date(state.startDate)),
            formattedEndDate = state.endDate?.let { dateFormat.format(Date(it)) } ?: "Em andamento",
            startDateUtcMillis = localToUtcMillis(state.startDate),
            endDateUtcMillis = localToUtcMillis(state.endDate ?: state.startDate),
            isValid = state.title.isNotBlank() && hasValidPeriod
        )
    }.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5_000),
        initialValue = TripFormUiState(isLoading = true)
    )

    init {
        if (isEditing) loadTrip()
    }

    private fun loadTrip() {
        _transientState.update { it.copy(isLoading = true) }
        viewModelScope.launch {
            val trip = repository.getTripOnce(tripId)
            if (trip == null) {
                _transientState.update {
                    it.copy(
                        isLoading = false,
                        errorMessage = "Viagem não encontrada."
                    )
                }
                return@launch
            }
            _transientState.update {
                it.copy(
                    title = trip.title,
                    description = trip.description,
                    startDate = trip.startDate,
                    endDate = trip.endDate,
                    coverImageUri = trip.coverImageUri,
                    isLoading = false
                )
            }
        }
    }

    // Conversões de fuso horário isoladas na ViewModel
    private fun localToUtcMillis(millis: Long): Long =
        Instant.ofEpochMilli(millis).atZone(ZoneId.systemDefault()).toLocalDate()
            .atStartOfDay(ZoneOffset.UTC).toInstant().toEpochMilli()

    private fun utcToLocalMillis(millis: Long): Long =
        Instant.ofEpochMilli(millis).atZone(ZoneOffset.UTC).toLocalDate()
            .atStartOfDay(ZoneId.systemDefault()).toInstant().toEpochMilli()

    fun updateTitle(newTitle: String) =
        _transientState.update { it.copy(title = newTitle, errorMessage = null) }

    fun updateDescription(newDescription: String) =
        _transientState.update { it.copy(description = newDescription) }

    fun updateCoverImageUri(uri: String?) = _transientState.update { it.copy(coverImageUri = uri) }

    // O DatePicker devolve UTC, a ViewModel salva como Local Time
    fun updateStartDateFromUtc(utcMillis: Long) = _transientState.update {
        it.copy(startDate = utcToLocalMillis(utcMillis), errorMessage = null)
    }

    fun updateEndDateFromUtc(utcMillis: Long?) = _transientState.update {
        it.copy(endDate = utcMillis?.let { utcToLocalMillis(it) }, errorMessage = null)
    }

    fun saveTrip(onSuccess: (Long) -> Unit) {
        val currentState = _transientState.value
        val hasValidPeriod =
            currentState.endDate == null || currentState.endDate >= currentState.startDate

        if (currentState.title.isBlank()) {
            _transientState.update { it.copy(errorMessage = "O título da viagem é obrigatório.") }
            return
        }
        if (!hasValidPeriod) {
            _transientState.update { it.copy(errorMessage = "A data de fim não pode ser anterior à de início.") }
            return
        }

        viewModelScope.launch {
            _transientState.update { it.copy(isSaving = true) }
            try {
                val trip = TripEntity(
                    id = if (isEditing) tripId else 0L,
                    title = currentState.title.trim(),
                    description = currentState.description.trim(),
                    startDate = currentState.startDate,
                    endDate = currentState.endDate,
                    coverImageUri = currentState.coverImageUri
                )

                val savedId = if (isEditing) {
                    repository.updateTrip(trip)
                    tripId
                } else {
                    repository.insertTrip(trip)
                }

                _transientState.update { it.copy(isSaving = false) }
                onSuccess(savedId)
            } catch (e: Exception) {
                _transientState.update {
                    it.copy(
                        isSaving = false,
                        errorMessage = "Erro ao salvar: ${e.localizedMessage}"
                    )
                }
            }
        }
    }
}