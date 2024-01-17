package com.euntaek.mymusic.ui.player

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.gestures.detectDragGestures
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material.MaterialTheme
import androidx.compose.material.ripple.rememberRipple
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.livedata.observeAsState
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import coil.compose.rememberAsyncImagePainter
import com.euntaek.mymusic.R
import com.euntaek.mymusic.data.entities.Song
import com.euntaek.mymusic.ui.viewmodels.MainViewModel
import com.euntaek.mymusic.utility.isPlaying


@Composable
fun SmallMusicPlayer(
    modifier: Modifier = Modifier,
    isDarkMode: Boolean = isSystemInDarkTheme(),
    viewModel: MainViewModel = hiltViewModel()
) {
    var offsetX by remember { mutableStateOf(0f) }
    val currentSong = viewModel.currentPlayingSong.value
    val playbackStateCompat by viewModel.playbackState.observeAsState()
    val backgroundColor = if (isDarkMode) Color.DarkGray else Color.LightGray

    AnimatedVisibility(
        modifier = modifier,
        visible = currentSong != null
    ) {
        if (currentSong != null) {
            val song = currentSong!!.toSong()
            Box(
                modifier = Modifier
                    .fillMaxWidth()
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
                    .background(backgroundColor),
            ) {
                SmallMusicPlayerContent(
                    song = song!!,
                    isPlaying = playbackStateCompat?.isPlaying,
                    onClick = { viewModel.showPlayerFullScreen = true },
                    onPlayToggleClick = { viewModel.playOrToggleSong(song, true) }
                )
            }
        }
    }
}


@Composable
private fun SmallMusicPlayerContent(
    song: Song,
    isPlaying: Boolean?,
    onClick: () -> Unit,
    onPlayToggleClick: () -> Unit
) {
    Box(
        modifier = Modifier
            .height(64.dp)
            .clickable(onClick = onClick)
    ) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween,
            modifier = Modifier.fillMaxWidth()
        ) {
            Image(
                painter = rememberAsyncImagePainter(song.imageUrl),
                contentDescription = song.title,
                contentScale = ContentScale.Crop,
                modifier = Modifier
                    .size(48.dp)
                    .offset(16.dp)
            )

            Column(
                verticalArrangement = Arrangement.Center,
                modifier = Modifier
                    .weight(1f)
                    .fillMaxHeight()
                    .padding(vertical = 8.dp, horizontal = 32.dp),
            ) {
                Text(
                    song.title,
                    style = MaterialTheme.typography.body2,
                    color = MaterialTheme.colors.onBackground,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis,
                )

                Text(
                    modifier = Modifier.graphicsLayer { alpha = 0.60f },
                    text = song.subtitle,
                    style = MaterialTheme.typography.body2,
                    color = MaterialTheme.colors.onBackground,
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
private fun PlayToggle(
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