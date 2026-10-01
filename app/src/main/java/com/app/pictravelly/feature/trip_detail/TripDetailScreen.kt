package com.app.pictravelly.feature.trip_detail

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ExtendedFloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import com.app.pictravelly.core.design.components.PicTravellyText
import com.app.pictravelly.feature.trip_detail.components.EmptySpotsInTripPlaceholder
import com.app.pictravelly.feature.trip_detail.components.SpotPageCard
import com.app.pictravelly.feature.trip_detail.components.TripHeaderCard

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun TripDetailScreen(
    uiState: TripDetailUiState,
    showDeleteTripDialog: Boolean,
    onNavigateBack: () -> Unit,
    onEditTripClick: () -> Unit,
    onEditSpotClick: (Long) -> Unit,
    onAddSpotClick: () -> Unit,
    onDeleteTripClick: () -> Unit,
    onDeleteTripConfirm: () -> Unit,
    onDeleteTripDismiss: () -> Unit,
    onSpotClick: (Long) -> Unit,
    onSpotUnlink: (Long) -> Unit,
    onSpotDelete: (Long) -> Unit,
    modifier: Modifier = Modifier
) {
    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Text(
                        text = uiState.displayTitle.ifBlank { "Viagem" },
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                },
                navigationIcon = {
                    IconButton(onClick = onNavigateBack) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Voltar")
                    }
                },
                actions = {
                    if (uiState.tripHeader != null && !uiState.isLooseGroup) {
                        IconButton(onClick = onEditTripClick) {
                            Icon(Icons.Default.Edit, contentDescription = "Editar viagem")
                        }
                        IconButton(onClick = onDeleteTripClick) {
                            Icon(
                                Icons.Default.Delete,
                                contentDescription = "Apagar viagem",
                                tint = MaterialTheme.colorScheme.error
                            )
                        }
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(containerColor = MaterialTheme.colorScheme.surface)
            )
        },
        floatingActionButton = {
            ExtendedFloatingActionButton(
                onClick = onAddSpotClick,
                containerColor = MaterialTheme.colorScheme.primary,
                contentColor = MaterialTheme.colorScheme.onPrimary,
                icon = { Icon(Icons.Default.Add, contentDescription = null) },
                text = { Text("Ponto turístico") }
            )
        }
    ) { innerPadding ->

        if (uiState.isLoading) {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(innerPadding),
                contentAlignment = Alignment.Center
            ) {
                CircularProgressIndicator(color = MaterialTheme.colorScheme.primary)
            }
            return@Scaffold
        }

        LazyColumn(
            modifier = modifier
                .fillMaxSize()
                .background(MaterialTheme.colorScheme.background)
                .padding(innerPadding),
            contentPadding = PaddingValues(
                start = 16.dp,
                end = 16.dp,
                top = 12.dp,
                bottom = 110.dp
            ),
            verticalArrangement = Arrangement.spacedBy(14.dp)
        ) {
            uiState.tripHeader?.let { header ->
                item { TripHeaderCard(header = header, spotsCount = uiState.spotsCount) }
            }

            if (uiState.isLooseGroup) {
                item {
                    PicTravellyText(
                        text = "Estes pontos ainda não pertencem a nenhuma viagem. Edite um ponto para agrupá-lo.",
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        isSmall = true
                    )
                }
            }

            if (uiState.spots.isEmpty()) {
                item { EmptySpotsInTripPlaceholder() }
            } else {
                items(uiState.spots, key = { it.spot.id }) { spotWithImages ->
                    SpotPageCard(
                        spotWithImages = spotWithImages,
                        showUnlinkAction = !uiState.isLooseGroup,
                        onClick = { onSpotClick(spotWithImages.spot.id) },
                        onEdit = { onEditSpotClick(spotWithImages.spot.id) },
                        onUnlink = { onSpotUnlink(spotWithImages.spot.id) },
                        onDelete = { onSpotDelete(spotWithImages.spot.id) }
                    )
                }
            }
        }
    }

    if (showDeleteTripDialog) {
        AlertDialog(
            onDismissRequest = onDeleteTripDismiss,
            title = { Text("Apagar viagem?") },
            text = {
                Text(
                    "A viagem \"${uiState.tripHeader?.title.orEmpty()}\" será removida do diário. " +
                            "Os ${uiState.spotsCount} ponto(s) turístico(s) continuam salvos e passam para o grupo \"Pontos sem viagem\"."
                )
            },
            confirmButton = {
                TextButton(onClick = onDeleteTripConfirm) {
                    Text("Apagar", color = MaterialTheme.colorScheme.error)
                }
            },
            dismissButton = {
                TextButton(onClick = onDeleteTripDismiss) { Text("Cancelar") }
            }
        )
    }
}
