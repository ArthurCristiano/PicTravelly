package com.app.pictravelly.core.location

import android.Manifest
import android.annotation.SuppressLint
import android.content.Context
import android.content.pm.PackageManager
import android.location.Location
import android.location.LocationManager
import androidx.core.content.ContextCompat
import com.google.android.gms.location.LocationServices
import kotlinx.coroutines.suspendCancellableCoroutine
import kotlin.coroutines.resume

/**
 * Utilitário para obtenção da localização atual do dispositivo.
 */
object LocationHelper {

    // Coordenadas padrão de fallback quando o GPS ou permissão estiverem indisponíveis.
    const val DEFAULT_LATITUDE = -26.227568
    const val DEFAULT_LONGITUDE = -52.671660

    fun hasLocationPermission(context: Context): Boolean {
        val finePermission = ContextCompat.checkSelfPermission(
            context,
            Manifest.permission.ACCESS_FINE_LOCATION
        ) == PackageManager.PERMISSION_GRANTED

        val coarsePermission = ContextCompat.checkSelfPermission(
            context,
            Manifest.permission.ACCESS_COARSE_LOCATION
        ) == PackageManager.PERMISSION_GRANTED

        return finePermission || coarsePermission
    }

    @SuppressLint("MissingPermission")
    suspend fun getCurrentLocation(context: Context): Pair<Double, Double> {
        if (!hasLocationPermission(context)) {
            return Pair(DEFAULT_LATITUDE, DEFAULT_LONGITUDE)
        }

        return try {
            val fusedClient = LocationServices.getFusedLocationProviderClient(context)
            suspendCancellableCoroutine { continuation ->
                try {
                    fusedClient.lastLocation
                        .addOnSuccessListener { location: Location? ->
                            if (continuation.isActive) {
                                if (location != null) {
                                    continuation.resume(Pair(location.latitude, location.longitude))
                                } else {
                                    // Fallback para LocationManager nativo
                                    val fallback = getNativeLastLocation(context)
                                    continuation.resume(fallback)
                                }
                            }
                        }
                        .addOnFailureListener {
                            if (continuation.isActive) {
                                val fallback = getNativeLastLocation(context)
                                continuation.resume(fallback)
                            }
                        }
                } catch (e: Exception) {
                    if (continuation.isActive) {
                        continuation.resume(Pair(DEFAULT_LATITUDE, DEFAULT_LONGITUDE))
                    }
                }
            }
        } catch (e: Exception) {
            getNativeLastLocation(context)
        }
    }

    @SuppressLint("MissingPermission")
    private fun getNativeLastLocation(context: Context): Pair<Double, Double> {
        if (!hasLocationPermission(context)) {
            return Pair(DEFAULT_LATITUDE, DEFAULT_LONGITUDE)
        }
        return try {
            val locationManager = context.getSystemService(Context.LOCATION_SERVICE) as? LocationManager
            val providers = try {
                locationManager?.getProviders(true) ?: emptyList()
            } catch (e: Exception) {
                emptyList()
            }
            var bestLocation: Location? = null

            for (provider in providers) {
                val l = try {
                    locationManager?.getLastKnownLocation(provider)
                } catch (e: Exception) {
                    null
                } ?: continue

                if (bestLocation == null || l.accuracy < bestLocation.accuracy) {
                    bestLocation = l
                }
            }

            if (bestLocation != null) {
                Pair(bestLocation.latitude, bestLocation.longitude)
            } else {
                Pair(DEFAULT_LATITUDE, DEFAULT_LONGITUDE)
            }
        } catch (e: Exception) {
            Pair(DEFAULT_LATITUDE, DEFAULT_LONGITUDE)
        }
    }
}
