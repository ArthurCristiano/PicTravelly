package com.app.pictravelly.core.map.components.mapLegend

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.slideInVertically
import androidx.compose.animation.slideOutVertically
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.FormatListBulleted
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.app.pictravelly.core.map.model.MapMarkerData
import com.app.pictravelly.core.map.model.MarkerType


/**
 * Componente interno expansível para exibir a legenda de coordenadas.
 */
@Composable
fun MapLegendCard(
    markers: List<MapMarkerData>,
    onMarkerClick: (MapMarkerData) -> Unit,
    onCenterOnUser: (() -> Unit)?,
    modifier: Modifier = Modifier
) {
    var isExpanded by remember { mutableStateOf(false) }

    // Separa o usuário dos pontos turísticos reais
    val userMarker = markers.find { it.type == MarkerType.USER_LOCATION }
    val touristSpots = markers.filter { it.type == MarkerType.TOURIST_SPOT }

    Column(modifier = modifier, horizontalAlignment = Alignment.Start) {
        // Botão Fechado (Ícone Flutuante)
        AnimatedVisibility(
            visible = !isExpanded,
            enter = fadeIn(),
            exit = fadeOut()
        ) {
            Box(
                modifier = Modifier
                    .size(48.dp)
                    .clip(CircleShape)
                    .background(MaterialTheme.colorScheme.surface.copy(alpha = 0.90f))
                    .border(1.dp, Color.White.copy(alpha = 0.3f), CircleShape)
                    .clickable { isExpanded = true },
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = Icons.Default.FormatListBulleted,
                    contentDescription = "Abrir Legenda",
                    tint = MaterialTheme.colorScheme.onSurface
                )
            }
        }

        // Card Expandido (Lista de Coordenadas)
        AnimatedVisibility(
            visible = isExpanded,
            enter = slideInVertically(initialOffsetY = { -it }) + fadeIn(),
            exit = slideOutVertically(targetOffsetY = { -it }) + fadeOut()
        ) {
            Card(
                shape = RoundedCornerShape(16.dp),
                colors = CardDefaults.cardColors(
                    containerColor = MaterialTheme.colorScheme.surface.copy(alpha = 0.95f)
                ),
                elevation = CardDefaults.cardElevation(defaultElevation = 6.dp),
                modifier = Modifier
                    .width(260.dp)
                    .heightIn(max = 400.dp) // Evita que a lista ocupe a tela inteira
            ) {
                Column(modifier = Modifier.padding(12.dp)) {
                    // Cabeçalho
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = "Legenda do Mapa",
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Bold
                        )
                        Icon(
                            imageVector = Icons.Default.Close,
                            contentDescription = "Fechar",
                            modifier = Modifier
                                .size(24.dp)
                                .clip(CircleShape)
                                .clickable { isExpanded = false },
                            tint = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }

                    HorizontalDivider(modifier = Modifier.padding(vertical = 8.dp))

                    LazyColumn(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                        // 1. Posição Atual (Aparece SOMENTE se o marker do usuário existir)
                        userMarker?.let { user ->
                            item {
                                LegendListItem(
                                    title = "Sua Localização",
                                    latitude = user.latitude,
                                    longitude = user.longitude,
                                    isUser = true,
                                    onClick = {
                                        onCenterOnUser?.invoke()
                                        isExpanded = false
                                    }
                                )
                            }
                        }

                        // 2. Pontos Turísticos
                        items(touristSpots) { spot ->
                            LegendListItem(
                                title = spot.title,
                                latitude = spot.latitude,
                                longitude = spot.longitude,
                                isUser = false,
                                onClick = {
                                    onMarkerClick(spot)
                                    isExpanded = false
                                }
                            )
                        }
                    }
                }
            }
        }
    }
}
