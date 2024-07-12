package com.euntaek.mymusic.ui.home

import androidx.compose.animation.AnimatedVisibilityScope
import androidx.compose.animation.ExperimentalSharedTransitionApi
import androidx.compose.animation.SharedTransitionScope
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.euntaek.mymusic.R
import com.euntaek.mymusic.data.entities.AppInfo
import com.euntaek.mymusic.data.entities.Artist
import com.euntaek.mymusic.data.entities.Song
import com.euntaek.mymusic.ui.components.ProgressIndicatorPage
import com.euntaek.mymusic.ui.components.TopBarMenu
import com.euntaek.mymusic.ui.components.TopDrawerColumn
import com.euntaek.mymusic.viewmodels.MainViewModel

@OptIn(ExperimentalSharedTransitionApi::class)
@Composable
fun SharedTransitionScope.HomeScreen(
    viewModel: MainViewModel,
    animatedVisibilityScope: AnimatedVisibilityScope,
    navigateToProfile: () -> Unit,
    navigateToProfileDetail: (String) -> Unit,//artist.id
    navigateToSettings: () -> Unit,
    navigateToMailBox: (() -> Unit)? = null
) {
    val appId = stringResource(id = R.string.app_id)
    LaunchedEffect(Unit) { viewModel.getAppInfo(appId = appId) }

    val appInfo by viewModel.appInfo.collectAsStateWithLifecycle()
    val musicData by viewModel.musicData.collectAsStateWithLifecycle()

    if (appInfo != null && musicData != null) {
        MainPage(
            modifier = Modifier
                .background(color = MaterialTheme.colorScheme.background)
                .fillMaxSize(),
            appInfo = appInfo!!,
            songs = musicData!!.songs,
            artists = musicData!!.artists,
            animatedVisibilityScope = animatedVisibilityScope,
            onItemClick = viewModel::playOrToggleSong,
            onImageClick = navigateToProfileDetail,
            onAboutButtonClick = navigateToProfile,
            onMailIconButtonClick = navigateToMailBox,
            onSettingIconClick = navigateToSettings
        )
    } else {
        ProgressIndicatorPage()
    }
}

@OptIn(ExperimentalSharedTransitionApi::class)
@Composable
private fun SharedTransitionScope.MainPage(
    modifier: Modifier = Modifier,
    animatedVisibilityScope: AnimatedVisibilityScope,
    appInfo: AppInfo,
    artists: List<Artist>,
    songs: List<Song>,
    onItemClick: (Song) -> Unit,
    onImageClick: (String) -> Unit, //index
    onAboutButtonClick: () -> Unit,
    onMailIconButtonClick: (() -> Unit)? = null,
    onSettingIconClick: () -> Unit
) {
    TopDrawerColumn(
        modifier = modifier,
        topBar = { scale ->
            TopBarMenu(
                alpha = 1 - scale,
                title = appInfo.appName,
                contentColor = MaterialTheme.colorScheme.primary
            )
        },
        drawer = { scale ->
            AlbumInfo(
                title = appInfo.appName,
                artists = artists,
                animatedVisibilityScope = animatedVisibilityScope,
                scale = scale,
                onImageClick = onImageClick
            )
        },
        content = { _, state ->
            SongList(
                songs = songs,
                state = state,
                header = {
                    HeaderMenu(
                        modifier = Modifier.fillMaxWidth(),
                        youTubeMusicLink = appInfo.youTubeMusicLink,
                        spotifyLink = appInfo.spotifyLink,
                        instagramLink = appInfo.instagramLink,
                        whatsappLink = appInfo.whatsappLink,
                        onAboutButtonClick = onAboutButtonClick,
                        onMailIconButtonClick = onMailIconButtonClick,
                        onSettingIconClick = onSettingIconClick
                    )
                },
                onItemClick = onItemClick
            )
        }
    )
}

