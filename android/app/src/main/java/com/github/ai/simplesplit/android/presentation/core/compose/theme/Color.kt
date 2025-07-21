package com.github.ai.simplesplit.android.presentation.core.compose.theme

import androidx.compose.runtime.Immutable
import androidx.compose.ui.graphics.Color

@Immutable
data class AppColors(
    val primary: Color,
    val secondary: Color,
    val tertiary: Color,
    val background: Color,
    val secondaryBackground: Color,
    val cardPrimaryBackground: Color,
    val cardOnPrimarySelectedBackground: Color,
    val cardSecondaryBackground: Color,
    val primaryText: Color,
    val secondaryText: Color,
    val errorText: Color,
    val primaryButton: Color,
    val primaryIcon: Color,
    val dividerOnPrimary: Color,
    val chipColor: Color,
    val redButtonColor: Color
)

val LightAppColors = AppColors(
    primary = Color(0xFF_1c7c92),
    secondary = Color.Green,
    tertiary = Color.Blue,
    background = Color(0xFF_edf2ec),
    secondaryBackground = Color(0xFF_ececed),
    cardPrimaryBackground = Color(0xFF_ffffff),
    cardOnPrimarySelectedBackground = Color(0xFF_DDDDDD),
    cardSecondaryBackground = Color(0xFF_dbe5dc),
    primaryText = Color(0xFF_00000d),
    secondaryText = Color(0xFF_888888),
    errorText = Color(0xFF_f2473b),
    primaryButton = Color(0xFF_1c7c92),
    primaryIcon = Color(0xFF_00000d),
    dividerOnPrimary = Color(0xFF_e0e0e0),
    chipColor = Color(0xFF_f2f4f7),
    redButtonColor = Color(0xFF_f2473b)
)

val DarkAppColors = AppColors(
    primary = Color(0xFFD0BCFF),
    secondary = Color(0xFFCCC2DC),
    tertiary = Color(0xFFEFB8C8),
    background = Color(0xFF_181c1e),
    secondaryBackground = Color(0xFF_ececed),
    cardPrimaryBackground = Color(0xFF_181c1e),
    cardOnPrimarySelectedBackground = Color(0xFF_DDDDDD),
    cardSecondaryBackground = Color(0xFF_dbe5dc),
    primaryText = Color.White,
    secondaryText = Color.Gray,
    errorText = Color(0xFFdd1445),
    primaryButton = Color(0xFF_5dd4e4),
    primaryIcon = Color(0xFF_888888),
    dividerOnPrimary = Color(0xFF_e0e0e0),
    chipColor = Color(0xFF_f2f4f7),
    redButtonColor = Color(0xFF_f2473b)
)