package com.app.pictravelly.feature.spot_detail

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.app.pictravelly.core.data.SettingsRepository
import com.app.pictravelly.core.data.TouristSpotRepository
import com.app.pictravelly.core.database.model.settings.MapEngineType
import com.app.pictravelly.core.database.model.touristSpot.TouristSpotWithImages
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

/**
 * Estado dinâmico provisório (Ações efêmeras da tela).
 */
private data class SpotDetailTransientState(
    val isDeleted: Boolean = false
)

/**
 * Estado consolidado da tela de detalhes.
 */
data class SpotDetailUiState(
    val spotWithImages: TouristSpotWithImages? = null,
    val isLoading: Boolean = true,
    val isDeleted: Boolean = false,
    val mapEngine: MapEngineType = MapEngineType.OSM,
    val mapZoom: Float = 13f // Necessário para os componentes genéricos de mapa
)

/**
 * ViewModel que carrega os dados completos de uma entrada do diário para leitura e exclusão.
 */
class SpotDetailViewModel(
    private val touristSpotRepository: TouristSpotRepository,
    private val settingsRepository: SettingsRepository, // Injetado para ler as preferências globais
    private val spotId: Long
) : ViewModel() {

    private val _transientState = MutableStateFlow(SpotDetailTransientState())

    // A Mágica: Funde o Ponto Turístico com as configurações de Mapa em tempo real
    val uiState: StateFlow<SpotDetailUiState> = combine(
        touristSpotRepository.getSpotStream(spotId),
        settingsRepository.userDataStream,
        _transientState
    ) { spot, userSettings, transient ->
        SpotDetailUiState(
            spotWithImages = spot,
            isLoading = false, // Se o combine emitiu, o banco já respondeu (mesmo que seja null)
            isDeleted = transient.isDeleted,
            mapEngine = userSettings?.mapEngine ?: MapEngineType.OSM,
            mapZoom = userSettings?.lastZoom ?: 13f
        )
    }.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5_000),
        initialValue = SpotDetailUiState(isLoading = true) // Inicia carregando até o banco emitir a 1ª vez
    )

    fun deleteSpot(onDeleted: () -> Unit) {
        viewModelScope.launch {
            touristSpotRepository.deleteSpot(spotId)
            _transientState.update { it.copy(isDeleted = true) }
            onDeleted() // Aciona a navegação via callback
        }
    }

    /**
     * Persiste o nível de zoom se o usuário interagir com o mapa expandido nesta tela.
     */
    fun updateZoom(newZoom: Float) {
        viewModelScope.launch {
            settingsRepository.setLastZoom(newZoom)
        }
    }
}