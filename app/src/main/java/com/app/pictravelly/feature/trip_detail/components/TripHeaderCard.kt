package com.app.pictravelly.feature.trip_detail.components

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CalendarToday
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.unit.dp
import coil.compose.AsyncImage
import com.app.pictravelly.core.design.components.PicTravellyCard
import com.app.pictravelly.core.design.components.PicTravellyText
import com.app.pictravelly.feature.trip_detail.TripDetailHeaderUiModel


@Composable
fun TripHeaderCard(
    header: TripDetailHeaderUiModel,
    spotsCount: Int
) {
    PicTravellyCard(
        modifier = Modifier.fillMaxWidth(),
        containerColor = MaterialTheme.colorScheme.surface
    ) {
        Column {
            header.coverImageUri?.let { uri ->
                AsyncImage(
                    model = uri,
                    contentDescription = "Capa da viagem",
                    modifier = Modifier
                        .fillMaxWidth()
                        .aspectRatio(16f / 9f),
                    contentScale = ContentScale.Crop
                )
            }

            Column(modifier = Modifier.padding(16.dp)) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(
                        Icons.Default.CalendarToday,
                        contentDescription = null,
                        modifier = Modifier.size(14.dp),
                        tint = MaterialTheme.colorScheme.primary
                    )
                    Spacer(modifier = Modifier.width(6.dp))
                    Text(
                        text = header.formattedPeriod,
                        style = MaterialTheme.typography.labelMedium,
                        color = MaterialTheme.colorScheme.primary
                    )
                }

                Spacer(modifier = Modifier.height(8.dp))

                Text(
                    text = if (spotsCount == 1) "1 ponto turístico registrado" else "$spotsCount pontos turísticos registrados",
                    style = MaterialTheme.typography.labelMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )

                if (header.description.isNotBlank()) {
                    Spacer(modifier = Modifier.height(10.dp))
                    PicTravellyText(
                        text = header.description,
                        color = MaterialTheme.colorScheme.onSurface
                    )
                }
            }
        }
    }
}
