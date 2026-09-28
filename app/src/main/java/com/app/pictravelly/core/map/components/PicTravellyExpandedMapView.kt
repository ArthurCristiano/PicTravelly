package com.app.pictravelly.core.map.components

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxScope
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Close
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import com.app.pictravelly.core.database.model.settings.MapEngineType
import com.app.pictravelly.core.map.MapMarkerData
import com.app.pictravelly.core.map.PicTravellyMap

@Composable
fun PicTravellyExpandedMapView(
    latitude: Double,
    longitude: Double,
    engine: MapEngineType,
    zoom: Float, // <--- Este zoom vem atualizado da ViewModel via DataStore
    markers: List<MapMarkerData>,
    onClose: () -> Unit,
    onMarkerClick: (MapMarkerData) -> Unit,
    onZoomChange: (Float) -> Unit, // <--- Função da ViewModel que salva no DataStore
    modifier: Modifier = Modifier,
    showCloseButton: Boolean = true,
    topContent: @Composable (BoxScope.() -> Unit)? = null,
    bottomContent: @Composable (BoxScope.() -> Unit)? = null
) {
    // Inicializa o zoom efêmero com o valor vindo do banco/ViewModel
    var ephemeralZoom by remember(zoom) { mutableFloatStateOf(zoom) }

    // Salva no DataStore EXATAMENTE quando o componente for destruído (ao fechar o mapa)
    DisposableEffect(Unit) {
        onDispose {
            // Salva apenas se o usuário realmente alterou o zoom em relação ao original
            if (ephemeralZoom != zoom) {
                onZoomChange(ephemeralZoom)
            }
        }
    }

    Box(
        modifier = modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background)
    ) {
        PicTravellyMap(
            latitude = latitude,
            longitude = longitude,
            zoom = ephemeralZoom,
            engine = engine,
            markers = markers,
            onMarkerClick = onMarkerClick,
            onZoomChange = { newZoom ->
                // Atualiza o zoom na memória enquanto o usuário usa a pinça ou os botões + / -
                ephemeralZoom = newZoom
            },
            modifier = Modifier.fillMaxSize(),
            isInteractive = true
        )

        topContent?.let { it() }

        if (showCloseButton) {
            Box(
                modifier = Modifier
                    .align(Alignment.TopEnd)
                    .statusBarsPadding()
                    .padding(16.dp)
                    .size(48.dp)
                    .clip(CircleShape)
                    .background(MaterialTheme.colorScheme.surface.copy(alpha = 0.90f))
                    .border(1.dp, Color.White.copy(alpha = 0.3f), CircleShape)
                    .clickable(onClick = {
                        // Força o salvamento imediato antes de chamar o fechamento da tela
                        if (ephemeralZoom != zoom) {
                            onZoomChange(ephemeralZoom)
                        }
                        onClose()
                    }),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = Icons.Default.Close,
                    contentDescription = "Recolher Mapa",
                    tint = MaterialTheme.colorScheme.onSurface
                )
            }
        }

        bottomContent?.let { it() }
    }
}