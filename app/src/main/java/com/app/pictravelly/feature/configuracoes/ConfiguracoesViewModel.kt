package com.app.pictravelly.feature.configuracoes

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.app.pictravelly.core.configuracoes.ConfiguracaoDoMapa
import com.app.pictravelly.core.configuracoes.RepositorioDeConfiguracoes
import com.app.pictravelly.core.configuracoes.TipoMapa
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

class ConfiguracoesViewModel(
    private val repositorioDeConfiguracoes: RepositorioDeConfiguracoes
) : ViewModel() {

    val configuracao = repositorioDeConfiguracoes.mapaConfig.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(TEMPO_PARADA_MS),
        initialValue = ConfiguracaoDoMapa()
    )

    fun definirZoom(zoom: Float) = viewModelScope.launch {
        repositorioDeConfiguracoes.definirZoomPadrao(zoom)
    }

    fun definirTipoMapa(tipo: TipoMapa) = viewModelScope.launch {
        repositorioDeConfiguracoes.definirTipoMapa(tipo)
    }

    fun definirMostrarTransito(mostrar: Boolean) = viewModelScope.launch {
        repositorioDeConfiguracoes.definirMostrarTransito(mostrar)
    }

    private companion object {
        const val TEMPO_PARADA_MS = 5_000L
    }
}
