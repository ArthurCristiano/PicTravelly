package com.app.pictravelly.feature.mapa

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
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.MyLocation
import androidx.compose.material.icons.filled.Place
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.key
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import coil.compose.AsyncImage
import com.app.pictravelly.core.design.componentes.PicTravellyCartao
import com.app.pictravelly.core.design.componentes.PicTravellyTexto
import com.app.pictravelly.core.di.ProvedorDeViewModels
import com.app.pictravelly.core.navegacao.Rotas
import com.app.pictravelly.core.ui.util.Geocodigos
import com.app.pictravelly.data.local.entidade.Ponto
import com.google.android.gms.maps.CameraUpdateFactory
import com.google.android.gms.maps.model.CameraPosition
import com.google.android.gms.maps.model.LatLng
import com.google.maps.android.compose.GoogleMap
import com.google.maps.android.compose.MapProperties
import com.google.maps.android.compose.MapUiSettings
import com.google.maps.android.compose.Marker
import com.google.maps.android.compose.rememberCameraPositionState
import com.google.maps.android.compose.rememberUpdatedMarkerState
import java.io.File

/** Curitiba: ponto de partida do mapa quando ainda nao ha nada cadastrado. */
private val PADRAO = LatLng(-25.4284, -49.2733)

/**
 * Todos os pontos turisticos do diario em um Google Map, com o tipo de mapa
 * e o zoom vindos da tela de configuracao.
 */
@Composable
fun TelaMapa(
    onAbrirViagem: (Long) -> Unit,
    contentPadding: PaddingValues,
    modifier: Modifier = Modifier,
    viewModel: MapaViewModel = viewModel(factory = ProvedorDeViewModels.Fabrica)
) {
    val estado by viewModel.estado.collectAsStateWithLifecycle()
    var pontoSelecionado by remember { mutableStateOf<Ponto?>(null) }

    val cameraPositionState = rememberCameraPositionState {
        position = CameraPosition.fromLatLngZoom(PADRAO, estado.configuracao.zoomPadrao)
    }

    // Ao abrir, centraliza no ponto pedido ou no cadastro mais recente,
    // sempre com o zoom padrao definido pelo usuario.
    LaunchedEffect(estado.carregando, estado.pontos, estado.configuracao.zoomPadrao) {
        if (estado.carregando) return@LaunchedEffect

        val alvo = estado.pontos.firstOrNull { it.id == viewModel.pontoEmFoco }
            ?: estado.pontos.firstOrNull()
            ?: return@LaunchedEffect

        if (viewModel.pontoEmFoco != Rotas.SEM_ID) pontoSelecionado = alvo

        cameraPositionState.move(
            CameraUpdateFactory.newLatLngZoom(
                LatLng(alvo.latitude, alvo.longitude),
                estado.configuracao.zoomPadrao
            )
        )
    }

    Box(modifier = modifier.fillMaxSize()) {
        GoogleMap(
            modifier = Modifier.fillMaxSize(),
            cameraPositionState = cameraPositionState,
            properties = MapProperties(
                mapType = estado.configuracao.tipoMapa.mapType,
                isTrafficEnabled = estado.configuracao.mostrarTransito
            ),
            uiSettings = MapUiSettings(zoomControlsEnabled = false),
            contentPadding = contentPadding,
            onMapClick = { pontoSelecionado = null }
        ) {
            estado.pontos.forEach { ponto ->
                // A chave mantem cada marcador ligado ao seu ponto quando a
                // lista muda de ordem ou tamanho.
                key(ponto.id) {
                    Marker(
                        state = rememberUpdatedMarkerState(
                            position = LatLng(ponto.latitude, ponto.longitude)
                        ),
                        title = ponto.nome,
                        snippet = ponto.endereco,
                        onClick = {
                            pontoSelecionado = ponto
                            false // false mantem o comportamento padrao do marcador
                        }
                    )
                }
            }
        }

        if (!estado.carregando && estado.pontos.isEmpty()) {
            AvisoMapaVazio(
                modifier = Modifier
                    .align(Alignment.TopCenter)
                    .padding(
                        top = contentPadding.calculateTopPadding() + 16.dp,
                        start = 16.dp,
                        end = 16.dp
                    )
            )
        }

        pontoSelecionado?.let { ponto ->
            CartaoDoPonto(
                ponto = ponto,
                onFechar = { pontoSelecionado = null },
                onAbrirViagem = { onAbrirViagem(ponto.viagemId) },
                modifier = Modifier
                    .align(Alignment.BottomCenter)
                    .padding(
                        start = 16.dp,
                        end = 16.dp,
                        bottom = contentPadding.calculateBottomPadding() + 16.dp
                    )
            )
        }
    }
}

@Composable
private fun CartaoDoPonto(
    ponto: Ponto,
    onFechar: () -> Unit,
    onAbrirViagem: () -> Unit,
    modifier: Modifier = Modifier
) {
    PicTravellyCartao(modifier = modifier.fillMaxWidth()) {
        ponto.caminhoImagem?.let { caminho ->
            AsyncImage(
                model = File(caminho),
                contentDescription = "Foto de " + ponto.nome,
                contentScale = ContentScale.Crop,
                modifier = Modifier
                    .fillMaxWidth()
                    .height(140.dp)
            )
        }

        Column(modifier = Modifier.padding(start = 16.dp, end = 8.dp, top = 12.dp, bottom = 8.dp)) {
            Row(verticalAlignment = Alignment.Top) {
                Text(
                    text = ponto.nome,
                    style = MaterialTheme.typography.titleLarge,
                    color = MaterialTheme.colorScheme.onSurface,
                    modifier = Modifier.weight(1f)
                )
                IconButton(onClick = onFechar) {
                    Icon(Icons.Default.Close, contentDescription = "Fechar")
                }
            }

            LinhaInfo(
                icone = Icons.Default.Place,
                texto = ponto.endereco ?: "Endereço não disponível"
            )
            LinhaInfo(
                icone = Icons.Default.MyLocation,
                texto = Geocodigos.formatar(ponto.latitude, ponto.longitude)
            )

            if (ponto.descricao.isNotBlank()) {
                Spacer(modifier = Modifier.height(8.dp))
                PicTravellyTexto(
                    text = ponto.descricao,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    maxLines = 3,
                    pequeno = true
                )
            }

            TextButton(onClick = onAbrirViagem) {
                Text("Abrir viagem")
            }
        }
    }
}

@Composable
private fun LinhaInfo(
    icone: androidx.compose.ui.graphics.vector.ImageVector,
    texto: String
) {
    Row(verticalAlignment = Alignment.CenterVertically) {
        Icon(
            imageVector = icone,
            contentDescription = null,
            tint = MaterialTheme.colorScheme.onSurfaceVariant,
            modifier = Modifier.size(16.dp)
        )
        Text(
            text = texto,
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            modifier = Modifier.padding(start = 6.dp)
        )
    }
}

@Composable
private fun AvisoMapaVazio(modifier: Modifier = Modifier) {
    PicTravellyCartao(
        modifier = modifier.fillMaxWidth(),
        containerColor = MaterialTheme.colorScheme.secondaryContainer
    ) {
        Text(
            text = "Nenhum ponto turístico cadastrado ainda. Use o + para registrar o primeiro.",
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSecondaryContainer,
            textAlign = TextAlign.Center,
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp)
        )
    }
}
