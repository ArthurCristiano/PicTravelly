package com.app.pictravelly.core.ui

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Place
import androidx.compose.material.icons.outlined.Luggage
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.ListItem
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.navigation.NavGraph.Companion.findStartDestination
import androidx.navigation.NavHostController
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import androidx.navigation.navArgument
import com.app.pictravelly.core.design.componentes.PicTravellyBarraFlutuante
import com.app.pictravelly.core.navegacao.Destino
import com.app.pictravelly.core.navegacao.Rotas
import com.app.pictravelly.feature.configuracoes.TelaConfiguracoes
import com.app.pictravelly.feature.inicio.TelaInicio
import com.app.pictravelly.feature.mapa.TelaMapa
import com.app.pictravelly.feature.perfil.TelaPerfil
import com.app.pictravelly.feature.ponto.TelaEditorPonto
import com.app.pictravelly.feature.viagem.TelaDetalheViagem
import com.app.pictravelly.feature.viagem.TelaEditorViagem

/** Rotas que exibem o rodape com as abas e o botao "+". */
private val ROTAS_COM_RODAPE = Destino.entries.map { it.rota }.toSet()

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun PicTravellyTelaPrincipal() {
    // 1. O cérebro da navegação
    val navController = rememberNavController()

    // 2. Observador reativo da rota atual (Protege contra o botão "Voltar")
    val navBackStackEntry by navController.currentBackStackEntryAsState()
    val rotaAtual = Rotas.semArgumentos(navBackStackEntry?.destination?.route)
    val mostrarRodape = rotaAtual in ROTAS_COM_RODAPE

    var menuDeCadastroAberto by remember { mutableStateOf(false) }
    val estadoDaFolha = rememberModalBottomSheetState()

    Scaffold(
        bottomBar = {
            if (mostrarRodape) {
                PicTravellyBarraFlutuante(
                    destinos = Destino.entries,
                    rotaAtual = rotaAtual,
                    onNavegarPara = { destino ->
                        navController.navigate(destino.rota) {
                            // Trava 1: Previne pilhas infinitas e volta para o Início
                            popUpTo(navController.graph.findStartDestination().id) {
                                saveState = true
                            }
                            // Trava 2: Evita abrir duas telas iguais por clique rápido duplo
                            launchSingleTop = true
                            // Trava 3: Restaura a rolagem de onde parou
                            restoreState = true
                        }
                    },
                    onAdicionar = { menuDeCadastroAberto = true }
                )
            }
        }
    ) { paddingValues ->
        // 3. O Motor de Roteamento
        NavHost(
            navController = navController,
            startDestination = Rotas.INICIO
        ) {
            composable(Rotas.INICIO) {
                TelaInicio(
                    onAbrirViagem = { navController.abrirViagem(it) },
                    contentPadding = paddingValues
                )
            }

            composable(
                route = Rotas.padraoDoMapa(),
                arguments = listOf(
                    navArgument(Rotas.ARG_PONTO_ID) {
                        type = NavType.LongType
                        defaultValue = Rotas.SEM_ID
                    }
                )
            ) {
                TelaMapa(
                    onAbrirViagem = { navController.abrirViagem(it) },
                    contentPadding = paddingValues
                )
            }

            composable(Rotas.PERFIL) {
                TelaPerfil(contentPadding = paddingValues)
            }

            composable(Rotas.CONFIGURACOES) {
                TelaConfiguracoes(contentPadding = paddingValues)
            }

            composable(
                route = Rotas.DETALHE_VIAGEM,
                arguments = listOf(navArgument(Rotas.ARG_VIAGEM_ID) { type = NavType.LongType })
            ) {
                TelaDetalheViagem(
                    onVoltar = { navController.popBackStack() },
                    onEditarViagem = { viagemId ->
                        navController.navigate(Rotas.editorDeViagem(viagemId))
                    },
                    onAdicionarPonto = { viagemId ->
                        navController.navigate(Rotas.editorDePonto(viagemId = viagemId))
                    },
                    onEditarPonto = { viagemId, pontoId ->
                        navController.navigate(Rotas.editorDePonto(viagemId, pontoId))
                    },
                    onVerNoMapa = { pontoId -> navController.abrirMapaNoPonto(pontoId) }
                )
            }

            composable(
                route = Rotas.EDITOR_VIAGEM,
                arguments = listOf(
                    navArgument(Rotas.ARG_VIAGEM_ID) {
                        type = NavType.LongType
                        defaultValue = Rotas.SEM_ID
                    }
                )
            ) {
                TelaEditorViagem(onConcluido = { navController.popBackStack() })
            }

            composable(
                route = Rotas.EDITOR_PONTO,
                arguments = listOf(
                    navArgument(Rotas.ARG_VIAGEM_ID) {
                        type = NavType.LongType
                        defaultValue = Rotas.SEM_ID
                    },
                    navArgument(Rotas.ARG_PONTO_ID) {
                        type = NavType.LongType
                        defaultValue = Rotas.SEM_ID
                    }
                )
            ) {
                TelaEditorPonto(onConcluido = { navController.popBackStack() })
            }
        }
    }

    if (menuDeCadastroAberto) {
        ModalBottomSheet(
            onDismissRequest = { menuDeCadastroAberto = false },
            sheetState = estadoDaFolha
        ) {
            MenuDeCadastro(
                onNovaViagem = {
                    menuDeCadastroAberto = false
                    navController.navigate(Rotas.editorDeViagem())
                },
                onNovoPonto = {
                    menuDeCadastroAberto = false
                    navController.navigate(Rotas.editorDePonto())
                }
            )
        }
    }
}

@Composable
private fun MenuDeCadastro(
    onNovaViagem: () -> Unit,
    onNovoPonto: () -> Unit
) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .navigationBarsPadding()
            .padding(bottom = 16.dp)
    ) {
        Text(
            text = "O que você quer registrar?",
            style = MaterialTheme.typography.titleLarge,
            color = MaterialTheme.colorScheme.onSurface,
            modifier = Modifier.padding(horizontal = 24.dp, vertical = 8.dp)
        )

        ListItem(
            headlineContent = { Text("Nova viagem") },
            supportingContent = { Text("Cria um novo card no diário") },
            leadingContent = { Icon(Icons.Outlined.Luggage, contentDescription = null) },
            modifier = Modifier.clickable(onClick = onNovaViagem)
        )

        ListItem(
            headlineContent = { Text("Novo ponto turístico") },
            supportingContent = { Text("Geocódigos, nome, descrição e foto") },
            leadingContent = { Icon(Icons.Default.Place, contentDescription = null) },
            modifier = Modifier.clickable(onClick = onNovoPonto)
        )
    }
}

private fun NavHostController.abrirViagem(viagemId: Long) {
    navigate(Rotas.detalheDaViagem(viagemId))
}

/** Leva para a aba de Mapa ja centralizada no ponto escolhido. */
private fun NavHostController.abrirMapaNoPonto(pontoId: Long) {
    navigate(Rotas.mapaNoPonto(pontoId)) {
        popUpTo(graph.findStartDestination().id) { saveState = false }
        launchSingleTop = true
    }
}
