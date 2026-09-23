package com.app.pictravelly.core.localizacao

import android.content.Context
import android.location.Address
import android.location.Geocoder
import android.os.Build
import java.util.Locale
import kotlin.coroutines.resume
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.suspendCancellableCoroutine
import kotlinx.coroutines.withContext

/**
 * Converte geocodigos (latitude/longitude) em um endereco textual usando a API
 * de geocodificacao reversa do Android.
 *
 * O resultado e gravado junto do ponto no Room, entao o endereco continua
 * disponivel offline depois de resolvido uma vez.
 */
class ServicoDeGeocodificacao(context: Context) {

    private val geocoder = Geocoder(context.applicationContext, Locale.getDefault())

    val disponivel: Boolean get() = Geocoder.isPresent()

    suspend fun enderecoDe(latitude: Double, longitude: Double): String? {
        if (!disponivel) return null
        val enderecos = runCatching { consultar(latitude, longitude) }.getOrNull()
        return enderecos?.firstOrNull()?.let(::formatar)
    }

    private suspend fun consultar(latitude: Double, longitude: Double): List<Address> =
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            // A partir do Android 13 a consulta e assincrona por callback.
            suspendCancellableCoroutine { continuacao ->
                geocoder.getFromLocation(latitude, longitude, MAX_RESULTADOS) { resultado ->
                    if (continuacao.isActive) continuacao.resume(resultado)
                }
            }
        } else {
            withContext(Dispatchers.IO) {
                @Suppress("DEPRECATION")
                geocoder.getFromLocation(latitude, longitude, MAX_RESULTADOS).orEmpty()
            }
        }

    /**
     * Prefere a linha de endereco pronta que a API devolve; se ela vier vazia,
     * monta uma versao curta com rua, bairro, cidade e estado.
     */
    private fun formatar(endereco: Address): String? {
        endereco.getAddressLine(0)?.takeIf { it.isNotBlank() }?.let { return it }

        val partes = listOfNotNull(
            endereco.thoroughfare,
            endereco.subLocality,
            endereco.locality ?: endereco.subAdminArea,
            endereco.adminArea,
            endereco.countryName
        ).filter { it.isNotBlank() }

        return partes.joinToString(", ").takeIf { it.isNotBlank() }
    }

    private companion object {
        const val MAX_RESULTADOS = 1
    }
}
