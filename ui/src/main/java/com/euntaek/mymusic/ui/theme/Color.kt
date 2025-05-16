package com.euntaek.mymusic.ui.theme

import androidx.compose.material3.darkColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.res.colorResource
import com.euntaek.mymusic.ui.R


@Composable
fun getColorScheme() = darkColorScheme(
    primary = colorResource(id = R.color.primary),
    onPrimary = colorResource(id = R.color.on_primary),
    secondary = colorResource(id = R.color.secondary),
    secondaryContainer = colorResource(id = R.color.secondary_container),
    background = colorResource(id = R.color.background),
    onBackground = colorResource(id = R.color.on_background),
    error = colorResource(id = R.color.md_theme_error),
    onError = colorResource(id = R.color.md_theme_onError),
    errorContainer = colorResource(id = R.color.md_theme_errorContainer),
    onErrorContainer = colorResource(id = R.color.md_theme_onErrorContainer),
    outline = colorResource(id = R.color.secondary),
    outlineVariant = colorResource(id = R.color.on_primary),
    scrim = colorResource(id = R.color.md_theme_scrim),
)
