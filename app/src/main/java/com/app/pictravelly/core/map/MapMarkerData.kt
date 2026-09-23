package com.app.pictravelly.core.map

/**
 * Modelo agnóstico de dados para marcadores (pins) no mapa.
 */
data class MapMarkerData(
    val id: Long,
    val title: String,
    val snippet: String,
    val latitude: Double,
    val longitude: Double
)
