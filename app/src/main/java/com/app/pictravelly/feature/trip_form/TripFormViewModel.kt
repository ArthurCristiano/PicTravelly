package com.app.pictravelly.feature.trip_form

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.app.pictravelly.core.data.TripRepository
import com.app.pictravelly.core.database.model.trip.TripEntity
import com.app.pictravelly.core.navigation.DestinationScreen
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

data class TripFormUiState(
    val title: String = "",
    val description: String = "",
    val startDate: Long = System.currentTimeMillis(),
    val endDate: Long? = null,
    val coverImageUri: String? = null,
    val isEditing: Boolean = false,
    val isLoading: Boolean = false,
    val isSaving: Boolean = false,
    val errorMessage: String? = null
) {
    val hasValidPeriod: Boolean
        get() = endDate == null || endDate >= startDate

    val isValid: Boolean
        get() = title.isNotBlank() && hasValidPeriod
}

/**
 * ViewModel que valida e persiste uma viagem, criando uma nova ou editando
 * uma existente quando recebe um tripId válido.
 */
class TripFormViewModel(
    private val repository: TripRepository,
    private val tripId: Long = DestinationScreen.NO_TRIP_ID
) : ViewModel() {

    private val _uiState = MutableStateFlow(TripFormUiState())
    val uiState: StateFlow<TripFormUiState> = _uiState.asStateFlow()

    private val isEditing = tripId != DestinationScreen.NO_TRIP_ID

    init {
        if (isEditing) loadTrip()
    }

    private fun loadTrip() {
        _uiState.update { it.copy(isLoading = true, isEditing = true) }
        viewModelScope.launch {
            val trip = repository.getTripOnce(tripId)
            if (trip == null) {
                _uiState.update {
                    it.copy(isLoading = false, errorMessage = "Viagem não encontrada.")
                }
                return@launch
            }
            _uiState.update {
                it.copy(
                    title = trip.title,
                    description = trip.description,
                    startDate = trip.startDate,
                    endDate = trip.endDate,
                    coverImageUri = trip.coverImageUri,
                    isEditing = true,
                    isLoading = false
                )
            }
        }
    }

    fun updateTitle(newTitle: String) {
        _uiState.update { it.copy(title = newTitle, errorMessage = null) }
    }

    fun updateDescription(newDescription: String) {
        _uiState.update { it.copy(description = newDescription) }
    }

    fun updateStartDate(millis: Long) {
        _uiState.update { it.copy(startDate = millis, errorMessage = null) }
    }

    fun updateEndDate(millis: Long?) {
        _uiState.update { it.copy(endDate = millis, errorMessage = null) }
    }

    fun updateCoverImageUri(uri: String?) {
        _uiState.update { it.copy(coverImageUri = uri) }
    }

    fun saveTrip(onSuccess: (Long) -> Unit) {
        val currentState = _uiState.value

        if (currentState.title.isBlank()) {
            _uiState.update { it.copy(errorMessage = "O título da viagem é obrigatório.") }
            return
        }
        if (!currentState.hasValidPeriod) {
            _uiState.update {
                it.copy(errorMessage = "A data de fim não pode ser anterior à de início.")
            }
            return
        }

        viewModelScope.launch {
            _uiState.update { it.copy(isSaving = true) }
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

                _uiState.update { it.copy(isSaving = false) }
                onSuccess(savedId)
            } catch (e: Exception) {
                _uiState.update {
                    it.copy(
                        isSaving = false,
                        errorMessage = "Erro ao salvar: ${e.localizedMessage}"
                    )
                }
            }
        }
    }
}
