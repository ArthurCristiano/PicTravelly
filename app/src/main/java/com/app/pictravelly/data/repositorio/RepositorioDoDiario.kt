package com.app.pictravelly.data.repositorio

import com.app.pictravelly.core.localizacao.ServicoDeGeocodificacao
import com.app.pictravelly.core.midia.ArmazenamentoDeImagens
import com.app.pictravelly.data.local.dao.PontoDao
import com.app.pictravelly.data.local.dao.ViagemDao
import com.app.pictravelly.data.local.entidade.Ponto
import com.app.pictravelly.data.local.entidade.Viagem
import com.app.pictravelly.data.local.relacao.ViagemComPontos
import kotlinx.coroutines.flow.Flow

/**
 * Ponto unico de acesso ao diario. Junta o Room, os arquivos de imagem e a
 * geocodificacao reversa para que as telas conversem com uma API so.
 */
class RepositorioDoDiario(
    private val viagemDao: ViagemDao,
    private val pontoDao: PontoDao,
    private val armazenamentoDeImagens: ArmazenamentoDeImagens,
    private val servicoDeGeocodificacao: ServicoDeGeocodificacao
) {

    // ---------- Viagens ----------

    fun observarViagens(): Flow<List<ViagemComPontos>> = viagemDao.observarTodasComPontos()

    fun observarViagensSimples(): Flow<List<Viagem>> = viagemDao.observarTodas()

    fun observarViagem(id: Long): Flow<ViagemComPontos?> = viagemDao.observarComPontos(id)

    suspend fun buscarViagem(id: Long): Viagem? = viagemDao.buscarPorId(id)

    fun contarViagens(): Flow<Int> = viagemDao.contar()

    suspend fun salvarViagem(viagem: Viagem): Long =
        if (viagem.id == 0L) {
            viagemDao.inserir(viagem)
        } else {
            // Se a capa mudou, o arquivo antigo nao serve mais para ninguem.
            val anterior = viagemDao.buscarPorId(viagem.id)
            if (anterior?.caminhoCapa != null && anterior.caminhoCapa != viagem.caminhoCapa) {
                armazenamentoDeImagens.apagar(anterior.caminhoCapa)
            }
            viagemDao.atualizar(viagem)
            viagem.id
        }

    /** Apaga a viagem, seus pontos (cascata no Room) e todas as fotos em disco. */
    suspend fun apagarViagem(viagem: Viagem) {
        val fotosDosPontos = pontoDao.caminhosDeImagemDaViagem(viagem.id)
        viagemDao.apagar(viagem)
        armazenamentoDeImagens.apagar(viagem.caminhoCapa)
        fotosDosPontos.forEach { armazenamentoDeImagens.apagar(it) }
    }

    // ---------- Pontos turisticos ----------

    fun observarTodosOsPontos(): Flow<List<Ponto>> = pontoDao.observarTodos()

    fun observarPontosDaViagem(viagemId: Long): Flow<List<Ponto>> =
        pontoDao.observarDaViagem(viagemId)

    suspend fun buscarPonto(id: Long): Ponto? = pontoDao.buscarPorId(id)

    fun contarPontos(): Flow<Int> = pontoDao.contar()

    suspend fun salvarPonto(ponto: Ponto): Long =
        if (ponto.id == 0L) {
            pontoDao.inserir(ponto)
        } else {
            val anterior = pontoDao.buscarPorId(ponto.id)
            if (anterior?.caminhoImagem != null && anterior.caminhoImagem != ponto.caminhoImagem) {
                armazenamentoDeImagens.apagar(anterior.caminhoImagem)
            }
            pontoDao.atualizar(ponto)
            ponto.id
        }

    suspend fun apagarPonto(ponto: Ponto) {
        pontoDao.apagar(ponto)
        armazenamentoDeImagens.apagar(ponto.caminhoImagem)
    }

    // ---------- Apoio ----------

    /** Converte os geocodigos em endereco textual para mostrar ao usuario. */
    suspend fun enderecoDe(latitude: Double, longitude: Double): String? =
        servicoDeGeocodificacao.enderecoDe(latitude, longitude)
}
