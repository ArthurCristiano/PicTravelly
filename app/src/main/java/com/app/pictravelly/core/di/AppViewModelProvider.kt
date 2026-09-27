package com.app.pictravelly.core.di

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewmodel.CreationExtras
import androidx.lifecycle.viewmodel.initializer
import androidx.lifecycle.viewmodel.viewModelFactory
import com.app.pictravelly.MainActivityViewModel
import com.app.pictravelly.PicTravellyApp
import com.app.pictravelly.core.data.TouristSpotRepository
import com.app.pictravelly.core.data.TripRepository
import com.app.pictravelly.feature.home.HomeViewModel
import com.app.pictravelly.feature.map.MapViewModel
import com.app.pictravelly.feature.settings.SettingsViewModel
import com.app.pictravelly.feature.spot_detail.SpotDetailViewModel
import com.app.pictravelly.feature.spot_form.SpotFormViewModel
import com.app.pictravelly.feature.spots.SpotsViewModel
import com.app.pictravelly.feature.trip_detail.TripDetailViewModel
import com.app.pictravelly.feature.trip_form.TripFormViewModel
import com.app.pictravelly.feature.trips.TripsViewModel

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

        // Inicializador do TripsViewModel (aba Diário)
        initializer {
            TripsViewModel(
                repository = picTravellyApplication().container.tripRepository
            )
        }

        // Inicializador do MapViewModel (aba Mapa)
        initializer {
            MapViewModel(
                repository = picTravellyApplication().container.touristSpotRepository
            )
        }

        // Inicializador do SettingsViewModel
        initializer {
            SettingsViewModel(
                repository = picTravellyApplication().container.settingsRepository
            )
        }

        // Inicializador do MainActivityViewModel
        initializer {
            MainActivityViewModel(
                settingsRepository = picTravellyApplication().container.settingsRepository
            )
        }
    }

    /**
     * Factory personalizada para instanciar o SpotDetailViewModel com o spotId recebido pela rota.
     */
    fun createSpotDetailFactory(
        spotId: Long,
        repository: TouristSpotRepository
    ): ViewModelProvider.Factory {
        return object : ViewModelProvider.Factory {
            @Suppress("UNCHECKED_CAST")
            override fun <T : ViewModel> create(modelClass: Class<T>): T {
                return SpotDetailViewModel(repository, spotId) as T
            }
        }
    }

    /**
     * Factory do detalhe da viagem, que precisa do tripId da rota e dos dois
     * repositórios (a viagem em si e os pontos que ela agrupa).
     */
    fun createTripDetailFactory(
        tripId: Long,
        tripRepository: TripRepository,
        spotRepository: TouristSpotRepository
    ): ViewModelProvider.Factory {
        return object : ViewModelProvider.Factory {
            @Suppress("UNCHECKED_CAST")
            override fun <T : ViewModel> create(modelClass: Class<T>): T {
                return TripDetailViewModel(tripRepository, spotRepository, tripId) as T
            }
        }
    }

    /**
     * Factory do formulário de viagem. Recebe NO_TRIP_ID para criar uma nova.
     */
    fun createTripFormFactory(
        tripId: Long,
        tripRepository: TripRepository
    ): ViewModelProvider.Factory {
        return object : ViewModelProvider.Factory {
            @Suppress("UNCHECKED_CAST")
            override fun <T : ViewModel> create(modelClass: Class<T>): T {
                return TripFormViewModel(tripRepository, tripId) as T
            }
        }
    }

    /**
     * Factory do formulário de ponto turístico. O tripId vem da rota quando o
     * cadastro é aberto de dentro de uma viagem.
     */
    fun createSpotFormFactory(
        tripId: Long,
        spotRepository: TouristSpotRepository,
        tripRepository: TripRepository
    ): ViewModelProvider.Factory {
        return object : ViewModelProvider.Factory {
            @Suppress("UNCHECKED_CAST")
            override fun <T : ViewModel> create(modelClass: Class<T>): T {
                return SpotFormViewModel(spotRepository, tripRepository, tripId) as T
            }
        }
    }
}

/**
 * Função de extensão que recupera a instância da aplicação PicTravellyApp a partir de CreationExtras.
 */
fun CreationExtras.picTravellyApplication(): PicTravellyApp =
    (this[ViewModelProvider.AndroidViewModelFactory.APPLICATION_KEY] as PicTravellyApp)
