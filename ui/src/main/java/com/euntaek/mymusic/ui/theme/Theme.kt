package com.euntaek.mymusic.ui.theme

import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable


@Composable
fun MyMusicTheme(
    content: @Composable () -> Unit
) {
    MaterialTheme(
        colorScheme = getColorScheme(),
        typography = MyMusicTypography,
        shapes = MyMusicShapes,
        content = content
    )
}
