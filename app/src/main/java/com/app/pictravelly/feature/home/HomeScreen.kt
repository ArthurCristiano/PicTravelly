package com.app.pictravelly.feature.home

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.app.pictravelly.core.database.model.touristSpot.TouristSpotWithImages
import com.app.pictravelly.core.design.theme.PicTravellyTheme
import com.app.pictravelly.core.map.components.ExpandedMapView
import com.app.pictravelly.core.map.components.HomeMapCard
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
                travelerLevel = uiState.travelerLevel,
                modifier = modifier
            )

            HomeGamificationCard(
                travelerLevel = uiState.travelerLevel,
                xpProgress = uiState.xpProgress,
                totalSpots = uiState.totalSpotsCount,
                modifier = modifier
            )

            HomeMapCard(
                uiState = uiState,
                onExpandClick = { onMapExpandedChange(true) },
                onSpotSelect = { spot ->
                    onSpotSelect(spot)
                    onMapExpandedChange(true)
                }
            )

            HomeNewEntryActionCard(
                onNavigateToCreate = onNavigateToCreate,
                modifier = modifier
            )

            RecentMemoriesSection(
                spots = uiState.spots,
                onSpotClick = onNavigateToDetail,
                onViewAllClick = onNavigateToSpots,
                modifier = modifier
            )

            Spacer(modifier = Modifier.height(72.dp))
        }

        // Modo Mapa Expandido
        AnimatedVisibility(
            visible = uiState.isMapExpanded,
            enter = fadeIn(),
            exit = fadeOut(),
            modifier = modifier
        ) {
            ExpandedMapView(
                uiState = uiState,
                onClose = { onMapExpandedChange(false) },
                onSpotSelect = { spot -> onSpotSelect(spot) },
                onNavigateToDetail = onNavigateToDetail
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
            onNavigateToCreate = {}
        )
    }
}