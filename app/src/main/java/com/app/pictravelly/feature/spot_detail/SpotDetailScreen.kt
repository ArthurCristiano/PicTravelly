package com.app.pictravelly.feature.spot_detail

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.CalendarToday
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Place
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import coil.compose.AsyncImage
import com.app.pictravelly.core.database.model.settings.GoogleMapType
import com.app.pictravelly.core.database.model.settings.MapEngineType
import com.app.pictravelly.core.design.components.PicTravellyCard
import com.app.pictravelly.core.design.components.PicTravellyText
import com.app.pictravelly.core.design.components.PicTravellyTitle
import com.app.pictravelly.core.map.model.MapMarkerData
import com.app.pictravelly.core.map.components.expandedMap.PicTravellyExpandedMapView
import com.app.pictravelly.core.map.components.previewCard.PicTravellyPreviewMapCard
import com.app.pictravelly.feature.spot_detail.components.DetailSpotFloatingCard
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale


/**
 * TELA: 100% Visual e Testável. Nenhuma dependência de ViewModel.
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SpotDetailScreen(
    uiState: SpotDetailUiState, // Garanta que seu UiState contenha mapEngine e mapZoom
    showDeleteDialog: Boolean,
    isMapExpanded: Boolean,
    onNavigateBack: () -> Unit,
    onDeleteClick: () -> Unit,
    onDeleteConfirm: () -> Unit,
    onDeleteDismiss: () -> Unit,
    onMapExpandedChange: (Boolean) -> Unit,
    onZoomChange: (Float) -> Unit,
    onEngineChange: (MapEngineType) -> Unit,
    onMapTypeChange: (GoogleMapType) -> Unit,
    modifier: Modifier = Modifier
) {
    if (showDeleteDialog) {
        AlertDialog(
            onDismissRequest = onDeleteDismiss,
            title = { Text("Excluir Entrada") },
            text = { Text("Tem certeza que deseja apagar este relato do seu diário de viagem?") },
            confirmButton = {
                TextButton(onClick = onDeleteConfirm) {
                    Text("Excluir", color = MaterialTheme.colorScheme.error)
                }
            },
            dismissButton = {
                TextButton(onClick = onDeleteDismiss) {
                    Text("Cancelar")
                }
            }
        )
    }

    Box(modifier = modifier.fillMaxSize()) {
        Scaffold(
            topBar = {
                TopAppBar(
                    title = { Text("Diário de Viagem") },
                    navigationIcon = {
                        IconButton(onClick = onNavigateBack) {
                            Icon(
                                imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                                contentDescription = "Voltar"
                            )
                        }
                    },
                    actions = {
                        // Oculta o botão de deletar se o spot ainda não carregou
                        if (!uiState.isLoading && uiState.spotWithImages != null) {
                            IconButton(onClick = onDeleteClick) {
                                Icon(
                                    imageVector = Icons.Default.Delete,
                                    contentDescription = "Excluir Diário",
                                    tint = MaterialTheme.colorScheme.error
                                )
                            }
                        }
                    },
                    colors = TopAppBarDefaults.topAppBarColors(
                        containerColor = MaterialTheme.colorScheme.surface
                    )
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
            } else if (uiState.spotWithImages == null) {
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(innerPadding),
                    contentAlignment = Alignment.Center
                ) {
                    Text("Registro não encontrado.")
                }
            } else {
                val spot = uiState.spotWithImages!!.spot
                val images = uiState.spotWithImages!!.images

                // NOTA: Para performance, mova essa formatação de data para a ViewModel
                val formattedDate = remember(spot.visitDate) {
                    SimpleDateFormat("dd 'de' MMMM 'de' yyyy", Locale.forLanguageTag("pt-BR"))
                        .format(Date(spot.visitDate))
                }

                val singleMarker = remember(spot) {
                    listOf(
                        MapMarkerData(
                            id = spot.id,
                            title = spot.title,
                            snippet = spot.locationName,
                            latitude = spot.latitude,
                            longitude = spot.longitude
                        )
                    )
                }

                Column(
                    modifier = Modifier
                        .fillMaxSize()
                        .background(MaterialTheme.colorScheme.background)
                        .verticalScroll(rememberScrollState())
                        .padding(innerPadding)
                        .padding(16.dp),
                    verticalArrangement = Arrangement.spacedBy(18.dp)
                ) {
                    // Fotos em Carrossel Horizontal
                    if (images.isNotEmpty()) {
                        LazyRow(
                            horizontalArrangement = Arrangement.spacedBy(12.dp)
                        ) {
                            items(images) { img ->
                                AsyncImage(
                                    model = img.imageUri,
                                    contentDescription = spot.title,
                                    modifier = Modifier
                                        .size(240.dp, 180.dp)
                                        .clip(RoundedCornerShape(16.dp)),
                                    contentScale = ContentScale.Crop
                                )
                            }
                        }
                    }

                    // Título Principal
                    PicTravellyTitle(
                        text = spot.title,
                        modifier = Modifier.fillMaxWidth()
                    )

                    // Metadados: Localização e Data
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(
                                imageVector = Icons.Default.Place,
                                contentDescription = null,
                                tint = MaterialTheme.colorScheme.primary,
                                modifier = Modifier.size(18.dp)
                            )
                            Spacer(modifier = Modifier.width(4.dp))
                            Text(
                                text = spot.locationName,
                                style = MaterialTheme.typography.bodyMedium,
                                fontWeight = FontWeight.SemiBold,
                                color = MaterialTheme.colorScheme.onSurface
                            )
                        }

                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(
                                imageVector = Icons.Default.CalendarToday,
                                contentDescription = null,
                                tint = MaterialTheme.colorScheme.outline,
                                modifier = Modifier.size(14.dp)
                            )
                            Spacer(modifier = Modifier.width(4.dp))
                            Text(
                                text = formattedDate,
                                style = MaterialTheme.typography.labelSmall,
                                color = MaterialTheme.colorScheme.outline
                            )
                        }
                    }

                    // MAPA PADRONIZADO (Preview Stateless)
                    PicTravellyPreviewMapCard(
                        currentLatitude = spot.latitude,
                        currentLongitude = spot.longitude,
                        engine = uiState.mapEngine,
                        zoom = uiState.mapZoom,
                        googleMapType = uiState.googleMapType,
                        markers = singleMarker,
                        onExpandClick = { onMapExpandedChange(true) },
                        onMarkerSelect = { /* Ignorado na tela de detalhes */ }
                    )

                    // Relato do Diário
                    PicTravellyCard(
                        modifier = Modifier.fillMaxWidth(),
                        containerColor = MaterialTheme.colorScheme.surface
                    ) {
                        Column(modifier = Modifier.padding(18.dp)) {
                            Text(
                                text = "Memórias deste dia:",
                                style = MaterialTheme.typography.titleMedium,
                                fontWeight = FontWeight.Bold,
                                color = MaterialTheme.colorScheme.primary
                            )
                            Spacer(modifier = Modifier.height(10.dp))
                            PicTravellyText(
                                text = spot.description.ifBlank { "Nenhuma anotação adicionada para este local." },
                                color = MaterialTheme.colorScheme.onSurface
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(32.dp))
                }
            }
        }

        // MAPA PADRONIZADO (Expandido sobre a Scaffold)
        AnimatedVisibility(
            visible = isMapExpanded && uiState.spotWithImages != null,
            enter = fadeIn(),
            exit = fadeOut()
        ) {
            val spot = uiState.spotWithImages!!.spot
            val singleMarker = listOf(
                MapMarkerData(
                    id = spot.id,
                    title = spot.title,
                    snippet = spot.locationName,
                    latitude = spot.latitude,
                    longitude = spot.longitude
                )
            )

            PicTravellyExpandedMapView(
                latitude = spot.latitude,
                longitude = spot.longitude,
                engine = uiState.mapEngine,
                zoom = uiState.mapZoom,
                googleMapType = uiState.googleMapType,
                markers = singleMarker,
                onClose = { onMapExpandedChange(false) },
                onMarkerClick = { },
                onZoomChange = onZoomChange,
                // SLOT API: Card customizado para a tela de Detalhes
                bottomContent = {
                    DetailSpotFloatingCard(
                        selectedSpot = uiState.spotWithImages,
                        modifier = Modifier.align(Alignment.BottomCenter)
                    )
                },
                onEngineChange = onEngineChange,
                onMapTypeChange = onMapTypeChange,
                centerTrigger = uiState.centerTrigger
            )
        }
    }
}
