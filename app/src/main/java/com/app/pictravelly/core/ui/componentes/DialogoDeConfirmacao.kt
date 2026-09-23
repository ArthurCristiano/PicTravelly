package com.app.pictravelly.core.ui.componentes

import androidx.compose.material3.AlertDialog
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable

/** Dialogo padrao do app para acoes destrutivas (apagar viagem ou ponto). */
@Composable
fun DialogoDeConfirmacao(
    titulo: String,
    mensagem: String,
    textoConfirmar: String,
    onConfirmar: () -> Unit,
    onCancelar: () -> Unit,
    textoCancelar: String = "Cancelar"
) {
    AlertDialog(
        onDismissRequest = onCancelar,
        title = { Text(text = titulo) },
        text = { Text(text = mensagem) },
        confirmButton = {
            TextButton(onClick = onConfirmar) {
                Text(text = textoConfirmar, color = MaterialTheme.colorScheme.error)
            }
        },
        dismissButton = {
            TextButton(onClick = onCancelar) {
                Text(text = textoCancelar)
            }
        }
    )
}
