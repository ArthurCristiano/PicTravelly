package com.app.pictravelly.feature.ponto

import android.net.Uri
import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.app.pictravelly.core.localizacao.ProvedorDeLocalizacao
import com.app.pictravelly.core.midia.ArmazenamentoDeImagens
import com.app.pictravelly.core.navegacao.Rotas
import com.app.pictravelly.core.ui.util.Geocodigos
import com.app.pictravelly.data.local.entidade.Ponto
import com.app.pictravelly.data.local.entidade.Viagem
import com.app.pictravelly.data.repositorio.RepositorioDoDiario
import java.io.File
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

data class EstadoEditorPonto(
    val viagens: List<Viagem> = emptyList(),
    val viagemId: Long = Rotas.SEM_ID,
    val nome: String = "",
    val descricao: String = "",
    val latitude: String = "",
    val longitude: String = "",
    val endereco: String? = null,
    val caminhoImagem: String? = null,
    val edicao: Boolean = false,
    val carregando: Boolean = true,
    val buscandoLocalizacao: Boolean = false,
    val buscandoEndereco: Boolean = false,
    val salvo: Boolean = false,
    val mensagem: String? = null
) {
    val latitudeValida: Boolean
        get() = Geocodigos.interpretar(latitude)?.let(Geocodigos::latitudeValida) == true

    val longitudeValida: Boolean
        get() = Geocodigos.interpretar(longitude)?.let(Geocodigos::longitudeValida) == true

    val podeSalvar: Boolean
        get() = nome.isNotBlank() && latitudeValida && longitudeValida &&
            viagemId != Rotas.SEM_ID

    val nomeDaViagem: String
        get() = viagens.firstOrNull { it.id == viagemId }?.titulo ?: "Escolha uma viagem"
}

/**
 * Cadastro de um ponto turistico: nome, descricao, geocodigos e imagem.
 *
 * Os geocodigos podem vir do GPS ou ser digitados; em ambos os casos o
 * endereco textual e resolvido pela API de geocodificacao reversa.
 */
class EditorPontoViewModel(
    private val repositorio: RepositorioDoDiario,
    private val armazenamentoDeImagens: ArmazenamentoDeImagens,
    private val provedorDeLocalizacao: ProvedorDeLocalizacao,
    savedStateHandle: SavedStateHandle
) : ViewModel() {

    private val pontoId: Long = savedStateHandle[Rotas.ARG_PONTO_ID] ?: Rotas.SEM_ID
    private val viagemIdInicial: Long = savedStateHandle[Rotas.ARG_VIAGEM_ID] ?: Rotas.SEM_ID

    private val _estado = MutableStateFlow(EstadoEditorPonto())
    val estado = _estado.asStateFlow()

    /** Foto gravada nesta sessao mas ainda nao confirmada no banco. */
    private var fotoOrfa: String? = null

    init {
        observarViagens()
        carregarPonto()
    }

    /** Alimenta o seletor de viagem do formulario. */
    private fun observarViagens() = viewModelScope.launch {
        repositorio.observarViagensSimples().collect { lista ->
            _estado.update { atual ->
                val selecionada = when {
                    // Uma escolha ja feita (pela rota ou pelo usuario) tem prioridade.
                    atual.viagemId != Rotas.SEM_ID -> atual.viagemId
                    viagemIdInicial != Rotas.SEM_ID -> viagemIdInicial
                    else -> lista.firstOrNull()?.id ?: Rotas.SEM_ID
                }
                atual.copy(viagens = lista, viagemId = selecionada)
            }
        }
    }

    private fun carregarPonto() = viewModelScope.launch {
        if (pontoId == Rotas.SEM_ID) {
            _estado.update { it.copy(carregando = false) }
            return@launch
        }

        val ponto = repositorio.buscarPonto(pontoId)
        if (ponto == null) {
            _estado.update { it.copy(carregando = false, mensagem = "Ponto não encontrado.") }
            return@launch
        }

        _estado.update {
            it.copy(
                viagemId = ponto.viagemId,
                nome = ponto.nome,
                descricao = ponto.descricao,
                latitude = ponto.latitude.toString(),
                longitude = ponto.longitude.toString(),
                endereco = ponto.endereco,
                caminhoImagem = ponto.caminhoImagem,
                edicao = true,
                carregando = false
            )
        }
    }

    fun aoMudarViagem(id: Long) = _estado.update { it.copy(viagemId = id) }

    fun aoMudarNome(valor: String) = _estado.update { it.copy(nome = valor, mensagem = null) }

    fun aoMudarDescricao(valor: String) = _estado.update { it.copy(descricao = valor) }

    // Mexeu nos geocodigos, o endereco antigo deixa de valer.
    fun aoMudarLatitude(valor: String) =
        _estado.update { it.copy(latitude = valor, endereco = null, mensagem = null) }

    fun aoMudarLongitude(valor: String) =
        _estado.update { it.copy(longitude = valor, endereco = null, mensagem = null) }

    /** Preenche os geocodigos com a posicao atual do aparelho. */
    fun usarLocalizacaoAtual() {
        if (!provedorDeLocalizacao.temPermissao()) {
            _estado.update { it.copy(mensagem = "Permissão de localização negada.") }
            return
        }

        _estado.update { it.copy(buscandoLocalizacao = true, mensagem = null) }
        viewModelScope.launch {
            val coordenada = provedorDeLocalizacao.posicaoAtual()
            if (coordenada == null) {
                _estado.update {
                    it.copy(
                        buscandoLocalizacao = false,
                        mensagem = "Não foi possível obter a localização. " +
                            "Verifique se o GPS está ligado."
                    )
                }
                return@launch
            }

            _estado.update {
                it.copy(
                    latitude = coordenada.latitude.toString(),
                    longitude = coordenada.longitude.toString(),
                    endereco = null,
                    buscandoLocalizacao = false
                )
            }
            resolverEndereco()
        }
    }

    /** Converte os geocodigos atuais em endereco textual. */
    fun resolverEndereco() {
        val atual = _estado.value
        val latitude = Geocodigos.interpretar(atual.latitude)
        val longitude = Geocodigos.interpretar(atual.longitude)

        if (latitude == null || longitude == null ||
            !Geocodigos.latitudeValida(latitude) || !Geocodigos.longitudeValida(longitude)
        ) {
            _estado.update { it.copy(mensagem = "Informe geocódigos válidos antes de buscar o endereço.") }
            return
        }

        _estado.update { it.copy(buscandoEndereco = true, mensagem = null) }
        viewModelScope.launch {
            val endereco = repositorio.enderecoDe(latitude, longitude)
            _estado.update {
                it.copy(
                    endereco = endereco ?: it.endereco,
                    buscandoEndereco = false,
                    mensagem = if (endereco == null) {
                        "Endereço não encontrado para estes geocódigos."
                    } else {
                        null
                    }
                )
            }
        }
    }

    fun aoCapturarFoto(arquivo: File) = viewModelScope.launch {
        trocarFoto(armazenamentoDeImagens.salvarCaptura(arquivo))
    }

    fun aoEscolherFoto(uri: Uri) = viewModelScope.launch {
        trocarFoto(armazenamentoDeImagens.salvarDaGaleria(uri))
    }

    fun aoRemoverFoto() = viewModelScope.launch {
        descartarFotoOrfa()
        _estado.update { it.copy(caminhoImagem = null) }
    }

    private suspend fun trocarFoto(novoCaminho: String?) {
        if (novoCaminho == null) {
            _estado.update { it.copy(mensagem = "Não foi possível salvar a imagem.") }
            return
        }
        descartarFotoOrfa()
        fotoOrfa = novoCaminho
        _estado.update { it.copy(caminhoImagem = novoCaminho, mensagem = null) }
    }

    private suspend fun descartarFotoOrfa() {
        fotoOrfa?.let { armazenamentoDeImagens.apagar(it) }
        fotoOrfa = null
    }

    fun salvar() {
        val atual = _estado.value
        if (!atual.podeSalvar) return

        val latitude = Geocodigos.interpretar(atual.latitude) ?: return
        val longitude = Geocodigos.interpretar(atual.longitude) ?: return

        viewModelScope.launch {
            // Grava o endereco junto do ponto para que ele continue visivel offline.
            val endereco = atual.endereco ?: repositorio.enderecoDe(latitude, longitude)

            repositorio.salvarPonto(
                Ponto(
                    id = if (atual.edicao) pontoId else 0L,
                    viagemId = atual.viagemId,
                    nome = atual.nome.trim(),
                    descricao = atual.descricao.trim(),
                    latitude = latitude,
                    longitude = longitude,
                    endereco = endereco,
                    caminhoImagem = atual.caminhoImagem
                )
            )
            fotoOrfa = null
            _estado.update { it.copy(salvo = true) }
        }
    }

    fun apagar() {
        if (!_estado.value.edicao) return
        viewModelScope.launch {
            repositorio.buscarPonto(pontoId)?.let { repositorio.apagarPonto(it) }
            fotoOrfa = null
            _estado.update { it.copy(salvo = true) }
        }
    }

    fun descartar() {
        viewModelScope.launch { descartarFotoOrfa() }
    }
}
