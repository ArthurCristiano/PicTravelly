package com.app.pictravelly.core.design.componentes

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.unit.dp
import com.app.pictravelly.core.navegacao.Destino

/**
 * Rodape flutuante do app: duas abas, o botao "+" no centro e mais duas abas.
 *
 * O "+" nao e uma rota; ele dispara [onAdicionar], que abre o menu de cadastro.
 */
@Composable
fun PicTravellyBarraFlutuante(
    destinos: List<Destino>,
    rotaAtual: String,
    onNavegarPara: (Destino) -> Unit,
    onAdicionar: () -> Unit,
    modifier: Modifier = Modifier
) {
    val meio = destinos.size / 2
    val aEsquerda = destinos.take(meio)
    val aDireita = destinos.drop(meio)

    Surface(
        modifier = modifier
            // 1. PRIMEIRO repila o tamanho da barra do sistema operacional (botões ou gestos)
            .navigationBarsPadding()
            // 2. DEPOIS aplique o seu recuo estético para que a barra "flutue"
            .padding(horizontal = 16.dp, vertical = 16.dp)
            .fillMaxWidth()
            // 3. Troca de height fixo para heightIn (proteção de acessibilidade para textos grandes)
            .heightIn(min = 24.dp),
        shape = CircleShape,
        color = MaterialTheme.colorScheme.surface,
        shadowElevation = 8.dp
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 8.dp, vertical = 8.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            aEsquerda.forEach { destino ->
                ItemDaBarra(
                    destino = destino,
                    selecionado = rotaAtual == destino.rota,
                    onClick = { onNavegarPara(destino) }
                )
            }

            BotaoDeAdicionar(onClick = onAdicionar)

            aDireita.forEach { destino ->
                ItemDaBarra(
                    destino = destino,
                    selecionado = rotaAtual == destino.rota,
                    onClick = { onNavegarPara(destino) }
                )
            }
        }
    }
}

@Composable
private fun BotaoDeAdicionar(onClick: () -> Unit) {
    Box(
        modifier = Modifier
            .size(48.dp)
            .clip(CircleShape)
            .background(MaterialTheme.colorScheme.primary)
            .clickable(role = Role.Button, onClick = onClick),
        contentAlignment = Alignment.Center
    ) {
        Icon(
            imageVector = Icons.Default.Add,
            contentDescription = "Adicionar",
            tint = MaterialTheme.colorScheme.onPrimary,
            modifier = Modifier.size(28.dp)
        )
    }
}

@Composable
private fun ItemDaBarra(
    destino: Destino,
    selecionado: Boolean,
    onClick: () -> Unit
) {
    // Animação de transição de cor de fundo
    val corDeFundo by animateColorAsState(
        targetValue = if (selecionado) {
            MaterialTheme.colorScheme.primaryContainer
        } else {
            Color.Transparent
        },
        animationSpec = tween(durationMillis = 300),
        label = "cor_de_fundo"
    )

    // Animação de cor do ícone/texto
    val corDoConteudo by animateColorAsState(
        targetValue = if (selecionado) {
            MaterialTheme.colorScheme.onPrimaryContainer
        } else {
            MaterialTheme.colorScheme.onSurfaceVariant
        },
        animationSpec = tween(durationMillis = 300),
        label = "cor_do_conteudo"
    )

    Box(
        modifier = Modifier
            .clip(CircleShape)
            .background(corDeFundo)
            .clickable(
                interactionSource = remember { MutableInteractionSource() },
                indication = null, // Remove o ripple nativo redondo para não bugar a pílula
                onClick = onClick
            )
            .padding(horizontal = 12.dp, vertical = 12.dp)
    ) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.Center
        ) {
            Icon(
                imageVector = destino.icone,
                contentDescription = destino.titulo,
                tint = corDoConteudo,
                modifier = Modifier.size(24.dp)
            )
            AnimatedVisibility(visible = selecionado) {
                Text(
                    text = destino.titulo,
                    color = corDoConteudo,
                    style = MaterialTheme.typography.labelMedium,
                    modifier = Modifier.padding(start = 8.dp),
                    maxLines = 1
                )
            }
        }
    }
}
