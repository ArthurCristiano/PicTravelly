package com.app.pictravelly

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import com.app.pictravelly.core.design.tema.PicTravellyTema
import com.app.pictravelly.core.ui.PicTravellyTelaPrincipal

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            PicTravellyTema {
                PicTravellyTelaPrincipal()
            }
        }
    }
}
