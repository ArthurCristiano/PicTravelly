package com.app.pictravelly.core.design

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.app.pictravelly.core.design.componentes.PicTravellyBarraFlutuante
import com.app.pictravelly.core.navegacao.Destino

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun TelaShowroom() {
    var rotaAtual by remember { mutableStateOf("home") }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("PicTravelly - Design System") },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.primaryContainer,
                    titleContentColor = MaterialTheme.colorScheme.onPrimaryContainer
                )
            )
        },
        bottomBar = {
            PicTravellyBarraFlutuante(
                destinos = Destino.entries,
                rotaAtual = rotaAtual,
                onNavegarPara = { rotaAtual = it.rota },
                onAdicionar = { }
            )
        }
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .background(MaterialTheme.colorScheme.background)
                // 1. Aplica APENAS o recuo da TopAppBar na tela inteira
                .padding(top = paddingValues.calculateTopPadding())
                // 2. Permite a rolagem
                .verticalScroll(rememberScrollState())
                // 3. Aplica o recuo da BottomBar como um "respiro" no final do conteúdo.
                // Isso faz com que o texto passe atrás da barra flutuante quando está rolando,
                // mas quando chega no final da lista, ele para em um espaço seguro acima da barra.
                .padding(
                    bottom = paddingValues.calculateBottomPadding() + 16.dp,
                    start = 16.dp,
                    end = 16.dp
                ),
            verticalArrangement = Arrangement.spacedBy(24.dp)
        ) {
            // Seção de Tipografia
            TituloDeSecao("Tipografia")
            Text(
                "Headline Large",
                style = MaterialTheme.typography.headlineLarge,
                color = MaterialTheme.colorScheme.onBackground
            )
            Text(
                "Title Large",
                style = MaterialTheme.typography.titleLarge,
                color = MaterialTheme.colorScheme.onBackground
            )
            Text(
                "Body Large (Texto de descrição de diário que o usuário vai ler e deve ser muito legível)",
                style = MaterialTheme.typography.bodyLarge,
                color = MaterialTheme.colorScheme.onBackground
            )

            HorizontalDivider(color = MaterialTheme.colorScheme.outlineVariant)

            // Seção de Botões
            TituloDeSecao("Ações (Botões)")
            Button(
                onClick = { },
                modifier = Modifier.fillMaxWidth()
            ) {
                Text("Botão Primário (Salvar Local)")
            }



            OutlinedButton(
                onClick = { },
                modifier = Modifier.fillMaxWidth()
            ) {
                Text("Botão Outline (Cancelar)")
            }

            HorizontalDivider(color = MaterialTheme.colorScheme.outlineVariant)

            // Seção de Cards
            TituloDeSecao("Superfícies e Cards")
            Card(
                modifier = Modifier.fillMaxWidth(),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                elevation = CardDefaults.cardElevation(defaultElevation = 4.dp),
                shape = RoundedCornerShape(12.dp)
            ) {
                Column(modifier = Modifier.padding(16.dp))
                {
                    Text("Cataratas do Iguaçu", style = MaterialTheme.typography.titleLarge)
                    Spacer(modifier = Modifier.height(8.dp))
                    Text(
                        "Um dos lugares mais incríveis. Muita água e natureza exuberante.",
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }

            // Exemplo de componente de Erro
            Card(
                modifier = Modifier.fillMaxWidth(),
                colors = CardDefaults.cardColors(
                    containerColor = MaterialTheme.colorScheme.errorContainer,
                    contentColor = MaterialTheme.colorScheme.onErrorContainer
                )
            ) {
                Text(
                    text = "Erro: Sem permissão de GPS.",
                    modifier = Modifier.padding(16.dp),
                    style = MaterialTheme.typography.bodyMedium
                )
            }

        }


    }
}

@Composable
fun TituloDeSecao(title: String) {
    Text(
        text = title,
        style = MaterialTheme.typography.labelLarge,
        color = MaterialTheme.colorScheme.primary,
        modifier = Modifier.padding(top = 8.dp, bottom = 4.dp)
    )
}