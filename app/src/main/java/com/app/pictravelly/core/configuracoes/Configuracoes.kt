package com.app.pictravelly.core.configuracoes

import com.google.maps.android.compose.MapType

/** Tipos de mapa que o usuario pode escolher na tela de configuracao. */
enum class TipoMapa(val rotulo: String, val mapType: MapType) {
    RODOVIARIO("Rodoviário", MapType.NORMAL),
    SATELITE("Satélite", MapType.SATELLITE),
    TERRENO("Terreno", MapType.TERRAIN),
    HIBRIDO("Híbrido", MapType.HYBRID);

    companion object {
        fun porNome(nome: String?): TipoMapa =
            entries.firstOrNull { it.name == nome } ?: RODOVIARIO
    }
}

/** Preferencias do mapa definidas pelo usuario. */
data class ConfiguracaoDoMapa(
    val zoomPadrao: Float = ZOOM_PADRAO,
    val tipoMapa: TipoMapa = TipoMapa.RODOVIARIO,
    val mostrarTransito: Boolean = false
) {
    companion object {
        const val ZOOM_MINIMO = 2f
        const val ZOOM_MAXIMO = 20f
        const val ZOOM_PADRAO = 12f
    }
}

/** Dados do viajante mostrados na tela de perfil. */
data class Perfil(
    val nome: String = "",
    val bio: String = "",
    val caminhoAvatar: String? = null
)
