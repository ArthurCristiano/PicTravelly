package com.app.pictravelly

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
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.repeatOnLifecycle
import com.app.pictravelly.core.di.AppViewModelProvider
import com.app.pictravelly.core.ui.PicTravellyRoot
import kotlinx.coroutines.launch

class MainActivity : ComponentActivity() {
    private val viewModel: MainActivityViewModel by viewModels { AppViewModelProvider.Factory }
    override fun onCreate(savedInstanceState: Bundle?) {
        val splashScreen = installSplashScreen()
        super.onCreate(savedInstanceState)

        var uiState: MainActivityUiState by mutableStateOf(MainActivityUiState.Loading)

        // Escuta o ViewModel silenciosamente (Lógica de SO)
        lifecycleScope.launch {
            lifecycle.repeatOnLifecycle(Lifecycle.State.STARTED) {
                viewModel.uiState.collect { state ->
                    uiState = state
                }
            }
        }

        // Mantém a Splash até que o estado seja Success E os dados do usuário estejam presentes
        splashScreen.setKeepOnScreenCondition {
            val currentState = uiState
            when (currentState) {
                is MainActivityUiState.Loading -> true
                is MainActivityUiState.Success -> {
                    false
                }
            }
        }

        enableEdgeToEdge()

        // Entrega o estado blindado para a interface (Delegação limpa)
        setContent {
            PicTravellyRoot(uiState = uiState)
        }
    }
}