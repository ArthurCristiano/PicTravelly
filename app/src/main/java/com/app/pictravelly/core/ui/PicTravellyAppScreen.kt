package com.app.pictravelly.core.ui

import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.navigation.NavGraph.Companion.findStartDestination
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import com.app.pictravelly.core.design.ShowroomScreen
import com.app.pictravelly.core.design.components.PicTravellyFloatingNavBar
import com.app.pictravelly.core.navigation.DestinationScreen

@Composable
fun PicTravellyAppScreen() {
    // 1. O cérebro da navegação
    val navController = rememberNavController()

    // 2. Observador reativo da rota atual (Protege contra o botão "Voltar")
    val navBackStackEntry by navController.currentBackStackEntryAsState()
    val currentRoute = navBackStackEntry?.destination?.route ?: DestinationScreen.HOME.route

    Scaffold(
        bottomBar = {
            PicTravellyFloatingNavBar(
                destinations = DestinationScreen.entries,
                currentRoute = currentRoute,
                onNavigateToDestination = { destination ->
                    navController.navigate(destination.route) {
                        // Trava 1: Previne pilhas infinitas e volta para o Início
                        popUpTo(navController.graph.findStartDestination().id) {
                            saveState = true
                        }
                        // Trava 2: Evita abrir duas telas iguais por clique rápido duplo
                        launchSingleTop = true
                        // Trava 3: Restaura a rolagem de onde parou
                        restoreState = true
                    }
                }
            )
        }
    ) { paddingValues ->
        // 3. O Motor de Roteamento
        NavHost(
            navController = navController,
            startDestination = DestinationScreen.HOME.route
        ) {
            composable(DestinationScreen.HOME.route) {
                // Substitua pelas telas reais no futuro
                ShowroomScreen()
            }
            composable(DestinationScreen.SPOTS.route) {
                MockScreen("Tela de Locais", paddingValues)
            }
            composable(DestinationScreen.MENU.route) {
                MockScreen("Tela de Menu", paddingValues)
            }
        }
    }
}

@Composable
fun MockScreen(title: String, paddingValues: PaddingValues) {
    Text(text = title)
}