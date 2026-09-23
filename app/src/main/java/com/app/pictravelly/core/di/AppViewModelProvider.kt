package com.app.pictravelly.core.di

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewmodel.CreationExtras
import androidx.lifecycle.viewmodel.initializer
import androidx.lifecycle.viewmodel.viewModelFactory
import com.app.pictravelly.PicTravellyApp
import com.app.pictravelly.core.data.TouristSpotRepository
import com.app.pictravelly.feature.home.HomeViewModel
import com.app.pictravelly.feature.settings.SettingsViewModel
import com.app.pictravelly.feature.spot_detail.SpotDetailViewModel
import com.app.pictravelly.feature.spot_form.SpotFormViewModel
import com.app.pictravelly.feature.spots.SpotsViewModel

/**
 * Fábrica de ViewModels que provê instâncias com dependências manuais injetadas a partir do AppContainer.
 */
object AppViewModelProvider {

    val Factory = viewModelFactory {
        // Inicializador do HomeViewModel
        initializer {
            HomeViewModel(
                repository = picTravellyApplication().container.touristSpotRepository
            )
        }

        // Inicializador do SpotsViewModel
        initializer {
            SpotsViewModel(
                repository = picTravellyApplication().container.touristSpotRepository
            )
        }

        // Inicializador do SpotFormViewModel
        initializer {
            SpotFormViewModel(
                repository = picTravellyApplication().container.touristSpotRepository
            )
        }

        // Inicializador do SettingsViewModel
        initializer {
            SettingsViewModel()
        }
    }

    /**
     * Factory personalizada para instanciar o SpotDetailViewModel com o spotId recebido pela rota.
     */
    fun createSpotDetailFactory(spotId: Long, repository: TouristSpotRepository): ViewModelProvider.Factory {
        return object : ViewModelProvider.Factory {
            @Suppress("UNCHECKED_CAST")
            override fun <T : ViewModel> create(modelClass: Class<T>): T {
                return SpotDetailViewModel(repository, spotId) as T
            }
        }
    }
}

/**
 * Função de extensão que recupera a instância da aplicação PicTravellyApp a partir de CreationExtras.
 */
fun CreationExtras.picTravellyApplication(): PicTravellyApp =
    (this[ViewModelProvider.AndroidViewModelFactory.APPLICATION_KEY] as PicTravellyApp)
