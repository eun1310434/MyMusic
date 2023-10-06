package com.euntaek.mymusic.ui.player

import androidx.compose.material.Surface
import androidx.compose.material3.windowsizeclass.WindowSizeClass
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.window.layout.DisplayFeature


/**
 * Stateful version of the Podcast player
 */
@Composable
fun PlayerScreen(
    viewModel: PlayerViewModel,
    windowSizeClass: WindowSizeClass,
    displayFeatures: List<DisplayFeature>,
    onBackPress: () -> Unit
) {
    val uiState = viewModel.uiState
    PlayerScreen(uiState, windowSizeClass, displayFeatures, onBackPress)
}

/**
 * Stateless version of the Player screen
 */
@Composable
private fun PlayerScreen(
    uiState: PlayerUiState,
    windowSizeClass: WindowSizeClass,
    displayFeatures: List<DisplayFeature>,
    onBackPress: () -> Unit,
    modifier: Modifier = Modifier
) {
    Surface(modifier) {
        if (uiState.podcastName.isNotEmpty()) {
            //PlayerContent(uiState, windowSizeClass, displayFeatures, onBackPress)
        } else {
            //FullScreenLoading()
        }
    }
}