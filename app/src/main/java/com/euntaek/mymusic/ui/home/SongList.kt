package com.euntaek.mymusic.ui.home

import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyListState
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.unit.dp
import coil.compose.rememberAsyncImagePainter
import com.euntaek.mymusic.data.entities.Song

private object SongListDefaults {
    val HeaderRoundedCornerSize = 30.dp
    val HeaderBottomPadding = 5.dp
    val HeaderElevation = 5.dp
}

@OptIn(ExperimentalFoundationApi::class)
@Composable
fun SongList(
    songs: List<Song>,
    state: LazyListState = rememberLazyListState(),
    header: @Composable () -> Unit,
    onItemClick: (Song) -> Unit
) {
    Box(
        modifier = Modifier
            .background(MaterialTheme.colorScheme.background)
            .fillMaxSize(),
        contentAlignment = Alignment.BottomCenter
    ) {
        LazyColumn(
            modifier = Modifier
                .fillMaxWidth()
                .padding(bottom = 60.dp)
                .align(Alignment.TopCenter),
            state = state,
            userScrollEnabled = false
        ) {
            stickyHeader {
                Box(
                    modifier = Modifier
                        .shadow(
                            elevation = SongListDefaults.HeaderElevation,
                            shape = RoundedCornerShape(
                                bottomStart = SongListDefaults.HeaderRoundedCornerSize,
                                bottomEnd = SongListDefaults.HeaderRoundedCornerSize
                            )
                        )
                        .background(MaterialTheme.colorScheme.secondaryContainer)
                        .padding(
                            start = SongListDefaults.HeaderRoundedCornerSize / 2,
                            end = SongListDefaults.HeaderRoundedCornerSize / 2,
                            bottom = SongListDefaults.HeaderBottomPadding,
                        ),
                    contentAlignment = Alignment.TopCenter
                ) {
                    header()
                }
            }
            items(songs) { song ->
                SongListItem(
                    title = song.title,
                    subtitle = song.subtitle,
                    imageUrl = song.imageUrl,
                    onContentClick = { onItemClick(song) }
                )
            }
            item {
                Spacer(modifier = Modifier.height(40.dp))
            }
        }
    }
}

@Composable
private fun SongListItem(
    title: String,
    subtitle: String,
    imageUrl: String,
    onContentClick: () -> Unit
) {
    Row(
        modifier = Modifier
            .height(80.dp)
            .fillMaxWidth()
            .clip(shape = RoundedCornerShape(percent = 5))
            .clickable(onClick = onContentClick),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Spacer(modifier = Modifier.width(16.dp))
        Image(
            modifier = Modifier
                .size(56.dp)
                .clip(MaterialTheme.shapes.medium)
                .align(Alignment.CenterVertically),
            painter = rememberAsyncImagePainter(imageUrl),
            contentDescription = null,
            contentScale = ContentScale.Crop,
        )
        Spacer(modifier = Modifier.width(16.dp))
        Column(modifier = Modifier.weight(1f)) {
            Text(
                text = title,
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onBackground,
            )
            Spacer(modifier = Modifier.width(3.dp))
            Text(
                text = subtitle,
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onBackground.copy(alpha = 0.75f),
            )
        }
        Spacer(modifier = Modifier.width(16.dp))
    }
}