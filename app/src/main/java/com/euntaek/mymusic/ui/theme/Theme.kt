package com.euntaek.mymusic.ui.theme

import androidx.compose.material.MaterialTheme
import androidx.compose.runtime.Composable


@Composable
fun MyMusicTheme(
    content: @Composable () -> Unit
) {
    MaterialTheme(
        colors = MyMusicColors,
        typography = MyMusicTypography,
        shapes = MyMusicShapes,
        content = content
    )
}
