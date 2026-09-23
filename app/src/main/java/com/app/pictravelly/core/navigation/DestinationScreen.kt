package com.app.pictravelly.core.navigation

import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.CollectionsBookmark
import androidx.compose.material.icons.filled.Explore
import androidx.compose.material.icons.filled.Settings
import androidx.compose.ui.graphics.vector.ImageVector

/**
 * Destinos e rotas principais da aplicação.
 */
enum class DestinationScreen(
    val title: String,
    val icon: ImageVector,
    val route: String
) {
    HOME(
        title = "Início",
        icon = Icons.Default.Explore,
        route = "home_route"
    ),
    SPOTS(
        title = "Diário",
        icon = Icons.Default.CollectionsBookmark,
        route = "spots_route"
    ),
    SPOT_FORM(
        title = "Novo",
        icon = Icons.Default.Add,
        route = "spot_form_route"
    ),
    SETTINGS(
        title = "Ajustes",
        icon = Icons.Default.Settings,
        route = "settings_route"
    );

    companion object {
        const val SPOT_DETAIL_BASE_ROUTE = "spot_detail_route"
        const val SPOT_DETAIL_ROUTE = "$SPOT_DETAIL_BASE_ROUTE/{spotId}"

        fun createSpotDetailRoute(spotId: Long): String = "$SPOT_DETAIL_BASE_ROUTE/$spotId"
    }
}