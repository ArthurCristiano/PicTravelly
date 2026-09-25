package com.app.pictravelly.feature.trip_detail

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.CalendarToday
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.LinkOff
import androidx.compose.material.icons.filled.Place
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
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import coil.compose.AsyncImage
import com.app.pictravelly.core.database.model.TouristSpotWithImages
import com.app.pictravelly.core.database.model.TripEntity
import com.app.pictravelly.core.design.components.PicTravellyCard
import com.app.pictravelly.core.design.components.PicTravellyText
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

private val DATE_FORMAT = SimpleDateFormat("dd 'de' MMM, yyyy", Locale.forLanguageTag("pt-BR"))
private val SHORT_DATE_FORMAT = SimpleDateFormat("dd 'de' MMM", Locale.forLanguageTag("pt-BR"))

/**
 * Detalhe de uma viagem: mostra as fotos, as descrições e os endereços dos
 * pontos turísticos cadastrados nela.
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun TripDetailScreen(
    viewModel: TripDetailViewModel,
    onNavigateBack: () -> Unit,
    onEditTrip: (Long) -> Unit,
    onAddSpot: () -> Unit,
    onNavigateToSpotDetail: (Long) -> Unit,
    modifier: Modifier = Modifier
) {
    val uiState by viewModel.uiState.collectAsState()
    var showDeleteDialog by remember { mutableStateOf(false) }

    // A viagem apagada (ou inexistente) fecha a tela sozinha.
    LaunchedEffect(uiState.wasDeleted) {
        if (uiState.wasDeleted) onNavigateBack()
    }

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
                        Icon(
                            imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                            contentDescription = "Voltar"
                        )
                    }
                },
                actions = {
                    val trip = uiState.trip
                    if (trip != null && !uiState.isLooseGroup) {
                        IconButton(onClick = { onEditTrip(trip.id) }) {
                            Icon(
                                imageVector = Icons.Default.Edit,
                                contentDescription = "Editar viagem"
                            )
                        }
                        IconButton(onClick = { showDeleteDialog = true }) {
                            Icon(
                                imageVector = Icons.Default.Delete,
                                contentDescription = "Apagar viagem",
                                tint = MaterialTheme.colorScheme.error
                            )
                        }
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.surface
                )
            )
        },
        floatingActionButton = {
            ExtendedFloatingActionButton(
                onClick = onAddSpot,
                containerColor = MaterialTheme.colorScheme.primary,
                contentColor = MaterialTheme.colorScheme.onPrimary,
                icon = { Icon(Icons.Default.Add, contentDescription = null) },
                text = { Text("Ponto turístico") }
            )
        }
    ) { innerPadding ->
        // ============================================================================
        // 🎨 [DESIGN / HUMBERTO] - PÁGINAS DE UMA VIAGEM:
        // - Cada ponto turístico é uma página do capítulo desta viagem.
        // - Humberto: cabe aqui textura de papel, fita adesiva nas fotos e
        //   carimbo de data no canto do cartão.
        // ============================================================================
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
            contentPadding = androidx.compose.foundation.layout.PaddingValues(
                start = 16.dp,
                end = 16.dp,
                top = 12.dp,
                bottom = 110.dp
            ),
            verticalArrangement = Arrangement.spacedBy(14.dp)
        ) {
            uiState.trip?.let { trip ->
                item {
                    TripHeaderCard(trip = trip, spotsCount = uiState.spotsCount)
                }
            }

            if (uiState.isLooseGroup) {
                item {
                    PicTravellyText(
                        text = "Estes pontos ainda não pertencem a nenhuma viagem. " +
                            "Edite um ponto para agrupá-lo.",
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
                        onClick = { onNavigateToSpotDetail(spotWithImages.spot.id) },
                        onUnlink = { viewModel.removeSpotFromTrip(spotWithImages.spot.id) },
                        onDelete = { viewModel.deleteSpot(spotWithImages.spot.id) }
                    )
                }
            }
        }
    }

    if (showDeleteDialog) {
        val trip = uiState.trip
        AlertDialog(
            onDismissRequest = { showDeleteDialog = false },
            title = { Text("Apagar viagem?") },
            text = {
                Text(
                    "A viagem \"${trip?.title.orEmpty()}\" será removida do diário. " +
                        "Os ${uiState.spotsCount} ponto(s) turístico(s) continuam salvos e " +
                        "passam para o grupo \"Pontos sem viagem\"."
                )
            },
            confirmButton = {
                TextButton(
                    onClick = {
                        showDeleteDialog = false
                        viewModel.deleteTrip()
                    }
                ) {
                    Text("Apagar", color = MaterialTheme.colorScheme.error)
                }
            },
            dismissButton = {
                TextButton(onClick = { showDeleteDialog = false }) {
                    Text("Cancelar")
                }
            }
        )
    }
}

/**
 * Cabeçalho com a capa, o período e o relato geral da viagem.
 */
@Composable
private fun TripHeaderCard(
    trip: TripEntity,
    spotsCount: Int
) {
    PicTravellyCard(
        modifier = Modifier.fillMaxWidth(),
        containerColor = MaterialTheme.colorScheme.surface
    ) {
        Column {
            trip.coverImageUri?.let { uri ->
                AsyncImage(
                    model = uri,
                    contentDescription = "Capa da viagem",
                    modifier = Modifier
                        .fillMaxWidth()
                        .aspectRatio(16f / 9f),
                    contentScale = ContentScale.Crop
                )
            }

            Column(modifier = Modifier.padding(16.dp)) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(
                        imageVector = Icons.Default.CalendarToday,
                        contentDescription = null,
                        modifier = Modifier.size(14.dp),
                        tint = MaterialTheme.colorScheme.primary
                    )
                    Spacer(modifier = Modifier.width(6.dp))
                    Text(
                        text = formatPeriod(trip.startDate, trip.endDate),
                        style = MaterialTheme.typography.labelMedium,
                        color = MaterialTheme.colorScheme.primary
                    )
                }

                Spacer(modifier = Modifier.height(8.dp))

                Text(
                    text = if (spotsCount == 1) {
                        "1 ponto turístico registrado"
                    } else {
                        "$spotsCount pontos turísticos registrados"
                    },
                    style = MaterialTheme.typography.labelMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )

                if (trip.description.isNotBlank()) {
                    Spacer(modifier = Modifier.height(10.dp))
                    PicTravellyText(
                        text = trip.description,
                        color = MaterialTheme.colorScheme.onSurface
                    )
                }
            }
        }
    }
}

/**
 * Página do diário: um ponto turístico com as suas fotos e a descrição.
 */
@Composable
private fun SpotPageCard(
    spotWithImages: TouristSpotWithImages,
    showUnlinkAction: Boolean,
    onClick: () -> Unit,
    onUnlink: () -> Unit,
    onDelete: () -> Unit
) {
    val spot = spotWithImages.spot
    var showDeleteDialog by remember { mutableStateOf(false) }

    PicTravellyCard(
        modifier = Modifier
            .fillMaxWidth()
            .clickable { onClick() },
        containerColor = MaterialTheme.colorScheme.surface
    ) {
        Column {
            // Carrossel com todas as fotos do ponto
            if (spotWithImages.images.isNotEmpty()) {
                LazyRow(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(2.dp)
                ) {
                    items(spotWithImages.images, key = { it.id }) { image ->
                        AsyncImage(
                            model = image.imageUri,
                            contentDescription = spot.title,
                            modifier = Modifier
                                .height(190.dp)
                                .width(if (spotWithImages.images.size == 1) 360.dp else 260.dp),
                            contentScale = ContentScale.Crop
                        )
                    }
                }
            }

            Column(modifier = Modifier.padding(14.dp)) {
                Text(
                    text = spot.title,
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onSurface
                )

                Spacer(modifier = Modifier.height(4.dp))

                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(
                        imageVector = Icons.Default.Place,
                        contentDescription = null,
                        modifier = Modifier.size(14.dp),
                        tint = MaterialTheme.colorScheme.primary
                    )
                    Spacer(modifier = Modifier.width(4.dp))
                    Text(
                        text = spot.locationName,
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                }

                Spacer(modifier = Modifier.height(2.dp))

                Text(
                    text = "Lat: %.5f, Lng: %.5f".format(spot.latitude, spot.longitude),
                    style = MaterialTheme.typography.labelSmall,
                    color = MaterialTheme.colorScheme.outline
                )

                if (spot.description.isNotBlank()) {
                    Spacer(modifier = Modifier.height(8.dp))
                    Text(
                        text = spot.description,
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        maxLines = 4,
                        overflow = TextOverflow.Ellipsis
                    )
                }

                Spacer(modifier = Modifier.height(4.dp))

                Row(horizontalArrangement = Arrangement.spacedBy(2.dp)) {
                    if (showUnlinkAction) {
                        IconButton(onClick = onUnlink) {
                            Icon(
                                imageVector = Icons.Default.LinkOff,
                                contentDescription = "Remover desta viagem",
                                tint = MaterialTheme.colorScheme.onSurfaceVariant,
                                modifier = Modifier.size(20.dp)
                            )
                        }
                    }
                    IconButton(onClick = { showDeleteDialog = true }) {
                        Icon(
                            imageVector = Icons.Default.Delete,
                            contentDescription = "Apagar ponto",
                            tint = MaterialTheme.colorScheme.error,
                            modifier = Modifier.size(20.dp)
                        )
                    }
                }
            }
        }
    }

    if (showDeleteDialog) {
        AlertDialog(
            onDismissRequest = { showDeleteDialog = false },
            title = { Text("Apagar ponto?") },
            text = { Text("O ponto \"${spot.title}\" e as suas fotos serão removidos do diário.") },
            confirmButton = {
                TextButton(
                    onClick = {
                        showDeleteDialog = false
                        onDelete()
                    }
                ) {
                    Text("Apagar", color = MaterialTheme.colorScheme.error)
                }
            },
            dismissButton = {
                TextButton(onClick = { showDeleteDialog = false }) {
                    Text("Cancelar")
                }
            }
        )
    }
}

@Composable
private fun EmptySpotsInTripPlaceholder() {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 24.dp, vertical = 32.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Box(
            modifier = Modifier
                .size(54.dp)
                .clip(CircleShape)
                .background(MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.5f)),
            contentAlignment = Alignment.Center
        ) {
            Icon(
                imageVector = Icons.Default.Place,
                contentDescription = null,
                tint = MaterialTheme.colorScheme.primary,
                modifier = Modifier.size(28.dp)
            )
        }
        Spacer(modifier = Modifier.height(14.dp))
        Text(
            text = "Nenhum ponto turístico nesta viagem ainda.",
            style = MaterialTheme.typography.bodyMedium,
            fontWeight = FontWeight.Medium,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
        Spacer(modifier = Modifier.height(4.dp))
        Text(
            text = "Use o botão abaixo para registrar o primeiro.",
            style = MaterialTheme.typography.bodySmall,
            color = MaterialTheme.colorScheme.outline
        )
    }
}

private fun formatPeriod(startDate: Long, endDate: Long?): String {
    val start = Date(startDate)
    if (endDate == null) return "${DATE_FORMAT.format(start)} · em andamento"

    val end = Date(endDate)
    if (startDate == endDate) return DATE_FORMAT.format(start)

    return "${SHORT_DATE_FORMAT.format(start)} - ${DATE_FORMAT.format(end)}"
}
