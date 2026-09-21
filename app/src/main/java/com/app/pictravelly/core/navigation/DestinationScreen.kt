package com.app.pictravelly.core.navigation

import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.Menu
import androidx.compose.material.icons.filled.Place
import androidx.compose.ui.graphics.vector.ImageVector

enum class DestinationScreen(
    val title: String,
    val icon: ImageVector,
    val route: String
) {
    HOME(
        title = "Início",
        icon = Icons.Default.Home,
        route = "home_route"
    ),
    SPOTS(
        title = "Locais",
        icon = Icons.Default.Place,
        route = "spots_route"
    ),
    MENU(
        title = "Menu",
        icon = Icons.Default.Menu,
        route = "menu_route"
    )
}