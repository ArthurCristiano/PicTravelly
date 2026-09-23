package com.app.pictravelly.core.design.componentes

import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow

@Composable
fun PicTravellyTexto(
    text: String,
    modifier: Modifier = Modifier,
    // Cor neutra de alta legibilidade para corpos de texto
    color: Color = MaterialTheme.colorScheme.onSurface,
    textAlign: TextAlign? = null,
    maxLines: Int = Int.MAX_VALUE,
    // Permite trocar entre BodyLarge (descrições) e BodyMedium (datas, locais menores)
    pequeno: Boolean = false
) {
    Text(
        text = text,
        modifier = modifier,
        color = color,
        textAlign = textAlign,
        maxLines = maxLines,
        overflow = if (maxLines != Int.MAX_VALUE) TextOverflow.Ellipsis else TextOverflow.Clip,
        style = if (pequeno) MaterialTheme.typography.bodyMedium else MaterialTheme.typography.bodyLarge
    )
}