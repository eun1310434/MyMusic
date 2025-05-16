package com.euntaek.mymusic.ui

import androidx.annotation.DrawableRes
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.painter.Painter
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.unit.dp


object IconDefaults {
    val Size = 40.dp
    val MarkerSize = 5.dp
}

@Composable
fun Icon(
    modifier: Modifier = Modifier,
    @DrawableRes iconRes: Int,
    color: Color = MaterialTheme.colorScheme.secondary,
    markAlign: Alignment = Alignment.TopEnd,
    isUpdateMarkEnabled: Boolean = false
) {
    Icon(
        modifier = modifier,
        painter = painterResource(iconRes),
        color = color,
        markAlign = markAlign,
        isUpdateMarkEnabled = isUpdateMarkEnabled
    )
}

@Composable
fun Icon(
    modifier: Modifier,
    painter: Painter,
    color: Color = MaterialTheme.colorScheme.secondary,
    markAlign: Alignment = Alignment.TopEnd,
    isUpdateMarkEnabled: Boolean = false
) {
    Box(
        modifier = modifier.size(IconDefaults.Size)
    ) {
        Icon(
            painter = painter,
            contentDescription = null,
            tint = color,
            modifier = Modifier
                .align(Alignment.Center)
                .clip(CircleShape)
                .padding(3.dp)
        )
        if (isUpdateMarkEnabled) {
            Box(
                modifier = Modifier
                    .align(markAlign)
                    .padding(IconDefaults.MarkerSize)
                    .size(IconDefaults.MarkerSize)
                    .clip(CircleShape)
                    .background(Color.Red)
            )
        }
    }
}