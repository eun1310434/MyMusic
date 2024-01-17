package com.euntaek.mymusic

import android.os.Bundle
import android.util.Log
import androidx.activity.ComponentActivity
import androidx.activity.OnBackPressedDispatcher
import androidx.activity.compose.setContent
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material.ExperimentalMaterialApi
import androidx.compose.runtime.Composable
import androidx.compose.runtime.SideEffect
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import com.euntaek.mymusic.ui.home.HomeScreen
import com.euntaek.mymusic.ui.player.SmallMusicPlayer
import com.euntaek.mymusic.ui.navigation.Destination
import com.euntaek.mymusic.ui.player.FullScreenMusicPlayer
import com.euntaek.mymusic.ui.theme.MyMusicTheme
import com.euntaek.mymusic.ui.viewmodels.MainViewModel
import com.google.accompanist.systemuicontroller.rememberSystemUiController
import dagger.hilt.android.AndroidEntryPoint
import timber.log.Timber

@AndroidEntryPoint
class MainActivity : ComponentActivity() {
    @ExperimentalMaterialApi
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        initTimber()
        setContent {
            MyMusicTheme {
                MusicPlayerApp(
                    backPressedDispatcher = onBackPressedDispatcher,
                    startDestination = Destination.home
                )
            }
        }
    }

    private fun initTimber() {
        if (BuildConfig.DEBUG) {
            Timber.plant(object : Timber.DebugTree() {
                override fun createStackElementTag(element: StackTraceElement): String? {
                    return super.createStackElementTag(element) + ':' + element.lineNumber
                }
            })
        } else {
            Timber.plant(ReleaseTree())
        }
    }
}


private class ReleaseTree : Timber.Tree() {
    override fun isLoggable(tag: String?, priority: Int): Boolean {
        return !(priority == Log.VERBOSE || priority == Log.DEBUG || priority == Log.INFO)
    }

    override fun log(priority: Int, tag: String?, message: String, t: Throwable?) {
        if (isLoggable(tag, priority)) {
            if (priority == Log.ERROR && t != null) {
                //You can save the Error log.
            }
        }
    }
}

@ExperimentalMaterialApi
@Composable
fun MusicPlayerApp(
    vm: MainViewModel = hiltViewModel(),
    startDestination: String = Destination.home,
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
            .fillMaxSize()
    ) {
        NavHost(navController = navController, startDestination = startDestination) {
            composable(Destination.home) {
                HomeScreen(vm)
            }
        }
        SmallMusicPlayer(modifier = Modifier.align(Alignment.BottomCenter))
        FullScreenMusicPlayer(
            backPressedDispatcher = backPressedDispatcher,
        )
    }
}