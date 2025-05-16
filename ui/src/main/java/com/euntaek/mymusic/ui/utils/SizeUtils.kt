package com.euntaek.mymusic.ui.utils

import androidx.compose.runtime.Composable
import androidx.compose.runtime.Stable
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.unit.Density
import androidx.compose.ui.unit.Dp

@Stable
private fun Dp.toPx(density: Density): Int = with(density) { this@toPx.roundToPx() }

@Stable
@Composable
fun Dp.toPx(): Int = toPx(LocalDensity.current)
