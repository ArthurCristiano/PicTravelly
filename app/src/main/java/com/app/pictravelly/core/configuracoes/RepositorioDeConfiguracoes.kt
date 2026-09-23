package com.app.pictravelly.core.configuracoes

import android.content.Context
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.booleanPreferencesKey
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.floatPreferencesKey
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

private val Context.dataStore: DataStore<Preferences> by preferencesDataStore(name = "pictravelly_config")

/**
 * Guarda as preferencias do app (zoom padrao, tipo de mapa e perfil) no
 * DataStore, o substituto recomendado do SharedPreferences.
 */
class RepositorioDeConfiguracoes(context: Context) {

    private val dataStore = context.applicationContext.dataStore

    val mapaConfig: Flow<ConfiguracaoDoMapa> = dataStore.data.map { prefs ->
        ConfiguracaoDoMapa(
            zoomPadrao = prefs[ZOOM] ?: ConfiguracaoDoMapa.ZOOM_PADRAO,
            tipoMapa = TipoMapa.porNome(prefs[TIPO_MAPA]),
            mostrarTransito = prefs[TRANSITO] ?: false
        )
    }

    val perfil: Flow<Perfil> = dataStore.data.map { prefs ->
        Perfil(
            nome = prefs[NOME] ?: "",
            bio = prefs[BIO] ?: "",
            caminhoAvatar = prefs[AVATAR]
        )
    }

    suspend fun definirZoomPadrao(zoom: Float) {
        dataStore.edit { prefs ->
            prefs[ZOOM] = zoom.coerceIn(ConfiguracaoDoMapa.ZOOM_MINIMO, ConfiguracaoDoMapa.ZOOM_MAXIMO)
        }
    }

    suspend fun definirTipoMapa(tipo: TipoMapa) {
        dataStore.edit { prefs -> prefs[TIPO_MAPA] = tipo.name }
    }

    suspend fun definirMostrarTransito(mostrar: Boolean) {
        dataStore.edit { prefs -> prefs[TRANSITO] = mostrar }
    }

    suspend fun salvarPerfil(perfil: Perfil) {
        dataStore.edit { prefs ->
            prefs[NOME] = perfil.nome
            prefs[BIO] = perfil.bio
            if (perfil.caminhoAvatar == null) prefs.remove(AVATAR)
            else prefs[AVATAR] = perfil.caminhoAvatar
        }
    }

    private companion object {
        val ZOOM = floatPreferencesKey("zoom_padrao")
        val TIPO_MAPA = stringPreferencesKey("tipo_mapa")
        val TRANSITO = booleanPreferencesKey("mostrar_transito")
        val NOME = stringPreferencesKey("perfil_nome")
        val BIO = stringPreferencesKey("perfil_bio")
        val AVATAR = stringPreferencesKey("perfil_avatar")
    }
}
