package com.app.pictravelly.core.navegacao

import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.Map
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.Settings
import androidx.compose.ui.graphics.vector.ImageVector

/**
 * As quatro abas fixas do rodape. O botao "+" central nao e uma aba:
 * ele abre o fluxo de cadastro, por isso fica fora deste enum.
 */
enum class Destino(
    val titulo: String,
    val icone: ImageVector,
    val rota: String
) {
    INICIO(
        titulo = "Início",
        icone = Icons.Default.Home,
        rota = Rotas.INICIO
    ),
    MAPA(
        titulo = "Mapa",
        icone = Icons.Default.Map,
        rota = Rotas.MAPA
    ),
    PERFIL(
        titulo = "Perfil",
        icone = Icons.Default.Person,
        rota = Rotas.PERFIL
    ),
    CONFIGURACOES(
        titulo = "Ajustes",
        icone = Icons.Default.Settings,
        rota = Rotas.CONFIGURACOES
    )
}
