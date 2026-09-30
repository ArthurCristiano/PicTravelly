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
 * Transições de tela projetadas de acordo com as diretrizes do Material 3 Expressive.
 *
 * Princípios aplicados:
 * - Movimento espacial e significativo:
 *   - Abas irmãs do Dock (Home, Diário, Mapa, Ajustes): Shared Axis X direcional com escala sutil.
 *   - Drill-down hierárquico (Detalhes de Ponto e Viagem, Lista Completa): Shared Axis X e Z (profundidade com paralaxe).
 *   - Telas de criação/formulário (Novo Ponto, Nova Viagem): Shared Axis Y modal (elevação a partir da base com âncora vertical).
 * - Curvas de aceleração expressivas (Emphasized Decelerate / Accelerate).
 */
object PicTravellyNavTransitions {

    /** Curva Emphasized Decelerate do Material 3 (para elementos entrando) */
    val EmphasizedDecelerate = CubicBezierEasing(0.05f, 0.7f, 0.1f, 1.0f)

    /** Curva Emphasized Accelerate do Material 3 (para elementos saindo) */
    val EmphasizedAccelerate = CubicBezierEasing(0.3f, 0.0f, 0.8f, 0.15f)

    // Ordem das abas principais na barra One UI Dock
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

    /**
     * Transição de entrada para navegação direta (Forward Navigation).
     */
    val enterTransition: AnimatedContentTransitionScope<NavBackStackEntry>.() -> EnterTransition = {
        val initialRoute = initialState.destination.route
        val targetRoute = targetState.destination.route

        when {
            // 1. Modais (SpotForm, TripForm): Elevação vertical suave a partir da base
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

            // 2. Detalhes Hierárquicos (SpotDetail, TripDetail, Spots): Entrada compartilhada horizontal
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

            // 3. Abas irmãs do One UI Dock (Shared Axis X direcional)
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

            // 4. Padrão Material 3 Expressive
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

    /**
     * Transição de saída para navegação direta (Forward Navigation).
     */
    val exitTransition: AnimatedContentTransitionScope<NavBackStackEntry>.() -> ExitTransition = {
        val initialRoute = initialState.destination.route
        val targetRoute = targetState.destination.route

        when {
            // 1. Indo para Modal: Tela de fundo recua sutilmente em profundidade
            isModalRoute(targetRoute) -> {
                scaleOut(
                    targetScale = 0.92f,
                    transformOrigin = TransformOrigin.Center,
                    animationSpec = tween(durationMillis = 350, easing = EmphasizedAccelerate)
                ) + fadeOut(
                    animationSpec = tween(durationMillis = 220, easing = EmphasizedAccelerate)
                )
            }

            // 2. Indo para Detalhe: Tela anterior afasta-se com paralaxe sutil para a esquerda
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

            // 3. Abas irmãs do One UI Dock (Shared Axis X direcional)
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

            // 4. Padrão Material 3 Expressive
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

    /**
     * Transição de reentrada ao voltar na pilha (Pop Enter / Backward Navigation).
     */
    val popEnterTransition: AnimatedContentTransitionScope<NavBackStackEntry>.() -> EnterTransition = {
        val initialRoute = initialState.destination.route
        val targetRoute = targetState.destination.route

        when {
            // 1. Voltando de um Modal: Restaura a tela de fundo ao tamanho original
            isModalRoute(initialRoute) -> {
                scaleIn(
                    initialScale = 0.92f,
                    transformOrigin = TransformOrigin.Center,
                    animationSpec = tween(durationMillis = 350, easing = EmphasizedDecelerate)
                ) + fadeIn(
                    animationSpec = tween(durationMillis = 280, easing = EmphasizedDecelerate)
                )
            }

            // 2. Voltando de um Detalhe: Tela pai reaparece avançando da paralaxe esquerda
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

            // 3. Abas irmãs do One UI Dock
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

            // 4. Padrão
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

    /**
     * Transição de saída ao descartar a tela atual na pilha (Pop Exit / Backward Navigation).
     */
    val popExitTransition: AnimatedContentTransitionScope<NavBackStackEntry>.() -> ExitTransition = {
        val initialRoute = initialState.destination.route
        val targetRoute = targetState.destination.route

        when {
            // 1. Fechando um Modal: Desce suavemente em direção à base
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

            // 2. Fechando um Detalhe: Desliza para a direita em direção de saída com profundidade
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

            // 3. Abas irmãs do One UI Dock
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

            // 4. Padrão
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
