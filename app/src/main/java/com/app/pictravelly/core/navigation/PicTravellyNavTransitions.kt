package com.app.pictravelly.core.navigation

import androidx.compose.animation.AnimatedContentTransitionScope
import androidx.compose.animation.EnterTransition
import androidx.compose.animation.ExitTransition
import androidx.compose.animation.core.CubicBezierEasing
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.scaleIn
import androidx.compose.animation.scaleOut
import androidx.compose.animation.slideInHorizontally
import androidx.compose.animation.slideInVertically
import androidx.compose.animation.slideOutHorizontally
import androidx.compose.animation.slideOutVertically
import androidx.compose.ui.graphics.TransformOrigin
import androidx.navigation.NavBackStackEntry

/**
 * Configuração de transições de animação entre telas na navegação.
 */
object PicTravellyNavTransitions {

    val EmphasizedDecelerate = CubicBezierEasing(0.05f, 0.7f, 0.1f, 1.0f)
    val EmphasizedAccelerate = CubicBezierEasing(0.3f, 0.0f, 0.8f, 0.15f)

    private val tabOrder = mapOf(
        DestinationScreen.HOME.route to 0,
        DestinationScreen.TRIPS.route to 1,
        DestinationScreen.MAP.route to 2,
        DestinationScreen.SETTINGS.route to 3
    )

    fun cleanRoute(route: String?): String {
        if (route == null) return ""
        return route.substringBefore('?').substringBefore('/')
    }

    private fun getTabIndex(route: String?): Int? {
        val base = cleanRoute(route)
        return tabOrder[base]
    }

    private fun isModalRoute(route: String?): Boolean {
        val base = cleanRoute(route)
        return base == DestinationScreen.SPOT_FORM.route ||
               base == DestinationScreen.TRIP_FORM.route
    }

    private fun isDetailRoute(route: String?): Boolean {
        val base = cleanRoute(route)
        return base == DestinationScreen.SPOT_DETAIL_BASE_ROUTE ||
               base == DestinationScreen.TRIP_DETAIL_BASE_ROUTE ||
               base == DestinationScreen.SPOTS.route
    }

    val enterTransition: AnimatedContentTransitionScope<NavBackStackEntry>.() -> EnterTransition = {
        val initialRoute = initialState.destination.route
        val targetRoute = targetState.destination.route

        when {
            isModalRoute(targetRoute) -> {
                slideInVertically(
                    initialOffsetY = { (it * 0.22f).toInt() },
                    animationSpec = tween(durationMillis = 400, easing = EmphasizedDecelerate)
                ) + fadeIn(
                    animationSpec = tween(durationMillis = 280, easing = EmphasizedDecelerate)
                ) + scaleIn(
                    initialScale = 0.92f,
                    transformOrigin = TransformOrigin(0.5f, 1.0f),
                    animationSpec = tween(durationMillis = 400, easing = EmphasizedDecelerate)
                )
            }

            isDetailRoute(targetRoute) -> {
                slideInHorizontally(
                    initialOffsetX = { (it * 0.35f).toInt() },
                    animationSpec = tween(durationMillis = 400, easing = EmphasizedDecelerate)
                ) + fadeIn(
                    animationSpec = tween(durationMillis = 280, easing = EmphasizedDecelerate)
                ) + scaleIn(
                    initialScale = 0.92f,
                    transformOrigin = TransformOrigin.Center,
                    animationSpec = tween(durationMillis = 400, easing = EmphasizedDecelerate)
                )
            }

            getTabIndex(initialRoute) != null && getTabIndex(targetRoute) != null -> {
                val fromIdx = getTabIndex(initialRoute)!!
                val toIdx = getTabIndex(targetRoute)!!
                val movingRight = toIdx > fromIdx

                slideInHorizontally(
                    initialOffsetX = { if (movingRight) (it * 0.30f).toInt() else -(it * 0.30f).toInt() },
                    animationSpec = tween(durationMillis = 380, easing = EmphasizedDecelerate)
                ) + fadeIn(
                    animationSpec = tween(durationMillis = 280, easing = EmphasizedDecelerate)
                ) + scaleIn(
                    initialScale = 0.96f,
                    animationSpec = tween(durationMillis = 380, easing = EmphasizedDecelerate)
                )
            }

            else -> {
                fadeIn(
                    animationSpec = tween(durationMillis = 300, easing = EmphasizedDecelerate)
                ) + scaleIn(
                    initialScale = 0.95f,
                    animationSpec = tween(durationMillis = 300, easing = EmphasizedDecelerate)
                )
            }
        }
    }

    val exitTransition: AnimatedContentTransitionScope<NavBackStackEntry>.() -> ExitTransition = {
        val initialRoute = initialState.destination.route
        val targetRoute = targetState.destination.route

        when {
            isModalRoute(targetRoute) -> {
                scaleOut(
                    targetScale = 0.92f,
                    transformOrigin = TransformOrigin.Center,
                    animationSpec = tween(durationMillis = 350, easing = EmphasizedAccelerate)
                ) + fadeOut(
                    animationSpec = tween(durationMillis = 220, easing = EmphasizedAccelerate)
                )
            }

            isDetailRoute(targetRoute) -> {
                slideOutHorizontally(
                    targetOffsetX = { -(it * 0.15f).toInt() },
                    animationSpec = tween(durationMillis = 350, easing = EmphasizedAccelerate)
                ) + scaleOut(
                    targetScale = 0.94f,
                    animationSpec = tween(durationMillis = 350, easing = EmphasizedAccelerate)
                ) + fadeOut(
                    animationSpec = tween(durationMillis = 200, easing = EmphasizedAccelerate)
                )
            }

            getTabIndex(initialRoute) != null && getTabIndex(targetRoute) != null -> {
                val fromIdx = getTabIndex(initialRoute)!!
                val toIdx = getTabIndex(targetRoute)!!
                val movingRight = toIdx > fromIdx

                slideOutHorizontally(
                    targetOffsetX = { if (movingRight) -(it * 0.30f).toInt() else (it * 0.30f).toInt() },
                    animationSpec = tween(durationMillis = 300, easing = EmphasizedAccelerate)
                ) + fadeOut(
                    animationSpec = tween(durationMillis = 200, easing = EmphasizedAccelerate)
                ) + scaleOut(
                    targetScale = 0.96f,
                    animationSpec = tween(durationMillis = 300, easing = EmphasizedAccelerate)
                )
            }

            else -> {
                fadeOut(
                    animationSpec = tween(durationMillis = 220, easing = EmphasizedAccelerate)
                ) + scaleOut(
                    targetScale = 0.95f,
                    animationSpec = tween(durationMillis = 220, easing = EmphasizedAccelerate)
                )
            }
        }
    }

    val popEnterTransition: AnimatedContentTransitionScope<NavBackStackEntry>.() -> EnterTransition = {
        val initialRoute = initialState.destination.route
        val targetRoute = targetState.destination.route

        when {
            isModalRoute(initialRoute) -> {
                scaleIn(
                    initialScale = 0.92f,
                    transformOrigin = TransformOrigin.Center,
                    animationSpec = tween(durationMillis = 350, easing = EmphasizedDecelerate)
                ) + fadeIn(
                    animationSpec = tween(durationMillis = 280, easing = EmphasizedDecelerate)
                )
            }

            isDetailRoute(initialRoute) -> {
                slideInHorizontally(
                    initialOffsetX = { -(it * 0.15f).toInt() },
                    animationSpec = tween(durationMillis = 380, easing = EmphasizedDecelerate)
                ) + scaleIn(
                    initialScale = 0.94f,
                    animationSpec = tween(durationMillis = 380, easing = EmphasizedDecelerate)
                ) + fadeIn(
                    animationSpec = tween(durationMillis = 280, easing = EmphasizedDecelerate)
                )
            }

            getTabIndex(initialRoute) != null && getTabIndex(targetRoute) != null -> {
                val fromIdx = getTabIndex(initialRoute)!!
                val toIdx = getTabIndex(targetRoute)!!
                val movingRight = toIdx > fromIdx

                slideInHorizontally(
                    initialOffsetX = { if (movingRight) (it * 0.30f).toInt() else -(it * 0.30f).toInt() },
                    animationSpec = tween(durationMillis = 380, easing = EmphasizedDecelerate)
                ) + fadeIn(
                    animationSpec = tween(durationMillis = 280, easing = EmphasizedDecelerate)
                ) + scaleIn(
                    initialScale = 0.96f,
                    animationSpec = tween(durationMillis = 380, easing = EmphasizedDecelerate)
                )
            }

            else -> {
                fadeIn(
                    animationSpec = tween(durationMillis = 300, easing = EmphasizedDecelerate)
                ) + scaleIn(
                    initialScale = 0.95f,
                    animationSpec = tween(durationMillis = 300, easing = EmphasizedDecelerate)
                )
            }
        }
    }

    val popExitTransition: AnimatedContentTransitionScope<NavBackStackEntry>.() -> ExitTransition = {
        val initialRoute = initialState.destination.route
        val targetRoute = targetState.destination.route

        when {
            isModalRoute(initialRoute) -> {
                slideOutVertically(
                    targetOffsetY = { (it * 0.22f).toInt() },
                    animationSpec = tween(durationMillis = 300, easing = EmphasizedAccelerate)
                ) + scaleOut(
                    targetScale = 0.92f,
                    transformOrigin = TransformOrigin(0.5f, 1.0f),
                    animationSpec = tween(durationMillis = 300, easing = EmphasizedAccelerate)
                ) + fadeOut(
                    animationSpec = tween(durationMillis = 200, easing = EmphasizedAccelerate)
                )
            }

            isDetailRoute(initialRoute) -> {
                slideOutHorizontally(
                    targetOffsetX = { (it * 0.35f).toInt() },
                    animationSpec = tween(durationMillis = 300, easing = EmphasizedAccelerate)
                ) + scaleOut(
                    targetScale = 0.92f,
                    animationSpec = tween(durationMillis = 300, easing = EmphasizedAccelerate)
                ) + fadeOut(
                    animationSpec = tween(durationMillis = 200, easing = EmphasizedAccelerate)
                )
            }

            getTabIndex(initialRoute) != null && getTabIndex(targetRoute) != null -> {
                val fromIdx = getTabIndex(initialRoute)!!
                val toIdx = getTabIndex(targetRoute)!!
                val movingRight = toIdx > fromIdx

                slideOutHorizontally(
                    targetOffsetX = { if (movingRight) -(it * 0.30f).toInt() else (it * 0.30f).toInt() },
                    animationSpec = tween(durationMillis = 300, easing = EmphasizedAccelerate)
                ) + fadeOut(
                    animationSpec = tween(durationMillis = 200, easing = EmphasizedAccelerate)
                ) + scaleOut(
                    targetScale = 0.96f,
                    animationSpec = tween(durationMillis = 300, easing = EmphasizedAccelerate)
                )
            }

            else -> {
                fadeOut(
                    animationSpec = tween(durationMillis = 200, easing = EmphasizedAccelerate)
                ) + scaleOut(
                    targetScale = 0.95f,
                    animationSpec = tween(durationMillis = 200, easing = EmphasizedAccelerate)
                )
            }
        }
    }
}
