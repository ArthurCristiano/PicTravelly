package com.app.pictravelly.core.ui.util

import java.util.Locale

/** Apresentacao dos geocodigos crus para o usuario. */
object Geocodigos {

    fun formatar(latitude: Double, longitude: Double): String =
        String.format(Locale.US, "%.5f, %.5f", latitude, longitude)

    /** Aceita tanto "-25,4284" quanto "-25.4284" ao ler o que foi digitado. */
    fun interpretar(texto: String): Double? =
        texto.trim().replace(',', '.').toDoubleOrNull()

    fun latitudeValida(valor: Double) = valor in -90.0..90.0

    fun longitudeValida(valor: Double) = valor in -180.0..180.0
}
