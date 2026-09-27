package com.app.pictravelly.core.ui

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.slideInVertically
import androidx.compose.animation.slideOutVertically
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Luggage
import androidx.compose.material.icons.filled.Place
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
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
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
import com.app.pictravelly.feature.home.HomeRoute
import com.app.pictravelly.feature.home.HomeViewModel
import com.app.pictravelly.feature.map.MapScreen
import com.app.pictravelly.feature.map.MapViewModel
import com.app.pictravelly.feature.settings.SettingsRoute
import com.app.pictravelly.feature.settings.SettingsViewModel
import com.app.pictravelly.feature.spot_detail.SpotDetailScreen
import com.app.pictravelly.feature.spot_detail.SpotDetailViewModel
import com.app.pictravelly.feature.spot_form.SpotFormScreen
import com.app.pictravelly.feature.spot_form.SpotFormViewModel
import com.app.pictravelly.feature.spots.SpotsListScreen
import com.app.pictravelly.feature.spots.SpotsViewModel
import com.app.pictravelly.feature.trip_detail.TripDetailScreen
import com.app.pictravelly.feature.trip_detail.TripDetailViewModel
import com.app.pictravelly.feature.trip_form.TripFormScreen
import com.app.pictravelly.feature.trip_form.TripFormViewModel
import com.app.pictravelly.feature.trips.TripsListScreen
import com.app.pictravelly.feature.trips.TripsViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun PicTravellyAppScreen() {
    val navController = rememberNavController()
    val navBackStackEntry by navController.currentBackStackEntryAsState()
    val currentRoute = DestinationScreen.baseRouteOf(
        navBackStackEntry?.destination?.route ?: DestinationScreen.HOME.route
    )
    val context = LocalContext.current
    val app = context.applicationContext as PicTravellyApp
    val spotRepository = app.container.touristSpotRepository
    val tripRepository = app.container.tripRepository

    // Oculta o dock nos formulários e nas telas de detalhe
    val shouldShowDock = currentRoute in listOf(
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
                startDestination = DestinationScreen.HOME.route
            ) {
                // 1. Rota da Home (Dashboard + Mapa Expansível + Diário)
                composable(DestinationScreen.HOME.route) {
                    val homeViewModel: HomeViewModel =
                        viewModel(factory = AppViewModelProvider.Factory)
                    HomeRoute(
                        viewModel = homeViewModel,
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
                    )
                }

                // 2. Rota da aba Diário: lista de viagens registradas
                composable(DestinationScreen.TRIPS.route) {
                    val tripsViewModel: TripsViewModel =
                        viewModel(factory = AppViewModelProvider.Factory)
                    TripsListScreen(
                        viewModel = tripsViewModel,
                        onNavigateToTripDetail = { tripId ->
                            navController.navigate(DestinationScreen.createTripDetailRoute(tripId))
                        },
                        onNavigateToAllSpots = {
                            navController.navigate(DestinationScreen.SPOTS.route)
                        },
                        contentPadding = paddingValues
                    )
                }

                // 3. Rota da aba Mapa: todos os pontos turísticos cadastrados
                composable(DestinationScreen.MAP.route) {
                    val mapViewModel: MapViewModel =
                        viewModel(factory = AppViewModelProvider.Factory)
                    MapScreen(
                        viewModel = mapViewModel,
                        onNavigateToSpotDetail = { spotId ->
                            navController.navigate(DestinationScreen.createSpotDetailRoute(spotId))
                        },
                        contentPadding = paddingValues
                    )
                }

                // 4. Rota de busca em todos os pontos (alcançada pela aba Diário)
                composable(DestinationScreen.SPOTS.route) {
                    val spotsViewModel: SpotsViewModel =
                        viewModel(factory = AppViewModelProvider.Factory)
                    SpotsListScreen(
                        viewModel = spotsViewModel,
                        onNavigateToDetail = { spotId ->
                            navController.navigate(DestinationScreen.createSpotDetailRoute(spotId))
                        },
                        contentPadding = paddingValues
                    )
                }

                // 5. Rota de Cadastro de Ponto Turístico (com a viagem opcional na rota)
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
                            tripRepository = tripRepository
                        )
                    )
                    SpotFormScreen(
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
                    TripFormScreen(
                        viewModel = tripFormViewModel,
                        onNavigateBack = { navController.popBackStack() },
                        onTripSaved = { savedTripId ->
                            navController.popBackStack()
                            navController.navigate(
                                DestinationScreen.createTripDetailRoute(savedTripId)
                            )
                        }
                    )
                }

                // 7. Rota de Ajustes / Preferências
                composable(DestinationScreen.SETTINGS.route) {
                    val settingsViewModel: SettingsViewModel =
                        viewModel(factory = AppViewModelProvider.Factory)
                    SettingsRoute(
                        viewModel = settingsViewModel,
                        contentPadding = paddingValues
                    )
                }

                // 8. Rota de Detalhe da Viagem (os pontos turísticos dela)
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
                    TripDetailScreen(
                        viewModel = tripDetailViewModel,
                        onNavigateBack = { navController.popBackStack() },
                        onEditTrip = { editingTripId ->
                            navController.navigate(
                                DestinationScreen.createTripFormRoute(editingTripId)
                            )
                        },
                        onAddSpot = {
                            navController.navigate(
                                DestinationScreen.createSpotFormRoute(tripId)
                            )
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
                            spotId,
                            spotRepository
                        )
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

    // ============================================================================
    // ➕ FOLHA DO BOTÃO CENTRAL: escolha entre nova viagem e novo ponto turístico
    // ============================================================================
    if (showAddSheet) {
        ModalBottomSheet(
            onDismissRequest = {},
            sheetState = addSheetState
        ) {
            AddEntrySheetContent(
                onNewTrip = {
                    navController.navigate(DestinationScreen.createTripFormRoute())
                },
                onNewSpot = {
                    navController.navigate(DestinationScreen.createSpotFormRoute())
                }
            )
        }
    }
}

/**
 * Conteúdo da folha aberta pelo "+" do dock.
 */
@Composable
private fun AddEntrySheetContent(
    onNewTrip: () -> Unit,
    onNewSpot: () -> Unit
) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .navigationBarsPadding()
            .padding(bottom = 16.dp)
    ) {
        Text(
            text = "O que você quer registrar?",
            style = MaterialTheme.typography.titleMedium,
            fontWeight = FontWeight.Bold,
            color = MaterialTheme.colorScheme.onSurface,
            modifier = Modifier.padding(horizontal = 24.dp, vertical = 10.dp)
        )

        ListItem(
            headlineContent = { Text("Nova viagem") },
            supportingContent = { Text("Abre um capítulo novo no diário") },
            leadingContent = {
                Icon(
                    imageVector = Icons.Default.Luggage,
                    contentDescription = null,
                    tint = MaterialTheme.colorScheme.primary
                )
            },
            modifier = Modifier.clickable(onClick = onNewTrip)
        )

        ListItem(
            headlineContent = { Text("Novo ponto turístico") },
            supportingContent = { Text("Geocódigos, endereço, fotos e relato") },
            leadingContent = {
                Icon(
                    imageVector = Icons.Default.Place,
                    contentDescription = null,
                    tint = MaterialTheme.colorScheme.primary
                )
            },
            modifier = Modifier.clickable(onClick = onNewSpot)
        )
    }
}

/**
 * Navegação entre as abas do dock, preservando o estado de cada uma.
 */
private fun NavHostController.navigateToTab(route: String) {
    navigate(route) {
        popUpTo(graph.findStartDestination().id) { saveState = true }
        launchSingleTop = true
        restoreState = true
    }
}
