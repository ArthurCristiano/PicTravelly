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

@Composable
fun HomeHeaderSection(
    modifier: Modifier = Modifier
) {
    Column(modifier = modifier) {
        PicTravellyTitle(
            text = "PicTravelly",
            modifier = Modifier.fillMaxWidth()
        )
        Spacer(modifier = Modifier.height(2.dp))
        PicTravellyText(
            text = "Diário fotográfico de viagens",
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            isSmall = true
        )
    }
}

@Preview(showBackground = true)
@Composable
private fun HomeHeaderSectionPreview() {
    PicTravellyTheme {
        HomeHeaderSection()
    }
}