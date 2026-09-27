package com.app.pictravelly.feature.spot_detail

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.app.pictravelly.core.data.TouristSpotRepository
import com.app.pictravelly.core.database.model.touristSpot.TouristSpotWithImages
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

data class SpotDetailUiState(
    val spotWithImages: TouristSpotWithImages? = null,
    val isLoading: Boolean = true,
    val isDeleted: Boolean = false
)

/**
 * ViewModel que carrega os dados completos de uma entrada do diário para leitura e exclusão.
 */
class SpotDetailViewModel(
    private val repository: TouristSpotRepository,
    private val spotId: Long
) : ViewModel() {

    private val _uiState = MutableStateFlow(SpotDetailUiState())
    val uiState: StateFlow<SpotDetailUiState> = _uiState.asStateFlow()

    init {
        loadSpot()
    }

    private fun loadSpot() {
        viewModelScope.launch {
            repository.getSpotStream(spotId).collect { spot ->
                _uiState.update {
                    it.copy(
                        spotWithImages = spot,
                        isLoading = false
                    )
                }
            }
        }
    }

    fun deleteSpot(onDeleted: () -> Unit) {
        viewModelScope.launch {
            repository.deleteSpot(spotId)
            _uiState.update { it.copy(isDeleted = true) }
            onDeleted()
        }
    }
}
