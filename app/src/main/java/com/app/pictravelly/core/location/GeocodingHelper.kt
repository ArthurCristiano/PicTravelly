package com.app.pictravelly.core.location

import android.content.Context
import android.location.Address
import android.location.Geocoder
import android.os.Build
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.suspendCancellableCoroutine
import kotlinx.coroutines.withContext
import java.util.Locale
import kotlin.coroutines.resume

/**
 * Utilitário que converte geocódigos (latitude/longitude) em um endereço textual
 * usando a API de geocodificação reversa do Android.
 *
 * O endereço resolvido é gravado junto do ponto turístico, então continua
 * visível offline depois de consultado uma vez.
 */
object GeocodingHelper {

    private const val MAX_RESULTS = 1

    fun isAvailable(): Boolean = Geocoder.isPresent()

    /**
     * Devolve o endereço por extenso, ou null quando o serviço não está
     * disponível, não há conexão ou nada foi encontrado para as coordenadas.
     */
    suspend fun getAddressFromCoordinates(
        context: Context,
        latitude: Double,
        longitude: Double
    ): String? {
        if (!isAvailable()) return null

        val geocoder = Geocoder(context.applicationContext, Locale.forLanguageTag("pt-BR"))
        val addresses = runCatching { query(geocoder, latitude, longitude) }.getOrNull()
        return addresses?.firstOrNull()?.let(::format)
    }

    private suspend fun query(
        geocoder: Geocoder,
        latitude: Double,
        longitude: Double
    ): List<Address> {
        return if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            // A partir do Android 13 a consulta é assíncrona por callback.
            suspendCancellableCoroutine { continuation ->
                geocoder.getFromLocation(latitude, longitude, MAX_RESULTS) { result ->
                    if (continuation.isActive) continuation.resume(result)
                }
            }
        } else {
            withContext(Dispatchers.IO) {
                @Suppress("DEPRECATION")
                geocoder.getFromLocation(latitude, longitude, MAX_RESULTS).orEmpty()
            }
        }
    }

    /**
     * Monta um texto curto no formato "Bairro, Cidade - UF", que é o que o campo
     * "Cidade / Região" do formulário espera. Cai para a linha completa que a
     * API devolve quando esses campos vêm vazios.
     */
    private fun format(address: Address): String? {
        val neighborhood = address.subLocality?.takeIf { it.isNotBlank() }
        val city = (address.locality ?: address.subAdminArea)?.takeIf { it.isNotBlank() }
        val state = address.adminArea?.takeIf { it.isNotBlank() }

        val cityWithState = when {
            city != null && state != null -> "$city - $state"
            else -> city ?: state
        }

        val shortLabel = listOfNotNull(neighborhood, cityWithState).joinToString(", ")

        return shortLabel.takeIf { it.isNotBlank() }
            ?: address.getAddressLine(0)?.takeIf { it.isNotBlank() }
    }
}
