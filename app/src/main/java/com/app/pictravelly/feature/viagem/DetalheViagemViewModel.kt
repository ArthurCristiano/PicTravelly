package com.app.pictravelly.feature.viagem

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.app.pictravelly.core.navegacao.Rotas
import com.app.pictravelly.data.local.entidade.Ponto
import com.app.pictravelly.data.local.relacao.ViagemComPontos
import com.app.pictravelly.data.repositorio.RepositorioDoDiario
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

data class EstadoDetalheViagem(
    val viagem: ViagemComPontos? = null,
    val carregando: Boolean = true
)

class DetalheViagemViewModel(
    private val repositorio: RepositorioDoDiario,
    savedStateHandle: SavedStateHandle
) : ViewModel() {

    val viagemId: Long = checkNotNull(savedStateHandle[Rotas.ARG_VIAGEM_ID])

    val estado = repositorio.observarViagem(viagemId)
        .map { EstadoDetalheViagem(viagem = it, carregando = false) }
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(TEMPO_PARADA_MS),
            initialValue = EstadoDetalheViagem()
        )

    fun apagarViagem() {
        val viagem = estado.value.viagem?.viagem ?: return
        viewModelScope.launch { repositorio.apagarViagem(viagem) }
    }

    fun apagarPonto(ponto: Ponto) {
        viewModelScope.launch { repositorio.apagarPonto(ponto) }
    }

    private companion object {
        const val TEMPO_PARADA_MS = 5_000L
    }
}
