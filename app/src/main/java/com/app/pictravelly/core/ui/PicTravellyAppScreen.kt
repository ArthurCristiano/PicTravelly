package com.app.pictravelly.core.ui

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.slideInVertically
import androidx.compose.animation.slideOutVertically
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.Scaffold
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavGraph.Companion.findStartDestination
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import androidx.navigation.navArgument
import com.app.pictravelly.PicTravellyApp
import com.app.pictravelly.core.design.components.DockDestination
import com.app.pictravelly.core.design.components.PicTravellyOneUiDock
import com.app.pictravelly.core.di.AppViewModelProvider
import com.app.pictravelly.core.navigation.DestinationScreen
import com.app.pictravelly.feature.home.HomeScreen
import com.app.pictravelly.feature.home.HomeViewModel
import com.app.pictravelly.feature.settings.SettingsScreen
import com.app.pictravelly.feature.settings.SettingsViewModel
import com.app.pictravelly.feature.spot_detail.SpotDetailScreen
import com.app.pictravelly.feature.spot_detail.SpotDetailViewModel
import com.app.pictravelly.feature.spot_form.SpotFormScreen
import com.app.pictravelly.feature.spot_form.SpotFormViewModel
import com.app.pictravelly.feature.spots.SpotsListScreen
import com.app.pictravelly.feature.spots.SpotsViewModel

@Composable
fun PicTravellyAppScreen() {
    val navController = rememberNavController()
    val navBackStackEntry by navController.currentBackStackEntryAsState()
    val currentRoute = navBackStackEntry?.destination?.route ?: DestinationScreen.HOME.route
    val context = LocalContext.current
    val app = context.applicationContext as PicTravellyApp
    val repository = app.container.touristSpotRepository

    // Oculta o dock no formulário de cadastro e na tela de detalhes
    val shouldShowDock = currentRoute in listOf(
        DestinationScreen.HOME.route,
        DestinationScreen.SPOTS.route,
        DestinationScreen.SETTINGS.route
    )

    Box(modifier = Modifier.fillMaxSize()) {
        Scaffold { paddingValues ->
            NavHost(
                navController = navController,
                startDestination = DestinationScreen.HOME.route
            ) {
                // 1. Rota da Home (Dashboard + Mapa Expansível + Diário)
                composable(DestinationScreen.HOME.route) {
                    val homeViewModel: HomeViewModel = viewModel(factory = AppViewModelProvider.Factory)
                    HomeScreen(
                        viewModel = homeViewModel,
                        onNavigateToDetail = { spotId ->
                            navController.navigate(DestinationScreen.createSpotDetailRoute(spotId))
                        },
                        onNavigateToSpots = {
                            navController.navigate(DestinationScreen.SPOTS.route) {
                                popUpTo(navController.graph.findStartDestination().id) { saveState = true }
                                launchSingleTop = true
                                restoreState = true
                            }
                        },
                        onNavigateToCreate = {
                            navController.navigate(DestinationScreen.SPOT_FORM.route)
                        },
                        contentPadding = paddingValues
                    )
                }

                // 2. Rota de Lista de Diários / Locais
                composable(DestinationScreen.SPOTS.route) {
                    val spotsViewModel: SpotsViewModel = viewModel(factory = AppViewModelProvider.Factory)
                    SpotsListScreen(
                        viewModel = spotsViewModel,
                        onNavigateToDetail = { spotId ->
                            navController.navigate(DestinationScreen.createSpotDetailRoute(spotId))
                        },
                        contentPadding = paddingValues
                    )
                }

                // 3. Rota de Cadastro de Ponto Turístico
                composable(DestinationScreen.SPOT_FORM.route) {
                    val spotFormViewModel: SpotFormViewModel = viewModel(factory = AppViewModelProvider.Factory)
                    SpotFormScreen(
                        viewModel = spotFormViewModel,
                        onNavigateBack = { navController.popBackStack() },
                        onSpotSaved = { spotId ->
                            navController.popBackStack()
                            navController.navigate(DestinationScreen.createSpotDetailRoute(spotId))
                        }
                    )
                }

                // 4. Rota de Ajustes / Preferências
                composable(DestinationScreen.SETTINGS.route) {
                    val settingsViewModel: SettingsViewModel = viewModel(factory = AppViewModelProvider.Factory)
                    SettingsScreen(
                        viewModel = settingsViewModel,
                        contentPadding = paddingValues
                    )
                }

                // 5. Rota de Visualização Imersiva do Diário (Spot Detail)
                composable(
                    route = DestinationScreen.SPOT_DETAIL_ROUTE,
                    arguments = listOf(navArgument("spotId") { type = NavType.LongType })
                ) { backStackEntry ->
                    val spotId = backStackEntry.arguments?.getLong("spotId") ?: 0L
                    val spotDetailViewModel: SpotDetailViewModel = viewModel(
                        factory = AppViewModelProvider.createSpotDetailFactory(spotId, repository)
                    )
                    SpotDetailScreen(
                        viewModel = spotDetailViewModel,
                        onNavigateBack = { navController.popBackStack() }
                    )
                }
            }
        }

        // Dock translúcido flutuante One UI 9 posicionado sobre a tela
        AnimatedVisibility(
            visible = shouldShowDock,
            modifier = Modifier.align(Alignment.BottomCenter),
            enter = slideInVertically(initialOffsetY = { it }),
            exit = slideOutVertically(targetOffsetY = { it })
        ) {
            PicTravellyOneUiDock(
                currentRoute = currentRoute,
                onNavigate = { destination ->
                    when (destination) {
                        DockDestination.Home -> {
                            navController.navigate(DestinationScreen.HOME.route) {
                                popUpTo(navController.graph.findStartDestination().id) { saveState = true }
                                launchSingleTop = true
                                restoreState = true
                            }
                        }
                        DockDestination.Spots -> {
                            navController.navigate(DestinationScreen.SPOTS.route) {
                                popUpTo(navController.graph.findStartDestination().id) { saveState = true }
                                launchSingleTop = true
                                restoreState = true
                            }
                        }
                        DockDestination.Settings -> {
                            navController.navigate(DestinationScreen.SETTINGS.route) {
                                popUpTo(navController.graph.findStartDestination().id) { saveState = true }
                                launchSingleTop = true
                                restoreState = true
                            }
                        }
                    }
                }
            )
        }
    }
}