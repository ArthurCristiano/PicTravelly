package com.app.pictravelly.feature.spots

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.app.pictravelly.core.data.TouristSpotRepository
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.FlowPreview
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.debounce
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.stateIn
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

/**
 * Modelo de dados exclusivo e otimizado para a Tela.
 * Elimina o esforço de processamento visual no Jetpack Compose.
 */
data class SpotItemUiModel(
    val id: Long,
    val title: String,
    val locationName: String,
    val description: String,
    val coverImageUri: String?,
    val formattedDate: String
)

/**
 * Estado consolidado da tela de listagem de diários.
 */
data class SpotsUiState(
    val searchQuery: String = "",
    val spots: List<SpotItemUiModel> = emptyList(),
    val isLoading: Boolean = true
)

@OptIn(FlowPreview::class, ExperimentalCoroutinesApi::class)
class SpotsViewModel(
    private val repository: TouristSpotRepository
) : ViewModel() {

    // Instanciado uma única vez na ViewModel, poupando a View
    private val dateFormatter =
        SimpleDateFormat("dd 'de' MMM, yyyy", Locale.forLanguageTag("pt-BR"))

    // Fluxo instantâneo: Garante que a digitação não trave na tela
    private val _searchQuery = MutableStateFlow("")

    // Fluxo atrasado: Garante que o banco não seja inundado de pesquisas a cada letra
    private val _searchResults = _searchQuery
        .debounce(300)
        .flatMapLatest { query ->
            repository.searchSpotsStream(query)
        }

    // A Mágica: Junta o texto instantâneo da UI com a pesquisa atrasada do Banco
    val uiState: StateFlow<SpotsUiState> = combine(
        _searchQuery,
        _searchResults
    ) { query, spotsList ->
        // Mapeia a entidade pesada para um modelo leve de UI
        val mappedSpots = spotsList.map { spotWithImages ->
            val spot = spotWithImages.spot
            SpotItemUiModel(
                id = spot.id,
                title = spot.title,
                locationName = spot.locationName,
                description = spot.description,
                coverImageUri = spotWithImages.coverImageUri,
                formattedDate = dateFormatter.format(Date(spot.visitDate))
            )
        }

        SpotsUiState(
            searchQuery = query,
            spots = mappedSpots,
            isLoading = false
        )
    }.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5_000),
        initialValue = SpotsUiState(isLoading = true)
    )

    fun onSearchQueryChanged(newQuery: String) {
        _searchQuery.value = newQuery
    }
}