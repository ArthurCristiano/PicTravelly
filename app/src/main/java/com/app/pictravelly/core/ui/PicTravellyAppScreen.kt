package com.app.pictravelly.core.ui

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.spring
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.slideInVertically
import androidx.compose.animation.slideOutVertically
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.BottomSheetDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.Scaffold
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavGraph.Companion.findStartDestination
import androidx.navigation.NavHostController
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
import com.app.pictravelly.core.navigation.PicTravellyNavTransitions
import com.app.pictravelly.core.ui.components.AddEntrySheetContent
import com.app.pictravelly.feature.home.HomeRoute
import com.app.pictravelly.feature.map.MapRoute
import com.app.pictravelly.feature.settings.SettingsRoute
import com.app.pictravelly.feature.spot_detail.SpotDetailRoute
import com.app.pictravelly.feature.spot_detail.SpotDetailViewModel
import com.app.pictravelly.feature.spot_form.SpotFormRoute
import com.app.pictravelly.feature.spot_form.SpotFormViewModel
import com.app.pictravelly.feature.spots.SpotsRoute
import com.app.pictravelly.feature.trip_detail.TripDetailRoute
import com.app.pictravelly.feature.trip_detail.TripDetailViewModel
import com.app.pictravelly.feature.trip_form.TripFormRoute
import com.app.pictravelly.feature.trip_form.TripFormViewModel
import com.app.pictravelly.feature.trips.TripsRoute

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun PicTravellyAppScreen() {
    val navController = rememberNavController()
    val navBackStackEntry by navController.currentBackStackEntryAsState()
    val currentRoute = DestinationScreen.baseRouteOf(
        navBackStackEntry?.destination?.route ?: DestinationScreen.HOME.route
    )

    // Injeção de Dependências Manual do Container
    val context = LocalContext.current
    val app = context.applicationContext as PicTravellyApp
    val spotRepository = app.container.touristSpotRepository
    val tripRepository = app.container.tripRepository
    val settingsRepository = app.container.settingsRepository // CORREÇÃO: Adicionado

    // Oculta o dock quando o mapa estiver expandido em tela cheia na Home
    var isHomeMapExpanded by remember { mutableStateOf(false) }

    androidx.compose.runtime.LaunchedEffect(currentRoute) {
        if (currentRoute != DestinationScreen.HOME.route) {
            isHomeMapExpanded = false
        }
    }

    // Oculta o dock nos formulários, nas telas de detalhe e quando o mapa da home estiver expandido
    val shouldShowDock = !isHomeMapExpanded && currentRoute in listOf(
        DestinationScreen.HOME.route,
        DestinationScreen.TRIPS.route,
        DestinationScreen.MAP.route,
        DestinationScreen.SPOTS.route,
        DestinationScreen.SETTINGS.route
    )

    // Folha de escolha aberta pelo botão "+" central do dock
    var showAddSheet by remember { mutableStateOf(false) }
    val addSheetState = rememberModalBottomSheetState()

    Box(modifier = Modifier.fillMaxSize()) {
        Scaffold { paddingValues ->
            NavHost(
                navController = navController,
                startDestination = DestinationScreen.HOME.route,
                enterTransition = PicTravellyNavTransitions.enterTransition,
                exitTransition = PicTravellyNavTransitions.exitTransition,
                popEnterTransition = PicTravellyNavTransitions.popEnterTransition,
                popExitTransition = PicTravellyNavTransitions.popExitTransition
            ) {
                // 1. Rota da Home (Dashboard)
                composable(DestinationScreen.HOME.route) {
                    HomeRoute(
                        onNavigateToDetail = { spotId ->
                            navController.navigate(DestinationScreen.createSpotDetailRoute(spotId))
                        },
                        onNavigateToSpots = {
                            navController.navigate(DestinationScreen.SPOTS.route)
                        },
                        onNavigateToCreate = {
                            navController.navigate(DestinationScreen.createSpotFormRoute())
                        },
                        contentPadding = paddingValues,
                        onMapExpandedChange = { expanded ->
                            isHomeMapExpanded = expanded
                        }
                    )
                }

                // 2. Rota da aba Diário: lista de viagens registradas
                composable(DestinationScreen.TRIPS.route) {
                    TripsRoute(
                        onNavigateToTripDetail = { tripId ->
                            navController.navigate(DestinationScreen.createTripDetailRoute(tripId))
                        },
                        onNavigateToAllSpots = {
                            navController.navigate(DestinationScreen.SPOTS.route)
                        },
                        contentPadding = paddingValues
                    )
                }

                // 3. Rota da aba Mapa: todos os pontos turísticos
                composable(DestinationScreen.MAP.route) {
                    MapRoute(
                        onNavigateToSpotDetail = { spotId ->
                            navController.navigate(DestinationScreen.createSpotDetailRoute(spotId))
                        },
                        contentPadding = paddingValues
                    )
                }

                // 4. Rota de busca em todos os pontos
                composable(DestinationScreen.SPOTS.route) {
                    SpotsRoute(
                        onNavigateToDetail = { spotId ->
                            navController.navigate(DestinationScreen.createSpotDetailRoute(spotId))
                        },
                        contentPadding = paddingValues
                    )
                }

                // 5. Rota de Cadastro de Ponto Turístico
                composable(
                    route = DestinationScreen.SPOT_FORM_ROUTE,
                    arguments = listOf(
                        navArgument("tripId") {
                            type = NavType.LongType
                            defaultValue = DestinationScreen.NO_TRIP_ID
                        }
                    )
                ) { backStackEntry ->
                    val tripId = backStackEntry.arguments?.getLong("tripId")
                        ?: DestinationScreen.NO_TRIP_ID

                    val spotFormViewModel: SpotFormViewModel = viewModel(
                        factory = AppViewModelProvider.createSpotFormFactory(
                            tripId = tripId,
                            spotRepository = spotRepository,
                            tripRepository = tripRepository,
                            settingsRepository = settingsRepository // CORREÇÃO: Adicionado
                        )
                    )

                    SpotFormRoute( // CORREÇÃO: Usando a Route
                        viewModel = spotFormViewModel,
                        onNavigateBack = { navController.popBackStack() },
                        onSpotSaved = { spotId ->
                            navController.popBackStack()
                            navController.navigate(DestinationScreen.createSpotDetailRoute(spotId))
                        }
                    )
                }

                // 6. Rota de Cadastro / Edição de Viagem
                composable(
                    route = DestinationScreen.TRIP_FORM_ROUTE,
                    arguments = listOf(
                        navArgument("tripId") {
                            type = NavType.LongType
                            defaultValue = DestinationScreen.NO_TRIP_ID
                        }
                    )
                ) { backStackEntry ->
                    val tripId = backStackEntry.arguments?.getLong("tripId")
                        ?: DestinationScreen.NO_TRIP_ID

                    val tripFormViewModel: TripFormViewModel = viewModel(
                        factory = AppViewModelProvider.createTripFormFactory(
                            tripId = tripId,
                            tripRepository = tripRepository
                        )
                    )

                    TripFormRoute( // CORREÇÃO: Usando a Route
                        viewModel = tripFormViewModel,
                        onNavigateBack = { navController.popBackStack() },
                        onTripSaved = { savedTripId ->
                            navController.popBackStack()
                            navController.navigate(
                                DestinationScreen.createTripDetailRoute(
                                    savedTripId
                                )
                            )
                        }
                    )
                }

                // 7. Rota de Ajustes / Preferências
                composable(DestinationScreen.SETTINGS.route) {
                    SettingsRoute(contentPadding = paddingValues)
                }

                // 8. Rota de Detalhe da Viagem
                composable(
                    route = DestinationScreen.TRIP_DETAIL_ROUTE,
                    arguments = listOf(navArgument("tripId") { type = NavType.LongType })
                ) { backStackEntry ->
                    val tripId = backStackEntry.arguments?.getLong("tripId")
                        ?: DestinationScreen.NO_TRIP_ID

                    val tripDetailViewModel: TripDetailViewModel = viewModel(
                        factory = AppViewModelProvider.createTripDetailFactory(
                            tripId = tripId,
                            tripRepository = tripRepository,
                            spotRepository = spotRepository
                        )
                    )

                    TripDetailRoute( // CORREÇÃO: Usando a Route
                        viewModel = tripDetailViewModel,
                        onNavigateBack = { navController.popBackStack() },
                        onEditTrip = { editingTripId ->
                            navController.navigate(
                                DestinationScreen.createTripFormRoute(
                                    editingTripId
                                )
                            )
                        },
                        onAddSpot = {
                            navController.navigate(DestinationScreen.createSpotFormRoute(tripId))
                        },
                        onNavigateToSpotDetail = { spotId ->
                            navController.navigate(DestinationScreen.createSpotDetailRoute(spotId))
                        }
                    )
                }

                // 9. Rota de Visualização Imersiva do Diário (Spot Detail)
                composable(
                    route = DestinationScreen.SPOT_DETAIL_ROUTE,
                    arguments = listOf(navArgument("spotId") { type = NavType.LongType })
                ) { backStackEntry ->
                    val spotId = backStackEntry.arguments?.getLong("spotId") ?: 0L

                    val spotDetailViewModel: SpotDetailViewModel = viewModel(
                        factory = AppViewModelProvider.createSpotDetailFactory(
                            spotId = spotId,
                            spotRepository = spotRepository,
                            settingsRepository = settingsRepository // CORREÇÃO: Adicionado
                        )
                    )

                    SpotDetailRoute( // CORREÇÃO: Usando a Route
                        viewModel = spotDetailViewModel,
                        onNavigateBack = { navController.popBackStack() }
                    )
                }
            }
        }

        // Dock translúcido flutuante One UI
        AnimatedVisibility(
            visible = shouldShowDock,
            modifier = Modifier.align(Alignment.BottomCenter),
            enter = slideInVertically(
                initialOffsetY = { it },
                animationSpec = spring(
                    dampingRatio = 0.82f,
                    stiffness = Spring.StiffnessMediumLow
                )
            ) + fadeIn(
                animationSpec = tween(
                    durationMillis = 220,
                    easing = PicTravellyNavTransitions.EmphasizedDecelerate
                )
            ),
            exit = slideOutVertically(
                targetOffsetY = { it },
                animationSpec = tween(
                    durationMillis = 250,
                    easing = PicTravellyNavTransitions.EmphasizedAccelerate
                )
            ) + fadeOut(
                animationSpec = tween(
                    durationMillis = 180,
                    easing = PicTravellyNavTransitions.EmphasizedAccelerate
                )
            )
        ) {
            PicTravellyOneUiDock(
                currentRoute = currentRoute,
                onNavigate = { destination ->
                    val route = when (destination) {
                        DockDestination.Home -> DestinationScreen.HOME.route
                        DockDestination.Trips -> DestinationScreen.TRIPS.route
                        DockDestination.Map -> DestinationScreen.MAP.route
                        DockDestination.Settings -> DestinationScreen.SETTINGS.route
                    }
                    navController.navigateToTab(route)
                },
                onAddEntry = { showAddSheet = true }
            )
        }
    }

    if (showAddSheet) {
        ModalBottomSheet(
            onDismissRequest = {
                showAddSheet = false
            },
            sheetState = addSheetState,
            containerColor = MaterialTheme.colorScheme.surface,
            dragHandle = {
                BottomSheetDefaults.DragHandle(
                    color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.4f)
                )
            }
        ) {
            AddEntrySheetContent(
                onNewTrip = {
                    showAddSheet = false
                    navController.navigate(DestinationScreen.createTripFormRoute())
                },
                onNewSpot = {
                    showAddSheet = false
                    navController.navigate(DestinationScreen.createSpotFormRoute())
                }
            )
        }
    }
}

private fun NavHostController.navigateToTab(route: String) {
    val currentRoute = currentBackStackEntry?.destination?.route
    if (currentRoute == route) {
        return
    }
    runCatching {
        navigate(route) {
            popUpTo(graph.findStartDestination().id) { saveState = true }
            launchSingleTop = true
            restoreState = true
        }
    }
}