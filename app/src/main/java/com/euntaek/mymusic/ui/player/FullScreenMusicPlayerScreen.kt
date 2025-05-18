package com.euntaek.mymusic.ui.player

import androidx.activity.OnBackPressedCallback
import androidx.activity.OnBackPressedDispatcher
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.slideInVertically
import androidx.compose.animation.slideOutVertically
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.gestures.Orientation
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.systemBarsPadding
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.ContentAlpha
import androidx.compose.material.ExperimentalMaterialApi
import androidx.compose.material.FractionalThreshold
import androidx.compose.material.LocalContentAlpha
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.Forward10
import androidx.compose.material.icons.rounded.KeyboardArrowDown
import androidx.compose.material.icons.rounded.Replay10
import androidx.compose.material.icons.rounded.SkipNext
import androidx.compose.material.icons.rounded.SkipPrevious
import androidx.compose.material.rememberSwipeableState
import androidx.compose.material.swipeable
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.LocalContentColor
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Slider
import androidx.compose.material3.SliderDefaults
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.ColorFilter
import androidx.compose.ui.graphics.Shape
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.euntaek.mymusic.R
import com.euntaek.mymusic.data.entities.Song
import com.euntaek.mymusic.utility.formatLong
import com.euntaek.mymusic.viewmodels.MainViewModel
import com.euntaek.uicomponent.cachedasyncImage.CachedAsyncImage


@ExperimentalMaterialApi
@Composable
fun FullScreenMusicPlayerScreen(
    viewModel: MainViewModel,
    backPressedDispatcher: OnBackPressedDispatcher,
) {
    val playerBackgroundImage by viewModel.playerBackgroundImage.collectAsStateWithLifecycle()
    val isPlayerFullScreenShowing by viewModel.isPlayerFullScreenShowing.collectAsStateWithLifecycle()
    val currentPlaybackPosition by viewModel.currentPlaybackPosition.collectAsStateWithLifecycle()
    val playbackProgress by viewModel.playbackProgress.collectAsStateWithLifecycle()
    val isSongPlaying by viewModel.isSongPlaying.collectAsStateWithLifecycle()
    val currentSong by viewModel.currentPlayingSong.collectAsStateWithLifecycle()

    LaunchedEffect(Unit) { viewModel.updateCurrentPlaybackPosition() }

    AnimatedVisibility(
        visible = currentSong != null && isPlayerFullScreenShowing,
        enter = slideInVertically(initialOffsetY = { it }),
        exit = slideOutVertically(targetOffsetY = { it })
    ) {
        FullScreenMusicPlayerContent(
            song = currentSong!!,
            backgroundImage = playerBackgroundImage,
            backPressedDispatcher = backPressedDispatcher,
            isSongPlaying = isSongPlaying,
            playbackProgress = playbackProgress,
            seekTo = viewModel::seekTo,
            hideFullScreenPlayer = viewModel::hideFullScreenPlayer,
            playOrToggleSong = viewModel::playOrToggleSong,
            playNextSong = viewModel::skipToNextSong,
            playPreviousSong = viewModel::skipToPreviousSong,
            currentPlaybackPosition = currentPlaybackPosition
        )
    }
}

@ExperimentalMaterialApi
@Composable
private fun FullScreenMusicPlayerContent(
    song: Song,
    backPressedDispatcher: OnBackPressedDispatcher,
    isSongPlaying: Boolean,
    playbackProgress: Float,
    currentPlaybackPosition: Long,
    backgroundImage: String?,
    hideFullScreenPlayer: () -> Unit,
    playOrToggleSong: (Song, Boolean) -> Unit,
    playNextSong: () -> Unit,
    playPreviousSong: () -> Unit,
    seekTo: (Float) -> Unit
) {
    val swappableState = rememberSwipeableState(initialValue = 0)
    val endAnchor = LocalConfiguration.current.screenHeightDp * LocalDensity.current.density
    val anchors = mapOf(0f to 0, endAnchor to 1)

    val backCallback = remember {
        object : OnBackPressedCallback(true) {
            override fun handleOnBackPressed() {
                hideFullScreenPlayer()
            }
        }
    }

    val backgroundColor = MaterialTheme.colorScheme.primaryContainer
    var sliderIsChanging by remember { mutableStateOf(false) }
    var localSliderValue by remember { mutableFloatStateOf(0f) }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(backgroundColor)
            .swipeable(
                state = swappableState,
                anchors = anchors,
                thresholds = { _, _ -> FractionalThreshold(0.34f) },
                orientation = Orientation.Vertical
            )
    ) {
        if (swappableState.currentValue >= 1) {
            LaunchedEffect("key") {
                hideFullScreenPlayer()
            }
        }
        FullScreenMusicPlayerContent(
            song = song,
            backgroundImage = backgroundImage,
            isSongPlaying = isSongPlaying,
            playbackProgress = playbackProgress,
            currentTime = currentPlaybackPosition.formatLong(),
            totalTime = song.duration?.formatLong(),
            playOrToggleSong = { playOrToggleSong(song, true) },
            playNextSong = playNextSong,
            playPreviousSong = playPreviousSong,
            onSliderChange = { newPosition ->
                localSliderValue = newPosition
                sliderIsChanging = true
            },
            onSliderChangeFinished = {
                if (song.duration != null) {
                    seekTo(song.duration * localSliderValue)
                    sliderIsChanging = false
                }
            },
            onForward = {
                seekTo(currentPlaybackPosition + 10 * 1000f)
            },
            onRewind = {
                seekTo(if (currentPlaybackPosition - 10 * 1000f < 0) 0f else currentPlaybackPosition - 10 * 1000f)
            },
            onClose = hideFullScreenPlayer
        )
    }

    DisposableEffect(backPressedDispatcher) {
        backPressedDispatcher.addCallback(backCallback)

        onDispose {
            backCallback.remove()
            hideFullScreenPlayer()
        }
    }
}

@Composable
private fun FullScreenMusicPlayerContent(
    song: Song,
    backgroundImage: String?,
    isSongPlaying: Boolean,
    playbackProgress: Float,
    currentTime: String,
    totalTime: String?,
    playOrToggleSong: () -> Unit,
    playNextSong: () -> Unit,
    playPreviousSong: () -> Unit,
    onSliderChange: (Float) -> Unit,
    onSliderChangeFinished: () -> Unit,
    onRewind: () -> Unit,
    onForward: () -> Unit,
    onClose: () -> Unit
) {
    val gradientColors = listOf(
        Color.Transparent,
        Color.Transparent,
        MaterialTheme.colorScheme.background
    )

    Box(modifier = Modifier.fillMaxSize()) {
        Surface {
            if (!backgroundImage.isNullOrEmpty()) {
                CachedAsyncImage(
                    modifier = Modifier.fillMaxSize(),
                    contentScale = ContentScale.Crop,
                    imageUrl = backgroundImage,
                    isGIF = true
                )
            }

            Box(
                modifier = Modifier
                    .background(
                        Brush.verticalGradient(
                            colors = gradientColors,
                            endY = LocalConfiguration.current.screenHeightDp.toFloat() * LocalDensity.current.density
                        )
                    )
                    .fillMaxSize()
                    .systemBarsPadding()
            ) {
                Column {
                    IconButton(onClick = onClose) {
                        Image(
                            imageVector = Icons.Rounded.KeyboardArrowDown,
                            contentDescription = "Close",
                            colorFilter = ColorFilter.tint(LocalContentColor.current)
                        )
                    }
                    Column(modifier = Modifier.padding(horizontal = 24.dp)) {
                        Spacer(modifier = Modifier.fillMaxHeight(0.6f))
                        PlayerTitle(title = song.title, subtitle = song.subtitle)
                        PlayerSlider(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(vertical = 24.dp),
                            playbackProgress = playbackProgress,
                            currentTime = currentTime,
                            totalTime = totalTime,
                            onSliderChange = onSliderChange,
                            onSliderChangeFinished = onSliderChangeFinished
                        )
                        PlayerControlButtons(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(vertical = 8.dp),
                            isSongPlaying = isSongPlaying,
                            playOrToggleSong = playOrToggleSong,
                            playNextSong = playNextSong,
                            playPreviousSong = playPreviousSong,
                            onRewind = onRewind,
                            onForward = onForward
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun PlayerTitle(title: String, subtitle: String) {
    Text(
        text = title,
        style = MaterialTheme.typography.headlineMedium,
        color = MaterialTheme.colorScheme.onBackground,
        maxLines = 1,
        overflow = TextOverflow.Ellipsis
    )

    Text(
        text = subtitle,
        style = MaterialTheme.typography.titleMedium,
        color = MaterialTheme.colorScheme.onBackground.copy(alpha = 0.60f),
        maxLines = 1,
        overflow = TextOverflow.Ellipsis
    )
}

@Composable
private fun PlayerSlider(
    modifier: Modifier,
    playbackProgress: Float,
    currentTime: String,
    totalTime: String?,
    onSliderChange: (Float) -> Unit,
    onSliderChangeFinished: () -> Unit,
) {
    val sliderColors = SliderDefaults.colors(
        thumbColor = MaterialTheme.colorScheme.onBackground,
        activeTrackColor = MaterialTheme.colorScheme.onBackground,
        inactiveTrackColor = MaterialTheme.colorScheme.onBackground.copy(alpha = 0.24f)
    )

    Column(modifier = modifier) {
        Slider(
            value = playbackProgress,
            modifier = Modifier
                .fillMaxWidth(),
            colors = sliderColors,
            onValueChange = onSliderChange,
            onValueChangeFinished = onSliderChangeFinished
        )
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            CompositionLocalProvider(LocalContentAlpha provides ContentAlpha.medium) {
                Text(
                    text = currentTime,
                    color = MaterialTheme.colorScheme.onBackground,
                    style = MaterialTheme.typography.bodyMedium
                )
            }

            if (totalTime.isNullOrEmpty()) {
                CircularProgressIndicator(
                    modifier = Modifier.size(15.dp),
                    strokeWidth = 2.dp,
                    color = MaterialTheme.colorScheme.onBackground
                )
            } else {
                CompositionLocalProvider(LocalContentAlpha provides ContentAlpha.medium) {
                    Text(
                        text = totalTime,
                        color = MaterialTheme.colorScheme.onBackground,
                        style = MaterialTheme.typography.bodyMedium
                    )
                }
            }
        }
    }
}

@Composable
private fun PlayerControlButtons(
    modifier: Modifier,
    isSongPlaying: Boolean,
    color: Color = MaterialTheme.colorScheme.onBackground,
    playOrToggleSong: () -> Unit,
    playNextSong: () -> Unit,
    playPreviousSong: () -> Unit,
    onRewind: () -> Unit,
    onForward: () -> Unit
) {
    Row(
        modifier = modifier,
        horizontalArrangement = Arrangement.SpaceAround,
        verticalAlignment = Alignment.CenterVertically
    ) {
        PlayIconButton(
            imageVector = Icons.Rounded.SkipPrevious,
            color = color,
            contentDescription = "Skip Previous",
            onClick = playPreviousSong
        )
        PlayIconButton(
            imageVector = Icons.Rounded.Replay10,
            color = color,
            contentDescription = "Replay 10 seconds",
            onClick = onRewind
        )
        PlayToggleIconButton(
            isPlaying = isSongPlaying,
            playOrToggleSong = playOrToggleSong
        )
        PlayIconButton(
            imageVector = Icons.Rounded.Forward10,
            color = color,
            contentDescription = "Forward 10 seconds",
            onClick = onForward
        )
        PlayIconButton(
            imageVector = Icons.Rounded.SkipNext,
            color = color,
            contentDescription = "Skip Next",
            onClick = playNextSong
        )
    }
}

@Composable
private fun PlayToggleIconButton(
    size: Dp = 64.dp,
    shape: Shape = CircleShape,
    backgroundColor: Color = MaterialTheme.colorScheme.onBackground,
    isPlaying: Boolean,
    playOrToggleSong: () -> Unit,
) {
    val playPauseIcon =
        if (isPlaying) R.drawable.ic_round_pause else R.drawable.ic_round_play_arrow

    Icon(
        painter = painterResource(playPauseIcon),
        contentDescription = "Play",
        tint = MaterialTheme.colorScheme.background,
        modifier = Modifier
            .clip(shape)
            .background(backgroundColor)
            .clickable(onClick = playOrToggleSong)
            .size(size)
            .padding(8.dp)
    )
}


@Composable
private fun PlayIconButton(
    imageVector: ImageVector,
    color: Color,
    contentDescription: String?,
    onClick: () -> Unit
) {
    Icon(
        imageVector = imageVector,
        tint = color,
        contentDescription = contentDescription,
        modifier = Modifier
            .clip(CircleShape)
            .clickable(onClick = onClick)
            .padding(12.dp)
            .size(32.dp)
    )
}
