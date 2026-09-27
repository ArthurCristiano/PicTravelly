package com.app.pictravelly.feature.spots

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.app.pictravelly.core.data.TouristSpotRepository
import com.app.pictravelly.core.database.model.touristSpot.TouristSpotWithImages
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.FlowPreview
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.debounce
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn

/**
 * Estado da tela de listagem de diários.
 */
data class SpotsUiState(
    val searchQuery: String = "",
    val spots: List<TouristSpotWithImages> = emptyList(),
    val isLoading: Boolean = false
)

/**
 * ViewModel que gerencia a busca, ordenação e exibição de todos os locais cadastrados.
 */
@OptIn(FlowPreview::class, ExperimentalCoroutinesApi::class)
class SpotsViewModel(
    private val repository: TouristSpotRepository
) : ViewModel() {

    private val _searchQuery = MutableStateFlow("")
    val searchQuery: StateFlow<String> = _searchQuery.asStateFlow()

    val uiState: StateFlow<SpotsUiState> = _searchQuery
        .debounce(300)
        .flatMapLatest { query ->
            repository.searchSpotsStream(query)
        }
        .map { spotsList ->
            SpotsUiState(
                searchQuery = _searchQuery.value,
                spots = spotsList,
                isLoading = false
            )
        }
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = SpotsUiState(isLoading = true)
        )

    fun onSearchQueryChanged(newQuery: String) {
        _searchQuery.value = newQuery
    }
}
