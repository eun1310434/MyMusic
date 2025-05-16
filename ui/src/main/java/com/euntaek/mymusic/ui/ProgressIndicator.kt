package com.euntaek.mymusic.ui

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp

@Composable
fun ProgressIndicatorPage(modifier: Modifier = Modifier) {
    Box(
        modifier = modifier
            .background(Color.Transparent)
            .fillMaxSize()
            .clickable(
                interactionSource = remember { MutableInteractionSource() },
                indication = null,
                onClick = {} // Block the click function.
            )
    ) {
        CircularProgressIndicator(
            color = MaterialTheme.colorScheme.onBackground,
            modifier = Modifier
                .size(100.dp)
                .fillMaxHeight()
                .align(Alignment.Center)
                .padding(16.dp)
        )
    }
}