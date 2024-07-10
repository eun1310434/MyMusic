package com.euntaek.mymusic.ui.player

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.LinearOutSlowInEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.gestures.detectDragGestures
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.ripple.rememberRipple
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.livedata.observeAsState
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.rotate
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.graphics.painter.Painter
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import coil.compose.rememberAsyncImagePainter
import com.euntaek.mymusic.R
import com.euntaek.mymusic.data.entities.Song
import com.euntaek.mymusic.ui.theme.roundedShape
import com.euntaek.mymusic.viewmodels.MainViewModel
import com.euntaek.mymusic.utility.isPlaying

object SmallMusicPlayerDefaults {
    val Height = 60.dp
}

@Composable
fun SmallMusicPlayer(
    modifier: Modifier = Modifier,
    viewModel: MainViewModel = hiltViewModel()
) {
    var offsetX by remember { mutableFloatStateOf(0f) }
    val songs by viewModel.songs.collectAsStateWithLifecycle()
    val currentSong by viewModel.currentPlayingSong.collectAsStateWithLifecycle()
    val playbackStateCompat by viewModel.playbackState.collectAsStateWithLifecycle()
    val backgroundColor = MaterialTheme.colorScheme.secondaryContainer
    LaunchedEffect(key1 = currentSong, key2 = songs) {
        if (currentSong == null) {
            viewModel.prepareFirstSong()
        }
    }

    AnimatedVisibility(
        modifier = modifier,
        visible = currentSong != null
    ) {
        if (currentSong != null) {
            val song = currentSong
            Box(
                modifier = Modifier
                    .pointerInput(Unit) {
                        detectDragGestures(
                            onDragEnd = {
                                when {
                                    offsetX > 0 -> viewModel.skipToPreviousSong()
                                    offsetX < 0 -> viewModel.skipToNextSong()
                                }
                            },
                            onDrag = { change, dragAmount ->
                                change.consume()
                                offsetX = dragAmount.x
                            }
                        )
                    }
                    .height(SmallMusicPlayerDefaults.Height)
                    .fillMaxWidth(),
            ) {
                SmallMusicPlayerContent(
                    modifier = Modifier
                        .padding(start = 5.dp, end = 5.dp, bottom = 5.dp)
                        .shadow(
                            spotColor = MaterialTheme.colorScheme.secondaryContainer,
                            elevation = 5.dp,
                            shape = RoundedCornerShape(5.dp)
                        )
                        .background(backgroundColor),
                    song = song!!.toSong()!!,
                    isPlaying = playbackStateCompat?.isPlaying,
                    onClick = { viewModel.showFullScreenPlayer() },
                    onPlayToggleClick = { viewModel.playOrToggleSong(song.toSong()!!, true) }
                )
            }
        }
    }
}


@Composable
private fun SmallMusicPlayerContent(
    modifier: Modifier,
    song: Song,
    isPlaying: Boolean?,
    onClick: () -> Unit,
    onPlayToggleClick: () -> Unit
) {
    Box(
        modifier = modifier
            .clickable(onClick = onClick)
    ) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween,
            modifier = Modifier
                .padding(10.dp)
                .fillMaxWidth()
        ) {

            VinylAnimation(
                modifier = Modifier
                    .size(48.dp),
                painter = rememberAsyncImagePainter(song.imageUrl),
                isSongPlaying = isPlaying == true
            )
            Spacer(modifier = Modifier.width(8.dp))
            Column(
                verticalArrangement = Arrangement.Center,
                modifier = Modifier
                    .weight(1f),
            ) {
                Text(
                    song.title,
                    style = MaterialTheme.typography.bodyLarge,
                    color = MaterialTheme.colorScheme.onPrimaryContainer,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis,
                )

                Text(
                    modifier = Modifier.graphicsLayer { alpha = 0.60f },
                    text = song.subtitle,
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onPrimaryContainer,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
            }

            PlayToggle(
                isPlaying = isPlaying,
                onPlayToggleClick = onPlayToggleClick
            )
        }
    }
}

@Composable
fun PlayToggle(
    isPlaying: Boolean?,
    onPlayToggleClick: () -> Unit
) {
    val toggleIconId =
        if (isPlaying == true) R.drawable.ic_round_pause else R.drawable.ic_round_play_arrow

    Image(
        painter = painterResource(id = toggleIconId),
        contentDescription = null,
        contentScale = ContentScale.Crop,
        modifier = Modifier
            .padding(end = 16.dp)
            .size(48.dp)
            .clickable(
                interactionSource = remember {
                    MutableInteractionSource()
                },
                indication = rememberRipple(
                    bounded = false,
                    radius = 24.dp
                ),
                onClick = onPlayToggleClick
            )
    )
}


@Composable
fun VinylAnimation(
    modifier: Modifier = Modifier,
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

    Vinyl(modifier = modifier, painter = painter, rotationDegrees = rotation.value)
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
                    .fillMaxSize(0.7f)
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