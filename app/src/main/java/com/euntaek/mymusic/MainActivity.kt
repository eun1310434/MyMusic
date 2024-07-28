package com.euntaek.mymusic

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.OnBackPressedDispatcher
import androidx.activity.compose.setContent
import androidx.compose.animation.ExperimentalSharedTransitionApi
import androidx.compose.animation.SharedTransitionLayout
import androidx.compose.foundation.background
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material.ExperimentalMaterialApi
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.SideEffect
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import androidx.navigation.navArgument
import com.euntaek.mymusic.ui.home.HomeScreen
import com.euntaek.mymusic.ui.mailbox.MailBoxScreen
import com.euntaek.mymusic.ui.navigation.Destination
import com.euntaek.mymusic.ui.player.FullScreenMusicPlayerScreen
import com.euntaek.mymusic.ui.player.SmallMusicPlayer
import com.euntaek.mymusic.ui.profile.ProfileDetailScreen
import com.euntaek.mymusic.ui.profile.ProfileScreen
import com.euntaek.mymusic.ui.settings.SettingsScreen
import com.euntaek.mymusic.ui.theme.MyMusicTheme
import com.euntaek.mymusic.viewmodels.MainViewModel
import com.google.accompanist.systemuicontroller.rememberSystemUiController
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class MainActivity : ComponentActivity() {
    @ExperimentalMaterialApi
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            MyMusicTheme {
                MusicPlayerApp(
                    backPressedDispatcher = onBackPressedDispatcher,
                    startDestination = Destination.HOME
                )
            }
        }
    }
}

@OptIn(ExperimentalSharedTransitionApi::class)
@ExperimentalMaterialApi
@Composable
fun MusicPlayerApp(
    viewModel: MainViewModel = hiltViewModel(),
    startDestination: String = Destination.HOME,
    backPressedDispatcher: OnBackPressedDispatcher
) {
    val systemUiController = rememberSystemUiController()

    val useDarkIcons = !isSystemInDarkTheme()

    SideEffect {
        systemUiController.setSystemBarsColor(
            color = Color.Transparent,
            darkIcons = useDarkIcons
        )
    }

    val navController = rememberNavController()
    Box(
        modifier = Modifier
            .background(MaterialTheme.colorScheme.background)
            .fillMaxSize()
    ) {
        SharedTransitionLayout {
            NavHost(
                navController = navController,
                startDestination = startDestination
            ) {
                composable(Destination.HOME) {
                    HomeScreen(
                        viewModel = viewModel,
                        animatedVisibilityScope = this,
                        navigateToProfileDetail = { index -> navController.navigate("${Destination.PROFILE_DETAIL}/$index") },
                        navigateToProfile = { navController.navigate(route = Destination.PROFILE) },
                        navigateToSettings = { navController.navigate(route = Destination.SETTINGS) },
                        navigateToMailBox = null // TODO impl, { navController.navigate(route = Destination.MAIL_BOX) }
                    )
                }
                composable(
                    route = "${Destination.PROFILE_DETAIL}/{artistId}",
                    arguments = listOf(
                        navArgument("artistId") {
                            type = NavType.StringType
                        }
                    )
                ) {
                    val artistId = it.arguments?.getString("artistId").orEmpty()
                    ProfileDetailScreen(
                        viewModel = viewModel,
                        animatedVisibilityScope = this,
                        artistId = artistId
                    )
                }
                composable(Destination.PROFILE) {
                    ProfileScreen(
                        viewModel = viewModel,
                        animatedVisibilityScope = this
                    ) { artistId ->
                        navController.navigate("${Destination.PROFILE_DETAIL}/$artistId")
                    }
                }
                composable(Destination.MAIL_BOX) {
                    MailBoxScreen(
                        viewModel = viewModel,
                        backPressedDispatcher = backPressedDispatcher
                    )
                }
                composable(Destination.SETTINGS) {
                    SettingsScreen(viewModel = viewModel)
                }
            }
            SmallMusicPlayer(modifier = Modifier.align(Alignment.BottomCenter))
            FullScreenMusicPlayerScreen(
                viewModel = viewModel,
                backPressedDispatcher = backPressedDispatcher,
            )
        }
    }
}