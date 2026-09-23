package com.app.pictravelly.feature.inicio

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.app.pictravelly.data.local.relacao.ViagemComPontos
import com.app.pictravelly.data.repositorio.RepositorioDoDiario
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn

data class EstadoInicio(
    val viagens: List<ViagemComPontos> = emptyList(),
    val carregando: Boolean = true
)

class InicioViewModel(repositorio: RepositorioDoDiario) : ViewModel() {

    val estado = repositorio.observarViagens()
        .map { EstadoInicio(viagens = it, carregando = false) }
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(TEMPO_PARADA_MS),
            initialValue = EstadoInicio()
        )

    private companion object {
        const val TEMPO_PARADA_MS = 5_000L
    }
}
