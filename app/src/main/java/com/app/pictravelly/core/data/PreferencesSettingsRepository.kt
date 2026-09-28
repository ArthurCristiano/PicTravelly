package com.app.pictravelly.core.data

import android.util.Log
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.emptyPreferences
import androidx.datastore.preferences.core.floatPreferencesKey
import androidx.datastore.preferences.core.stringPreferencesKey
import com.app.pictravelly.core.database.model.settings.GoogleMapType
import com.app.pictravelly.core.database.model.settings.AppTheme
import com.app.pictravelly.core.database.model.settings.MapEngineType
import com.app.pictravelly.core.database.model.settings.UserSettings
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.map
import java.io.IOException

class PreferencesSettingsRepository(
    private val dataStore: DataStore<Preferences>
) : SettingsRepository {

    private object Keys {
        val THEME_PREFERENCE = stringPreferencesKey("theme_preference")
        val MAP_TYPE_PREFERENCE = stringPreferencesKey("map_type_preference")
        val MAP_ENGINE_PREFERENCE = stringPreferencesKey("map_engine_preference")
        val LAST_ZOOM_PREFERENCE = floatPreferencesKey("last_zoom_preference")
    }

    override val userDataStream: Flow<UserSettings> = dataStore.data
        .catch { exception ->
            if (exception is IOException) {
                Log.e("SettingsRepo", "Erro ao ler as preferências", exception)
                emit(emptyPreferences())
            } else {
                throw exception
            }
        }
        .map { preferences ->
            val theme = runCatching { AppTheme.valueOf(preferences[Keys.THEME_PREFERENCE] ?: "") }
                .getOrDefault(AppTheme.SYSTEM)

            val googleMapType =
                runCatching { GoogleMapType.valueOf(preferences[Keys.MAP_TYPE_PREFERENCE] ?: "") }
                    .getOrDefault(GoogleMapType.NORMAL)

            val mapEngine =
                runCatching { MapEngineType.valueOf(preferences[Keys.MAP_ENGINE_PREFERENCE] ?: "") }
                    .getOrDefault(MapEngineType.OSM)

            val lastZoom = preferences[Keys.LAST_ZOOM_PREFERENCE] ?: 13f

            UserSettings(theme, googleMapType, mapEngine, lastZoom)
        }

    override suspend fun setTheme(theme: AppTheme) {
        dataStore.edit { preferences ->
            preferences[Keys.THEME_PREFERENCE] = theme.name
        }
    }

    override suspend fun setMapType(googleMapType: GoogleMapType) {
        dataStore.edit { preferences ->
            preferences[Keys.MAP_TYPE_PREFERENCE] = googleMapType.name
        }
    }

    override suspend fun setMapEngine(engine: MapEngineType) {
        dataStore.edit { preferences ->
            preferences[Keys.MAP_ENGINE_PREFERENCE] = engine.name
        }
    }

    override suspend fun setLastZoom(zoom: Float) {
        dataStore.edit { preferences ->
            preferences[Keys.LAST_ZOOM_PREFERENCE] = zoom
        }
    }
}
