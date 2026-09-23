package com.app.pictravelly.core.ui.util

import java.time.Instant
import java.time.LocalDate
import java.time.ZoneId
import java.time.ZoneOffset
import java.time.format.DateTimeFormatter
import java.util.Locale

/** Formatacao das datas do diario em pt-BR. */
object Datas {

    private val CURTA = DateTimeFormatter.ofPattern("dd/MM/yyyy", Locale.forLanguageTag("pt-BR"))
    private val DIA_MES = DateTimeFormatter.ofPattern("dd/MM", Locale.forLanguageTag("pt-BR"))

    fun formatar(millis: Long): String = paraData(millis).format(CURTA)

    /** "12/03/2026", "12/03 - 20/03/2026" ou "12/03/2026 - em andamento". */
    fun intervalo(inicio: Long, fim: Long?): String {
        val dataInicio = paraData(inicio)
        if (fim == null) return "${dataInicio.format(CURTA)} · em andamento"

        val dataFim = paraData(fim)
        if (dataInicio == dataFim) return dataInicio.format(CURTA)

        val formatoInicio = if (dataInicio.year == dataFim.year) DIA_MES else CURTA
        return "${dataInicio.format(formatoInicio)} - ${dataFim.format(CURTA)}"
    }

    fun hoje(): Long =
        LocalDate.now().atStartOfDay(ZoneId.systemDefault()).toInstant().toEpochMilli()

    /**
     * O DatePicker do Material 3 trabalha em UTC. Sem estas duas conversoes a
     * data escolhida aparece um dia atrasada em fusos negativos, como o do Brasil.
     */
    fun deSelecaoUtc(millis: Long): Long =
        Instant.ofEpochMilli(millis)
            .atZone(ZoneOffset.UTC)
            .toLocalDate()
            .atStartOfDay(ZoneId.systemDefault())
            .toInstant()
            .toEpochMilli()

    fun paraSelecaoUtc(millis: Long): Long =
        paraData(millis).atStartOfDay(ZoneOffset.UTC).toInstant().toEpochMilli()

    private fun paraData(millis: Long): LocalDate =
        Instant.ofEpochMilli(millis).atZone(ZoneId.systemDefault()).toLocalDate()
}
