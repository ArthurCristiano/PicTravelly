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
 * Converte coordenadas geográficas em endereço textual usando a API do Android.
 */
object GeocodingHelper {

    private const val MAX_RESULTS = 1

    fun isAvailable(): Boolean = Geocoder.isPresent()

    /**
     * Retorna o endereço formatado ou null em caso de indisponibilidade ou falha.
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
            // No Android 13+ a consulta ao Geocoder é assíncrona via callback.
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

    // Formata o endereço como "Bairro, Cidade - UF", com fallback para getAddressLine(0).
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
