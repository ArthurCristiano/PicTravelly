package com.app.pictravelly.core.navigation

import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.CollectionsBookmark
import androidx.compose.material.icons.filled.Explore
import androidx.compose.material.icons.filled.Luggage
import androidx.compose.material.icons.filled.Map
import androidx.compose.material.icons.filled.Place
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
    TRIPS(
        title = "Diário",
        icon = Icons.Default.CollectionsBookmark,
        route = "trips_route"
    ),
    MAP(
        title = "Mapa",
        icon = Icons.Default.Map,
        route = "map_route"
    ),
    SPOTS(
        title = "Todos os pontos",
        icon = Icons.Default.Place,
        route = "spots_route"
    ),
    TRIP_FORM(
        title = "Nova viagem",
        icon = Icons.Default.Luggage,
        route = "trip_form_route"
    ),
    SPOT_FORM(
        title = "Novo ponto",
        icon = Icons.Default.Add,
        route = "spot_form_route"
    ),
    SETTINGS(
        title = "Ajustes",
        icon = Icons.Default.Settings,
        route = "settings_route"
    );

    companion object {
        /** Identificador usado quando o ponto não pertence a nenhuma viagem. */
        const val NO_TRIP_ID = -1L

        /** Identificador padrão quando nenhum ponto está sendo editado. */
        const val NO_SPOT_ID = -1L

        const val SPOT_DETAIL_BASE_ROUTE = "spot_detail_route"
        const val SPOT_DETAIL_ROUTE = "$SPOT_DETAIL_BASE_ROUTE/{spotId}"

        const val TRIP_DETAIL_BASE_ROUTE = "trip_detail_route"
        const val TRIP_DETAIL_ROUTE = "$TRIP_DETAIL_BASE_ROUTE/{tripId}"

        /** O formulário de viagem serve para criar e para editar. */
        const val TRIP_FORM_ROUTE = "trip_form_route?tripId={tripId}"

        /** O formulário de ponto pode vir com a viagem já escolhida e/ou para editar um ponto existente. */
        const val SPOT_FORM_ROUTE = "spot_form_route?tripId={tripId}&spotId={spotId}"

        fun createSpotDetailRoute(spotId: Long): String = "$SPOT_DETAIL_BASE_ROUTE/$spotId"

        fun createTripDetailRoute(tripId: Long): String = "$TRIP_DETAIL_BASE_ROUTE/$tripId"

        fun createTripFormRoute(tripId: Long = NO_TRIP_ID): String =
            "${TRIP_FORM.route}?tripId=$tripId"

        fun createSpotFormRoute(
            tripId: Long = NO_TRIP_ID,
            spotId: Long = NO_SPOT_ID
        ): String = "${SPOT_FORM.route}?tripId=$tripId&spotId=$spotId"

        /** "spot_form_route?tripId=3" -> "spot_form_route". */
        fun baseRouteOf(route: String?): String = route?.substringBefore('?').orEmpty()
    }
}
