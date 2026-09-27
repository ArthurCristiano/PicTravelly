package com.app.pictravelly.feature.settings

import android.content.res.Configuration
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Language
import androidx.compose.material.icons.filled.Map
import androidx.compose.material.icons.filled.Palette
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.app.pictravelly.core.database.model.settings.AppLanguage
import com.app.pictravelly.core.database.model.settings.AppTheme
import com.app.pictravelly.core.design.components.PicTravellyTitle
import com.app.pictravelly.core.database.model.settings.MapEngineType
import com.app.pictravelly.feature.settings.components.SettingsAboutCard
import com.app.pictravelly.feature.settings.components.SettingsRadioRow
import com.app.pictravelly.feature.settings.components.SettingsSection

@Composable
fun SettingsScreen(
    uiState: SettingsUiState, // Recebe apenas os dados prontos
    contentPadding: PaddingValues,
    onThemeChange: (AppTheme) -> Unit,
    onMapEngineChange: (MapEngineType) -> Unit,
    onLanguageChange: (AppLanguage) -> Unit,
    modifier: Modifier = Modifier
) {
    // ============================================================================
    // 🎨 [DESIGN / HUMBERTO] - TELA DE PREFERÊNCIAS E MENU:
    // - Humberto: você pode aplicar agrupamentos visuais elegantes, divisores de couro
    //   ou texturas de diário nas seções de opções.
    // ============================================================================
    Column(
        modifier = modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background)
            .verticalScroll(rememberScrollState())
            .padding(contentPadding)
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(20.dp)
    ) {
        PicTravellyTitle(text = "Configurações")

        // Seção: Aparência e Tema
        SettingsSection(title = "Aparência", icon = Icons.Default.Palette) {
            AppTheme.entries.forEach { theme ->
                val themeLabel = when (theme) {
                    AppTheme.SYSTEM -> "Padrão do Sistema"
                    AppTheme.LIGHT -> "Modo Claro"
                    AppTheme.DARK -> "Modo Escuro"
                }
                SettingsRadioRow(
                    label = themeLabel,
                    isSelected = uiState.selectedTheme == theme,
                    onClick = { onThemeChange(theme) })
            }
        }

        // Seção: Motor e Provedor de Mapa
        SettingsSection(title = "Provedor de Mapa", icon = Icons.Default.Map) {
            MapEngineType.entries.forEach { engine ->
                SettingsRadioRow(
                    label = engine.label,
                    isSelected = uiState.selectedMapEngine == engine,
                    onClick = { onMapEngineChange(engine) })
            }
        }

        // Seção: Idioma
        SettingsSection(title = "Idioma da Interface", icon = Icons.Default.Language) {
            AppLanguage.entries.forEach { lang ->
                val visualLabel = when (lang) {
                    AppLanguage.PT_BR -> "Português (Brasil)"
                    AppLanguage.EN -> "English (US)"
                    AppLanguage.ES -> "Español"
                }
                SettingsRadioRow(
                    label = visualLabel,
                    isSelected = uiState.selectedLanguage == lang,
                    onClick = { onLanguageChange(lang) })
            }
        }

        // Seção: Sobre
        SettingsAboutCard(
            appVersion = "1.0 (Build 2026)" // Pode ser trocado por BuildConfig.VERSION_NAME depois
        )

        Spacer(modifier = Modifier.height(72.dp))
    }
}

@Preview(name = "Settings - Light Mode", showBackground = true)
@Preview(
    name = "Settings - Dark Mode",
    showBackground = true,
    uiMode = Configuration.UI_MODE_NIGHT_YES
)
@Composable
private fun SettingsScreenPreview() {
    // 1. Envolve o Preview com o Design System do seu app
    MaterialTheme { // Substitua por PicTravellyTheme { se existir
        SettingsScreen(
            uiState = SettingsUiState(
                selectedTheme = AppTheme.SYSTEM,
                selectedLanguage = AppLanguage.PT_BR,
                // Assumindo que a Engine padrão seja instanciável diretamente
                selectedMapEngine = MapEngineType.entries.first()
            ),
            contentPadding = PaddingValues(0.dp),
            onThemeChange = {},
            onMapEngineChange = {},
            onLanguageChange = {}
        )
    }
}