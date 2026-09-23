package com.app.pictravelly.core.map

/**
 * Motores de mapa suportados pela aplicação de forma modular.
 */
enum class MapEngineType(val label: String) {
    OSM("OpenStreetMap (Livre e Gratuito)"),
    GOOGLE_MAPS("Google Maps")
}
