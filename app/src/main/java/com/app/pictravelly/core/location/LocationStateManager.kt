package com.app.pictravelly.core.location

import android.Manifest
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.content.pm.PackageManager
import android.location.LocationManager
import android.os.Build
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.State
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberUpdatedState
import androidx.compose.runtime.setValue
import androidx.compose.ui.platform.LocalContext
import androidx.core.content.ContextCompat
import com.app.pictravelly.core.location.model.LocationStatus


/**
 * Monitora o estado de permissão e disponibilidade do provedor de localização.
 */
@Composable
fun rememberLocationStatus(): State<LocationStatus> {
    val context = LocalContext.current
    val locationManager = remember(context) {
        runCatching {
            context.getSystemService(Context.LOCATION_SERVICE) as? LocationManager
        }.getOrNull()
    }

    var status by remember { mutableStateOf(LocationStatus.PERMISSION_DENIED) }

    val checkStatus = {
        val hasPermission = runCatching {
            ContextCompat.checkSelfPermission(
                context,
                Manifest.permission.ACCESS_FINE_LOCATION
            ) == PackageManager.PERMISSION_GRANTED ||
                    ContextCompat.checkSelfPermission(
                        context,
                        Manifest.permission.ACCESS_COARSE_LOCATION
                    ) == PackageManager.PERMISSION_GRANTED
        }.getOrDefault(false)

        val isGpsEnabled = if (!hasPermission) {
            false
        } else {
            runCatching {
                locationManager?.let { lm ->
                    val isGps = runCatching { lm.isProviderEnabled(LocationManager.GPS_PROVIDER) }.getOrDefault(false)
                    val isNetwork = runCatching { lm.isProviderEnabled(LocationManager.NETWORK_PROVIDER) }.getOrDefault(false)
                    isGps || isNetwork
                } ?: false
            }.getOrDefault(false)
        }

        status = when {
            !hasPermission -> LocationStatus.PERMISSION_DENIED
            !isGpsEnabled -> LocationStatus.GPS_DISABLED
            else -> LocationStatus.READY
        }
    }

    val lifecycleOwner = androidx.lifecycle.compose.LocalLifecycleOwner.current
    DisposableEffect(lifecycleOwner) {
        val observer = androidx.lifecycle.LifecycleEventObserver { _, event ->
            if (event == androidx.lifecycle.Lifecycle.Event.ON_RESUME) {
                checkStatus()
            }
        }
        lifecycleOwner.lifecycle.addObserver(observer)
        onDispose {
            lifecycleOwner.lifecycle.removeObserver(observer)
        }
    }

    // Escuta mudanças de estado dos provedores de localização do sistema.
    DisposableEffect(context) {
        checkStatus()
        val receiver = object : BroadcastReceiver() {
            override fun onReceive(context: Context?, intent: Intent?) {
                if (intent?.action == LocationManager.PROVIDERS_CHANGED_ACTION) {
                    checkStatus()
                }
            }
        }
        val filter = IntentFilter(LocationManager.PROVIDERS_CHANGED_ACTION)
        val appContext = context.applicationContext
        runCatching {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.UPSIDE_DOWN_CAKE) {
                ContextCompat.registerReceiver(
                    appContext,
                    receiver,
                    filter,
                    ContextCompat.RECEIVER_EXPORTED
                )
            } else if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
                ContextCompat.registerReceiver(
                    appContext,
                    receiver,
                    filter,
                    ContextCompat.RECEIVER_NOT_EXPORTED
                )
            } else {
                appContext.registerReceiver(receiver, filter)
            }
        }
        onDispose {
            runCatching {
                appContext.unregisterReceiver(receiver)
            }
        }
    }

    return rememberUpdatedState(status)
}