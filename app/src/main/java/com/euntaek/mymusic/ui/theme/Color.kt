package com.euntaek.mymusic.ui.theme

import androidx.compose.material.darkColors
import androidx.compose.material3.darkColorScheme
import androidx.compose.ui.graphics.Color


/**
 * This is the minimum amount of calculated contrast for a color to be used on top of the
 * surface color. These values are defined within the WCAG AA guidelines, and we use a value of
 * 3:1 which is the minimum for user-interface components.
 */
const val MinContrastOfPrimaryVsSurface = 3f

val Yellow800 = Color(0xFFF29F05)
val Red300 = Color(0xFFEA6D7E)

val MyMusicColors = darkColorScheme(
    primary = Yellow800,
    onPrimary = Color.Black,
    primaryContainer = Yellow800,
    secondary = Yellow800,
    onSecondary = Color.Black,
    error = Red300,
    onError = Color.Black
)