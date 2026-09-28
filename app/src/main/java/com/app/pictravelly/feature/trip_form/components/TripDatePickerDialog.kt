package com.app.pictravelly.feature.trip_form.components

import androidx.compose.foundation.layout.Row
import androidx.compose.material3.DatePicker
import androidx.compose.material3.DatePickerDialog
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.rememberDatePickerState
import androidx.compose.runtime.Composable

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun TripDatePickerDialog(
    initialMillis: Long,
    allowClear: Boolean,
    onConfirm: (Long) -> Unit,
    onClear: () -> Unit,
    onDismiss: () -> Unit
) {
    val pickerState = rememberDatePickerState(initialSelectedDateMillis = initialMillis)

    DatePickerDialog(
        onDismissRequest = onDismiss,
        confirmButton = {
            TextButton(onClick = {
                pickerState.selectedDateMillis?.let { onConfirm(it) } ?: onDismiss()
            }) {
                Text("Confirmar")
            }
        },
        dismissButton = {
            Row {
                if (allowClear) {
                    TextButton(onClick = onClear) { Text("Em andamento") }
                }
                TextButton(onClick = onDismiss) { Text("Cancelar") }
            }
        }
    ) {
        DatePicker(state = pickerState)
    }
}