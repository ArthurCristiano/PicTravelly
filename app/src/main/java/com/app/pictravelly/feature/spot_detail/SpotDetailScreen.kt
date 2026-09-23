package com.app.pictravelly.feature.spot_detail

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
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
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
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
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
import androidx.compose.ui.unit.dp
import coil.compose.AsyncImage
import com.app.pictravelly.core.design.components.PicTravellyCard
import com.app.pictravelly.core.design.components.PicTravellyText
import com.app.pictravelly.core.design.components.PicTravellyTitle
import com.app.pictravelly.core.map.MapMarkerData
import com.app.pictravelly.core.map.PicTravellyMap
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SpotDetailScreen(
    viewModel: SpotDetailViewModel,
    onNavigateBack: () -> Unit,
    modifier: Modifier = Modifier
) {
    val uiState by viewModel.uiState.collectAsState()
    var showDeleteDialog by remember { mutableStateOf(false) }

    if (showDeleteDialog) {
        AlertDialog(
            onDismissRequest = { showDeleteDialog = false },
            title = { Text("Excluir Entrada") },
            text = { Text("Tem certeza que deseja apagar este relato do seu diário de viagem?") },
            confirmButton = {
                TextButton(
                    onClick = {
                        showDeleteDialog = false
                        viewModel.deleteSpot(onNavigateBack)
                    }
                ) {
                    Text("Excluir", color = MaterialTheme.colorScheme.error)
                }
            },
            dismissButton = {
                TextButton(onClick = { showDeleteDialog = false }) {
                    Text("Cancelar")
                }
            }
        )
    }

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
                    IconButton(onClick = { showDeleteDialog = true }) {
                        Icon(
                            imageVector = Icons.Default.Delete,
                            contentDescription = "Excluir Diário",
                            tint = MaterialTheme.colorScheme.error
                        )
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
            val formattedDate = SimpleDateFormat("dd 'de' MMMM 'de' yyyy", Locale.forLanguageTag("pt-BR"))
                .format(Date(spot.visitDate))

            // ============================================================================
            // 🎨 [DESIGN / HUMBERTO] - PÁGINA DO DIÁRIO IMERSIVA:
            // - Aqui você pode aplicar estética de folha de diário (fundo papel artesanal,
            //   margens duplas, linhas suaves ou fita marcadora).
            // ============================================================================
            Column(
                modifier = modifier
                    .fillMaxSize()
                    .background(MaterialTheme.colorScheme.background)
                    .verticalScroll(rememberScrollState())
                    .padding(innerPadding)
                    .padding(16.dp),
                verticalArrangement = Arrangement.spacedBy(18.dp)
            ) {
                // Fotos em Carrossel Horizontal
                if (images.isNotEmpty()) {
                    /* 🎨 [DESIGN / HUMBERTO]: Cada foto pode ter moldura de polaroid ou cantoneira */
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

                // ============================================================================
                // 🗺️ MAPA DE CONTEXTO DO PONTO TURÍSTICO:
                // - Exibe o mapa focalizado no local onde a entrada foi registrada.
                // ============================================================================
                PicTravellyCard(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(200.dp),
                    containerColor = MaterialTheme.colorScheme.surface
                ) {
                    PicTravellyMap(
                        latitude = spot.latitude,
                        longitude = spot.longitude,
                        zoom = 14.0,
                        markers = listOf(
                            MapMarkerData(
                                id = spot.id,
                                title = spot.title,
                                snippet = spot.locationName,
                                latitude = spot.latitude,
                                longitude = spot.longitude
                            )
                        ),
                        modifier = Modifier.fillMaxSize(),
                        isInteractive = true
                    )
                }

                // ============================================================================
                // 📝 RELATO DO DIÁRIO (TEXTO IMERSIVO):
                // - Humberto: a tipografia Serifada do Design System já é aplicada aqui
                //   para passar o tom clássico de leitura literária.
                // ============================================================================
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
                            text = if (spot.description.isNotBlank()) spot.description else "Nenhuma anotação adicionada para este local.",
                            color = MaterialTheme.colorScheme.onSurface
                        )
                    }
                }

                Spacer(modifier = Modifier.height(32.dp))
            }
        }
    }
}
