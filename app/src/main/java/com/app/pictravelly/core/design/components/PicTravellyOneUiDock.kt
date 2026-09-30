package com.app.pictravelly.core.design.components

import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.spring
import androidx.compose.animation.core.tween
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.CollectionsBookmark
import androidx.compose.material.icons.filled.Explore
import androidx.compose.material.icons.filled.Map
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp

/**
 * Representação de cada atalho do Dock One UI 9.
 *
 * O botão central de cadastro não entra nesta lista: ele não é uma rota,
 * apenas abre a folha de escolha entre nova viagem e novo ponto turístico.
 */
sealed class DockDestination(
    val route: String,
    val title: String,
    val icon: ImageVector
) {
    object Home : DockDestination("home_route", "Início", Icons.Default.Explore)
    object Trips : DockDestination("trips_route", "Diário", Icons.Default.CollectionsBookmark)
    object Map : DockDestination("map_route", "Mapa", Icons.Default.Map)
    object Settings : DockDestination("settings_route", "Ajustes", Icons.Default.Settings)

    companion object {
        /** Duas abas à esquerda do "+" e duas à direita. */
        val items = listOf(Home, Trips, Map, Settings)
    }
}

/**
 * Dock Pill-Shaped flutuante com acabamento translúcido (Frosted Glass) inspirado no One UI 9.
 *
 * Cada atalho mostra o ícone com o rótulo logo abaixo, o que mantém os quatro
 * destinos e o botão central de cadastro confortáveis na mesma linha.
 */
@Composable
fun PicTravellyOneUiDock(
    currentRoute: String,
    onNavigate: (DockDestination) -> Unit,
    onAddEntry: () -> Unit,
    modifier: Modifier = Modifier
) {
    val half = DockDestination.items.size / 2
    val leftItems = DockDestination.items.take(half)
    val rightItems = DockDestination.items.drop(half)

    Box(
        modifier = modifier
            .fillMaxWidth()
            .navigationBarsPadding()
            .padding(horizontal = 16.dp, vertical = 12.dp),
        contentAlignment = Alignment.Center
    ) {
        // Superfície translúcida One UI 9 com cantos em pílula
        Surface(
            modifier = Modifier
                .shadow(
                    elevation = 16.dp,
                    shape = CircleShape,
                    spotColor = MaterialTheme.colorScheme.primary.copy(alpha = 0.20f),
                    ambientColor = Color.Black.copy(alpha = 0.15f)
                )
                .clip(CircleShape)
                .border(
                    BorderStroke(
                        width = 1.dp,
                        color = Color.White.copy(alpha = 0.25f)
                    ),
                    shape = CircleShape
                ),
            shape = CircleShape,
            color = MaterialTheme.colorScheme.surface.copy(alpha = 0.82f) // Frosted glass translucency
        ) {
            Row(
                modifier = Modifier.padding(horizontal = 8.dp, vertical = 6.dp),
                horizontalArrangement = Arrangement.spacedBy(2.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                leftItems.forEach { item ->
                    DockItem(
                        item = item,
                        isSelected = currentRoute == item.route,
                        onClick = {
                            if (currentRoute != item.route) {
                                onNavigate(item)
                            }
                        }
                    )
                }

                DockAddButton(onClick = onAddEntry)

                rightItems.forEach { item ->
                    DockItem(
                        item = item,
                        isSelected = currentRoute == item.route,
                        onClick = {
                            if (currentRoute != item.route) {
                                onNavigate(item)
                            }
                        }
                    )
                }
            }
        }
    }
}

/**
 * Botão central de cadastro, em destaque sobre a cor primária.
 */
@Composable
private fun DockAddButton(onClick: () -> Unit) {
    Box(
        modifier = Modifier
            .padding(horizontal = 6.dp)
            .size(48.dp)
            .shadow(
                elevation = 8.dp,
                shape = CircleShape,
                spotColor = MaterialTheme.colorScheme.primary.copy(alpha = 0.45f)
            )
            .clip(CircleShape)
            .background(MaterialTheme.colorScheme.primary)
            .clickable(
                interactionSource = remember { MutableInteractionSource() },
                indication = null,
                role = Role.Button,
                onClick = onClick
            ),
        contentAlignment = Alignment.Center
    ) {
        Icon(
            imageVector = Icons.Default.Add,
            contentDescription = "Adicionar viagem ou ponto turístico",
            tint = MaterialTheme.colorScheme.onPrimary,
            modifier = Modifier.size(26.dp)
        )
    }
}

/**
 * Atalho individual do dock: ícone com o rótulo logo abaixo.
 */
@Composable
private fun DockItem(
    item: DockDestination,
    isSelected: Boolean,
    onClick: () -> Unit
) {
    val backgroundColor by animateColorAsState(
        targetValue = if (isSelected) {
            MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.90f)
        } else {
            Color.Transparent
        },
        animationSpec = spring(dampingRatio = Spring.DampingRatioMediumBouncy),
        label = "dock_bg_color"
    )

    val contentColor by animateColorAsState(
        targetValue = if (isSelected) {
            MaterialTheme.colorScheme.onPrimaryContainer
        } else {
            MaterialTheme.colorScheme.onSurfaceVariant
        },
        animationSpec = tween(200),
        label = "dock_content_color"
    )

    Box(
        modifier = Modifier
            .width(62.dp)
            .clip(RoundedCornerShape(20.dp))
            .background(backgroundColor)
            .clickable(
                interactionSource = remember { MutableInteractionSource() },
                indication = null,
                onClick = onClick
            )
            .padding(vertical = 6.dp),
        contentAlignment = Alignment.Center
    ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            Icon(
                imageVector = item.icon,
                contentDescription = item.title,
                tint = contentColor,
                modifier = Modifier.size(22.dp)
            )

            Spacer(modifier = Modifier.height(2.dp))

            Text(
                text = item.title,
                color = contentColor,
                style = MaterialTheme.typography.labelSmall,
                fontWeight = if (isSelected) FontWeight.SemiBold else FontWeight.Normal,
                textAlign = TextAlign.Center,
                maxLines = 1
            )
        }
    }
}
