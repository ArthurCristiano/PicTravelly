package com.app.pictravelly.core.di

import android.content.Context
import com.app.pictravelly.core.banco.BancoDeDados
import com.app.pictravelly.core.configuracoes.RepositorioDeConfiguracoes
import com.app.pictravelly.core.localizacao.ProvedorDeLocalizacao
import com.app.pictravelly.core.localizacao.ServicoDeGeocodificacao
import com.app.pictravelly.core.midia.ArmazenamentoDeImagens
import com.app.pictravelly.data.repositorio.RepositorioDoDiario

/**
 * Injecao de dependencia manual: uma instancia unica de cada peca,
 * criada sob demanda e mantida viva pela Application.
 */
class ContainerDoApp(context: Context) {

    private val contextoDoApp = context.applicationContext

    val armazenamentoDeImagens: ArmazenamentoDeImagens by lazy {
        ArmazenamentoDeImagens(contextoDoApp)
    }

    val servicoDeGeocodificacao: ServicoDeGeocodificacao by lazy {
        ServicoDeGeocodificacao(contextoDoApp)
    }

    val provedorDeLocalizacao: ProvedorDeLocalizacao by lazy {
        ProvedorDeLocalizacao(contextoDoApp)
    }

    val repositorioDeConfiguracoes: RepositorioDeConfiguracoes by lazy {
        RepositorioDeConfiguracoes(contextoDoApp)
    }

    val repositorioDoDiario: RepositorioDoDiario by lazy {
        val banco = BancoDeDados.obter(contextoDoApp)
        RepositorioDoDiario(
            viagemDao = banco.viagemDao(),
            pontoDao = banco.pontoDao(),
            armazenamentoDeImagens = armazenamentoDeImagens,
            servicoDeGeocodificacao = servicoDeGeocodificacao
        )
    }
}
