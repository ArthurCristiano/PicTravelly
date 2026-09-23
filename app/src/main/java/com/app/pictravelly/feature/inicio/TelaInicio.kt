package com.app.pictravelly.feature.inicio

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
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Place
import androidx.compose.material.icons.outlined.Luggage
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import coil.compose.AsyncImage
import com.app.pictravelly.core.design.componentes.PicTravellyCartao
import com.app.pictravelly.core.design.componentes.PicTravellyTexto
import com.app.pictravelly.core.di.ProvedorDeViewModels
import com.app.pictravelly.core.ui.util.Datas
import com.app.pictravelly.data.local.relacao.ViagemComPontos
import java.io.File

/**
 * Tela inicial: os cards das viagens registradas no diario.
 */
@Composable
fun TelaInicio(
    onAbrirViagem: (Long) -> Unit,
    contentPadding: PaddingValues,
    modifier: Modifier = Modifier,
    viewModel: InicioViewModel = viewModel(factory = ProvedorDeViewModels.Fabrica)
) {
    val estado by viewModel.estado.collectAsStateWithLifecycle()

    Box(modifier = modifier.fillMaxSize()) {
        when {
            estado.carregando -> CircularProgressIndicator(
                modifier = Modifier.align(Alignment.Center)
            )

            estado.viagens.isEmpty() -> DiarioVazio(
                modifier = Modifier
                    .align(Alignment.Center)
                    .padding(32.dp)
            )

            else -> LazyColumn(
                modifier = Modifier.fillMaxSize(),
                contentPadding = PaddingValues(
                    top = contentPadding.calculateTopPadding() + 16.dp,
                    bottom = contentPadding.calculateBottomPadding() + 16.dp,
                    start = 16.dp,
                    end = 16.dp
                ),
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                item {
                    Text(
                        text = "Minhas viagens",
                        style = MaterialTheme.typography.headlineLarge,
                        color = MaterialTheme.colorScheme.onBackground
                    )
                }

                items(items = estado.viagens, key = { it.viagem.id }) { viagemComPontos ->
                    CartaoDeViagem(
                        viagemComPontos = viagemComPontos,
                        onClick = { onAbrirViagem(viagemComPontos.viagem.id) }
                    )
                }
            }
        }
    }
}

@Composable
private fun CartaoDeViagem(
    viagemComPontos: ViagemComPontos,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    val viagem = viagemComPontos.viagem
    // Sem capa escolhida, a foto do primeiro ponto cadastrado vira a capa.
    val capa = viagem.caminhoCapa
        ?: viagemComPontos.pontos.firstNotNullOfOrNull { it.caminhoImagem }

    PicTravellyCartao(modifier = modifier.fillMaxWidth(), onClick = onClick) {
        if (capa != null) {
            AsyncImage(
                model = File(capa),
                contentDescription = "Capa da viagem " + viagem.titulo,
                contentScale = ContentScale.Crop,
                modifier = Modifier
                    .fillMaxWidth()
                    .height(180.dp)
            )
        } else {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(100.dp)
                    .background(MaterialTheme.colorScheme.primaryContainer),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = Icons.Outlined.Luggage,
                    contentDescription = null,
                    tint = MaterialTheme.colorScheme.onPrimaryContainer,
                    modifier = Modifier.size(40.dp)
                )
            }
        }

        Column(modifier = Modifier.padding(16.dp)) {
            Text(
                text = viagem.titulo,
                style = MaterialTheme.typography.titleLarge,
                color = MaterialTheme.colorScheme.onSurface,
                maxLines = 2
            )

            Spacer(modifier = Modifier.height(4.dp))

            Text(
                text = Datas.intervalo(viagem.dataInicio, viagem.dataFim),
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )

            if (viagem.descricao.isNotBlank()) {
                Spacer(modifier = Modifier.height(8.dp))
                PicTravellyTexto(
                    text = viagem.descricao,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    maxLines = 2,
                    pequeno = true
                )
            }

            Spacer(modifier = Modifier.height(12.dp))

            SeloDePontos(quantidade = viagemComPontos.pontos.size)
        }
    }
}

@Composable
private fun SeloDePontos(quantidade: Int) {
    Row(
        verticalAlignment = Alignment.CenterVertically,
        modifier = Modifier
            .clip(RoundedCornerShape(50))
            .background(MaterialTheme.colorScheme.secondaryContainer)
            .padding(horizontal = 12.dp, vertical = 6.dp)
    ) {
        Icon(
            imageVector = Icons.Default.Place,
            contentDescription = null,
            tint = MaterialTheme.colorScheme.onSecondaryContainer,
            modifier = Modifier.size(16.dp)
        )
        Text(
            text = if (quantidade == 1) "1 ponto turístico" else "$quantidade pontos turísticos",
            style = MaterialTheme.typography.labelMedium,
            color = MaterialTheme.colorScheme.onSecondaryContainer,
            modifier = Modifier.padding(start = 6.dp)
        )
    }
}

@Composable
private fun DiarioVazio(modifier: Modifier = Modifier) {
    Column(
        modifier = modifier,
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Icon(
            imageVector = Icons.Outlined.Luggage,
            contentDescription = null,
            tint = MaterialTheme.colorScheme.primary,
            modifier = Modifier.size(64.dp)
        )
        Spacer(modifier = Modifier.height(16.dp))
        Text(
            text = "Seu diário está vazio",
            style = MaterialTheme.typography.titleLarge,
            color = MaterialTheme.colorScheme.onBackground,
            textAlign = TextAlign.Center
        )
        Spacer(modifier = Modifier.height(8.dp))
        Text(
            text = "Toque no + para registrar a sua primeira viagem.",
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            textAlign = TextAlign.Center
        )
    }
}
