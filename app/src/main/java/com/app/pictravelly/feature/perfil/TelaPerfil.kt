package com.app.pictravelly.feature.perfil

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
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.Place
import androidx.compose.material.icons.outlined.Luggage
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import coil.compose.AsyncImage
import com.app.pictravelly.core.design.componentes.PicTravellyBotaoContornado
import com.app.pictravelly.core.design.componentes.PicTravellyCartao
import com.app.pictravelly.core.design.componentes.PicTravellySeletorDeFoto
import com.app.pictravelly.core.design.componentes.PicTravellyTexto
import com.app.pictravelly.core.di.ProvedorDeViewModels
import java.io.File

/** Perfil do viajante: foto, nome, bio e o resumo do diario. */
@Composable
fun TelaPerfil(
    contentPadding: PaddingValues,
    modifier: Modifier = Modifier,
    viewModel: PerfilViewModel = viewModel(factory = ProvedorDeViewModels.Fabrica)
) {
    val estado by viewModel.estado.collectAsStateWithLifecycle()
    val editando by viewModel.editando.collectAsStateWithLifecycle()

    Column(
        modifier = modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(
                top = contentPadding.calculateTopPadding() + 24.dp,
                bottom = contentPadding.calculateBottomPadding() + 16.dp,
                start = 16.dp,
                end = 16.dp
            ),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        Avatar(caminho = estado.perfil.caminhoAvatar)

        Text(
            text = estado.perfil.nome.ifBlank { "Viajante" },
            style = MaterialTheme.typography.headlineLarge,
            color = MaterialTheme.colorScheme.onBackground,
            textAlign = TextAlign.Center
        )

        if (estado.perfil.bio.isNotBlank()) {
            PicTravellyTexto(
                text = estado.perfil.bio,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                textAlign = TextAlign.Center
            )
        }

        Row(horizontalArrangement = Arrangement.spacedBy(16.dp)) {
            Estatistica(
                valor = estado.totalDeViagens,
                rotulo = if (estado.totalDeViagens == 1) "viagem" else "viagens",
                icone = Icons.Outlined.Luggage,
                modifier = Modifier.weight(1f)
            )
            Estatistica(
                valor = estado.totalDePontos,
                rotulo = if (estado.totalDePontos == 1) "ponto" else "pontos",
                icone = Icons.Default.Place,
                modifier = Modifier.weight(1f)
            )
        }

        PicTravellyBotaoContornado(
            text = "Editar perfil",
            onClick = viewModel::abrirEdicao,
            modifier = Modifier.fillMaxWidth()
        )

        PicTravellyCartao(modifier = Modifier.fillMaxWidth()) {
            Column(modifier = Modifier.padding(16.dp)) {
                Text(
                    text = "Foto do perfil",
                    style = MaterialTheme.typography.titleLarge,
                    color = MaterialTheme.colorScheme.onSurface
                )
                Spacer(modifier = Modifier.height(12.dp))
                PicTravellySeletorDeFoto(
                    caminhoImagem = estado.perfil.caminhoAvatar,
                    onFotoCapturada = viewModel::aoCapturarAvatar,
                    onFotoEscolhida = viewModel::aoEscolherAvatar,
                    onRemoverFoto = viewModel::aoRemoverAvatar,
                    textoVazio = "Escolha uma foto de perfil"
                )
            }
        }
    }

    if (editando) {
        DialogoDeEdicao(
            nomeInicial = estado.perfil.nome,
            bioInicial = estado.perfil.bio,
            onSalvar = viewModel::salvarDados,
            onCancelar = viewModel::fecharEdicao
        )
    }
}

@Composable
private fun Avatar(caminho: String?) {
    Box(
        modifier = Modifier
            .size(120.dp)
            .clip(CircleShape)
            .background(MaterialTheme.colorScheme.primaryContainer),
        contentAlignment = Alignment.Center
    ) {
        if (caminho != null) {
            AsyncImage(
                model = File(caminho),
                contentDescription = "Foto do perfil",
                contentScale = ContentScale.Crop,
                modifier = Modifier.fillMaxSize()
            )
        } else {
            Icon(
                imageVector = Icons.Default.Person,
                contentDescription = null,
                tint = MaterialTheme.colorScheme.onPrimaryContainer,
                modifier = Modifier.size(56.dp)
            )
        }
    }
}

@Composable
private fun Estatistica(
    valor: Int,
    rotulo: String,
    icone: androidx.compose.ui.graphics.vector.ImageVector,
    modifier: Modifier = Modifier
) {
    PicTravellyCartao(modifier = modifier) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(vertical = 16.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Icon(
                imageVector = icone,
                contentDescription = null,
                tint = MaterialTheme.colorScheme.primary,
                modifier = Modifier.size(24.dp)
            )
            Spacer(modifier = Modifier.height(4.dp))
            Text(
                text = valor.toString(),
                style = MaterialTheme.typography.headlineLarge,
                color = MaterialTheme.colorScheme.onSurface
            )
            Text(
                text = rotulo,
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
    }
}

@Composable
private fun DialogoDeEdicao(
    nomeInicial: String,
    bioInicial: String,
    onSalvar: (String, String) -> Unit,
    onCancelar: () -> Unit
) {
    var nome by remember { mutableStateOf(nomeInicial) }
    var bio by remember { mutableStateOf(bioInicial) }

    AlertDialog(
        onDismissRequest = onCancelar,
        icon = { Icon(Icons.Default.Edit, contentDescription = null) },
        title = { Text("Editar perfil") },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                OutlinedTextField(
                    value = nome,
                    onValueChange = { nome = it },
                    label = { Text("Nome") },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth()
                )
                OutlinedTextField(
                    value = bio,
                    onValueChange = { bio = it },
                    label = { Text("Sobre você") },
                    minLines = 2,
                    modifier = Modifier.fillMaxWidth()
                )
            }
        },
        confirmButton = {
            TextButton(onClick = { onSalvar(nome, bio) }) { Text("Salvar") }
        },
        dismissButton = {
            TextButton(onClick = onCancelar) { Text("Cancelar") }
        }
    )
}
