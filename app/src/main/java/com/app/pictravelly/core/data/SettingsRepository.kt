package com.app.pictravelly.core.data

import com.app.pictravelly.core.database.model.settings.AppLanguage
import com.app.pictravelly.core.database.model.settings.AppTheme
import com.app.pictravelly.core.database.model.settings.MapEngineType
import com.app.pictravelly.core.database.model.settings.UserSettings
import kotlinx.coroutines.flow.Flow

interface SettingsRepository {
    val userDataStream: Flow<UserSettings>
    suspend fun setTheme(theme: AppTheme)
    suspend fun setLanguage(language: AppLanguage)
    suspend fun setMapEngine(engine: MapEngineType)
}