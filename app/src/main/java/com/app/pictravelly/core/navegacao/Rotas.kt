package com.app.pictravelly.core.navegacao

/**
 * Rotas do NavHost em um lugar so, para nao haver string solta espalhada
 * pelas telas.
 */
object Rotas {

    // Abas do rodape
    const val INICIO = "inicio"
    const val MAPA = "mapa"
    const val PERFIL = "perfil"
    const val CONFIGURACOES = "ajustes"

    // Argumentos
    const val ARG_VIAGEM_ID = "viagemId"
    const val ARG_PONTO_ID = "pontoId"

    const val SEM_ID = -1L

    // Telas de detalhe / formularios
    const val DETALHE_VIAGEM = "viagem/{$ARG_VIAGEM_ID}"
    const val EDITOR_VIAGEM = "viagem/editar?$ARG_VIAGEM_ID={$ARG_VIAGEM_ID}"
    const val EDITOR_PONTO =
        "ponto/editar?$ARG_VIAGEM_ID={$ARG_VIAGEM_ID}&$ARG_PONTO_ID={$ARG_PONTO_ID}"

    fun detalheDaViagem(viagemId: Long) = "viagem/$viagemId"

    fun editorDeViagem(viagemId: Long = SEM_ID) = "viagem/editar?$ARG_VIAGEM_ID=$viagemId"

    fun editorDePonto(viagemId: Long = SEM_ID, pontoId: Long = SEM_ID) =
        "ponto/editar?$ARG_VIAGEM_ID=$viagemId&$ARG_PONTO_ID=$pontoId"

    /** Rota do mapa ja centralizada em um ponto: "mapa?pontoId=7". */
    fun mapaNoPonto(pontoId: Long) = "$MAPA?$ARG_PONTO_ID=$pontoId"

    /** Padrao de rota do mapa registrado no NavHost. */
    fun padraoDoMapa() = "$MAPA?$ARG_PONTO_ID={$ARG_PONTO_ID}"

    /** "viagem/editar?viagemId={viagemId}" -> "viagem/editar". */
    fun semArgumentos(rota: String?) = rota?.substringBefore('?').orEmpty()
}
