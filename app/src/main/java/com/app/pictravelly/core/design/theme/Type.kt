package com.app.pictravelly.core.design.theme

import androidx.compose.material3.Typography
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.sp

// Estratégia Híbrida: Serif para o "Diário" e SansSerif para "Utilidade/UI"
private val DiaryTitleFont = FontFamily.Serif
private val AppBodyFont = FontFamily.SansSerif

val Typography = Typography(
    // Títulos Principais (ex: Nome do Ponto Turístico na tela de detalhes)
    headlineLarge = TextStyle(
        fontFamily = DiaryTitleFont,
        fontWeight = FontWeight.Bold,
        fontSize = 32.sp,
        lineHeight = 40.sp,
        letterSpacing = 0.sp
    ),
    // Títulos Menores (ex: Títulos das fotos do diário, cabeçalhos de seções)
    titleLarge = TextStyle(
        fontFamily = DiaryTitleFont,
        fontWeight = FontWeight.Medium,
        fontSize = 22.sp,
        lineHeight = 28.sp,
        letterSpacing = 0.sp
    ),

    // Corpo do texto padrão (ex: A descrição da viagem digitada pelo usuário)
    // Usamos SansSerif para hiper-legibilidade sob luz do sol, com altura de linha (lineHeight) generosa de 150% (24sp para 16sp de fonte).
    bodyLarge = TextStyle(
        fontFamily = AppBodyFont,
        fontWeight = FontWeight.Normal,
        fontSize = 16.sp,
        lineHeight = 24.sp,
        letterSpacing = 0.5.sp
    ),
    // Corpo menor (ex: datas, localizações secundárias)
    bodyMedium = TextStyle(
        fontFamily = AppBodyFont,
        fontWeight = FontWeight.Normal,
        fontSize = 14.sp,
        lineHeight = 20.sp,
        letterSpacing = 0.25.sp
    ),

    // Elementos de Interface Pura (ex: Textos dentro de botões, abas, chips)
    labelLarge = TextStyle(
        fontFamily = AppBodyFont,
        fontWeight = FontWeight.Medium,
        fontSize = 14.sp,
        lineHeight = 20.sp,
        letterSpacing = 0.1.sp
    ),
    labelSmall = TextStyle(
        fontFamily = AppBodyFont,
        fontWeight = FontWeight.Medium,
        fontSize = 11.sp,
        lineHeight = 16.sp,
        letterSpacing = 0.5.sp
    )
)