package com.app.pictravelly.feature.mapa

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.app.pictravelly.core.configuracoes.ConfiguracaoDoMapa
import com.app.pictravelly.core.configuracoes.RepositorioDeConfiguracoes
import com.app.pictravelly.core.navegacao.Rotas
import com.app.pictravelly.data.local.entidade.Ponto
import com.app.pictravelly.data.repositorio.RepositorioDoDiario
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn

data class EstadoMapa(
    val pontos: List<Ponto> = emptyList(),
    val configuracao: ConfiguracaoDoMapa = ConfiguracaoDoMapa(),
    val carregando: Boolean = true
)

/**
 * Mostra no mapa todos os pontos turisticos ja cadastrados, respeitando o
 * zoom padrao e o tipo de mapa escolhidos na tela de configuracao.
 */
class MapaViewModel(
    repositorio: RepositorioDoDiario,
    repositorioDeConfiguracoes: RepositorioDeConfiguracoes,
    savedStateHandle: SavedStateHandle
) : ViewModel() {

    /** Ponto que a tela deve centralizar ao abrir, quando veio de "Ver no mapa". */
    val pontoEmFoco: Long = savedStateHandle[Rotas.ARG_PONTO_ID] ?: Rotas.SEM_ID

    val estado = combine(
        repositorio.observarTodosOsPontos(),
        repositorioDeConfiguracoes.mapaConfig
    ) { pontos, configuracao ->
        EstadoMapa(pontos = pontos, configuracao = configuracao, carregando = false)
    }.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(TEMPO_PARADA_MS),
        initialValue = EstadoMapa()
    )

    private companion object {
        const val TEMPO_PARADA_MS = 5_000L
    }
}
