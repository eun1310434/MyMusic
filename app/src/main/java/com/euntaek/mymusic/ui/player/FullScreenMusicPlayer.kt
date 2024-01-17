package com.euntaek.mymusic.ui.player

import android.support.v4.media.MediaMetadataCompat
import androidx.activity.OnBackPressedCallback
import androidx.activity.OnBackPressedDispatcher
import androidx.annotation.DrawableRes
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.LinearOutSlowInEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.tween
import androidx.compose.animation.slideInVertically
import androidx.compose.animation.slideOutVertically
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.gestures.Orientation
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.aspectRatio
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
import androidx.compose.runtime.livedata.observeAsState
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.rotate
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.ColorFilter
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.graphics.painter.Painter
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import coil.compose.rememberAsyncImagePainter
import com.euntaek.mymusic.R
import com.euntaek.mymusic.data.entities.Song
import com.euntaek.mymusic.ui.theme.roundedShape
import com.euntaek.mymusic.ui.viewmodels.MainViewModel
import com.euntaek.mymusic.ui.viewmodels.SongViewModel


@ExperimentalMaterialApi
@Composable
fun FullScreenMusicPlayer(
    backPressedDispatcher: OnBackPressedDispatcher,
    viewModel: MainViewModel = hiltViewModel(),
    songViewModel: SongViewModel = hiltViewModel()
) {
    var offsetX by remember { mutableStateOf(0f) }
    val currentSong = viewModel.currentPlayingSong.value
    val playbackStateCompat by viewModel.playbackState.observeAsState()

    AnimatedVisibility(
        visible = currentSong != null && viewModel.showPlayerFullScreen,
        enter = slideInVertically(initialOffsetY = { it }),
        exit = slideOutVertically(targetOffsetY = { it })
    ) {
        FullScreenMusicPlayerContent(
            song = currentSong!!.toSong()!!,
            backPressedDispatcher = backPressedDispatcher,
            mainViewModel = viewModel,
            songViewModel = songViewModel
        )
    }
}


fun MediaMetadataCompat.toSong(): Song? {
    return description?.let {
        Song(
            it.mediaId ?: "",
            it.title.toString(),
            it.subtitle.toString(),
            it.mediaUri.toString(),
            it.iconUri.toString()
        )
    }
}

@ExperimentalMaterialApi
@Composable
private fun FullScreenMusicPlayerContent(
    song: Song,
    backPressedDispatcher: OnBackPressedDispatcher,
    mainViewModel: MainViewModel,
    songViewModel: SongViewModel
) {
    val swappableState = rememberSwipeableState(initialValue = 0)
    val endAnchor = LocalConfiguration.current.screenHeightDp * LocalDensity.current.density
    val anchors = mapOf(0f to 0, endAnchor to 1)

    val backCallback = remember {
        object : OnBackPressedCallback(true) {
            override fun handleOnBackPressed() {
                mainViewModel.showPlayerFullScreen = false
            }
        }
    }

    val backgroundColor = MaterialTheme.colorScheme.background
    val dominantColor by remember { mutableStateOf(backgroundColor) }
    val imagePainter = rememberAsyncImagePainter(song.imageUrl)
    val iconResId =
        if (mainViewModel.songIsPlaying) R.drawable.ic_round_pause else R.drawable.ic_round_play_arrow
    val isSongPlaying = mainViewModel.songIsPlaying
    var sliderIsChanging by remember { mutableStateOf(false) }
    var localSliderValue by remember { mutableFloatStateOf(0f) }

    val sliderProgress =
        if (sliderIsChanging) localSliderValue else songViewModel.currentPlayerPosition

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
                mainViewModel.showPlayerFullScreen = false
            }
        }
        FullScreenMusicPlayerContent(
            song = song,
            isSongPlaying = isSongPlaying,
            imagePainter = imagePainter,
            dominantColor = dominantColor,
            playbackProgress = sliderProgress,
            currentTime = songViewModel.currentPlaybackFormattedPosition,
            totalTime = "",
            playPauseIcon = iconResId,
            playOrToggleSong = { mainViewModel.playOrToggleSong(song, true) },
            playNextSong = { mainViewModel.skipToNextSong() },
            playPreviousSong = { mainViewModel.skipToPreviousSong() },
            onSliderChange = { newPosition ->
                localSliderValue = newPosition
                sliderIsChanging = true
            },
            onSliderChangeFinished = {
                mainViewModel.seekTo(songViewModel.currentSongDuration * localSliderValue)
                sliderIsChanging = false
            },
            onForward = {
                songViewModel.currentPlaybackPosition.let { currentPosition ->
                    mainViewModel.seekTo(currentPosition + 10 * 1000f)
                }
            },
            onRewind = {
                songViewModel.currentPlaybackPosition.let { currentPosition ->
                    mainViewModel.seekTo(if (currentPosition - 10 * 1000f < 0) 0f else currentPosition - 10 * 1000f)
                }
            },
            onClose = {
                mainViewModel.showPlayerFullScreen = false
            }
        )
    }

    LaunchedEffect("playbackPosition") {
        songViewModel.updateCurrentPlaybackPosition()
    }

    DisposableEffect(backPressedDispatcher) {
        backPressedDispatcher.addCallback(backCallback)

        onDispose {
            backCallback.remove()
            mainViewModel.showPlayerFullScreen = false
        }
    }
}

@Composable
private fun FullScreenMusicPlayerContent(
    song: Song,
    isSongPlaying: Boolean,
    imagePainter: Painter,
    dominantColor: Color,
    playbackProgress: Float,
    currentTime: String,
    totalTime: String,
    @DrawableRes playPauseIcon: Int,
    playOrToggleSong: () -> Unit,
    playNextSong: () -> Unit,
    playPreviousSong: () -> Unit,
    onSliderChange: (Float) -> Unit,
    onSliderChangeFinished: () -> Unit,
    onRewind: () -> Unit,
    onForward: () -> Unit,
    onClose: () -> Unit
) {
    val gradientColors = if (isSystemInDarkTheme()) {
        listOf(
            dominantColor,
            MaterialTheme.colorScheme.background
        )
    } else {
        listOf(
            MaterialTheme.colorScheme.background,
            MaterialTheme.colorScheme.background
        )
    }

    val sliderColors = if (isSystemInDarkTheme()) {
        SliderDefaults.colors(
            thumbColor = MaterialTheme.colorScheme.onBackground,
            activeTrackColor = MaterialTheme.colorScheme.onBackground,
            inactiveTrackColor = MaterialTheme.colorScheme.onBackground.copy(
                //alpha = ProgressIndicatorDefaults.IndicatorBackgroundOpacity
            ),
        )
    } else SliderDefaults.colors(
        thumbColor = dominantColor,
        activeTrackColor = dominantColor,
        inactiveTrackColor = dominantColor.copy(
            //alpha = ProgressIndicatorDefaults.IndicatorBackgroundOpacity
        ),
    )

    Box(
        modifier = Modifier
            .fillMaxSize()
    ) {
        Surface {
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
                    IconButton(
                        onClick = onClose
                    ) {
                        Image(
                            imageVector = Icons.Rounded.KeyboardArrowDown,
                            contentDescription = "Close",
                            colorFilter = ColorFilter.tint(LocalContentColor.current)
                        )
                    }
                    Column(
                        modifier = Modifier
                            .padding(horizontal = 24.dp)
                    ) {
                        Box(
                            modifier = Modifier
                                .padding(vertical = 32.dp)
                                .clip(MaterialTheme.shapes.medium)
                                .weight(1f, fill = false)
                                .aspectRatio(1f)

                        ) {
                            VinylAnimation(painter = imagePainter, isSongPlaying = isSongPlaying)
                        }

                        Text(
                            text = song.title,
                            style = MaterialTheme.typography.headlineMedium,
                            color = MaterialTheme.colorScheme.onBackground,
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis
                        )

                        Text(
                            song.subtitle,
                            style = MaterialTheme.typography.titleMedium,
                            color = MaterialTheme.colorScheme.onBackground,
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis,
                            modifier = Modifier.graphicsLayer {
                                alpha = 0.60f
                            }
                        )

                        Column(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(vertical = 24.dp)
                        ) {
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
                                CompositionLocalProvider(LocalContentAlpha provides ContentAlpha.medium) {
                                    Text(
                                        text = totalTime,
                                        color = MaterialTheme.colorScheme.onBackground,
                                        style = MaterialTheme.typography.bodyMedium
                                    )
                                }
                            }
                        }

                        Row(
                            horizontalArrangement = Arrangement.SpaceAround,
                            verticalAlignment = Alignment.CenterVertically,
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(vertical = 8.dp),
                        ) {
                            Icon(
                                imageVector = Icons.Rounded.SkipPrevious,
                                tint = MaterialTheme.colorScheme.onBackground,
                                contentDescription = "Skip Previous",
                                modifier = Modifier
                                    .clip(CircleShape)
                                    .clickable(onClick = playPreviousSong)
                                    .padding(12.dp)
                                    .size(32.dp)
                            )
                            Icon(
                                imageVector = Icons.Rounded.Replay10,
                                tint = MaterialTheme.colorScheme.onBackground,
                                contentDescription = "Replay 10 seconds",
                                modifier = Modifier
                                    .clip(CircleShape)
                                    .clickable(onClick = onRewind)
                                    .padding(12.dp)
                                    .size(32.dp)
                            )
                            Icon(
                                painter = painterResource(playPauseIcon),
                                contentDescription = "Play",
                                tint = MaterialTheme.colorScheme.background,
                                modifier = Modifier
                                    .clip(CircleShape)
                                    .background(MaterialTheme.colorScheme.onBackground)
                                    .clickable(onClick = playOrToggleSong)
                                    .size(64.dp)
                                    .padding(8.dp)
                            )
                            Icon(
                                imageVector = Icons.Rounded.Forward10,
                                tint = MaterialTheme.colorScheme.onBackground,
                                contentDescription = "Forward 10 seconds",
                                modifier = Modifier
                                    .clip(CircleShape)
                                    .clickable(onClick = onForward)
                                    .padding(12.dp)
                                    .size(32.dp)
                            )
                            Icon(
                                imageVector = Icons.Rounded.SkipNext,
                                tint = MaterialTheme.colorScheme.onBackground,
                                contentDescription = "Skip Next",
                                modifier = Modifier
                                    .clip(CircleShape)
                                    .clickable(onClick = playNextSong)
                                    .padding(12.dp)
                                    .size(32.dp)
                            )
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun Vinyl(
    modifier: Modifier = Modifier,
    rotationDegrees: Float = 0f,
    painter: Painter?
) {
    Box(
        modifier = modifier
            .aspectRatio(1.0f)
            .clip(roundedShape)
    ) {
        // Vinyl background
        Image(
            modifier = Modifier
                .fillMaxSize()
                .rotate(rotationDegrees),
            painter = painterResource(id = R.drawable.vinyl_background),
            contentDescription = null
        )

        // Vinyl song cover
        if (painter != null) {
            Image(
                modifier = Modifier
                    .fillMaxSize(0.5f)
                    .rotate(rotationDegrees)
                    .aspectRatio(1.0f)
                    .align(Alignment.Center)
                    .clip(roundedShape),
                painter = painter,
                contentDescription = null
            )
        }
    }
}

@Composable
fun VinylAnimation(
    isSongPlaying: Boolean = true,
    painter: Painter?
) {
    var currentRotation by remember { mutableFloatStateOf(0f) }
    val rotation = remember { Animatable(currentRotation) }

    LaunchedEffect(isSongPlaying) {
        if (isSongPlaying) {
            rotation.animateTo(
                targetValue = currentRotation + 360f,
                animationSpec = infiniteRepeatable(
                    animation = tween(3000, easing = LinearEasing),
                    repeatMode = RepeatMode.Restart
                ),
                block = { currentRotation = value }
            )
        } else {
            if (currentRotation > 0f) {
                rotation.animateTo(
                    targetValue = currentRotation + 50,
                    animationSpec = tween(
                        delayMillis = 1250,
                        easing = LinearOutSlowInEasing
                    ),
                    block = { currentRotation = value }
                )
            }
        }
    }

    Vinyl(painter = painter, rotationDegrees = rotation.value)
}


