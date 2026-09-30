package com.app.pictravelly.feature.settings

import android.content.res.Configuration
import androidx.compose.animation.AnimatedVisibility
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
import androidx.compose.material.icons.filled.Archive
import androidx.compose.material.icons.filled.Map
import androidx.compose.material.icons.filled.Palette
import androidx.compose.material.icons.filled.Preview
import androidx.compose.material.icons.filled.Share
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.app.pictravelly.core.database.model.settings.GoogleMapType
import com.app.pictravelly.core.database.model.settings.AppTheme
import com.app.pictravelly.core.design.components.PicTravellyTitle
import com.app.pictravelly.core.database.model.settings.MapEngineType
import com.app.pictravelly.core.design.theme.PicTravellyTheme
import com.app.pictravelly.feature.settings.components.SettingsAboutCard
import com.app.pictravelly.feature.settings.components.SettingsActionRow
import com.app.pictravelly.feature.settings.components.SettingsRadioRow
import com.app.pictravelly.feature.settings.components.SettingsSection

@Composable
fun SettingsScreen(
    uiState: SettingsUiState,
    contentPadding: PaddingValues,
    onThemeChange: (AppTheme) -> Unit,
    onMapEngineChange: (MapEngineType) -> Unit,
    onMapTypeChange: (GoogleMapType) -> Unit,
    onExportPhotos: () -> Unit,
    modifier: Modifier = Modifier
) {
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

        SettingsSection(title = "Provedor de Mapa", icon = Icons.Default.Map) {
            MapEngineType.entries.forEach { engine ->
                SettingsRadioRow(
                    label = engine.label,
                    isSelected = uiState.selectedMapEngine == engine,
                    onClick = { onMapEngineChange(engine) })
            }
        }

        AnimatedVisibility(visible = uiState.selectedMapEngine == MapEngineType.GOOGLE_MAPS) {
            SettingsSection(title = "Tipo de mapa (Google Maps)", icon = Icons.Default.Preview) {
                GoogleMapType.entries.forEach { type ->
                    val visualLabel = when (type) {
                        GoogleMapType.NORMAL -> "Padrão"
                        GoogleMapType.SATELLITE -> "Satélite"
                        GoogleMapType.HYBRID -> "Híbrido"
                    }
                    SettingsRadioRow(
                        label = visualLabel,
                        isSelected = uiState.selectedGoogleMapType == type,
                        onClick = { onMapTypeChange(type) })
                }
            }
        }

        SettingsSection(title = "Backup e Exportação", icon = Icons.Default.Archive) {
            SettingsActionRow(
                title = "Exportar fotos do diário (.zip)",
                subtitle = "Gera um backup compactado com as fotos salvas para envio a outros apps",
                icon = Icons.Default.Share,
                isLoading = uiState.isExporting,
                onClick = onExportPhotos
            )
        }

        SettingsAboutCard(
            appVersion = "1.0 (Build 2026)"
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
    PicTravellyTheme {
        SettingsScreen(
            uiState = SettingsUiState(
                selectedTheme = AppTheme.SYSTEM,
                selectedGoogleMapType = GoogleMapType.NORMAL,
                selectedMapEngine = MapEngineType.entries.first()
            ),
            contentPadding = PaddingValues(0.dp),
            onThemeChange = {},
            onMapEngineChange = {},
            onMapTypeChange = {},
            onExportPhotos = {}
        )
    }
}