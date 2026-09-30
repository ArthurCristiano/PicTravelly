package com.app.pictravelly.core.location

import android.Manifest
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.content.pm.PackageManager
import android.location.LocationManager
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
 * Hook reativo que escuta as permissões e o rádio do GPS em tempo real.
 */
@Composable
fun rememberLocationStatus(): State<LocationStatus> {
    val context = LocalContext.current
    val locationManager = context.getSystemService(Context.LOCATION_SERVICE) as LocationManager

    var status by remember { mutableStateOf(LocationStatus.PERMISSION_DENIED) }

    val checkStatus = {
        val hasPermission = ContextCompat.checkSelfPermission(
            context,
            Manifest.permission.ACCESS_FINE_LOCATION
        ) == PackageManager.PERMISSION_GRANTED ||
                ContextCompat.checkSelfPermission(
                    context,
                    Manifest.permission.ACCESS_COARSE_LOCATION
                ) == PackageManager.PERMISSION_GRANTED

        val isGpsEnabled = locationManager.isProviderEnabled(LocationManager.GPS_PROVIDER) ||
                locationManager.isProviderEnabled(LocationManager.NETWORK_PROVIDER)

        status = when {
            !hasPermission -> LocationStatus.PERMISSION_DENIED
            !isGpsEnabled -> LocationStatus.GPS_DISABLED
            else -> LocationStatus.READY
        }
    }

    // Escuta liga/desliga do GPS diretamente da barra de tarefas do Android
    DisposableEffect(context) {
        checkStatus() // Verificação inicial
        val receiver = object : BroadcastReceiver() {
            override fun onReceive(context: Context?, intent: Intent?) {
                if (intent?.action == LocationManager.PROVIDERS_CHANGED_ACTION) {
                    checkStatus()
                }
            }
        }
        context.registerReceiver(receiver, IntentFilter(LocationManager.PROVIDERS_CHANGED_ACTION))
        onDispose { context.unregisterReceiver(receiver) }
    }

    return rememberUpdatedState(status)
}