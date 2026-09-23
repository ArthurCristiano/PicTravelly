package com.app.pictravelly.feature.ponto

import android.Manifest
import androidx.activity.compose.BackHandler
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.imePadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.MyLocation
import androidx.compose.material.icons.filled.Place
import androidx.compose.material.icons.filled.Search
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
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import com.app.pictravelly.core.design.componentes.PicTravellyBotao
import com.app.pictravelly.core.design.componentes.PicTravellyBotaoContornado
import com.app.pictravelly.core.design.componentes.PicTravellyCartao
import com.app.pictravelly.core.design.componentes.PicTravellySeletorDeFoto
import com.app.pictravelly.core.di.ProvedorDeViewModels
import com.app.pictravelly.core.localizacao.ProvedorDeLocalizacao
import com.app.pictravelly.core.ui.componentes.DialogoDeConfirmacao

/**
 * Formulario de cadastro e edicao de um ponto turistico.
 *
 * Reune as quatro informacoes exigidas pelo trabalho: geocodigos, nome,
 * descricao e imagem, alem do endereco resolvido a partir dos geocodigos.
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun TelaEditorPonto(
    onConcluido: () -> Unit,
    modifier: Modifier = Modifier,
    viewModel: EditorPontoViewModel = viewModel(factory = ProvedorDeViewModels.Fabrica)
) {
    val estado by viewModel.estado.collectAsStateWithLifecycle()
    var confirmarExclusao by remember { mutableStateOf(false) }

    val permissaoLocalizacao = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.RequestMultiplePermissions()
    ) { concedidas ->
        if (concedidas.values.any { it }) viewModel.usarLocalizacaoAtual()
    }

    LaunchedEffect(estado.salvo) {
        if (estado.salvo) onConcluido()
    }

    val sair = {
        viewModel.descartar()
        onConcluido()
    }

    BackHandler(onBack = sair)

    Scaffold(
        modifier = modifier,
        topBar = {
            TopAppBar(
                title = { Text(if (estado.edicao) "Editar ponto" else "Novo ponto turístico") },
                navigationIcon = {
                    IconButton(onClick = sair) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Voltar")
                    }
                },
                actions = {
                    if (estado.edicao) {
                        IconButton(onClick = { confirmarExclusao = true }) {
                            Icon(
                                Icons.Default.Delete,
                                contentDescription = "Apagar ponto",
                                tint = MaterialTheme.colorScheme.error
                            )
                        }
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.primaryContainer,
                    titleContentColor = MaterialTheme.colorScheme.onPrimaryContainer,
                    navigationIconContentColor = MaterialTheme.colorScheme.onPrimaryContainer,
                    actionIconContentColor = MaterialTheme.colorScheme.onPrimaryContainer
                )
            )
        }
    ) { innerPadding ->
        if (estado.carregando) {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(innerPadding),
                contentAlignment = Alignment.Center
            ) {
                CircularProgressIndicator()
            }
            return@Scaffold
        }

        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .imePadding()
                .verticalScroll(rememberScrollState())
                .padding(horizontal = 16.dp, vertical = 16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            PicTravellySeletorDeFoto(
                caminhoImagem = estado.caminhoImagem,
                onFotoCapturada = viewModel::aoCapturarFoto,
                onFotoEscolhida = viewModel::aoEscolherFoto,
                onRemoverFoto = viewModel::aoRemoverFoto,
                textoVazio = "Registre uma foto do local"
            )

            SeletorDeViagem(
                estado = estado,
                onSelecionar = viewModel::aoMudarViagem
            )

            OutlinedTextField(
                value = estado.nome,
                onValueChange = viewModel::aoMudarNome,
                label = { Text("Nome do ponto turístico") },
                placeholder = { Text("Ex.: Cataratas do Iguaçu") },
                singleLine = true,
                isError = estado.nome.isBlank(),
                supportingText = { if (estado.nome.isBlank()) Text("Informe um nome") },
                modifier = Modifier.fillMaxWidth()
            )

            OutlinedTextField(
                value = estado.descricao,
                onValueChange = viewModel::aoMudarDescricao,
                label = { Text("Descrição") },
                placeholder = { Text("O que você viu por lá?") },
                minLines = 3,
                modifier = Modifier.fillMaxWidth()
            )

            BlocoDeGeocodigos(
                estado = estado,
                onMudarLatitude = viewModel::aoMudarLatitude,
                onMudarLongitude = viewModel::aoMudarLongitude,
                onUsarGps = { permissaoLocalizacao.launch(ProvedorDeLocalizacao.PERMISSOES) },
                onBuscarEndereco = viewModel::resolverEndereco
            )

            estado.mensagem?.let { mensagem ->
                Text(
                    text = mensagem,
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.error
                )
            }

            if (estado.viagens.isEmpty()) {
                Text(
                    text = "Crie uma viagem antes de cadastrar pontos turísticos.",
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.error
                )
            }

            PicTravellyBotao(
                text = if (estado.edicao) "Salvar alterações" else "Cadastrar ponto",
                onClick = viewModel::salvar,
                enabled = estado.podeSalvar,
                modifier = Modifier.fillMaxWidth()
            )

            PicTravellyBotaoContornado(
                text = "Cancelar",
                onClick = sair,
                modifier = Modifier.fillMaxWidth()
            )
        }
    }

    if (confirmarExclusao) {
        DialogoDeConfirmacao(
            titulo = "Apagar ponto?",
            mensagem = "O ponto \"" + estado.nome + "\" será removido do diário.",
            textoConfirmar = "Apagar",
            onConfirmar = {
                confirmarExclusao = false
                viewModel.apagar()
            },
            onCancelar = { confirmarExclusao = false }
        )
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun SeletorDeViagem(
    estado: EstadoEditorPonto,
    onSelecionar: (Long) -> Unit
) {
    var aberto by remember { mutableStateOf(false) }

    ExposedDropdownMenuBox(
        expanded = aberto,
        onExpandedChange = { aberto = !aberto }
    ) {
        OutlinedTextField(
            value = estado.nomeDaViagem,
            onValueChange = {},
            readOnly = true,
            label = { Text("Viagem") },
            trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = aberto) },
            modifier = Modifier
                .fillMaxWidth()
                .menuAnchor(ExposedDropdownMenuAnchorType.PrimaryNotEditable)
        )

        ExposedDropdownMenu(
            expanded = aberto,
            onDismissRequest = { aberto = false }
        ) {
            estado.viagens.forEach { viagem ->
                DropdownMenuItem(
                    text = { Text(viagem.titulo) },
                    onClick = {
                        onSelecionar(viagem.id)
                        aberto = false
                    }
                )
            }
        }
    }
}

@Composable
private fun BlocoDeGeocodigos(
    estado: EstadoEditorPonto,
    onMudarLatitude: (String) -> Unit,
    onMudarLongitude: (String) -> Unit,
    onUsarGps: () -> Unit,
    onBuscarEndereco: () -> Unit
) {
    PicTravellyCartao(modifier = Modifier.fillMaxWidth()) {
        Column(
            modifier = Modifier.padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            Text(
                text = "Geocódigos",
                style = MaterialTheme.typography.titleLarge,
                color = MaterialTheme.colorScheme.onSurface
            )

            Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                OutlinedTextField(
                    value = estado.latitude,
                    onValueChange = onMudarLatitude,
                    label = { Text("Latitude") },
                    singleLine = true,
                    isError = estado.latitude.isNotBlank() && !estado.latitudeValida,
                    keyboardOptions = KeyboardOptions(
                        keyboardType = KeyboardType.Decimal,
                        imeAction = ImeAction.Next
                    ),
                    modifier = Modifier.weight(1f)
                )
                OutlinedTextField(
                    value = estado.longitude,
                    onValueChange = onMudarLongitude,
                    label = { Text("Longitude") },
                    singleLine = true,
                    isError = estado.longitude.isNotBlank() && !estado.longitudeValida,
                    keyboardOptions = KeyboardOptions(
                        keyboardType = KeyboardType.Decimal,
                        imeAction = ImeAction.Done
                    ),
                    modifier = Modifier.weight(1f)
                )
            }

            Row(horizontalArrangement = Arrangement.spacedBy(4.dp)) {
                TextButton(onClick = onUsarGps, enabled = !estado.buscandoLocalizacao) {
                    if (estado.buscandoLocalizacao) {
                        CircularProgressIndicator(modifier = Modifier.size(16.dp), strokeWidth = 2.dp)
                    } else {
                        Icon(
                            Icons.Default.MyLocation,
                            contentDescription = null,
                            modifier = Modifier.size(18.dp)
                        )
                    }
                    Text(text = "Usar GPS", modifier = Modifier.padding(start = 6.dp))
                }

                TextButton(onClick = onBuscarEndereco, enabled = !estado.buscandoEndereco) {
                    if (estado.buscandoEndereco) {
                        CircularProgressIndicator(modifier = Modifier.size(16.dp), strokeWidth = 2.dp)
                    } else {
                        Icon(
                            Icons.Default.Search,
                            contentDescription = null,
                            modifier = Modifier.size(18.dp)
                        )
                    }
                    Text(text = "Buscar endereço", modifier = Modifier.padding(start = 6.dp))
                }
            }

            // Endereco textual devolvido pela geocodificacao reversa.
            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(
                    imageVector = Icons.Default.Place,
                    contentDescription = null,
                    tint = MaterialTheme.colorScheme.primary,
                    modifier = Modifier.size(18.dp)
                )
                Text(
                    text = estado.endereco ?: "Endereço ainda não resolvido",
                    style = MaterialTheme.typography.bodyMedium,
                    color = if (estado.endereco != null) {
                        MaterialTheme.colorScheme.onSurface
                    } else {
                        MaterialTheme.colorScheme.onSurfaceVariant
                    },
                    modifier = Modifier.padding(start = 6.dp)
                )
            }
        }
    }
}
