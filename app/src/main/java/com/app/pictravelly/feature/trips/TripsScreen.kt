package com.app.pictravelly.feature.trips

import androidx.compose.foundation.background
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
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.app.pictravelly.core.design.components.PicTravellyText
import com.app.pictravelly.core.design.components.PicTravellyTitle
import com.app.pictravelly.core.navigation.DestinationScreen
import com.app.pictravelly.feature.trips.components.EmptyTripsPlaceholder
import com.app.pictravelly.feature.trips.components.LooseSpotsItem
import com.app.pictravelly.feature.trips.components.TripJournalItem

@Composable
fun TripsScreen(
    uiState: TripsUiState,
    onNavigateToTripDetail: (Long) -> Unit,
    onNavigateToAllSpots: () -> Unit,
    modifier: Modifier = Modifier,
    contentPadding: PaddingValues = PaddingValues(0.dp)
) {
    Column(
        modifier = modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background)
            .padding(contentPadding)
            .padding(horizontal = 16.dp, vertical = 12.dp)
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically
        ) {
            PicTravellyTitle(text = "Diário de Viagens", modifier = Modifier.weight(1f))
            IconButton(onClick = onNavigateToAllSpots) {
                Icon(
                    Icons.Default.Search,
                    contentDescription = "Buscar em todos os pontos",
                    tint = MaterialTheme.colorScheme.primary
                )
            }
        }

        if (!uiState.isEmpty) {
            PicTravellyText(
                text = "${uiState.totalTrips} ${if (uiState.totalTrips == 1) "viagem" else "viagens"} · ${uiState.totalSpots} ${if (uiState.totalSpots == 1) "ponto" else "pontos"}",
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
                items(uiState.trips, key = { it.id }) { tripUiModel ->
                    TripJournalItem(
                        trip = tripUiModel,
                        onClick = { onNavigateToTripDetail(tripUiModel.id) }
                    )
                }

                if (uiState.looseSpotsCount > 0) {
                    item {
                        LooseSpotsItem(
                            count = uiState.looseSpotsCount,
                            coverUri = uiState.looseSpotsCoverUri,
                            onClick = { onNavigateToTripDetail(DestinationScreen.NO_TRIP_ID) }
                        )
                    }
                }

                item { Spacer(modifier = Modifier.height(88.dp)) }
            }
        }
    }
}
