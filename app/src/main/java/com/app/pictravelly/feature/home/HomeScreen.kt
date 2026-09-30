package com.app.pictravelly.feature.home

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.spring
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.scaleIn
import androidx.compose.animation.scaleOut
import androidx.compose.animation.slideInVertically
import androidx.compose.animation.slideOutVertically
import androidx.compose.ui.graphics.TransformOrigin
import com.app.pictravelly.core.navigation.PicTravellyNavTransitions
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
import com.app.pictravelly.core.map.components.PicTravellySelectedSpotFloatingCard
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

        // Modo Mapa Expandido Padronizado (Sobrepondo a Home com expansão orgânica estilo Container Transform)
        AnimatedVisibility(
            visible = uiState.isMapExpanded,
            enter = fadeIn(
                animationSpec = tween(
                    durationMillis = 350,
                    easing = PicTravellyNavTransitions.EmphasizedDecelerate
                )
            ) + scaleIn(
                initialScale = 0.86f,
                transformOrigin = TransformOrigin(0.5f, 0.35f),
                animationSpec = spring(
                    dampingRatio = 0.82f,
                    stiffness = Spring.StiffnessMediumLow
                )
            ),
            exit = fadeOut(
                animationSpec = tween(
                    durationMillis = 200,
                    easing = PicTravellyNavTransitions.EmphasizedAccelerate
                )
            ) + scaleOut(
                targetScale = 0.86f,
                transformOrigin = TransformOrigin(0.5f, 0.35f),
                animationSpec = tween(
                    durationMillis = 250,
                    easing = PicTravellyNavTransitions.EmphasizedAccelerate
                )
            )
        ) {
            PicTravellySmartMapView(
                latitude = uiState.currentLatitude,
                longitude = uiState.currentLongitude,
                markers = uiState.markers,
                onClose = { onMapExpandedChange(false) },
                onMarkerClick = { marker ->
                    val selected = uiState.spots.firstOrNull { it.spot.id == marker.id }
                    if (selected != null) onSpotSelect(selected)
                },
                onMapClick = { onSpotSelect(null) },
                onFetchLocationRequested = onFetchLocationRequested,
                bottomContent = {
                    PicTravellySelectedSpotFloatingCard(
                        selectedSpot = uiState.selectedSpot,
                        onNavigateToDetail = onNavigateToDetail,
                        onClose = { onSpotSelect(null) },
                        modifier = Modifier
                            .align(Alignment.BottomCenter)
                            .padding(bottom = 16.dp)
                    )
                },
                showCloseButton = true
            )
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