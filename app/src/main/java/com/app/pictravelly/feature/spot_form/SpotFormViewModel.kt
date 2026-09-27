package com.app.pictravelly.feature.spot_form

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.app.pictravelly.core.data.TouristSpotRepository
import com.app.pictravelly.core.data.TripRepository
import com.app.pictravelly.core.database.model.touristSpot.SpotImageEntity
import com.app.pictravelly.core.database.model.touristSpot.TouristSpotEntity
import com.app.pictravelly.core.database.model.trip.TripEntity
import com.app.pictravelly.core.location.LocationHelper
import com.app.pictravelly.core.navigation.DestinationScreen
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

data class SpotFormUiState(
    val title: String = "",
    val description: String = "",
    val locationName: String = "",
    val latitude: Double = LocationHelper.DEFAULT_LATITUDE,
    val longitude: Double = LocationHelper.DEFAULT_LONGITUDE,
    val imageUris: List<String> = emptyList(),
    /** Viagem escolhida, ou null para "Sem viagem". */
    val tripId: Long? = null,
    val availableTrips: List<TripEntity> = emptyList(),
    val isLocatingDevice: Boolean = false,
    val isResolvingAddress: Boolean = false,
    /** True quando as coordenadas ainda são o ponto neutro de fallback. */
    val isUsingFallbackLocation: Boolean = true,
    val isSaving: Boolean = false,
    val errorMessage: String? = null
) {
    val isValid: Boolean
        get() = title.isNotBlank() && locationName.isNotBlank()

    val selectedTripTitle: String
        get() = availableTrips.firstOrNull { it.id == tripId }?.title ?: "Sem viagem"
}

/**
 * ViewModel que valida e persiste um novo ponto turístico ou relato de diário.
 *
 * A captura de GPS e a geocodificação acontecem na tela, que tem acesso ao
 * Context, e chegam aqui apenas como dados já resolvidos.
 */
class SpotFormViewModel(
    private val repository: TouristSpotRepository,
    private val tripRepository: TripRepository,
    initialTripId: Long = DestinationScreen.NO_TRIP_ID
) : ViewModel() {

    private val _uiState = MutableStateFlow(
        SpotFormUiState(
            tripId = initialTripId.takeIf { it != DestinationScreen.NO_TRIP_ID }
        )
    )
    val uiState: StateFlow<SpotFormUiState> = _uiState.asStateFlow()

    init {
        observeTrips()
    }

    /** Alimenta o seletor de viagem do formulário. */
    private fun observeTrips() {
        viewModelScope.launch {
            tripRepository.getTripsForPickerStream().collect { trips ->
                _uiState.update { it.copy(availableTrips = trips) }
            }
        }
    }

    fun updateTitle(newTitle: String) {
        _uiState.update { it.copy(title = newTitle, errorMessage = null) }
    }

    fun updateDescription(newDescription: String) {
        _uiState.update { it.copy(description = newDescription) }
    }

    fun updateLocationName(newLocationName: String) {
        _uiState.update { it.copy(locationName = newLocationName, errorMessage = null) }
    }

    fun updateTripId(newTripId: Long?) {
        _uiState.update { it.copy(tripId = newTripId) }
    }

    /**
     * [isFallback] indica que as coordenadas são o ponto neutro do
     * [LocationHelper], e não uma posição real: o mapa então abre afastado em
     * vez de dar zoom de rua em um lugar errado.
     */
    fun updateCoordinates(lat: Double, lng: Double, isFallback: Boolean = false) {
        _uiState.update {
            it.copy(
                latitude = lat,
                longitude = lng,
                isUsingFallbackLocation = isFallback,
                errorMessage = null
            )
        }
    }

    fun setLocatingDevice(isLocating: Boolean) {
        _uiState.update { it.copy(isLocatingDevice = isLocating) }
    }

    fun setResolvingAddress(isResolving: Boolean) {
        _uiState.update { it.copy(isResolvingAddress = isResolving) }
    }

    fun setErrorMessage(message: String?) {
        _uiState.update { it.copy(errorMessage = message) }
    }

    fun addImageUri(uri: String) {
        if (!_uiState.value.imageUris.contains(uri)) {
            _uiState.update { it.copy(imageUris = it.imageUris + uri) }
        }
    }

    fun removeImageUri(uri: String) {
        _uiState.update { it.copy(imageUris = it.imageUris - uri) }
    }

    fun saveSpot(onSuccess: (Long) -> Unit) {
        val currentState = _uiState.value
        if (!currentState.isValid) {
            _uiState.update { it.copy(errorMessage = "Título e Localização são obrigatórios.") }
            return
        }

        viewModelScope.launch {
            _uiState.update { it.copy(isSaving = true) }
            try {
                val spot = TouristSpotEntity(
                    tripId = currentState.tripId,
                    title = currentState.title.trim(),
                    description = currentState.description.trim(),
                    locationName = currentState.locationName.trim(),
                    visitDate = System.currentTimeMillis(),
                    latitude = currentState.latitude,
                    longitude = currentState.longitude
                )

                val images = currentState.imageUris.mapIndexed { index, uri ->
                    SpotImageEntity(
                        spotId = 0,
                        imageUri = uri,
                        isCover = index == 0
                    )
                }

                val generatedId = repository.insertSpotWithImages(spot, images)
                _uiState.update { it.copy(isSaving = false) }
                onSuccess(generatedId)
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
