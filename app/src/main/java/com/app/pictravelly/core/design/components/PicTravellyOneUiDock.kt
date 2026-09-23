package com.app.pictravelly.core.design.components

import androidx.compose.animation.AnimatedVisibility
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
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CollectionsBookmark
import androidx.compose.material.icons.filled.Explore
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
import androidx.compose.ui.unit.dp

/**
 * Representação de cada atalho do Dock One UI 9.
 */
sealed class DockDestination(
    val route: String,
    val title: String,
    val icon: ImageVector
) {
    object Home : DockDestination("home_route", "Início", Icons.Default.Explore)
    object Spots : DockDestination("spots_route", "Diário", Icons.Default.CollectionsBookmark)
    object Settings : DockDestination("settings_route", "Ajustes", Icons.Default.Settings)

    companion object {
        val items = listOf(Home, Spots, Settings)
    }
}

/**
 * Dock Pill-Shaped flutuante com acabamento translúcido (Frosted Glass) inspirado no One UI 9.
 * Ícones consistentes e harmônicos entre Início, Diário e Ajustes.
 */
@Composable
fun PicTravellyOneUiDock(
    currentRoute: String,
    onNavigate: (DockDestination) -> Unit,
    modifier: Modifier = Modifier
) {
    Box(
        modifier = modifier
            .fillMaxWidth()
            .navigationBarsPadding()
            .padding(horizontal = 24.dp, vertical = 12.dp),
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
                modifier = Modifier.padding(horizontal = 10.dp, vertical = 6.dp),
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                DockDestination.items.forEach { item ->
                    val isSelected = currentRoute == item.route
                    DockItem(
                        item = item,
                        isSelected = isSelected,
                        onClick = { onNavigate(item) }
                    )
                }
            }
        }
    }
}

@Composable
private fun DockItem(
    item: DockDestination,
    isSelected: Boolean,
    onClick: () -> Unit
) {
    val backgroundColor by animateColorAsState(
        targetValue = if (isSelected) MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.90f) else Color.Transparent,
        animationSpec = spring(dampingRatio = Spring.DampingRatioMediumBouncy),
        label = "dock_bg_color"
    )

    val contentColor by animateColorAsState(
        targetValue = if (isSelected) MaterialTheme.colorScheme.onPrimaryContainer else MaterialTheme.colorScheme.onSurfaceVariant,
        animationSpec = tween(200),
        label = "dock_content_color"
    )

    Box(
        modifier = Modifier
            .height(44.dp)
            .clip(CircleShape)
            .background(backgroundColor)
            .clickable(
                interactionSource = remember { MutableInteractionSource() },
                indication = null,
                onClick = onClick
            )
            .padding(horizontal = 14.dp),
        contentAlignment = Alignment.Center
    ) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.Center
        ) {
            Icon(
                imageVector = item.icon,
                contentDescription = item.title,
                tint = contentColor,
                modifier = Modifier.size(22.dp)
            )

            AnimatedVisibility(visible = isSelected) {
                Text(
                    text = item.title,
                    color = contentColor,
                    style = MaterialTheme.typography.labelMedium,
                    modifier = Modifier.padding(start = 6.dp),
                    maxLines = 1
                )
            }
        }
    }
}
