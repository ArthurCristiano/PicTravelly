package com.app.pictravelly

import android.content.Intent
import android.net.Uri
import android.os.Build
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.viewModels
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.core.splashscreen.SplashScreen.Companion.installSplashScreen
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.repeatOnLifecycle
import com.app.pictravelly.core.di.AppViewModelProvider
import com.app.pictravelly.core.ui.PicTravellyRoot
import com.app.pictravelly.core.utils.ImageStorageManager
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

class MainActivity : ComponentActivity() {
    private val viewModel: MainActivityViewModel by viewModels { AppViewModelProvider.Factory }

    override fun onCreate(savedInstanceState: Bundle?) {
        val splashScreen = installSplashScreen()
        super.onCreate(savedInstanceState)

        handleIncomingIntent(intent)

        var uiState: MainActivityUiState by mutableStateOf(MainActivityUiState.Loading)

        lifecycleScope.launch {
            lifecycle.repeatOnLifecycle(Lifecycle.State.STARTED) {
                viewModel.uiState.collect { state ->
                    uiState = state
                }
            }
        }

        splashScreen.setKeepOnScreenCondition {
            val currentState = uiState
            when (currentState) {
                is MainActivityUiState.Loading -> true
                is MainActivityUiState.Success -> false
            }
        }

        enableEdgeToEdge()

        setContent {
            val pendingSharedImages by viewModel.pendingSharedImages.collectAsStateWithLifecycle()
            PicTravellyRoot(
                uiState = uiState,
                pendingSharedImages = pendingSharedImages,
                onConsumeSharedImages = viewModel::consumePendingSharedImages
            )
        }
    }

    override fun onNewIntent(intent: Intent) {
        super.onNewIntent(intent)
        setIntent(intent)
        handleIncomingIntent(intent)
    }

    private fun handleIncomingIntent(intent: Intent?) {
        if (intent == null) return
        val action = intent.action
        val type = intent.type
        if ((action == Intent.ACTION_SEND || action == Intent.ACTION_SEND_MULTIPLE) &&
            type?.startsWith("image/") == true
        ) {
            val uris = extractImageUris(intent)
            if (uris.isNotEmpty()) {
                lifecycleScope.launch(Dispatchers.IO) {
                    val copiedUris = uris.take(5).mapNotNull { sourceUri ->
                        ImageStorageManager.copyUriToInternalStorage(applicationContext, sourceUri)
                    }
                    if (copiedUris.isNotEmpty()) {
                        withContext(Dispatchers.Main) {
                            viewModel.setPendingSharedImages(copiedUris.map { it.toString() })
                        }
                    }
                }
            }
        }
    }

    private fun extractImageUris(intent: Intent): List<Uri> {
        val uris = mutableListOf<Uri>()
        when (intent.action) {
            Intent.ACTION_SEND -> {
                val uri: Uri? = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
                    intent.getParcelableExtra(Intent.EXTRA_STREAM, Uri::class.java)
                } else {
                    @Suppress("DEPRECATION")
                    intent.getParcelableExtra(Intent.EXTRA_STREAM)
                }
                if (uri != null) {
                    uris.add(uri)
                } else {
                    intent.clipData?.takeIf { it.itemCount > 0 }?.getItemAt(0)?.uri?.let {
                        uris.add(it)
                    }
                }
            }
            Intent.ACTION_SEND_MULTIPLE -> {
                val streamUris: ArrayList<Uri>? = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
                    intent.getParcelableArrayListExtra(Intent.EXTRA_STREAM, Uri::class.java)
                } else {
                    @Suppress("DEPRECATION")
                    intent.getParcelableArrayListExtra(Intent.EXTRA_STREAM)
                }
                if (!streamUris.isNullOrEmpty()) {
                    uris.addAll(streamUris)
                } else if (intent.clipData != null) {
                    for (i in 0 until intent.clipData!!.itemCount) {
                        intent.clipData!!.getItemAt(i).uri?.let { uris.add(it) }
                    }
                }
            }
        }
        return uris.take(5)
    }
}