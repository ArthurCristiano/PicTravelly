package com.app.pictravelly.core.ui.components

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Luggage
import androidx.compose.material.icons.filled.Place
import androidx.compose.material3.Icon
import androidx.compose.material3.ListItem
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp


@Composable
fun AddEntrySheetContent(
    onNewTrip: () -> Unit,
    onNewSpot: () -> Unit
) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .navigationBarsPadding()
            .padding(bottom = 16.dp)
    ) {
        Text(
            text = "O que você quer registrar?",
            style = MaterialTheme.typography.titleMedium,
            fontWeight = FontWeight.Bold,
            color = MaterialTheme.colorScheme.onSurface,
            modifier = Modifier.padding(horizontal = 24.dp, vertical = 10.dp)
        )

        ListItem(
            headlineContent = { Text("Nova viagem") },
            supportingContent = { Text("Abre um capítulo novo no diário") },
            leadingContent = {
                Icon(
                    imageVector = Icons.Default.Luggage,
                    contentDescription = null,
                    tint = MaterialTheme.colorScheme.primary
                )
            },
            modifier = Modifier.clickable(onClick = onNewTrip)
        )

        ListItem(
            headlineContent = { Text("Novo ponto turístico") },
            supportingContent = { Text("Geocódigos, endereço, fotos e relato") },
            leadingContent = {
                Icon(
                    imageVector = Icons.Default.Place,
                    contentDescription = null,
                    tint = MaterialTheme.colorScheme.primary
                )
            },
            modifier = Modifier.clickable(onClick = onNewSpot)
        )
    }
}