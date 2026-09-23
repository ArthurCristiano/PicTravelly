package com.app.pictravelly.core.design.componentes

import androidx.compose.foundation.layout.ColumnScope
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ElevatedCard
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun PicTravellyCartao(
    modifier: Modifier = Modifier,
    containerColor: Color = MaterialTheme.colorScheme.surface,
    onClick: (() -> Unit)? = null,
    // Slot API: Delega a construção interna para a tela que chama o card
    content: @Composable ColumnScope.() -> Unit
) {
    // Trava o formato visual de todos os cards da marca em 16dp
    val cardShape = RoundedCornerShape(16.dp)
    val cardElevation = CardDefaults.elevatedCardElevation(defaultElevation = 4.dp)
    val cardColors = CardDefaults.cardColors(containerColor = containerColor)

    // Avalia se o card possui ação de clique ou se é apenas exibição visual
    if (onClick != null) {
        ElevatedCard(
            onClick = onClick,
            modifier = modifier,
            shape = cardShape,
            colors = cardColors,
            elevation = cardElevation,
            content = content
        )
    } else {
        ElevatedCard(
            modifier = modifier,
            shape = cardShape,
            colors = cardColors,
            elevation = cardElevation,
            content = content
        )
    }
}