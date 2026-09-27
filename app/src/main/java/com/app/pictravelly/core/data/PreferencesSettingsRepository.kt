package com.app.pictravelly.core.data

import android.util.Log
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.emptyPreferences
import androidx.datastore.preferences.core.stringPreferencesKey
import com.app.pictravelly.core.database.model.settings.AppLanguage
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
        val LANGUAGE_PREFERENCE = stringPreferencesKey("language_preference")
        val MAP_ENGINE_PREFERENCE = stringPreferencesKey("map_engine_preference")
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
            // Lê do disco em String, tenta converter para o Enum.
            // Se falhar (ex: chave não existe), aplica o valor padrão.
            val theme = runCatching { AppTheme.valueOf(preferences[Keys.THEME_PREFERENCE] ?: "") }
                .getOrDefault(AppTheme.SYSTEM)

            val language =
                runCatching { AppLanguage.valueOf(preferences[Keys.LANGUAGE_PREFERENCE] ?: "") }
                    .getOrDefault(AppLanguage.PT_BR)

            val mapEngine =
                runCatching { MapEngineType.valueOf(preferences[Keys.MAP_ENGINE_PREFERENCE] ?: "") }
                    .getOrDefault(MapEngineType.OSM) // O seu Enum padrão real

            UserSettings(theme, language, mapEngine)
        }

    override suspend fun setTheme(theme: AppTheme) {
        dataStore.edit { preferences ->
            preferences[Keys.THEME_PREFERENCE] = theme.name
        }
    }

    override suspend fun setLanguage(language: AppLanguage) {
        dataStore.edit { preferences ->
            preferences[Keys.LANGUAGE_PREFERENCE] = language.name
        }
    }

    override suspend fun setMapEngine(engine: MapEngineType) {
        dataStore.edit { preferences ->
            preferences[Keys.MAP_ENGINE_PREFERENCE] = engine.name
        }
    }
}