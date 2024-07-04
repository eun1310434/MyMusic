package com.euntaek.mymusic.ui.components

import androidx.annotation.DrawableRes
import androidx.compose.foundation.clickable
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color


@Composable
fun IconButton(
    @DrawableRes iconRes: Int,
    color: Color = MaterialTheme.colorScheme.secondary,
    markAlign: Alignment = Alignment.TopEnd,
    isUpdateMarkEnabled: Boolean = false,
    onClick: () -> Unit,
) {
    Icon(
        modifier = Modifier
            .clip(CircleShape)
            .clickable(onClick = onClick),
        iconRes = iconRes,
        color = color,
        markAlign = markAlign,
        isUpdateMarkEnabled = isUpdateMarkEnabled
    )
}