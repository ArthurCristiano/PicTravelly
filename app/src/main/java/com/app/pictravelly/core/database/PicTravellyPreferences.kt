package com.app.pictravelly.core.database

import android.content.Context
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.preferencesDataStore

private const val SETTINGS_PREFERENCE_NAME = "pictravelly_settings"

/**
 * Instância Singleton do Preferences DataStore.
 * ATENÇÃO: Esta extensão deve ser chamada APENAS pelo AppContainer.
 * Repositórios devem receber a instância pronta via construtor.
 */
val Context.dataStore: DataStore<Preferences> by preferencesDataStore(
    name = SETTINGS_PREFERENCE_NAME
)