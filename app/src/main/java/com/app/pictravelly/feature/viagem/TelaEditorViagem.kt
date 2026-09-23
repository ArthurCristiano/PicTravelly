package com.app.pictravelly.feature.viagem

import androidx.activity.compose.BackHandler
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.imePadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.DateRange
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.DatePicker
import androidx.compose.material3.DatePickerDialog
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.material3.rememberDatePickerState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import com.app.pictravelly.core.design.componentes.PicTravellyBotao
import com.app.pictravelly.core.design.componentes.PicTravellyBotaoContornado
import com.app.pictravelly.core.design.componentes.PicTravellySeletorDeFoto
import com.app.pictravelly.core.di.ProvedorDeViewModels
import com.app.pictravelly.core.ui.util.Datas

/** Formulario de criacao e edicao de uma viagem do diario. */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun TelaEditorViagem(
    onConcluido: () -> Unit,
    modifier: Modifier = Modifier,
    viewModel: EditorViagemViewModel = viewModel(factory = ProvedorDeViewModels.Fabrica)
) {
    val estado by viewModel.estado.collectAsStateWithLifecycle()
    var seletorAberto by remember { mutableStateOf<CampoDeData?>(null) }

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
                title = { Text(if (estado.edicao) "Editar viagem" else "Nova viagem") },
                navigationIcon = {
                    IconButton(onClick = sair) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Voltar")
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.primaryContainer,
                    titleContentColor = MaterialTheme.colorScheme.onPrimaryContainer,
                    navigationIconContentColor = MaterialTheme.colorScheme.onPrimaryContainer
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
                caminhoImagem = estado.caminhoCapa,
                onFotoCapturada = viewModel::aoCapturarFoto,
                onFotoEscolhida = viewModel::aoEscolherFoto,
                onRemoverFoto = viewModel::aoRemoverFoto,
                textoVazio = "Escolha uma capa para a viagem"
            )

            OutlinedTextField(
                value = estado.titulo,
                onValueChange = viewModel::aoMudarTitulo,
                label = { Text("Título da viagem") },
                placeholder = { Text("Ex.: Férias no litoral do Paraná") },
                singleLine = true,
                isError = estado.titulo.isBlank(),
                supportingText = {
                    if (estado.titulo.isBlank()) Text("Informe um título")
                },
                modifier = Modifier.fillMaxWidth()
            )

            OutlinedTextField(
                value = estado.descricao,
                onValueChange = viewModel::aoMudarDescricao,
                label = { Text("Descrição") },
                placeholder = { Text("Como foi a viagem?") },
                minLines = 3,
                modifier = Modifier.fillMaxWidth()
            )

            Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                CampoDeDataBotao(
                    rotulo = "Início",
                    valor = Datas.formatar(estado.dataInicio),
                    onClick = { seletorAberto = CampoDeData.INICIO },
                    modifier = Modifier.weight(1f)
                )
                CampoDeDataBotao(
                    rotulo = "Fim",
                    valor = estado.dataFim?.let(Datas::formatar) ?: "Em andamento",
                    onClick = { seletorAberto = CampoDeData.FIM },
                    modifier = Modifier.weight(1f)
                )
            }

            val dataFim = estado.dataFim
            if (dataFim != null && dataFim < estado.dataInicio) {
                Text(
                    text = "A data de fim não pode ser anterior à de início.",
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.error
                )
            }

            estado.erro?.let { mensagem ->
                Text(
                    text = mensagem,
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.error
                )
            }

            PicTravellyBotao(
                text = if (estado.edicao) "Salvar alterações" else "Criar viagem",
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

    seletorAberto?.let { campo ->
        val valorInicial = when (campo) {
            CampoDeData.INICIO -> estado.dataInicio
            CampoDeData.FIM -> estado.dataFim ?: estado.dataInicio
        }
        SeletorDeData(
            valorInicial = valorInicial,
            permiteLimpar = campo == CampoDeData.FIM,
            onEscolher = { millis ->
                when (campo) {
                    CampoDeData.INICIO -> viewModel.aoMudarDataInicio(millis)
                    CampoDeData.FIM -> viewModel.aoMudarDataFim(millis)
                }
                seletorAberto = null
            },
            onLimpar = {
                viewModel.aoMudarDataFim(null)
                seletorAberto = null
            },
            onFechar = { seletorAberto = null }
        )
    }
}

private enum class CampoDeData { INICIO, FIM }

@Composable
private fun CampoDeDataBotao(
    rotulo: String,
    valor: String,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    Column(modifier = modifier) {
        Text(
            text = rotulo,
            style = MaterialTheme.typography.labelMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
        PicTravellyBotaoContornado(
            text = valor,
            onClick = onClick,
            modifier = Modifier.fillMaxWidth()
        )
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun SeletorDeData(
    valorInicial: Long,
    permiteLimpar: Boolean,
    onEscolher: (Long) -> Unit,
    onLimpar: () -> Unit,
    onFechar: () -> Unit
) {
    val estadoDoSeletor = rememberDatePickerState(
        initialSelectedDateMillis = Datas.paraSelecaoUtc(valorInicial)
    )

    DatePickerDialog(
        onDismissRequest = onFechar,
        confirmButton = {
            TextButton(
                onClick = {
                    val escolhida = estadoDoSeletor.selectedDateMillis
                    if (escolhida == null) onFechar() else onEscolher(Datas.deSelecaoUtc(escolhida))
                }
            ) {
                Text("Confirmar")
            }
        },
        dismissButton = {
            Row {
                if (permiteLimpar) {
                    TextButton(onClick = onLimpar) { Text("Em andamento") }
                }
                TextButton(onClick = onFechar) { Text("Cancelar") }
            }
        }
    ) {
        DatePicker(
            state = estadoDoSeletor,
            title = {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    modifier = Modifier.padding(start = 24.dp, top = 16.dp)
                ) {
                    Icon(Icons.Default.DateRange, contentDescription = null)
                    Text(text = "Escolha a data", modifier = Modifier.padding(start = 8.dp))
                }
            }
        )
    }
}
