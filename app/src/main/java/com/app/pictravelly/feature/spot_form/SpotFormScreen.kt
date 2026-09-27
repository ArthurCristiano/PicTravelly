package com.app.pictravelly.feature.spot_form

import android.Manifest
import android.content.Context
import android.net.Uri
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.imePadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Luggage
import androidx.compose.material.icons.filled.MyLocation
import androidx.compose.material.icons.filled.PhotoCamera
import androidx.compose.material.icons.filled.PhotoLibrary
import androidx.compose.material.icons.filled.Place
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.filled.TouchApp
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ExposedDropdownMenuAnchorType
import androidx.compose.material3.ExposedDropdownMenuBox
import androidx.compose.material3.ExposedDropdownMenuDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
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
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.core.content.FileProvider
import coil.compose.AsyncImage
import com.app.pictravelly.core.database.model.trip.TripEntity
import com.app.pictravelly.core.design.components.PicTravellyActionTile
import com.app.pictravelly.core.design.components.PicTravellyButton
import com.app.pictravelly.core.design.components.PicTravellyCard
import com.app.pictravelly.core.design.components.PicTravellyTitle
import com.app.pictravelly.core.location.GeocodingHelper
import com.app.pictravelly.core.location.LocationHelper
import com.app.pictravelly.core.map.MapMarkerData
import com.app.pictravelly.core.map.PicTravellyMap
import java.io.File
import kotlinx.coroutines.launch

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SpotFormScreen(
    viewModel: SpotFormViewModel,
    onNavigateBack: () -> Unit,
    onSpotSaved: (Long) -> Unit,
    modifier: Modifier = Modifier
) {
    val uiState by viewModel.uiState.collectAsState()
    val context = LocalContext.current
    val coroutineScope = rememberCoroutineScope()

    // Launcher para captura de fotos da galeria
    val photoPickerLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.GetMultipleContents()
    ) { uris: List<Uri> ->
        uris.forEach { uri -> viewModel.addImageUri(uri.toString()) }
    }

    // Variável de estado para guardar URI da foto sendo tirada pela câmera
    var tempPhotoUri by remember { mutableStateOf<Uri?>(null) }

    // Launcher para captura de foto na hora via câmera do dispositivo
    val cameraLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.TakePicture()
    ) { success: Boolean ->
        if (success) {
            tempPhotoUri?.let { uri ->
                viewModel.addImageUri(uri.toString())
            }
        }
    }

    fun launchCamera() {
        try {
            val photoFile = File.createTempFile(
                "spot_photo_${System.currentTimeMillis()}",
                ".jpg",
                context.cacheDir
            )
            val uri = FileProvider.getUriForFile(
                context,
                "${context.packageName}.fileprovider",
                photoFile
            )
            tempPhotoUri = uri
            cameraLauncher.launch(uri)
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    // Launcher para capturar GPS
    val locationPermissionLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.RequestMultiplePermissions()
    ) { permissions ->
        val granted = permissions[Manifest.permission.ACCESS_FINE_LOCATION] == true ||
                permissions[Manifest.permission.ACCESS_COARSE_LOCATION] == true
        if (granted) {
            coroutineScope.launch {
                captureCurrentLocation(context, viewModel, overwriteAddress = false)
            }
        } else {
            viewModel.setErrorMessage(
                "Sem permissão de localização. Toque no mapa para marcar o ponto."
            )
        }
    }

    // ============================================================================
    // 📍 POSIÇÃO AUTOMÁTICA AO ABRIR O FORMULÁRIO
    // - Já puxa onde a pessoa está e resolve o endereço por geocodificação reversa,
    //   para o usuário não precisar digitar nada disso.
    // ============================================================================
    LaunchedEffect(Unit) {
        if (LocationHelper.hasLocationPermission(context)) {
            captureCurrentLocation(context, viewModel, overwriteAddress = false)
        } else {
            locationPermissionLauncher.launch(
                arrayOf(
                    Manifest.permission.ACCESS_FINE_LOCATION,
                    Manifest.permission.ACCESS_COARSE_LOCATION
                )
            )
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Novo Ponto Turístico") },
                navigationIcon = {
                    IconButton(onClick = onNavigateBack) {
                        Icon(
                            imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                            contentDescription = "Voltar"
                        )
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.surface
                )
            )
        }
    ) { innerPadding ->
        // ============================================================================
        // 🎨 [DESIGN / HUMBERTO] - FORMULÁRIO DE ENTRADA DO DIÁRIO:
        // - Humberto: você pode personalizar aqui com estética de formulário de viagem,
        //   carimbo postal, textura de folha e áreas de colagem de fotos estilo Polaroid.
        // ============================================================================
        Column(
            modifier = modifier
                .fillMaxSize()
                .background(MaterialTheme.colorScheme.background)
                .verticalScroll(rememberScrollState())
                .padding(innerPadding)
                .imePadding()
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            PicTravellyTitle(
                text = "Registrar Lembrança",
                modifier = Modifier.fillMaxWidth()
            )

            // Mensagem de Erro (se houver)
            if (uiState.errorMessage != null) {
                Text(
                    text = uiState.errorMessage!!,
                    color = MaterialTheme.colorScheme.error,
                    style = MaterialTheme.typography.bodySmall
                )
            }

            // Campo: Viagem a que este ponto pertence
            TripSelectorField(
                selectedTripTitle = uiState.selectedTripTitle,
                availableTrips = uiState.availableTrips,
                onSelectTrip = { viewModel.updateTripId(it) }
            )

            // Campo: Título do Ponto Turístico
            OutlinedTextField(
                value = uiState.title,
                onValueChange = { viewModel.updateTitle(it) },
                label = { Text("Nome do Local ou Viagem *") },
                placeholder = { Text("Ex: Cristo Redentor, Praia do Forte...") },
                modifier = Modifier.fillMaxWidth(),
                singleLine = true,
                colors = OutlinedTextFieldDefaults.colors(
                    focusedBorderColor = MaterialTheme.colorScheme.primary,
                    focusedContainerColor = MaterialTheme.colorScheme.surface,
                    unfocusedContainerColor = MaterialTheme.colorScheme.surface
                ),
                shape = RoundedCornerShape(12.dp)
            )

            // ============================================================================
            // 📸 [DESIGN / HUMBERTO] - CAPTURA DE FOTOS COM A CÂMERA & GALERIA:
            // - Uma faixa única, da mesma largura dos campos de texto: o corpo abre a
            //   câmera e o ícone da direita abre a galeria.
            // - Humberto: a faixa pode virar lente vintage e o atalho da direita um
            //   álbum de recortes, mantendo o recorte florzinha nos dois ícones.
            // ============================================================================
            Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                Text(
                    text = "Foto do Local",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold
                )

                PicTravellyActionTile(
                    icon = Icons.Default.PhotoCamera,
                    label = "Tirar foto",
                    supportingText = "Abrir a câmera",
                    onClick = { launchCamera() },
                    trailingIcon = Icons.Default.PhotoLibrary,
                    trailingLabel = "Escolher da galeria",
                    onTrailingClick = { photoPickerLauncher.launch("image/*") }
                )

                // Lista de fotos capturadas
                if (uiState.imageUris.isNotEmpty()) {
                    Text(
                        text = "Fotos anexadas (${uiState.imageUris.size})",
                        style = MaterialTheme.typography.labelMedium,
                        fontWeight = FontWeight.SemiBold,
                        color = MaterialTheme.colorScheme.primary
                    )

                    LazyRow(
                        horizontalArrangement = Arrangement.spacedBy(10.dp)
                    ) {
                        items(uiState.imageUris) { uri ->
                            Box(modifier = Modifier.size(105.dp)) {
                                AsyncImage(
                                    model = uri,
                                    contentDescription = null,
                                    modifier = Modifier
                                        .fillMaxSize()
                                        .clip(RoundedCornerShape(12.dp)),
                                    contentScale = ContentScale.Crop
                                )
                                IconButton(
                                    onClick = { viewModel.removeImageUri(uri) },
                                    modifier = Modifier
                                        .align(Alignment.TopEnd)
                                        .padding(4.dp)
                                        .size(24.dp)
                                        .clip(CircleShape)
                                        .background(MaterialTheme.colorScheme.surface.copy(alpha = 0.85f))
                                ) {
                                    Icon(
                                        imageVector = Icons.Default.Close,
                                        contentDescription = "Remover Foto",
                                        modifier = Modifier.size(16.dp),
                                        tint = MaterialTheme.colorScheme.error
                                    )
                                }
                            }
                        }
                    }
                }
            }

            // Campo: Cidade ou Localização por Extenso (preenchido pela geocodificação)
            OutlinedTextField(
                value = uiState.locationName,
                onValueChange = { viewModel.updateLocationName(it) },
                label = { Text("Cidade / Região *") },
                placeholder = { Text("Ex: Rio de Janeiro, RJ") },
                leadingIcon = {
                    Icon(imageVector = Icons.Default.Place, contentDescription = null)
                },
                trailingIcon = {
                    if (uiState.isResolvingAddress) {
                        CircularProgressIndicator(
                            modifier = Modifier.size(18.dp),
                            strokeWidth = 2.dp,
                            color = MaterialTheme.colorScheme.primary
                        )
                    }
                },
                modifier = Modifier.fillMaxWidth(),
                singleLine = true,
                colors = OutlinedTextFieldDefaults.colors(
                    focusedBorderColor = MaterialTheme.colorScheme.primary,
                    focusedContainerColor = MaterialTheme.colorScheme.surface,
                    unfocusedContainerColor = MaterialTheme.colorScheme.surface
                ),
                shape = RoundedCornerShape(12.dp)
            )

            // ============================================================================
            // 🗺️ MAPA DO PONTO + COORDENADAS
            // - Abre já na posição do usuário; tocar no mapa reposiciona o ponto e
            //   dispara uma nova busca de endereço.
            // ============================================================================
            SpotLocationCard(
                uiState = uiState,
                onUseMyLocation = {
                    if (LocationHelper.hasLocationPermission(context)) {
                        coroutineScope.launch {
                            captureCurrentLocation(context, viewModel, overwriteAddress = true)
                        }
                    } else {
                        locationPermissionLauncher.launch(
                            arrayOf(
                                Manifest.permission.ACCESS_FINE_LOCATION,
                                Manifest.permission.ACCESS_COARSE_LOCATION
                            )
                        )
                    }
                },
                onSearchAddress = {
                    coroutineScope.launch {
                        resolveAddress(
                            context = context,
                            viewModel = viewModel,
                            latitude = uiState.latitude,
                            longitude = uiState.longitude,
                            overwrite = true
                        )
                    }
                },
                onPickOnMap = { lat, lng ->
                    viewModel.updateCoordinates(lat, lng)
                    coroutineScope.launch {
                        resolveAddress(
                            context = context,
                            viewModel = viewModel,
                            latitude = lat,
                            longitude = lng,
                            overwrite = true
                        )
                    }
                }
            )

            // Campo: Relato do Diário (Descrição longa)
            OutlinedTextField(
                value = uiState.description,
                onValueChange = { viewModel.updateDescription(it) },
                label = { Text("Relato da Viagem / Memórias") },
                placeholder = { Text("Conte como foi sua experiência, curiosidades, pessoas que conheceu...") },
                modifier = Modifier
                    .fillMaxWidth()
                    .height(160.dp),
                colors = OutlinedTextFieldDefaults.colors(
                    focusedBorderColor = MaterialTheme.colorScheme.primary,
                    focusedContainerColor = MaterialTheme.colorScheme.surface,
                    unfocusedContainerColor = MaterialTheme.colorScheme.surface
                ),
                shape = RoundedCornerShape(12.dp)
            )

            Spacer(modifier = Modifier.height(8.dp))

            // Botão Principal de Salvar
            if (uiState.isSaving) {
                Box(
                    modifier = Modifier.fillMaxWidth(),
                    contentAlignment = Alignment.Center
                ) {
                    CircularProgressIndicator(color = MaterialTheme.colorScheme.primary)
                }
            } else {
                PicTravellyButton(
                    text = "Salvar no Diário",
                    onClick = { viewModel.saveSpot(onSpotSaved) },
                    modifier = Modifier.fillMaxWidth()
                )
            }

            Spacer(modifier = Modifier.height(48.dp))
        }
    }
}

/**
 * Seletor da viagem que vai receber este ponto turístico.
 * A primeira opção deixa o ponto solto, no grupo "Pontos sem viagem".
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun TripSelectorField(
    selectedTripTitle: String,
    availableTrips: List<TripEntity>,
    onSelectTrip: (Long?) -> Unit
) {
    var expanded by remember { mutableStateOf(false) }

    ExposedDropdownMenuBox(
        expanded = expanded,
        onExpandedChange = { expanded = !expanded }
    ) {
        OutlinedTextField(
            value = selectedTripTitle,
            onValueChange = {},
            readOnly = true,
            label = { Text("Viagem") },
            leadingIcon = {
                Icon(imageVector = Icons.Default.Luggage, contentDescription = null)
            },
            trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = expanded) },
            modifier = Modifier
                .fillMaxWidth()
                .menuAnchor(ExposedDropdownMenuAnchorType.PrimaryNotEditable),
            colors = OutlinedTextFieldDefaults.colors(
                focusedBorderColor = MaterialTheme.colorScheme.primary,
                focusedContainerColor = MaterialTheme.colorScheme.surface,
                unfocusedContainerColor = MaterialTheme.colorScheme.surface
            ),
            shape = RoundedCornerShape(12.dp)
        )

        ExposedDropdownMenu(
            expanded = expanded,
            onDismissRequest = { expanded = false }
        ) {
            DropdownMenuItem(
                text = { Text("Sem viagem") },
                onClick = {
                    onSelectTrip(null)
                    expanded = false
                }
            )
            availableTrips.forEach { trip ->
                DropdownMenuItem(
                    text = { Text(trip.title) },
                    onClick = {
                        onSelectTrip(trip.id)
                        expanded = false
                    }
                )
            }
        }
    }
}

/**
 * Card com o mapa do ponto, as coordenadas e os atalhos de GPS e de endereço.
 */
@Composable
private fun SpotLocationCard(
    uiState: SpotFormUiState,
    onUseMyLocation: () -> Unit,
    onSearchAddress: () -> Unit,
    onPickOnMap: (Double, Double) -> Unit
) {
    PicTravellyCard(
        modifier = Modifier.fillMaxWidth(),
        containerColor = MaterialTheme.colorScheme.surface
    ) {
        Column(modifier = Modifier.padding(14.dp)) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(
                    imageVector = Icons.Default.MyLocation,
                    contentDescription = null,
                    tint = MaterialTheme.colorScheme.primary,
                    modifier = Modifier.size(18.dp)
                )
                Spacer(modifier = Modifier.width(8.dp))
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = "Localização do Ponto",
                        style = MaterialTheme.typography.titleSmall,
                        fontWeight = FontWeight.Bold
                    )
                    Text(
                        text = "Lat: %.5f, Lng: %.5f".format(uiState.latitude, uiState.longitude),
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
                if (uiState.isLocatingDevice) {
                    CircularProgressIndicator(
                        modifier = Modifier.size(20.dp),
                        strokeWidth = 2.dp,
                        color = MaterialTheme.colorScheme.primary
                    )
                }
            }

            Spacer(modifier = Modifier.height(12.dp))

            // Mapa do formulário: mostra e ajusta o ponto escolhido
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(200.dp)
                    .clip(RoundedCornerShape(14.dp))
                    .border(
                        1.dp,
                        MaterialTheme.colorScheme.outlineVariant,
                        RoundedCornerShape(14.dp)
                    )
            ) {
                PicTravellyMap(
                    latitude = uiState.latitude,
                    longitude = uiState.longitude,
                    zoom = if (uiState.isUsingFallbackLocation) 11.0 else 16.0,
                    markers = listOf(
                        MapMarkerData(
                            id = 0L,
                            title = uiState.title.ifBlank { "Ponto escolhido" },
                            snippet = uiState.locationName,
                            latitude = uiState.latitude,
                            longitude = uiState.longitude
                        )
                    ),
                    onMarkerClick = {},
                    isInteractive = true,
                    onLocationPick = onPickOnMap,
                    modifier = Modifier.fillMaxSize()
                )
            }

            Spacer(modifier = Modifier.height(8.dp))

            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(
                    imageVector = Icons.Default.TouchApp,
                    contentDescription = null,
                    tint = MaterialTheme.colorScheme.outline,
                    modifier = Modifier.size(14.dp)
                )
                Spacer(modifier = Modifier.width(4.dp))
                Text(
                    text = "Toque no mapa para ajustar o ponto exato",
                    style = MaterialTheme.typography.labelSmall,
                    color = MaterialTheme.colorScheme.outline
                )
            }

            Spacer(modifier = Modifier.height(8.dp))

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(4.dp)
            ) {
                TextButton(
                    onClick = onUseMyLocation,
                    enabled = !uiState.isLocatingDevice
                ) {
                    Icon(
                        imageVector = Icons.Default.MyLocation,
                        contentDescription = null,
                        modifier = Modifier.size(16.dp)
                    )
                    Spacer(modifier = Modifier.width(6.dp))
                    Text("Minha posição")
                }

                TextButton(
                    onClick = onSearchAddress,
                    enabled = !uiState.isResolvingAddress
                ) {
                    Icon(
                        imageVector = Icons.Default.Search,
                        contentDescription = null,
                        modifier = Modifier.size(16.dp)
                    )
                    Spacer(modifier = Modifier.width(6.dp))
                    Text("Buscar endereço")
                }
            }
        }
    }
}

/**
 * Lê a posição atual do aparelho e resolve o endereço correspondente.
 */
private suspend fun captureCurrentLocation(
    context: Context,
    viewModel: SpotFormViewModel,
    overwriteAddress: Boolean
) {
    viewModel.setLocatingDevice(true)
    val (latitude, longitude) = LocationHelper.getCurrentLocation(context)
    viewModel.setLocatingDevice(false)

    // Sem GPS nem permissão o helper devolve o ponto neutro; nesse caso não faz
    // sentido resolver endereço nem dar zoom de rua.
    val isFallback = latitude == LocationHelper.DEFAULT_LATITUDE &&
            longitude == LocationHelper.DEFAULT_LONGITUDE

    viewModel.updateCoordinates(latitude, longitude, isFallback = isFallback)

    if (isFallback) {
        if (overwriteAddress) {
            viewModel.setErrorMessage(
                "Não foi possível obter a sua posição. Toque no mapa para marcar o ponto."
            )
        }
        return
    }

    resolveAddress(
        context = context,
        viewModel = viewModel,
        latitude = latitude,
        longitude = longitude,
        overwrite = overwriteAddress
    )
}

/**
 * Converte os geocódigos em endereço textual (geocodificação reversa).
 *
 * Com [overwrite] falso, o texto que o usuário já digitou é preservado.
 */
private suspend fun resolveAddress(
    context: Context,
    viewModel: SpotFormViewModel,
    latitude: Double,
    longitude: Double,
    overwrite: Boolean
) {
    if (!overwrite && viewModel.uiState.value.locationName.isNotBlank()) return

    viewModel.setResolvingAddress(true)
    val address = GeocodingHelper.getAddressFromCoordinates(context, latitude, longitude)
    viewModel.setResolvingAddress(false)

    if (address == null) {
        if (overwrite) {
            viewModel.setErrorMessage(
                "Não foi possível obter o endereço. Você pode digitá-lo manualmente."
            )
        }
        return
    }

    viewModel.updateLocationName(address)
}
