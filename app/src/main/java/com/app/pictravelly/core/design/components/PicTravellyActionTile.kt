package com.app.pictravelly.core.design.components

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Outline
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.Shape
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.Density
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.LayoutDirection
import androidx.compose.ui.unit.dp
import kotlin.math.PI
import kotlin.math.cos
import kotlin.math.min
import kotlin.math.sin

/**
 * Moldura ondulada traçada em coordenadas polares variando o raio de um círculo.
 */
data class ScallopedShape(
    private val petals: Int = 10,
    private val depth: Float = 0.07f
) : Shape {

    override fun createOutline(
        size: Size,
        layoutDirection: LayoutDirection,
        density: Density
    ): Outline {
        val path = Path()
        val centerX = size.width / 2f
        val centerY = size.height / 2f
        // Mantém o desenho inteiro dentro dos limites do componente.
        val baseRadius = min(centerX, centerY) * (1f - depth)

        for (step in 0..STEPS) {
            val angle = (step.toFloat() / STEPS) * 2f * PI.toFloat()
            val radius = baseRadius * (1f + depth * cos(petals * angle))
            val x = centerX + radius * cos(angle)
            val y = centerY + radius * sin(angle)
            if (step == 0) path.moveTo(x, y) else path.lineTo(x, y)
        }
        path.close()

        return Outline.Generic(path)
    }

    private companion object {
        const val STEPS = 360
    }
}

/**
 * Botão de ação com ícone principal à esquerda e suporte a ação secundária à direita.
 */
@Composable
fun PicTravellyActionTile(
    icon: ImageVector,
    label: String,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    supportingText: String? = null,
    trailingIcon: ImageVector? = null,
    trailingLabel: String? = null,
    onTrailingClick: (() -> Unit)? = null,
    badgeSize: Dp = 56.dp,
    iconSize: Dp = 28.dp,
    trailingBadgeSize: Dp = 46.dp,
    trailingIconSize: Dp = 22.dp,
    containerColor: Color = MaterialTheme.colorScheme.surface,
    badgeColor: Color = MaterialTheme.colorScheme.primary,
    badgeContentColor: Color = MaterialTheme.colorScheme.onPrimary
) {
    PicTravellyCard(
        modifier = modifier.fillMaxWidth(),
        containerColor = containerColor,
        onClick = onClick
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 14.dp, vertical = 14.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Box(
                modifier = Modifier
                    .size(badgeSize)
                    .background(color = badgeColor, shape = ScallopedShape()),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = icon,
                    contentDescription = null,
                    tint = badgeContentColor,
                    modifier = Modifier.size(iconSize)
                )
            }

            Spacer(modifier = Modifier.width(14.dp))

            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = label,
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onSurface,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )

                if (supportingText != null) {
                    Text(
                        text = supportingText,
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                }
            }

            if (trailingIcon != null && onTrailingClick != null) {
                Spacer(modifier = Modifier.width(10.dp))

                // Ação secundária: consome o próprio toque, sem disparar o principal.
                Box(
                    modifier = Modifier
                        .size(trailingBadgeSize)
                        .clip(ScallopedShape())
                        .background(MaterialTheme.colorScheme.primaryContainer)
                        .clickable(role = Role.Button, onClick = onTrailingClick),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = trailingIcon,
                        contentDescription = trailingLabel,
                        tint = MaterialTheme.colorScheme.onPrimaryContainer,
                        modifier = Modifier.size(trailingIconSize)
                    )
                }
            }
        }
    }
}
