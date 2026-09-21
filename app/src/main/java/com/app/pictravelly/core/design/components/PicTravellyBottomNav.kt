package com.app.pictravelly.core.design.components

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
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
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.unit.dp
import com.app.pictravelly.core.navigation.DestinationScreen

data class BottomNavItem(
    val title: String,
    val icon: ImageVector,
    val route: String
)

@Composable
fun PicTravellyFloatingNavBar(
    destinations: List<DestinationScreen>,
    currentRoute: String,
    onNavigateToDestination: (DestinationScreen) -> Unit,
    modifier: Modifier = Modifier
) {
    Surface(

        modifier = modifier
            // 1. PRIMEIRO repila o tamanho da barra do sistema operacional (botões ou gestos)
            .navigationBarsPadding()
            // 2. DEPOIS aplique o seu recuo estético para que a barra "flutue"
            .padding(horizontal = 16.dp, vertical = 24.dp)
            .fillMaxWidth()
            // 3. Troca de height fixo para heightIn (proteção de acessibilidade para textos grandes)
            .heightIn(min = 24.dp),
        shape = CircleShape,
        color = MaterialTheme.colorScheme.surface,
        shadowElevation = 8.dp
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 8.dp, vertical = 8.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            destinations.forEach { destination ->
                val isSelected = currentRoute == destination.route
                NavBarItem(
                    destination = destination,
                    isSelected = isSelected,
                    onClick = { onNavigateToDestination(destination) }
                )
            }
        }
    }
}

@Composable
private fun NavBarItem(
    destination: DestinationScreen,
    isSelected: Boolean,
    onClick: () -> Unit
) {
    // Animação de transição de cor de fundo
    val backgroundColor by animateColorAsState(
        targetValue = if (isSelected) MaterialTheme.colorScheme.primaryContainer else Color.Transparent,
        animationSpec = tween(durationMillis = 300), label = "background_color"
    )

    // Animação de cor do ícone/texto
    val contentColor by animateColorAsState(
        targetValue = if (isSelected) MaterialTheme.colorScheme.onPrimaryContainer else MaterialTheme.colorScheme.onSurfaceVariant,
        animationSpec = tween(durationMillis = 300), label = "content_color"
    )

    Box(
        modifier = Modifier
            .clip(CircleShape)
            .background(backgroundColor)
            .clickable(
                interactionSource = remember { MutableInteractionSource() },
                indication = null, // Remove o ripple nativo redondo para não bugar a pílula
                onClick = onClick
            )
            .padding(horizontal = 16.dp, vertical = 12.dp)
    ) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.Center
        ) {
            Icon(
                imageVector = destination.icon,
                contentDescription = destination.title, // Pode ser trocado por stringResource no futuro
                tint = contentColor,
                modifier = Modifier.size(24.dp)
            )
            AnimatedVisibility(visible = isSelected) {
                Text(
                    text = destination.title,
                    color = contentColor,
                    style = MaterialTheme.typography.labelMedium,
                    modifier = Modifier.padding(start = 8.dp),
                    maxLines = 1
                )
            }
        }
    }
}