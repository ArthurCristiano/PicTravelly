package com.app.pictravelly.feature.viagem

import android.net.Uri
import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.app.pictravelly.core.midia.ArmazenamentoDeImagens
import com.app.pictravelly.core.navegacao.Rotas
import com.app.pictravelly.core.ui.util.Datas
import com.app.pictravelly.data.local.entidade.Viagem
import com.app.pictravelly.data.repositorio.RepositorioDoDiario
import java.io.File
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

data class EstadoEditorViagem(
    val titulo: String = "",
    val descricao: String = "",
    val dataInicio: Long = Datas.hoje(),
    val dataFim: Long? = null,
    val caminhoCapa: String? = null,
    val edicao: Boolean = false,
    val carregando: Boolean = true,
    val salvando: Boolean = false,
    val salvo: Boolean = false,
    val erro: String? = null
) {
    val podeSalvar: Boolean
        get() = titulo.isNotBlank() && !salvando &&
            (dataFim == null || dataFim >= dataInicio)
}

class EditorViagemViewModel(
    private val repositorio: RepositorioDoDiario,
    private val armazenamentoDeImagens: ArmazenamentoDeImagens,
    savedStateHandle: SavedStateHandle
) : ViewModel() {

    private val viagemId: Long = savedStateHandle[Rotas.ARG_VIAGEM_ID] ?: Rotas.SEM_ID

    private val _estado = MutableStateFlow(EstadoEditorViagem())
    val estado = _estado.asStateFlow()

    /** Capa gravada nesta sessao de edicao mas ainda nao confirmada no banco. */
    private var capaOrfa: String? = null

    init {
        if (viagemId == Rotas.SEM_ID) {
            _estado.update { it.copy(carregando = false, edicao = false) }
        } else {
            carregar()
        }
    }

    private fun carregar() = viewModelScope.launch {
        val viagem = repositorio.buscarViagem(viagemId)
        if (viagem == null) {
            _estado.update { it.copy(carregando = false, erro = "Viagem não encontrada.") }
            return@launch
        }
        _estado.update {
            it.copy(
                titulo = viagem.titulo,
                descricao = viagem.descricao,
                dataInicio = viagem.dataInicio,
                dataFim = viagem.dataFim,
                caminhoCapa = viagem.caminhoCapa,
                edicao = true,
                carregando = false
            )
        }
    }

    fun aoMudarTitulo(valor: String) = _estado.update { it.copy(titulo = valor, erro = null) }

    fun aoMudarDescricao(valor: String) = _estado.update { it.copy(descricao = valor) }

    fun aoMudarDataInicio(millis: Long) = _estado.update { it.copy(dataInicio = millis) }

    fun aoMudarDataFim(millis: Long?) = _estado.update { it.copy(dataFim = millis) }

    fun aoCapturarFoto(arquivo: File) = viewModelScope.launch {
        trocarCapa(armazenamentoDeImagens.salvarCaptura(arquivo))
    }

    fun aoEscolherFoto(uri: Uri) = viewModelScope.launch {
        trocarCapa(armazenamentoDeImagens.salvarDaGaleria(uri))
    }

    fun aoRemoverFoto() = viewModelScope.launch {
        descartarCapaOrfa()
        _estado.update { it.copy(caminhoCapa = null) }
    }

    private suspend fun trocarCapa(novoCaminho: String?) {
        if (novoCaminho == null) {
            _estado.update { it.copy(erro = "Não foi possível salvar a imagem.") }
            return
        }
        descartarCapaOrfa()
        capaOrfa = novoCaminho
        _estado.update { it.copy(caminhoCapa = novoCaminho, erro = null) }
    }

    /** Apaga o arquivo que o usuario trocou antes de salvar, para nao ficar lixo. */
    private suspend fun descartarCapaOrfa() {
        capaOrfa?.let { armazenamentoDeImagens.apagar(it) }
        capaOrfa = null
    }

    fun salvar() {
        val atual = _estado.value
        if (!atual.podeSalvar) return

        _estado.update { it.copy(salvando = true) }
        viewModelScope.launch {
            val viagem = Viagem(
                id = if (atual.edicao) viagemId else 0L,
                titulo = atual.titulo.trim(),
                descricao = atual.descricao.trim(),
                dataInicio = atual.dataInicio,
                dataFim = atual.dataFim,
                caminhoCapa = atual.caminhoCapa
            )
            repositorio.salvarViagem(viagem)
            // A capa agora pertence a uma viagem gravada: nao e mais orfa.
            capaOrfa = null
            _estado.update { it.copy(salvando = false, salvo = true) }
        }
    }

    /** Chamado quando a tela e abandonada sem salvar. */
    fun descartar() {
        viewModelScope.launch { descartarCapaOrfa() }
    }
}
