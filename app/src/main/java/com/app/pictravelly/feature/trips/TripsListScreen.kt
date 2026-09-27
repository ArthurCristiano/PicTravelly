package com.app.pictravelly.feature.trips

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CalendarToday
import androidx.compose.material.icons.filled.CollectionsBookmark
import androidx.compose.material.icons.filled.Luggage
import androidx.compose.material.icons.filled.Place
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import coil.compose.AsyncImage
import com.app.pictravelly.core.database.model.trip.TripWithSpots
import com.app.pictravelly.core.design.components.PicTravellyCard
import com.app.pictravelly.core.design.components.PicTravellyText
import com.app.pictravelly.core.design.components.PicTravellyTitle
import com.app.pictravelly.core.navigation.DestinationScreen
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

private val DATE_FORMAT = SimpleDateFormat("dd 'de' MMM, yyyy", Locale.forLanguageTag("pt-BR"))
private val SHORT_DATE_FORMAT = SimpleDateFormat("dd 'de' MMM", Locale.forLanguageTag("pt-BR"))

/**
 * Aba Diário: cada card é uma viagem registrada. Tocar em um card abre os
 * pontos turísticos daquela viagem, com as fotos e as descrições.
 */
@Composable
fun TripsListScreen(
    viewModel: TripsViewModel,
    onNavigateToTripDetail: (Long) -> Unit,
    onNavigateToAllSpots: () -> Unit,
    modifier: Modifier = Modifier,
    contentPadding: PaddingValues = PaddingValues(0.dp)
) {
    val uiState by viewModel.uiState.collectAsState()

    Column(
        modifier = modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background)
            .padding(contentPadding)
            .padding(horizontal = 16.dp, vertical = 12.dp)
    ) {
        // ============================================================================
        // 🎨 [DESIGN / HUMBERTO] - CABEÇALHO DA ESTANTE DE VIAGENS:
        // - Cada viagem é um volume do diário; pode virar lombada de livro ou
        //   etiqueta de mala com carimbos.
        // ============================================================================
        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically
        ) {
            PicTravellyTitle(
                text = "Diário de Viagens",
                modifier = Modifier.weight(1f)
            )
            IconButton(onClick = onNavigateToAllSpots) {
                Icon(
                    imageVector = Icons.Default.Search,
                    contentDescription = "Buscar em todos os pontos",
                    tint = MaterialTheme.colorScheme.primary
                )
            }
        }

        if (!uiState.isEmpty) {
            PicTravellyText(
                text = "${uiState.totalTrips} ${if (uiState.totalTrips == 1) "viagem" else "viagens"}" +
                    " · ${uiState.totalSpots} ${if (uiState.totalSpots == 1) "ponto" else "pontos"}",
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                isSmall = true
            )
        }

        Spacer(modifier = Modifier.height(14.dp))

        when {
            uiState.isLoading -> Box(
                modifier = Modifier.fillMaxSize(),
                contentAlignment = Alignment.Center
            ) {
                CircularProgressIndicator(color = MaterialTheme.colorScheme.primary)
            }

            uiState.isEmpty -> EmptyTripsPlaceholder()

            else -> LazyColumn(
                verticalArrangement = Arrangement.spacedBy(14.dp),
                modifier = Modifier.fillMaxSize()
            ) {
                items(uiState.trips, key = { it.trip.id }) { tripWithSpots ->
                    TripJournalItem(
                        tripWithSpots = tripWithSpots,
                        onClick = { onNavigateToTripDetail(tripWithSpots.trip.id) }
                    )
                }

                // Grupo dos pontos que ainda não foram colocados em uma viagem
                if (uiState.spotsWithoutTrip.isNotEmpty()) {
                    item {
                        LooseSpotsItem(
                            count = uiState.spotsWithoutTrip.size,
                            coverUri = uiState.spotsWithoutTrip.firstNotNullOfOrNull { it.coverImageUri },
                            onClick = { onNavigateToTripDetail(DestinationScreen.NO_TRIP_ID) }
                        )
                    }
                }

                // Respiro final para não colidir com o dock One UI
                item {
                    Spacer(modifier = Modifier.height(88.dp))
                }
            }
        }
    }
}

/**
 * Item individual representando um volume do diário: uma viagem.
 */
@Composable
private fun TripJournalItem(
    tripWithSpots: TripWithSpots,
    onClick: () -> Unit
) {
    val trip = tripWithSpots.trip
    val period = formatPeriod(trip.startDate, trip.endDate)

    /* 🎨 [DESIGN / HUMBERTO]: Personalize as bordas, sombra e papel de fundo do volume */
    PicTravellyCard(
        modifier = Modifier
            .fillMaxWidth()
            .clickable { onClick() },
        containerColor = MaterialTheme.colorScheme.surface
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(12.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            TripThumbnail(
                coverUri = tripWithSpots.displayCoverUri,
                fallbackIcon = Icons.Default.Luggage
            )

            Spacer(modifier = Modifier.width(14.dp))

            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = trip.title,
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onSurface,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )

                Spacer(modifier = Modifier.height(4.dp))

                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(
                        imageVector = Icons.Default.CalendarToday,
                        contentDescription = null,
                        modifier = Modifier.size(12.dp),
                        tint = MaterialTheme.colorScheme.outline
                    )
                    Spacer(modifier = Modifier.width(4.dp))
                    Text(
                        text = period,
                        style = MaterialTheme.typography.labelSmall,
                        color = MaterialTheme.colorScheme.outline
                    )
                }

                Spacer(modifier = Modifier.height(6.dp))

                SpotsCountBadge(count = tripWithSpots.spotsCount)

                if (trip.description.isNotBlank()) {
                    Spacer(modifier = Modifier.height(6.dp))
                    Text(
                        text = trip.description,
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        maxLines = 2,
                        overflow = TextOverflow.Ellipsis
                    )
                }
            }
        }
    }
}

/**
 * Card do grupo "Pontos sem viagem", para os registros soltos continuarem
 * acessíveis a partir da aba Diário.
 */
@Composable
private fun LooseSpotsItem(
    count: Int,
    coverUri: String?,
    onClick: () -> Unit
) {
    PicTravellyCard(
        modifier = Modifier
            .fillMaxWidth()
            .clickable { onClick() },
        containerColor = MaterialTheme.colorScheme.surface
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(12.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            TripThumbnail(
                coverUri = coverUri,
                fallbackIcon = Icons.Default.Place
            )

            Spacer(modifier = Modifier.width(14.dp))

            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = "Pontos sem viagem",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onSurface
                )
                Spacer(modifier = Modifier.height(4.dp))
                Text(
                    text = "Registros que ainda não foram agrupados",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
                Spacer(modifier = Modifier.height(6.dp))
                SpotsCountBadge(count = count)
            }
        }
    }
}

@Composable
private fun TripThumbnail(
    coverUri: String?,
    fallbackIcon: androidx.compose.ui.graphics.vector.ImageVector
) {
    if (coverUri != null) {
        AsyncImage(
            model = coverUri,
            contentDescription = null,
            modifier = Modifier
                .size(88.dp)
                .clip(RoundedCornerShape(12.dp)),
            contentScale = ContentScale.Crop
        )
    } else {
        Box(
            modifier = Modifier
                .size(88.dp)
                .clip(RoundedCornerShape(12.dp))
                .background(MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.5f)),
            contentAlignment = Alignment.Center
        ) {
            Icon(
                imageVector = fallbackIcon,
                contentDescription = null,
                tint = MaterialTheme.colorScheme.primary,
                modifier = Modifier.size(32.dp)
            )
        }
    }
}

@Composable
private fun SpotsCountBadge(count: Int) {
    Row(
        verticalAlignment = Alignment.CenterVertically,
        modifier = Modifier
            .clip(CircleShape)
            .background(MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.6f))
            .padding(horizontal = 10.dp, vertical = 4.dp)
    ) {
        Icon(
            imageVector = Icons.Default.Place,
            contentDescription = null,
            modifier = Modifier.size(12.dp),
            tint = MaterialTheme.colorScheme.onPrimaryContainer
        )
        Spacer(modifier = Modifier.width(4.dp))
        Text(
            text = if (count == 1) "1 ponto turístico" else "$count pontos turísticos",
            style = MaterialTheme.typography.labelSmall,
            fontWeight = FontWeight.Medium,
            color = MaterialTheme.colorScheme.onPrimaryContainer
        )
    }
}

/**
 * Placeholder para quando nenhuma viagem foi registrada ainda.
 */
@Composable
private fun EmptyTripsPlaceholder() {
    Box(
        modifier = Modifier
            .fillMaxSize()
            .padding(32.dp),
        contentAlignment = Alignment.Center
    ) {
        Column(horizontalAlignment = Alignment.CenterHorizontally) {
            Icon(
                imageVector = Icons.Default.CollectionsBookmark,
                contentDescription = null,
                tint = MaterialTheme.colorScheme.primary.copy(alpha = 0.6f),
                modifier = Modifier.size(54.dp)
            )
            Spacer(modifier = Modifier.height(16.dp))
            Text(
                text = "Seu diário ainda não possui viagens.",
                style = MaterialTheme.typography.bodyLarge,
                fontWeight = FontWeight.Medium,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
            Spacer(modifier = Modifier.height(6.dp))
            Text(
                text = "Toque no + do dock para criar a primeira.",
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.outline
            )
        }
    }
}

/**
 * "12 de mar" + "20 de mar, 2026", ou "em andamento" quando não há data de fim.
 */
private fun formatPeriod(startDate: Long, endDate: Long?): String {
    val start = Date(startDate)
    if (endDate == null) {
        return "${DATE_FORMAT.format(start)} · em andamento"
    }

    val end = Date(endDate)
    if (startDate == endDate) return DATE_FORMAT.format(start)

    return "${SHORT_DATE_FORMAT.format(start)} - ${DATE_FORMAT.format(end)}"
}
