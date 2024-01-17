package com.euntaek.mymusic.ui.home

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyListState
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Favorite
import androidx.compose.material.icons.outlined.Favorite
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.vector.rememberVectorPainter
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.euntaek.mymusic.data.entities.Song
import kotlinx.collections.immutable.ImmutableList

@Composable
fun SongListItem(
    modifier: Modifier = Modifier,
    number: Int,
    author: String = "",
    title: String,
    duration: String = "",
    isLiked: Boolean = true,
    onLikeClick: (index: Int) -> Unit,
    onContentClick: (index: Int) -> Unit,
) {
    val likedIcon = rememberVectorPainter(image = Icons.Filled.Favorite)
    val notLikedIcon = rememberVectorPainter(image = Icons.Outlined.Favorite)

    Row(
        modifier = Modifier
            .height(80.dp)
            .fillMaxWidth()
            .padding(horizontal = 16.dp)
            .clip(shape = RoundedCornerShape(percent = 50))
            .clickable { onContentClick(number) },
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Spacer(modifier = Modifier.width(16.dp))
        Text(
            text = "$number.",
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurface
        )
        Spacer(modifier = Modifier.width(16.dp))
        Column(modifier = Modifier.weight(1f)) {
            Text(
                modifier = Modifier.alpha(0.75f),
                text = author,
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurface,
                fontSize = 13.sp,
            )
            Text(
                text = title,
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurface,
            )
        }
        Spacer(modifier = Modifier.width(16.dp))
        Text(
            text = duration,
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurface,
        )
        Spacer(modifier = Modifier.width(16.dp))
        IconButton(onClick = { onLikeClick(number - 1) }) {
            Icon(
                painter = if (isLiked) likedIcon else notLikedIcon,
                contentDescription = "",
                tint = if (isLiked) {
                    MaterialTheme.colorScheme.secondary
                } else {
                    MaterialTheme.colorScheme.onSurface.copy(alpha = 0.2f)
                }
            )
        }
        Spacer(modifier = Modifier.width(16.dp))
    }
}

@Composable
fun SongList(
    modifier: Modifier = Modifier,
    items: ImmutableList<Song>,
    bottomPadding: Dp = 0.dp,
    scrollState: LazyListState,
    onLikeClick: (index: Int) -> Unit,
    onContentClick: (index: Int) -> Unit,
) {
    Box(modifier = modifier) {
        LazyColumn(
            userScrollEnabled = false,
            state = scrollState
        ) {
            item {
                Spacer(modifier = Modifier.height(24.dp))
            }
            itemsIndexed(
                items = items,
                key = { _, info -> info.mediaId }
            ) { index, info ->
                SongListItem(
                    number = index + 1,
                    title = info.title,
                    onLikeClick = onLikeClick,
                    onContentClick = onContentClick
                )
            }
            item {
                Spacer(modifier = Modifier.height(bottomPadding))
            }
        }
    }
}