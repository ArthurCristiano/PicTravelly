package com.app.pictravelly.feature.home

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.slideInVertically
import androidx.compose.animation.slideOutVertically
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
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
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
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import coil.compose.AsyncImage
import com.app.pictravelly.core.database.model.touristSpot.TouristSpotWithImages
import com.app.pictravelly.core.design.components.PicTravellyButton
import com.app.pictravelly.core.design.theme.PicTravellyTheme
import com.app.pictravelly.core.map.components.expandedMap.PicTravellySmartMapView
import com.app.pictravelly.core.map.components.previewCard.PicTravellySmartPreviewMapCard
import com.app.pictravelly.feature.home.components.HomeGamificationCard
import com.app.pictravelly.feature.home.components.HomeHeaderSection
import com.app.pictravelly.feature.home.components.HomeNewEntryActionCard
import com.app.pictravelly.feature.home.components.RecentMemoriesSection

@Composable
fun HomeScreen(
    uiState: HomeUiState,
    contentPadding: PaddingValues,
    onMapExpandedChange: (Boolean) -> Unit,
    onSpotSelect: (TouristSpotWithImages?) -> Unit,
    onNavigateToDetail: (Long) -> Unit,
    onNavigateToSpots: () -> Unit,
    onNavigateToCreate: () -> Unit,
    onFetchLocationRequested: () -> Unit,
    modifier: Modifier = Modifier
) {
    Box(modifier = modifier.fillMaxSize()) {

        // Modo Normal: Dashboard Rolável
        Column(
            modifier = Modifier
                .fillMaxSize()
                .background(MaterialTheme.colorScheme.background)
                .verticalScroll(rememberScrollState())
                .padding(contentPadding)
                .padding(horizontal = 16.dp, vertical = 16.dp),
            verticalArrangement = Arrangement.spacedBy(20.dp)
        ) {
            HomeHeaderSection(
                totalSpots = uiState.totalSpotsCount,
                travelerLevel = uiState.travelerLevel
            )

            HomeGamificationCard(
                travelerLevel = uiState.travelerLevel,
                xpProgress = uiState.xpProgress,
                totalSpots = uiState.totalSpotsCount
            )

            PicTravellySmartPreviewMapCard(
                currentLatitude = uiState.currentLatitude,
                currentLongitude = uiState.currentLongitude,
                markers = uiState.markers, // <--- Usa diretamente da UiState
                onExpandClick = { onMapExpandedChange(true) },
                onMarkerSelect = { markerId ->
                    val selected = uiState.spots.firstOrNull { it.spot.id == markerId }
                    if (selected != null) {
                        onSpotSelect(selected)
                        onMapExpandedChange(true)
                    }
                },
                onFetchLocationRequested = onFetchLocationRequested
            )

            HomeNewEntryActionCard(
                onNavigateToCreate = onNavigateToCreate
            )

            RecentMemoriesSection(
                spots = uiState.spots,
                onSpotClick = onNavigateToDetail,
                onViewAllClick = onNavigateToSpots
            )

            Spacer(modifier = Modifier.height(72.dp))
        }

        // Modo Mapa Expandido Padronizado (Sobrepondo a Home)
        AnimatedVisibility(
            visible = uiState.isMapExpanded,
            enter = fadeIn(),
            exit = fadeOut()
        ) {
            PicTravellySmartMapView(
                latitude = uiState.currentLatitude,
                longitude = uiState.currentLongitude,
                markers = uiState.markers, // <--- Usa diretamente da UiState
                onClose = { onMapExpandedChange(false) },
                onMarkerClick = { marker ->
                    val selected = uiState.spots.firstOrNull { it.spot.id == marker.id }
                    if (selected != null) onSpotSelect(selected)
                },
                onFetchLocationRequested = onFetchLocationRequested,
                bottomContent = {
                    SelectedSpotFloatingCard(
                        selectedSpot = uiState.selectedSpot,
                        onNavigateToDetail = onNavigateToDetail,
                        modifier = Modifier
                            .align(Alignment.BottomCenter)
                            .padding(bottom = contentPadding.calculateBottomPadding()) // <--- Correção do Bottom Nav
                    )
                },
                showCloseButton = true
            )
        }
    }
}

/**
 * Componente privado da Home para desenhar o card do ponto turístico.
 * Como ele vive na Feature, ele pode consumir a entidade do banco de dados livremente.
 */
@Composable
private fun SelectedSpotFloatingCard(
    selectedSpot: TouristSpotWithImages?,
    onNavigateToDetail: (Long) -> Unit,
    modifier: Modifier = Modifier
) {
    AnimatedVisibility(
        visible = selectedSpot != null,
        modifier = modifier
            .navigationBarsPadding()
            .padding(bottom = 24.dp, start = 16.dp, end = 16.dp),
        enter = slideInVertically(initialOffsetY = { it }) + fadeIn(),
        exit = slideOutVertically(targetOffsetY = { it }) + fadeOut()
    ) {
        selectedSpot?.let { selected ->
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(20.dp))
                    .clickable { onNavigateToDetail(selected.spot.id) },
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
                                .clip(RoundedCornerShape(12.dp)),
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

@Preview(showBackground = true)
@Composable
private fun HomeScreenPreview() {
    PicTravellyTheme {
        HomeScreen(
            uiState = HomeUiState(),
            contentPadding = PaddingValues(0.dp),
            onMapExpandedChange = {},
            onSpotSelect = {},
            onNavigateToDetail = {},
            onNavigateToSpots = {},
            onNavigateToCreate = {},
            onFetchLocationRequested = {}
        )
    }
}