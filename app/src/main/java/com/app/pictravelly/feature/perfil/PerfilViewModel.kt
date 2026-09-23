package com.app.pictravelly.feature.perfil

import android.net.Uri
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.app.pictravelly.core.configuracoes.Perfil
import com.app.pictravelly.core.configuracoes.RepositorioDeConfiguracoes
import com.app.pictravelly.core.midia.ArmazenamentoDeImagens
import com.app.pictravelly.data.repositorio.RepositorioDoDiario
import java.io.File
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

data class EstadoPerfil(
    val perfil: Perfil = Perfil(),
    val totalDeViagens: Int = 0,
    val totalDePontos: Int = 0
)

class PerfilViewModel(
    repositorio: RepositorioDoDiario,
    private val repositorioDeConfiguracoes: RepositorioDeConfiguracoes,
    private val armazenamentoDeImagens: ArmazenamentoDeImagens
) : ViewModel() {

    val estado = combine(
        repositorioDeConfiguracoes.perfil,
        repositorio.contarViagens(),
        repositorio.contarPontos()
    ) { perfil, viagens, pontos ->
        EstadoPerfil(perfil = perfil, totalDeViagens = viagens, totalDePontos = pontos)
    }.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(TEMPO_PARADA_MS),
        initialValue = EstadoPerfil()
    )

    private val _editando = MutableStateFlow(false)
    val editando = _editando.asStateFlow()

    fun abrirEdicao() = _editando.update { true }

    fun fecharEdicao() = _editando.update { false }

    fun salvarDados(nome: String, bio: String) = viewModelScope.launch {
        repositorioDeConfiguracoes.salvarPerfil(
            estado.value.perfil.copy(nome = nome.trim(), bio = bio.trim())
        )
        _editando.update { false }
    }

    fun aoCapturarAvatar(arquivo: File) = viewModelScope.launch {
        trocarAvatar(armazenamentoDeImagens.salvarCaptura(arquivo))
    }

    fun aoEscolherAvatar(uri: Uri) = viewModelScope.launch {
        trocarAvatar(armazenamentoDeImagens.salvarDaGaleria(uri))
    }

    fun aoRemoverAvatar() = viewModelScope.launch {
        val atual = estado.value.perfil
        repositorioDeConfiguracoes.salvarPerfil(atual.copy(caminhoAvatar = null))
        armazenamentoDeImagens.apagar(atual.caminhoAvatar)
    }

    private suspend fun trocarAvatar(novoCaminho: String?) {
        if (novoCaminho == null) return
        val atual = estado.value.perfil
        repositorioDeConfiguracoes.salvarPerfil(atual.copy(caminhoAvatar = novoCaminho))
        // A foto antiga nao e mais referenciada por ninguem.
        armazenamentoDeImagens.apagar(atual.caminhoAvatar)
    }

    private companion object {
        const val TEMPO_PARADA_MS = 5_000L
    }
}
