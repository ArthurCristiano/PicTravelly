package com.app.pictravelly.feature.configuracoes

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.FilterChip
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Slider
import androidx.compose.material3.Switch
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import com.app.pictravelly.core.configuracoes.ConfiguracaoDoMapa
import com.app.pictravelly.core.configuracoes.TipoMapa
import com.app.pictravelly.core.design.componentes.PicTravellyCartao
import com.app.pictravelly.core.di.ProvedorDeViewModels
import kotlin.math.roundToInt

/**
 * Tela de configuracao exigida pelo trabalho: nivel de zoom padrao e tipo de
 * mapa apresentado (rodoviario, satelite, terreno ou hibrido).
 */
@Composable
fun TelaConfiguracoes(
    contentPadding: PaddingValues,
    modifier: Modifier = Modifier,
    viewModel: ConfiguracoesViewModel = viewModel(factory = ProvedorDeViewModels.Fabrica)
) {
    val configuracao by viewModel.configuracao.collectAsStateWithLifecycle()

    Column(
        modifier = modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(
                top = contentPadding.calculateTopPadding() + 16.dp,
                bottom = contentPadding.calculateBottomPadding() + 16.dp,
                start = 16.dp,
                end = 16.dp
            ),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        Text(
            text = "Configurações",
            style = MaterialTheme.typography.headlineLarge,
            color = MaterialTheme.colorScheme.onBackground
        )

        BlocoDeZoom(
            zoom = configuracao.zoomPadrao,
            onMudarZoom = viewModel::definirZoom
        )

        BlocoDeTipoDeMapa(
            selecionado = configuracao.tipoMapa,
            onSelecionar = viewModel::definirTipoMapa
        )

        BlocoDeTransito(
            ligado = configuracao.mostrarTransito,
            onMudar = viewModel::definirMostrarTransito
        )
    }
}

@Composable
private fun BlocoDeZoom(
    zoom: Float,
    onMudarZoom: (Float) -> Unit
) {
    PicTravellyCartao(modifier = Modifier.fillMaxWidth()) {
        Column(modifier = Modifier.padding(16.dp)) {
            Text(
                text = "Nível de zoom padrão",
                style = MaterialTheme.typography.titleLarge,
                color = MaterialTheme.colorScheme.onSurface
            )
            Text(
                text = "Define a aproximação do mapa ao abri-lo.",
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )

            Spacer(modifier = Modifier.height(12.dp))

            Row(verticalAlignment = Alignment.CenterVertically) {
                Slider(
                    value = zoom,
                    onValueChange = { onMudarZoom(it.roundToInt().toFloat()) },
                    valueRange = ConfiguracaoDoMapa.ZOOM_MINIMO..ConfiguracaoDoMapa.ZOOM_MAXIMO,
                    steps = (ConfiguracaoDoMapa.ZOOM_MAXIMO - ConfiguracaoDoMapa.ZOOM_MINIMO).toInt() - 1,
                    modifier = Modifier.weight(1f)
                )
                Text(
                    text = zoom.roundToInt().toString(),
                    style = MaterialTheme.typography.titleLarge,
                    color = MaterialTheme.colorScheme.primary,
                    modifier = Modifier.padding(start = 12.dp)
                )
            }

            Text(
                text = descricaoDoZoom(zoom),
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
    }
}

private fun descricaoDoZoom(zoom: Float): String = when (zoom.roundToInt()) {
    in 0..4 -> "Visão de mundo"
    in 5..8 -> "Visão de país ou região"
    in 9..12 -> "Visão de cidade"
    in 13..16 -> "Visão de bairro e ruas"
    else -> "Visão de quarteirão e edifícios"
}

@Composable
private fun BlocoDeTipoDeMapa(
    selecionado: TipoMapa,
    onSelecionar: (TipoMapa) -> Unit
) {
    PicTravellyCartao(modifier = Modifier.fillMaxWidth()) {
        Column(modifier = Modifier.padding(16.dp)) {
            Text(
                text = "Tipo de mapa",
                style = MaterialTheme.typography.titleLarge,
                color = MaterialTheme.colorScheme.onSurface
            )
            Text(
                text = "Como o Google Maps será apresentado na tela de Mapa.",
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )

            Spacer(modifier = Modifier.height(12.dp))

            Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                TipoMapa.entries.chunked(2).forEach { linha ->
                    Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        linha.forEach { tipo ->
                            FilterChip(
                                selected = tipo == selecionado,
                                onClick = { onSelecionar(tipo) },
                                label = { Text(tipo.rotulo) },
                                modifier = Modifier.weight(1f)
                            )
                        }
                        // Mantem o alinhamento quando a ultima linha tem so um item.
                        if (linha.size == 1) Spacer(modifier = Modifier.weight(1f))
                    }
                }
            }
        }
    }
}

@Composable
private fun BlocoDeTransito(
    ligado: Boolean,
    onMudar: (Boolean) -> Unit
) {
    PicTravellyCartao(modifier = Modifier.fillMaxWidth()) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = "Camada de trânsito",
                    style = MaterialTheme.typography.titleLarge,
                    color = MaterialTheme.colorScheme.onSurface
                )
                Text(
                    text = "Exige conexão com a internet.",
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
            Switch(checked = ligado, onCheckedChange = onMudar)
        }
    }
}
