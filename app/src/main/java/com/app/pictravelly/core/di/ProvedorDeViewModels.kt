package com.app.pictravelly.core.di

import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.createSavedStateHandle
import androidx.lifecycle.viewmodel.CreationExtras
import androidx.lifecycle.viewmodel.initializer
import androidx.lifecycle.viewmodel.viewModelFactory
import com.app.pictravelly.PicTravellyApp
import com.app.pictravelly.feature.configuracoes.ConfiguracoesViewModel
import com.app.pictravelly.feature.inicio.InicioViewModel
import com.app.pictravelly.feature.mapa.MapaViewModel
import com.app.pictravelly.feature.perfil.PerfilViewModel
import com.app.pictravelly.feature.ponto.EditorPontoViewModel
import com.app.pictravelly.feature.viagem.DetalheViagemViewModel
import com.app.pictravelly.feature.viagem.EditorViagemViewModel

/** Fabrica unica de ViewModels, alimentada pelo [ContainerDoApp]. */
object ProvedorDeViewModels {

    val Fabrica = viewModelFactory {

        initializer {
            InicioViewModel(container().repositorioDoDiario)
        }

        initializer {
            MapaViewModel(
                repositorio = container().repositorioDoDiario,
                repositorioDeConfiguracoes = container().repositorioDeConfiguracoes,
                savedStateHandle = createSavedStateHandle()
            )
        }

        initializer {
            PerfilViewModel(
                repositorio = container().repositorioDoDiario,
                repositorioDeConfiguracoes = container().repositorioDeConfiguracoes,
                armazenamentoDeImagens = container().armazenamentoDeImagens
            )
        }

        initializer {
            ConfiguracoesViewModel(container().repositorioDeConfiguracoes)
        }

        initializer {
            DetalheViagemViewModel(
                repositorio = container().repositorioDoDiario,
                savedStateHandle = createSavedStateHandle()
            )
        }

        initializer {
            EditorViagemViewModel(
                repositorio = container().repositorioDoDiario,
                armazenamentoDeImagens = container().armazenamentoDeImagens,
                savedStateHandle = createSavedStateHandle()
            )
        }

        initializer {
            EditorPontoViewModel(
                repositorio = container().repositorioDoDiario,
                armazenamentoDeImagens = container().armazenamentoDeImagens,
                provedorDeLocalizacao = container().provedorDeLocalizacao,
                savedStateHandle = createSavedStateHandle()
            )
        }
    }
}

private fun CreationExtras.container(): ContainerDoApp =
    (this[ViewModelProvider.AndroidViewModelFactory.APPLICATION_KEY] as PicTravellyApp).container
