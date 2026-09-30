package com.app.pictravelly.feature.home.components

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.app.pictravelly.core.design.components.PicTravellyText
import com.app.pictravelly.core.design.components.PicTravellyTitle
import com.app.pictravelly.core.design.theme.PicTravellyTheme


/**
 * Cabeçalho inicial com dados de boas-vindas e resumo.
 */
@Composable
fun HomeHeaderSection(
    totalSpots: Int, travelerLevel: String, modifier: Modifier = Modifier
) {
    Column(modifier = modifier) {
        PicTravellyTitle(
            text = "Bem vindo $travelerLevel!", modifier = Modifier.fillMaxWidth()
        )
        Spacer(modifier = Modifier.height(4.dp))
        PicTravellyText(
            text =
                if (totalSpots > 0)
                    "Você já registrou $totalSpots ${if (totalSpots == 1) "lugar inesquecível" else "lugares inesquecíveis"}."
                else
                    "Você ainda não registrou nenhum ponto turístico.",
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            isSmall = false
        )
    }
}


@Preview(showBackground = true)
@Composable
private fun HomeHeaderSectionPreview() {
    PicTravellyTheme {
        HomeHeaderSection(
            travelerLevel = "Explorador Iniciante", totalSpots = 15, modifier = Modifier
        )
    }
}