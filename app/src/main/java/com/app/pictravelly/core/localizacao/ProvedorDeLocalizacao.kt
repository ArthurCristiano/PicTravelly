package com.app.pictravelly.core.localizacao

import android.Manifest
import android.annotation.SuppressLint
import android.content.Context
import android.content.pm.PackageManager
import android.location.Location
import android.location.LocationListener
import android.location.LocationManager
import android.os.Bundle
import android.os.Looper
import androidx.core.content.ContextCompat
import kotlin.coroutines.resume
import kotlinx.coroutines.TimeoutCancellationException
import kotlinx.coroutines.suspendCancellableCoroutine
import kotlinx.coroutines.withTimeout

/** Par latitude/longitude simples, sem depender das classes do Maps. */
data class Coordenada(val latitude: Double, val longitude: Double)

/**
 * Le os geocodigos do aparelho pelo LocationManager do proprio Android,
 * sem depender do Google Play Services, para o app continuar offline.
 */
class ProvedorDeLocalizacao(private val context: Context) {

    private val locationManager: LocationManager
        get() = context.getSystemService(Context.LOCATION_SERVICE) as LocationManager

    fun temPermissao(): Boolean =
        PERMISSOES.any {
            ContextCompat.checkSelfPermission(context, it) == PackageManager.PERMISSION_GRANTED
        }

    /**
     * Devolve a posicao atual, ou null se nao houver permissao, provedor ligado
     * ou resposta dentro do tempo limite.
     */
    @SuppressLint("MissingPermission")
    suspend fun posicaoAtual(): Coordenada? {
        if (!temPermissao()) return null

        val provedores = PROVEDORES.filter { provedor ->
            runCatching { locationManager.isProviderEnabled(provedor) }.getOrDefault(false)
        }
        if (provedores.isEmpty()) return null

        // Primeiro a ultima posicao conhecida: e instantanea e serve para o
        // usuario ja ver algo enquanto o GPS ainda esta buscando sinal.
        ultimaConhecida(provedores)?.let { return it }

        return runCatching {
            withTimeout(TIMEOUT_MS) { proximaLeitura(provedores.first()) }
        }.recover { erro ->
            if (erro is TimeoutCancellationException) null else throw erro
        }.getOrNull()
    }

    @SuppressLint("MissingPermission")
    private fun ultimaConhecida(provedores: List<String>): Coordenada? =
        provedores
            .mapNotNull { runCatching { locationManager.getLastKnownLocation(it) }.getOrNull() }
            .maxByOrNull { it.time }
            ?.paraCoordenada()

    @SuppressLint("MissingPermission")
    private suspend fun proximaLeitura(provedor: String): Coordenada? =
        suspendCancellableCoroutine { continuacao ->
            val ouvinte = object : LocationListener {
                override fun onLocationChanged(location: Location) {
                    locationManager.removeUpdates(this)
                    if (continuacao.isActive) continuacao.resume(location.paraCoordenada())
                }

                @Deprecated("Exigido em APIs antigas do LocationListener")
                override fun onStatusChanged(
                    provider: String?,
                    status: Int,
                    extras: Bundle?
                ) = Unit

                override fun onProviderDisabled(provider: String) {
                    locationManager.removeUpdates(this)
                    if (continuacao.isActive) continuacao.resume(null)
                }
            }

            continuacao.invokeOnCancellation { locationManager.removeUpdates(ouvinte) }

            runCatching {
                locationManager.requestLocationUpdates(
                    provedor,
                    /* minTimeMs = */ 0L,
                    /* minDistanceM = */ 0f,
                    ouvinte,
                    Looper.getMainLooper()
                )
            }.onFailure {
                if (continuacao.isActive) continuacao.resume(null)
            }
        }

    private fun Location.paraCoordenada() = Coordenada(latitude, longitude)

    companion object {
        val PERMISSOES = arrayOf(
            Manifest.permission.ACCESS_FINE_LOCATION,
            Manifest.permission.ACCESS_COARSE_LOCATION
        )

        private val PROVEDORES = listOf(
            LocationManager.GPS_PROVIDER,
            LocationManager.NETWORK_PROVIDER
        )

        private const val TIMEOUT_MS = 15_000L
    }
}
