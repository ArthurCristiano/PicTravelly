package com.app.pictravelly.core.midia

import android.content.Context
import android.net.Uri
import java.io.File
import java.util.UUID
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

/**
 * Guarda as fotos do diario em arquivos dentro do armazenamento interno do app
 * e devolve o caminho absoluto, que e o que fica gravado no Room.
 *
 * Gravar o arquivo em disco (em vez de um BLOB no banco) mantem o banco leve e
 * permite que o Coil carregue a imagem direto do caminho.
 */
class ArmazenamentoDeImagens(private val context: Context) {

    private val pastaDefinitiva: File
        get() = File(context.filesDir, PASTA_FOTOS).apply { mkdirs() }

    /** Move a foto recem capturada pela camera (cache) para a pasta definitiva. */
    suspend fun salvarCaptura(arquivoTemporario: File): String? = withContext(Dispatchers.IO) {
        if (!arquivoTemporario.exists() || arquivoTemporario.length() == 0L) return@withContext null
        val destino = File(pastaDefinitiva, novoNome())
        arquivoTemporario.copyTo(destino, overwrite = true)
        arquivoTemporario.delete()
        destino.absolutePath
    }

    /** Copia uma imagem escolhida na galeria para dentro do app. */
    suspend fun salvarDaGaleria(origem: Uri): String? = withContext(Dispatchers.IO) {
        val destino = File(pastaDefinitiva, novoNome())
        runCatching {
            val entrada = context.contentResolver.openInputStream(origem)
                ?: return@runCatching null
            entrada.use { fonte ->
                destino.outputStream().use { saida -> fonte.copyTo(saida) }
            }
            destino.absolutePath
        }.getOrNull()
    }

    /** Remove do disco a foto de um ponto ou viagem que foi apagado. */
    suspend fun apagar(caminho: String?) {
        if (caminho.isNullOrBlank()) return
        withContext(Dispatchers.IO) { runCatching { File(caminho).delete() } }
    }

    private fun novoNome() = "foto_${UUID.randomUUID()}.jpg"

    private companion object {
        const val PASTA_FOTOS = "fotos"
    }
}
