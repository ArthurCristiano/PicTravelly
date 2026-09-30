package com.app.pictravelly.core.map.components

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.slideInVertically
import androidx.compose.animation.slideOutVertically
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Place
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import coil.compose.AsyncImage
import com.app.pictravelly.core.database.model.touristSpot.TouristSpotWithImages
import com.app.pictravelly.core.design.components.PicTravellyButton

/**
 * Card exibido na base da tela quando um pino é clicado.
 */
@Composable
fun PicTravellySelectedSpotFloatingCard(
    selectedSpot: TouristSpotWithImages?,
    onNavigateToDetail: (Long) -> Unit,
    modifier: Modifier = Modifier
) {
    AnimatedVisibility(
        visible = selectedSpot != null,
        modifier = modifier.navigationBarsPadding(), // O padding bottom extra deve vir pelo modifier do pai!
        enter = slideInVertically(initialOffsetY = { it }) + fadeIn(),
        exit = slideOutVertically(targetOffsetY = { it }) + fadeOut()
    ) {
        selectedSpot?.let { selected ->
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(
                        horizontal = 16.dp,
                        vertical = 16.dp
                    ) // Paddings internos do componente
                    .clickable { onNavigateToDetail(selected.spot.id) },
                shape = RoundedCornerShape(20.dp), // <--- Forma correta de arredondar um Card
                colors = CardDefaults.cardColors(
                    containerColor = MaterialTheme.colorScheme.surface.copy(alpha = 0.95f)
                ),
                elevation = CardDefaults.cardElevation(defaultElevation = 8.dp)
            ) {
                Row(
                    modifier = Modifier.padding(16.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    if (selected.coverImageUri != null) {
                        AsyncImage(
                            model = selected.coverImageUri,
                            contentDescription = selected.spot.title,
                            modifier = Modifier
                                .size(72.dp)
                                .clip(RoundedCornerShape(12.dp)), // Aqui o clip faz sentido, pois AsyncImage não tem parâmetro shape
                            contentScale = ContentScale.Crop
                        )
                        Spacer(modifier = Modifier.width(14.dp))
                    }

                    Column(modifier = Modifier.weight(1f)) {
                        Text(
                            text = selected.spot.title,
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Bold,
                            maxLines = 1
                        )
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(
                                imageVector = Icons.Default.Place,
                                contentDescription = null,
                                modifier = Modifier.size(14.dp),
                                tint = MaterialTheme.colorScheme.primary
                            )
                            Spacer(modifier = Modifier.width(4.dp))
                            Text(
                                text = selected.spot.locationName,
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant,
                                maxLines = 1
                            )
                        }
                        Spacer(modifier = Modifier.height(8.dp))
                        PicTravellyButton(
                            text = "Abrir Diário",
                            onClick = { onNavigateToDetail(selected.spot.id) }
                        )
                    }
                }
            }
        }
    }
}