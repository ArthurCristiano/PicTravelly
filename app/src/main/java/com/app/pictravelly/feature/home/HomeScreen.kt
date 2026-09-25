package com.app.pictravelly.feature.home

import android.Manifest
import androidx.activity.compose.BackHandler
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.slideInVertically
import androidx.compose.animation.slideOutVertically
import androidx.compose.foundation.background
import androidx.compose.foundation.border
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
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.clickable
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.AddLocationAlt
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.EmojiEvents
import androidx.compose.material.icons.filled.Explore
import androidx.compose.material.icons.filled.Fullscreen
import androidx.compose.material.icons.filled.LocationOn
import androidx.compose.material.icons.filled.Place
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.rememberCoroutineScope
import kotlinx.coroutines.launch
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import coil.compose.AsyncImage
import com.app.pictravelly.core.database.model.TouristSpotWithImages
import com.app.pictravelly.core.design.components.PicTravellyButton
import com.app.pictravelly.core.design.components.PicTravellyCard
import com.app.pictravelly.core.design.components.PicTravellyText
import com.app.pictravelly.core.design.components.PicTravellyTitle
import com.app.pictravelly.core.location.LocationHelper
import com.app.pictravelly.core.map.MapMarkerData
import com.app.pictravelly.core.map.PicTravellyMap
import androidx.compose.runtime.remember

@Composable
fun HomeScreen(
    viewModel: HomeViewModel,
    onNavigateToDetail: (Long) -> Unit,
    onNavigateToSpots: () -> Unit,
    onNavigateToCreate: () -> Unit,
    modifier: Modifier = Modifier,
    contentPadding: PaddingValues = PaddingValues(0.dp)
) {
    val uiState by viewModel.uiState.collectAsState()
    val context = LocalContext.current
    val coroutineScope = androidx.compose.runtime.rememberCoroutineScope()

    // Fecha o mapa expandido graciosamente ao pressionar o botão voltar do sistema
    BackHandler(enabled = uiState.isMapExpanded) {
        viewModel.setMapExpanded(false)
    }

    // Solicitação graciosa de permissão de localização
    val permissionLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.RequestMultiplePermissions()
    ) { permissions ->
        val granted = permissions[Manifest.permission.ACCESS_FINE_LOCATION] == true ||
                permissions[Manifest.permission.ACCESS_COARSE_LOCATION] == true
        if (granted) {
            coroutineScope.launch {
                val (lat, lng) = LocationHelper.getCurrentLocation(context)
                viewModel.updateCurrentLocation(lat, lng)
            }
        }
    }

    LaunchedEffect(Unit) {
        if (LocationHelper.hasLocationPermission(context)) {
            val (lat, lng) = LocationHelper.getCurrentLocation(context)
            viewModel.updateCurrentLocation(lat, lng)
        } else {
            permissionLauncher.launch(
                arrayOf(
                    Manifest.permission.ACCESS_FINE_LOCATION,
                    Manifest.permission.ACCESS_COARSE_LOCATION
                )
            )
        }
    }

    Box(modifier = modifier.fillMaxSize()) {
        // Modo Normal: Conteúdo rolável com Dashboard + Card Expansível do Mapa
        Column(
            modifier = Modifier
                .fillMaxSize()
                .background(MaterialTheme.colorScheme.background)
                .verticalScroll(rememberScrollState())
                .padding(contentPadding)
                .padding(horizontal = 16.dp, vertical = 16.dp),
            verticalArrangement = Arrangement.spacedBy(20.dp)
        ) {
            // ============================================================================
            // 🎨 [DESIGN / HUMBERTO] - CABEÇALHO DO DIÁRIO DE BORDO:
            // - Você pode estilizar aqui com textura de capa de diário, tipografia personalizada,
            //   data atual ou nome do explorador.
            // ============================================================================
            HomeHeaderSection(
                totalSpots = uiState.totalSpotsCount,
                travelerLevel = uiState.travelerLevel
            )

            // ============================================================================
            // 🎨 [DESIGN / HUMBERTO] - CARD DE GAMIFICAÇÃO & XP:
            // - Estilize este card com selos de carimbo postal, medalhas e barra de progresso
            //   estilizada como pergaminho ou fita métrica de viagem.
            // ============================================================================
            HomeGamificationCard(
                travelerLevel = uiState.travelerLevel,
                xpProgress = uiState.xpProgress,
                totalSpots = uiState.totalSpotsCount
            )

            // ============================================================================
            // 🗺️ CARD DO MAPA (RECOLHIDO / CLICÁVEL PARA EXPANDIR)
            // - O mapa inicia centrado na posição atual e exibe os pins dos locais cadastrados.
            // - Ao tocar no card ou no botão de tela cheia, ele expande.
            // ============================================================================
            HomeMapCard(
                uiState = uiState,
                onExpandClick = { viewModel.setMapExpanded(true) },
                onSpotSelect = { spot ->
                    viewModel.selectSpot(spot)
                    viewModel.setMapExpanded(true)
                }
            )

            // ============================================================================
            // 🎨 [DESIGN / HUMBERTO] - CARD CONVIDATIVO PARA NOVO REGISTRO NO DIÁRIO:
            // - Chamada nobre na Home para criar novo relato.
            // - Humberto: Estilize como aba de abertura do diário ou carimbo de viagem.
            // ============================================================================
            HomeNewEntryActionCard(
                onNavigateToCreate = onNavigateToCreate
            )

            // ============================================================================
            // 🎨 [DESIGN / HUMBERTO] - ATALHOS PARA ÚLTIMAS LEMBRANÇAS:
            // - Carrossel horizontal das últimas anotações do diário.
            // - Humberto pode aplicar estilo de mini-polaroids ou cartões postais aqui.
            // ============================================================================
            RecentMemoriesSection(
                spots = uiState.spots,
                onSpotClick = onNavigateToDetail,
                onViewAllClick = onNavigateToSpots
            )

            // Espaço de respiro para o Dock flutuante
            Spacer(modifier = Modifier.height(72.dp))
        }

        // ============================================================================
        // 🗺️ MODO MAPA EXPANDIDO (TELA CHEIA)
        // - Ativado ao clicar no card de mapa da Home
        // ============================================================================
        AnimatedVisibility(
            visible = uiState.isMapExpanded,
            enter = fadeIn(),
            exit = fadeOut()
        ) {
            ExpandedMapView(
                uiState = uiState,
                onClose = { viewModel.setMapExpanded(false) },
                onSpotSelect = { spot -> viewModel.selectSpot(spot) },
                onNavigateToDetail = onNavigateToDetail
            )
        }
    }
}

/**
 * Cabeçalho inicial com dados de boas-vindas e resumo.
 */
@Composable
private fun HomeHeaderSection(
    totalSpots: Int,
    travelerLevel: String
) {
    Column {
        PicTravellyTitle(
            text = "Diário de Bordo",
            modifier = Modifier.fillMaxWidth()
        )
        Spacer(modifier = Modifier.height(4.dp))
        PicTravellyText(
            text = "Você já registrou $totalSpots ${if (totalSpots == 1) "lugar inesquecível" else "lugares inesquecíveis"}.",
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            isSmall = false
        )
    }
}

/**
 * Card de Gamificação mostrando progresso do usuário.
 */
@Composable
private fun HomeGamificationCard(
    travelerLevel: String,
    xpProgress: Float,
    totalSpots: Int
) {
    /* 🎨 [DESIGN / HUMBERTO]: Personalize cores, fundos e ícones aqui */
    PicTravellyCard(
        modifier = Modifier.fillMaxWidth(),
        containerColor = MaterialTheme.colorScheme.surface
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween,
                modifier = Modifier.fillMaxWidth()
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Box(
                        modifier = Modifier
                            .size(40.dp)
                            .clip(CircleShape)
                            .background(MaterialTheme.colorScheme.primaryContainer),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = Icons.Default.EmojiEvents,
                            contentDescription = "Conquistas",
                            tint = MaterialTheme.colorScheme.onPrimaryContainer
                        )
                    }
                    Spacer(modifier = Modifier.width(12.dp))
                    Column {
                        Text(
                            text = travelerLevel,
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.onSurface
                        )
                        Text(
                            text = "$totalSpots marcos registrados",
                            style = MaterialTheme.typography.labelMedium,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(14.dp))
            LinearProgressIndicator(
                progress = { xpProgress },
                modifier = Modifier
                    .fillMaxWidth()
                    .height(8.dp)
                    .clip(RoundedCornerShape(4.dp)),
                color = MaterialTheme.colorScheme.primary,
                trackColor = MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.4f)
            )
        }
    }
}

/**
 * Card contendo o Google Map recolhido, com pins e centrado na localização atual.
 */
@Composable
private fun HomeMapCard(
    uiState: HomeUiState,
    onExpandClick: () -> Unit,
    onSpotSelect: (TouristSpotWithImages) -> Unit
) {
    val markers = remember(uiState.spots) {
        uiState.spots.map { spotWithImages ->
            MapMarkerData(
                id = spotWithImages.spot.id,
                title = spotWithImages.spot.title,
                snippet = spotWithImages.spot.locationName,
                latitude = spotWithImages.spot.latitude,
                longitude = spotWithImages.spot.longitude
            )
        }
    }

    PicTravellyCard(
        modifier = Modifier
            .fillMaxWidth()
            .height(260.dp),
        containerColor = MaterialTheme.colorScheme.surface
    ) {
        Box(modifier = Modifier.fillMaxSize()) {
            PicTravellyMap(
                latitude = uiState.currentLatitude,
                longitude = uiState.currentLongitude,
                zoom = 12.0,
                markers = markers,
                onMarkerClick = { marker ->
                    val selected = uiState.spots.firstOrNull { it.spot.id == marker.id }
                    if (selected != null) onSpotSelect(selected)
                },
                modifier = Modifier.fillMaxSize(),
                isInteractive = false,
                onMapClick = onExpandClick
            )

            // Botão One UI flutuante de expandir no canto superior direito
            Box(
                modifier = Modifier
                    .align(Alignment.TopEnd)
                    .padding(12.dp)
                    .clip(CircleShape)
                    .background(MaterialTheme.colorScheme.surface.copy(alpha = 0.88f))
                    .border(1.dp, Color.White.copy(alpha = 0.3f), CircleShape)
            ) {
                IconButton(onClick = onExpandClick) {
                    Icon(
                        imageVector = Icons.Default.Fullscreen,
                        contentDescription = "Expandir Mapa",
                        tint = MaterialTheme.colorScheme.primary
                    )
                }
            }

            // Tag indicativa inferior
            Box(
                modifier = Modifier
                    .align(Alignment.BottomStart)
                    .padding(12.dp)
                    .clip(RoundedCornerShape(8.dp))
                    .background(MaterialTheme.colorScheme.surface.copy(alpha = 0.85f))
                    .padding(horizontal = 10.dp, vertical = 6.dp)
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(
                        imageVector = Icons.Default.Explore,
                        contentDescription = null,
                        modifier = Modifier.size(16.dp),
                        tint = MaterialTheme.colorScheme.primary
                    )
                    Spacer(modifier = Modifier.width(6.dp))
                    Text(
                        text = "Toque para explorar o mapa",
                        style = MaterialTheme.typography.labelSmall,
                        color = MaterialTheme.colorScheme.onSurface
                    )
                }
            }
        }
    }
}

/**
 * Visualização do mapa em tela cheia (Expandido).
 */
@Composable
private fun ExpandedMapView(
    uiState: HomeUiState,
    onClose: () -> Unit,
    onSpotSelect: (TouristSpotWithImages) -> Unit,
    onNavigateToDetail: (Long) -> Unit
) {
    val markers = remember(uiState.spots) {
        uiState.spots.map { spotWithImages ->
            MapMarkerData(
                id = spotWithImages.spot.id,
                title = spotWithImages.spot.title,
                snippet = spotWithImages.spot.locationName,
                latitude = spotWithImages.spot.latitude,
                longitude = spotWithImages.spot.longitude
            )
        }
    }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background)
    ) {
        PicTravellyMap(
            latitude = uiState.currentLatitude,
            longitude = uiState.currentLongitude,
            zoom = 13.0,
            markers = markers,
            onMarkerClick = { marker ->
                val selected = uiState.spots.firstOrNull { it.spot.id == marker.id }
                if (selected != null) onSpotSelect(selected)
            },
            modifier = Modifier.fillMaxSize(),
            isInteractive = true
        )

        // Botão flutuante One UI de fechar no topo
        Box(
            modifier = Modifier
                .align(Alignment.TopEnd)
                .statusBarsPadding()
                .padding(16.dp)
                .size(48.dp)
                .clip(CircleShape)
                .background(MaterialTheme.colorScheme.surface.copy(alpha = 0.90f))
                .border(1.dp, Color.White.copy(alpha = 0.3f), CircleShape)
                .clickable(onClick = onClose),
            contentAlignment = Alignment.Center
        ) {
            Icon(
                imageVector = Icons.Default.Close,
                contentDescription = "Recolher Mapa",
                tint = MaterialTheme.colorScheme.onSurface
            )
        }

        // Card flutuante One UI ao selecionar um pin no mapa
        AnimatedVisibility(
            visible = uiState.selectedSpot != null,
            modifier = Modifier
                .align(Alignment.BottomCenter)
                .navigationBarsPadding()
                .padding(bottom = 24.dp, start = 16.dp, end = 16.dp),
            enter = slideInVertically(initialOffsetY = { it }) + fadeIn(),
            exit = slideOutVertically(targetOffsetY = { it }) + fadeOut()
        ) {
            uiState.selectedSpot?.let { selected ->
                /* 🎨 [DESIGN / HUMBERTO]: Personalize este card de preview com estilo de papel e fita adesiva */
                Card(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(20.dp)),
                    colors = CardDefaults.cardColors(
                        containerColor = MaterialTheme.colorScheme.surface.copy(alpha = 0.95f)
                    ),
                    elevation = CardDefaults.cardElevation(defaultElevation = 8.dp)
                ) {
                    Row(
                        modifier = Modifier.padding(16.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        // Foto de capa (se houver)
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
}

/**
 * Seção horizontal com atalhos para os últimos locais adicionados.
 */
@Composable
private fun RecentMemoriesSection(
    spots: List<TouristSpotWithImages>,
    onSpotClick: (Long) -> Unit,
    onViewAllClick: () -> Unit
) {
    Column {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = "Últimas Lembranças",
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold
            )
            Text(
                text = "Ver tudo",
                style = MaterialTheme.typography.labelMedium,
                color = MaterialTheme.colorScheme.primary,
                modifier = Modifier.clickable { onViewAllClick() }
            )
        }

        Spacer(modifier = Modifier.height(12.dp))

        if (spots.isEmpty()) {
            /* 🎨 [DESIGN / HUMBERTO]: Estado vazio com ilustração e visual poético de diário novo */
            PicTravellyCard(
                modifier = Modifier.fillMaxWidth(),
                containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f)
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(24.dp),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Icon(
                        imageVector = Icons.Default.LocationOn,
                        contentDescription = null,
                        tint = MaterialTheme.colorScheme.primary,
                        modifier = Modifier.size(36.dp)
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                    Text(
                        text = "Seu diário ainda não possui registros.",
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    Text(
                        text = "Toque no botão central (+) para cadastrar seu primeiro local!",
                        style = MaterialTheme.typography.labelSmall,
                        color = MaterialTheme.colorScheme.primary
                    )
                }
            }
        } else {
            LazyRow(
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                items(spots.take(5)) { spotWithImages ->
                    /* 🎨 [DESIGN / HUMBERTO]: Cada card aqui pode virar uma polaroid com borda branca e sombra sutil */
                    PicTravellyCard(
                        modifier = Modifier
                            .width(180.dp)
                            .clickable { onSpotClick(spotWithImages.spot.id) },
                        containerColor = MaterialTheme.colorScheme.surface
                    ) {
                        Column {
                            if (spotWithImages.coverImageUri != null) {
                                AsyncImage(
                                    model = spotWithImages.coverImageUri,
                                    contentDescription = spotWithImages.spot.title,
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .height(110.dp),
                                    contentScale = ContentScale.Crop
                                )
                            } else {
                                Box(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .height(110.dp)
                                        .background(MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.5f)),
                                    contentAlignment = Alignment.Center
                                ) {
                                    Icon(
                                        imageVector = Icons.Default.Place,
                                        contentDescription = null,
                                        tint = MaterialTheme.colorScheme.primary
                                    )
                                }
                            }
                            Column(modifier = Modifier.padding(10.dp)) {
                                Text(
                                    text = spotWithImages.spot.title,
                                    style = MaterialTheme.typography.titleSmall,
                                    fontWeight = FontWeight.Bold,
                                    maxLines = 1
                                )
                                Spacer(modifier = Modifier.height(2.dp))
                                Text(
                                    text = spotWithImages.spot.locationName,
                                    style = MaterialTheme.typography.labelSmall,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                                    maxLines = 1
                                )
                            }
                        }
                    }
                }
            }
        }
    }
}

/**
 * Card de Ação Convidativo para Adicionar Novo Registro no Diário.
 */
@Composable
private fun HomeNewEntryActionCard(
    onNavigateToCreate: () -> Unit
) {
    /* 🎨 [DESIGN / HUMBERTO] - CARD DE NOVO REGISTRO:
     * Personalize com tipografia cursiva, textura de pergaminho ou carimbo vintage de viagem.
     */
    PicTravellyCard(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClick = onNavigateToCreate),
        containerColor = MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.45f)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier.weight(1f)
            ) {
                Box(
                    modifier = Modifier
                        .size(46.dp)
                        .clip(CircleShape)
                        .background(MaterialTheme.colorScheme.primary),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = Icons.Default.AddLocationAlt,
                        contentDescription = "Registrar Parada",
                        tint = MaterialTheme.colorScheme.onPrimary,
                        modifier = Modifier.size(24.dp)
                    )
                }
                Spacer(modifier = Modifier.width(14.dp))
                Column {
                    Text(
                        text = "Registrar Nova Parada",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.onSurface
                    )
                    Text(
                        text = "Fotografe e anote suas memórias de hoje",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }

            PicTravellyButton(
                text = "Escrever",
                onClick = onNavigateToCreate,
                modifier = Modifier.padding(start = 8.dp)
            )
        }
    }
}

