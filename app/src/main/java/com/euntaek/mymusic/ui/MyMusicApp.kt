package com.euntaek.mymusic.ui

import androidx.compose.material.AlertDialog
import androidx.compose.material.Text
import androidx.compose.material.TextButton
import androidx.compose.material3.windowsizeclass.WindowSizeClass
import androidx.compose.runtime.Composable
import androidx.compose.ui.res.stringResource
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.window.layout.DisplayFeature
import com.euntaek.mymusic.R
import com.euntaek.mymusic.ui.home.Home
import com.euntaek.mymusic.ui.player.PlayerScreen
import com.euntaek.mymusic.ui.player.PlayerViewModel
import com.euntaek.mymusic.ui.theme.MyMusicAppState
import com.euntaek.mymusic.ui.theme.Screen
import com.euntaek.mymusic.ui.theme.rememberMyMusicAppState


@Composable
fun MyMusicApp(
    windowSizeClass: WindowSizeClass,
    displayFeatures: List<DisplayFeature>,
    appState: MyMusicAppState = rememberMyMusicAppState()
) {
    if (appState.isOnline) {
        NavHost(
            navController = appState.navController,
            startDestination = Screen.Home.route
        ) {
            composable(Screen.Home.route) { backStackEntry ->
                Home(
                    navigateToPlayer = { musicUri ->
                        appState.navigateToPlayer(musicUri = musicUri, from = backStackEntry)
                    }
                )
            }
            composable(Screen.Player.route) { backStackEntry ->
                val playerViewModel: PlayerViewModel = viewModel(
                    factory = PlayerViewModel.provideFactory(
                        owner = backStackEntry,
                        defaultArgs = backStackEntry.arguments
                    )
                )
                PlayerScreen(
                    playerViewModel,
                    windowSizeClass,
                    displayFeatures,
                    onBackPress = appState::navigateBack
                )
            }
        }
    } else {
        OfflineDialog { appState.refreshOnline() }
    }
}


@Composable
fun OfflineDialog(onRetry: () -> Unit) {
    AlertDialog(
        onDismissRequest = {},
        title = { Text(text = stringResource(R.string.connection_error_title)) },
        text = { Text(text = stringResource(R.string.connection_error_message)) },
        confirmButton = {
            TextButton(onClick = onRetry) {
                Text(stringResource(R.string.retry_label))
            }
        }
    )
}
