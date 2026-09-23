package com.app.pictravelly.feature.viagem

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
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.MyLocation
import androidx.compose.material.icons.filled.Place
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ExtendedFloatingActionButton
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
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
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import coil.compose.AsyncImage
import com.app.pictravelly.core.design.componentes.PicTravellyCartao
import com.app.pictravelly.core.design.componentes.PicTravellyTexto
import com.app.pictravelly.core.di.ProvedorDeViewModels
import com.app.pictravelly.core.ui.componentes.DialogoDeConfirmacao
import com.app.pictravelly.core.ui.util.Datas
import com.app.pictravelly.core.ui.util.Geocodigos
import com.app.pictravelly.data.local.entidade.Ponto
import java.io.File

/**
 * Detalhe de uma viagem: as fotos, descricoes e enderecos dos pontos
 * turisticos cadastrados nela.
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun TelaDetalheViagem(
    onVoltar: () -> Unit,
    onEditarViagem: (Long) -> Unit,
    onAdicionarPonto: (Long) -> Unit,
    onEditarPonto: (Long, Long) -> Unit,
    onVerNoMapa: (Long) -> Unit,
    modifier: Modifier = Modifier,
    viewModel: DetalheViagemViewModel = viewModel(factory = ProvedorDeViewModels.Fabrica)
) {
    val estado by viewModel.estado.collectAsStateWithLifecycle()
    var confirmarExclusaoViagem by remember { mutableStateOf(false) }
    var pontoParaApagar by remember { mutableStateOf<Ponto?>(null) }

    // A viagem some da base quando e apagada; ai a tela se fecha sozinha.
    LaunchedEffect(estado.carregando, estado.viagem) {
        if (!estado.carregando && estado.viagem == null) onVoltar()
    }

    val viagem = estado.viagem?.viagem
    val pontos = estado.viagem?.pontos.orEmpty()

    Scaffold(
        modifier = modifier,
        topBar = {
            TopAppBar(
                title = { Text(text = viagem?.titulo ?: "Viagem", maxLines = 1) },
                navigationIcon = {
                    IconButton(onClick = onVoltar) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Voltar")
                    }
                },
                actions = {
                    if (viagem != null) {
                        IconButton(onClick = { onEditarViagem(viagem.id) }) {
                            Icon(Icons.Default.Edit, contentDescription = "Editar viagem")
                        }
                        IconButton(onClick = { confirmarExclusaoViagem = true }) {
                            Icon(
                                Icons.Default.Delete,
                                contentDescription = "Apagar viagem",
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
        },
        floatingActionButton = {
            if (viagem != null) {
                ExtendedFloatingActionButton(
                    onClick = { onAdicionarPonto(viagem.id) },
                    icon = { Icon(Icons.Default.Add, contentDescription = null) },
                    text = { Text("Ponto turístico") }
                )
            }
        }
    ) { innerPadding ->
        when {
            estado.carregando -> Box(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(innerPadding),
                contentAlignment = Alignment.Center
            ) {
                CircularProgressIndicator()
            }

            viagem != null -> LazyColumn(
                modifier = Modifier.fillMaxSize(),
                contentPadding = PaddingValues(
                    top = innerPadding.calculateTopPadding(),
                    bottom = innerPadding.calculateBottomPadding() + 96.dp
                ),
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                item {
                    CabecalhoDaViagem(
                        capa = viagem.caminhoCapa
                            ?: pontos.firstNotNullOfOrNull { it.caminhoImagem },
                        periodo = Datas.intervalo(viagem.dataInicio, viagem.dataFim),
                        descricao = viagem.descricao
                    )
                }

                item {
                    HorizontalDivider(
                        modifier = Modifier.padding(horizontal = 16.dp),
                        color = MaterialTheme.colorScheme.outlineVariant
                    )
                }

                if (pontos.isEmpty()) {
                    item { SemPontos() }
                } else {
                    item {
                        Text(
                            text = "Pontos turísticos",
                            style = MaterialTheme.typography.titleLarge,
                            color = MaterialTheme.colorScheme.onBackground,
                            modifier = Modifier.padding(horizontal = 16.dp)
                        )
                    }

                    items(items = pontos, key = { it.id }) { ponto ->
                        CartaoDePonto(
                            ponto = ponto,
                            onEditar = { onEditarPonto(viagem.id, ponto.id) },
                            onApagar = { pontoParaApagar = ponto },
                            onVerNoMapa = { onVerNoMapa(ponto.id) },
                            modifier = Modifier.padding(horizontal = 16.dp)
                        )
                    }
                }
            }
        }
    }

    if (confirmarExclusaoViagem && viagem != null) {
        DialogoDeConfirmacao(
            titulo = "Apagar viagem?",
            mensagem = "A viagem \"" + viagem.titulo + "\" e os seus " + pontos.size +
                " ponto(s) turístico(s) serão apagados do diário. Não dá para desfazer.",
            textoConfirmar = "Apagar",
            onConfirmar = {
                confirmarExclusaoViagem = false
                viewModel.apagarViagem()
            },
            onCancelar = { confirmarExclusaoViagem = false }
        )
    }

    pontoParaApagar?.let { ponto ->
        DialogoDeConfirmacao(
            titulo = "Apagar ponto?",
            mensagem = "O ponto \"" + ponto.nome + "\" será removido desta viagem.",
            textoConfirmar = "Apagar",
            onConfirmar = {
                pontoParaApagar = null
                viewModel.apagarPonto(ponto)
            },
            onCancelar = { pontoParaApagar = null }
        )
    }
}

@Composable
private fun CabecalhoDaViagem(
    capa: String?,
    periodo: String,
    descricao: String
) {
    Column {
        if (capa != null) {
            AsyncImage(
                model = File(capa),
                contentDescription = "Capa da viagem",
                contentScale = ContentScale.Crop,
                modifier = Modifier
                    .fillMaxWidth()
                    .height(220.dp)
            )
        }

        Column(modifier = Modifier.padding(horizontal = 16.dp, vertical = 16.dp)) {
            Text(
                text = periodo,
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.primary
            )

            if (descricao.isNotBlank()) {
                Spacer(modifier = Modifier.height(8.dp))
                PicTravellyTexto(
                    text = descricao,
                    color = MaterialTheme.colorScheme.onBackground
                )
            }
        }
    }
}

@Composable
private fun CartaoDePonto(
    ponto: Ponto,
    onEditar: () -> Unit,
    onApagar: () -> Unit,
    onVerNoMapa: () -> Unit,
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
                    .height(200.dp)
            )
        }

        Column(modifier = Modifier.padding(16.dp)) {
            Text(
                text = ponto.nome,
                style = MaterialTheme.typography.titleLarge,
                color = MaterialTheme.colorScheme.onSurface
            )

            Spacer(modifier = Modifier.height(6.dp))

            // Endereco textual obtido pela geocodificacao reversa dos geocodigos.
            LinhaDeLocal(
                icone = Icons.Default.Place,
                texto = ponto.endereco ?: "Endereço não disponível"
            )

            Spacer(modifier = Modifier.height(2.dp))

            LinhaDeLocal(
                icone = Icons.Default.MyLocation,
                texto = Geocodigos.formatar(ponto.latitude, ponto.longitude)
            )

            if (ponto.descricao.isNotBlank()) {
                Spacer(modifier = Modifier.height(10.dp))
                PicTravellyTexto(
                    text = ponto.descricao,
                    color = MaterialTheme.colorScheme.onSurface,
                    pequeno = true
                )
            }

            Spacer(modifier = Modifier.height(4.dp))

            Row(horizontalArrangement = Arrangement.spacedBy(4.dp)) {
                IconButton(onClick = onVerNoMapa) {
                    Icon(
                        Icons.Default.Place,
                        contentDescription = "Ver no mapa",
                        tint = MaterialTheme.colorScheme.primary
                    )
                }
                IconButton(onClick = onEditar) {
                    Icon(
                        Icons.Default.Edit,
                        contentDescription = "Editar ponto",
                        tint = MaterialTheme.colorScheme.primary
                    )
                }
                IconButton(onClick = onApagar) {
                    Icon(
                        Icons.Default.Delete,
                        contentDescription = "Apagar ponto",
                        tint = MaterialTheme.colorScheme.error
                    )
                }
            }
        }
    }
}

@Composable
private fun LinhaDeLocal(
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
private fun SemPontos() {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 32.dp, vertical = 24.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Icon(
            imageVector = Icons.Default.Place,
            contentDescription = null,
            tint = MaterialTheme.colorScheme.primary,
            modifier = Modifier.size(48.dp)
        )
        Spacer(modifier = Modifier.height(12.dp))
        Text(
            text = "Nenhum ponto turístico cadastrado nesta viagem ainda.",
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            textAlign = TextAlign.Center
        )
    }
}
