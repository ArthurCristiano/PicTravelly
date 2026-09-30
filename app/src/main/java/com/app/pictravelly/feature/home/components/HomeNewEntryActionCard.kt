package com.app.pictravelly.feature.home.components

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AddLocationAlt
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.app.pictravelly.core.design.components.PicTravellyButton
import com.app.pictravelly.core.design.components.PicTravellyCard
import com.app.pictravelly.core.design.theme.PicTravellyTheme


/**
 * Card de Ação Convidativo para Adicionar Novo Registro no Diário.
 */
@Composable
fun HomeNewEntryActionCard(
    onNavigateToCreate: () -> Unit,
    modifier: Modifier = Modifier
) {
    /* 🎨 [DESIGN / HUMBERTO] - CARD DE NOVO REGISTRO:
     * Personalize com tipografia cursiva, textura de pergaminho ou carimbo vintage de viagem.
     */
    PicTravellyCard(
        modifier = modifier
            .fillMaxWidth()
            .clickable(onClick = onNavigateToCreate),
        containerColor = MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.45f)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier.weight(1f)
            ) {
                Box(
                    modifier = Modifier
                        .size(46.dp)
                        .clip(CircleShape)
                        .background(MaterialTheme.colorScheme.primary),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = Icons.Default.AddLocationAlt,
                        contentDescription = "Registrar Parada",
                        tint = MaterialTheme.colorScheme.onPrimary,
                        modifier = Modifier.size(24.dp)
                    )
                }
                Spacer(modifier = Modifier.width(14.dp))
                Column {
                    Text(
                        text = "Registrar Nova Parada",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.onSurface
                    )
                    Text(
                        text = "Fotografe e anote suas memórias de hoje",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }

            PicTravellyButton(
                text = "Registrar",
                onClick = onNavigateToCreate,
                modifier = Modifier.padding(start = 8.dp)
            )
        }
    }
}


@Preview(showBackground = true)
@Composable
private fun HomeGamificationCardPreview() {
    PicTravellyTheme {
        HomeNewEntryActionCard(
            onNavigateToCreate = {},
            modifier = Modifier.padding(16.dp)
        )
    }
}