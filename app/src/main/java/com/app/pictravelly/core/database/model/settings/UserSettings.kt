package com.app.pictravelly.core.database.model.settings

data class UserSettings(
    val theme: AppTheme,
    val googleMapType: GoogleMapType,
    val mapEngine: MapEngineType,
    val lastZoom: Float = 13f
)
