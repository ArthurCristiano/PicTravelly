package com.app.pictravelly.feature.trip_form

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
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.imePadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.AddPhotoAlternate
import androidx.compose.material.icons.filled.CalendarToday
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.DatePicker
import androidx.compose.material3.DatePickerDialog
import androidx.compose.material3.ExperimentalMaterial3Api
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
import androidx.compose.material3.rememberDatePickerState
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
import com.app.pictravelly.core.design.components.PicTravellyButton
import com.app.pictravelly.core.design.components.PicTravellyCard
import com.app.pictravelly.core.design.components.PicTravellyOutlinedButton
import com.app.pictravelly.core.design.components.PicTravellyTitle
import java.text.SimpleDateFormat
import java.time.Instant
import java.time.ZoneId
import java.time.ZoneOffset
import java.util.Date
import java.util.Locale

private val DATE_FORMAT = SimpleDateFormat("dd 'de' MMMM, yyyy", Locale.forLanguageTag("pt-BR"))

private enum class DateField { START, END }

/**
 * Formulário de cadastro e edição de uma viagem, o agrupamento que recebe os
 * pontos turísticos no diário.
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun TripFormScreen(
    viewModel: TripFormViewModel,
    onNavigateBack: () -> Unit,
    onTripSaved: (Long) -> Unit,
    modifier: Modifier = Modifier
) {
    val uiState by viewModel.uiState.collectAsState()
    var openDateField by remember { mutableStateOf<DateField?>(null) }

    // Escolha da capa da viagem na galeria
    val coverPickerLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.GetContent()
    ) { uri: Uri? ->
        uri?.let { viewModel.updateCoverImageUri(it.toString()) }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text(if (uiState.isEditing) "Editar Viagem" else "Nova Viagem") },
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

        // ============================================================================
        // 🎨 [DESIGN / HUMBERTO] - ABERTURA DE UM NOVO CAPÍTULO DO DIÁRIO:
        // - Esta é a capa da viagem: cabe textura de couro, etiqueta de bagagem
        //   e carimbo de data de embarque.
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
                text = "Abrir um Capítulo",
                modifier = Modifier.fillMaxWidth()
            )

            if (uiState.errorMessage != null) {
                Text(
                    text = uiState.errorMessage!!,
                    color = MaterialTheme.colorScheme.error,
                    style = MaterialTheme.typography.bodySmall
                )
            }

            // Capa da viagem
            TripCoverPicker(
                coverImageUri = uiState.coverImageUri,
                onPickCover = { coverPickerLauncher.launch("image/*") },
                onRemoveCover = { viewModel.updateCoverImageUri(null) }
            )

            // Campo: Título da Viagem
            OutlinedTextField(
                value = uiState.title,
                onValueChange = { viewModel.updateTitle(it) },
                label = { Text("Título da Viagem *") },
                placeholder = { Text("Ex: Férias no litoral do Paraná") },
                modifier = Modifier.fillMaxWidth(),
                singleLine = true,
                colors = OutlinedTextFieldDefaults.colors(
                    focusedBorderColor = MaterialTheme.colorScheme.primary,
                    focusedContainerColor = MaterialTheme.colorScheme.surface,
                    unfocusedContainerColor = MaterialTheme.colorScheme.surface
                ),
                shape = RoundedCornerShape(12.dp)
            )

            // Período da viagem
            PicTravellyCard(
                modifier = Modifier.fillMaxWidth(),
                containerColor = MaterialTheme.colorScheme.surface
            ) {
                Column(modifier = Modifier.padding(14.dp)) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(
                            imageVector = Icons.Default.CalendarToday,
                            contentDescription = null,
                            tint = MaterialTheme.colorScheme.primary,
                            modifier = Modifier.size(18.dp)
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            text = "Período da Viagem",
                            style = MaterialTheme.typography.titleSmall,
                            fontWeight = FontWeight.Bold
                        )
                    }

                    Spacer(modifier = Modifier.height(12.dp))

                    Row(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                        DateFieldButton(
                            label = "Início",
                            value = DATE_FORMAT.format(Date(uiState.startDate)),
                            onClick = { openDateField = DateField.START },
                            modifier = Modifier.weight(1f)
                        )
                        DateFieldButton(
                            label = "Fim",
                            value = uiState.endDate?.let { DATE_FORMAT.format(Date(it)) }
                                ?: "Em andamento",
                            onClick = { openDateField = DateField.END },
                            modifier = Modifier.weight(1f)
                        )
                    }
                }
            }

            // Campo: Relato geral da viagem
            OutlinedTextField(
                value = uiState.description,
                onValueChange = { viewModel.updateDescription(it) },
                label = { Text("Sobre a Viagem") },
                placeholder = { Text("Com quem você foi, o que esperava encontrar...") },
                modifier = Modifier
                    .fillMaxWidth()
                    .height(150.dp),
                colors = OutlinedTextFieldDefaults.colors(
                    focusedBorderColor = MaterialTheme.colorScheme.primary,
                    focusedContainerColor = MaterialTheme.colorScheme.surface,
                    unfocusedContainerColor = MaterialTheme.colorScheme.surface
                ),
                shape = RoundedCornerShape(12.dp)
            )

            Spacer(modifier = Modifier.height(8.dp))

            if (uiState.isSaving) {
                Box(
                    modifier = Modifier.fillMaxWidth(),
                    contentAlignment = Alignment.Center
                ) {
                    CircularProgressIndicator(color = MaterialTheme.colorScheme.primary)
                }
            } else {
                PicTravellyButton(
                    text = if (uiState.isEditing) "Salvar Alterações" else "Criar Viagem",
                    onClick = { viewModel.saveTrip(onTripSaved) },
                    enabled = uiState.isValid,
                    modifier = Modifier.fillMaxWidth()
                )
            }

            Spacer(modifier = Modifier.height(48.dp))
        }
    }

    openDateField?.let { field ->
        val initialMillis = when (field) {
            DateField.START -> uiState.startDate
            DateField.END -> uiState.endDate ?: uiState.startDate
        }
        TripDatePickerDialog(
            initialMillis = initialMillis,
            allowClear = field == DateField.END,
            onConfirm = { millis ->
                when (field) {
                    DateField.START -> viewModel.updateStartDate(millis)
                    DateField.END -> viewModel.updateEndDate(millis)
                }
                openDateField = null
            },
            onClear = {
                viewModel.updateEndDate(null)
                openDateField = null
            },
            onDismiss = { openDateField = null }
        )
    }
}

/**
 * Bloco de escolha da imagem de capa da viagem.
 */
@Composable
private fun TripCoverPicker(
    coverImageUri: String?,
    onPickCover: () -> Unit,
    onRemoveCover: () -> Unit
) {
    PicTravellyCard(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(16.dp))
            .border(
                1.5.dp,
                MaterialTheme.colorScheme.primary.copy(alpha = 0.35f),
                RoundedCornerShape(16.dp)
            ),
        containerColor = MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.5f),
        onClick = onPickCover
    ) {
        if (coverImageUri != null) {
            Box(modifier = Modifier.fillMaxWidth()) {
                AsyncImage(
                    model = coverImageUri,
                    contentDescription = "Capa da viagem",
                    modifier = Modifier
                        .fillMaxWidth()
                        .aspectRatio(16f / 9f),
                    contentScale = ContentScale.Crop
                )
                IconButton(
                    onClick = onRemoveCover,
                    modifier = Modifier
                        .align(Alignment.TopEnd)
                        .padding(8.dp)
                        .size(30.dp)
                        .clip(CircleShape)
                        .background(MaterialTheme.colorScheme.surface.copy(alpha = 0.85f))
                ) {
                    Icon(
                        imageVector = Icons.Default.Delete,
                        contentDescription = "Remover capa",
                        tint = MaterialTheme.colorScheme.error,
                        modifier = Modifier.size(18.dp)
                    )
                }
            }
        } else {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 24.dp, horizontal = 16.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Box(
                    modifier = Modifier
                        .size(64.dp)
                        .clip(CircleShape)
                        .background(MaterialTheme.colorScheme.primary),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = Icons.Default.AddPhotoAlternate,
                        contentDescription = "Escolher capa",
                        tint = MaterialTheme.colorScheme.onPrimary,
                        modifier = Modifier.size(32.dp)
                    )
                }
                Spacer(modifier = Modifier.height(12.dp))
                Text(
                    text = "Escolher a Capa da Viagem",
                    style = MaterialTheme.typography.titleSmall,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onSurface
                )
                Spacer(modifier = Modifier.height(4.dp))
                Text(
                    text = "Opcional: sem capa, usamos a foto do primeiro ponto",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        }
    }
}

@Composable
private fun DateFieldButton(
    label: String,
    value: String,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    Column(modifier = modifier) {
        Text(
            text = label,
            style = MaterialTheme.typography.labelSmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
        Spacer(modifier = Modifier.height(4.dp))
        PicTravellyOutlinedButton(
            text = value,
            onClick = onClick,
            modifier = Modifier.fillMaxWidth()
        )
    }
}

/**
 * Seletor de data do Material 3.
 *
 * O DatePicker trabalha em UTC: sem converter para o fuso local a data
 * escolhida apareceria um dia atrasada no Brasil.
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun TripDatePickerDialog(
    initialMillis: Long,
    allowClear: Boolean,
    onConfirm: (Long) -> Unit,
    onClear: () -> Unit,
    onDismiss: () -> Unit
) {
    val pickerState = rememberDatePickerState(
        initialSelectedDateMillis = localToUtcMillis(initialMillis)
    )

    DatePickerDialog(
        onDismissRequest = onDismiss,
        confirmButton = {
            TextButton(
                onClick = {
                    val selected = pickerState.selectedDateMillis
                    if (selected == null) onDismiss() else onConfirm(utcToLocalMillis(selected))
                }
            ) {
                Text("Confirmar")
            }
        },
        dismissButton = {
            Row {
                if (allowClear) {
                    TextButton(onClick = onClear) { Text("Em andamento") }
                }
                TextButton(onClick = onDismiss) { Text("Cancelar") }
            }
        }
    ) {
        DatePicker(state = pickerState)
    }
}

/** Meia-noite local -> meia-noite UTC, que é o que o DatePicker espera. */
private fun localToUtcMillis(millis: Long): Long =
    Instant.ofEpochMilli(millis)
        .atZone(ZoneId.systemDefault())
        .toLocalDate()
        .atStartOfDay(ZoneOffset.UTC)
        .toInstant()
        .toEpochMilli()

/** Caminho inverso: o que o DatePicker devolve -> meia-noite local. */
private fun utcToLocalMillis(millis: Long): Long =
    Instant.ofEpochMilli(millis)
        .atZone(ZoneOffset.UTC)
        .toLocalDate()
        .atStartOfDay(ZoneId.systemDefault())
        .toInstant()
        .toEpochMilli()
