package com.app.pictravelly.feature.spot_form

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.app.pictravelly.core.data.TouristSpotRepository
import com.app.pictravelly.core.database.model.SpotImageEntity
import com.app.pictravelly.core.database.model.TouristSpotEntity
import com.app.pictravelly.core.location.LocationHelper
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
    val isSaving: Boolean = false,
    val errorMessage: String? = null
) {
    val isValid: Boolean
        get() = title.isNotBlank() && locationName.isNotBlank()
}

/**
 * ViewModel que valida e persiste um novo ponto turístico ou relato de diário.
 */
class SpotFormViewModel(
    private val repository: TouristSpotRepository
) : ViewModel() {

    private val _uiState = MutableStateFlow(SpotFormUiState())
    val uiState: StateFlow<SpotFormUiState> = _uiState.asStateFlow()

    fun updateTitle(newTitle: String) {
        _uiState.update { it.copy(title = newTitle, errorMessage = null) }
    }

    fun updateDescription(newDescription: String) {
        _uiState.update { it.copy(description = newDescription) }
    }

    fun updateLocationName(newLocationName: String) {
        _uiState.update { it.copy(locationName = newLocationName, errorMessage = null) }
    }

    fun updateCoordinates(lat: Double, lng: Double) {
        _uiState.update { it.copy(latitude = lat, longitude = lng) }
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
