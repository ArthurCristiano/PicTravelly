package com.app.pictravelly.core.data

import com.app.pictravelly.core.database.model.settings.GoogleMapType
import com.app.pictravelly.core.database.model.settings.AppTheme
import com.app.pictravelly.core.database.model.settings.MapEngineType
import com.app.pictravelly.core.database.model.settings.UserSettings
import kotlinx.coroutines.flow.Flow

interface SettingsRepository {
    val userDataStream: Flow<UserSettings>
    suspend fun setTheme(theme: AppTheme)
    suspend fun setMapType(googleMapType: GoogleMapType)
    suspend fun setMapEngine(engine: MapEngineType)
    suspend fun setLastZoom(zoom: Float)
}