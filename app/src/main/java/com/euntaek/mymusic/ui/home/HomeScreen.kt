package com.euntaek.mymusic.ui.home

import androidx.annotation.DrawableRes
import androidx.compose.foundation.Image
import androidx.compose.foundation.clickable
import androidx.compose.foundation.gestures.Orientation
import androidx.compose.foundation.gestures.ScrollableDefaults
import androidx.compose.foundation.gestures.rememberScrollableState
import androidx.compose.foundation.gestures.scrollable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxScope
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ColumnScope
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.RowScope
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyListState
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.CircularProgressIndicator
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Favorite
import androidx.compose.material.icons.outlined.Favorite
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.ColorFilter
import androidx.compose.ui.graphics.vector.rememberVectorPainter
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.layout.layout
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.IntOffset
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import coil.compose.AsyncImage
import coil.compose.rememberAsyncImagePainter
import com.euntaek.mymusic.R
import com.euntaek.mymusic.data.entities.Album
import com.euntaek.mymusic.data.entities.Song
import com.euntaek.mymusic.ui.util.AnimatedText
import com.euntaek.mymusic.ui.viewmodels.MainViewModel
import com.euntaek.mymusic.utility.Resource
import kotlinx.coroutines.launch

@Composable
fun HomeScreen(viewModel: MainViewModel) {
    val album by viewModel.album.collectAsStateWithLifecycle()

    album?.let {
        MusicList(
            modifier = Modifier.fillMaxSize(),
            songs = viewModel.mediaItems.value,
            album = it,
            onItemClick = viewModel::playOrToggleSong
        )
    }
}

@Composable
fun MusicList(
    modifier: Modifier = Modifier,
    album: Album,
    songs: Resource<List<Song>>,
    onItemClick: (Song) -> Unit,
) {
    MusicList(
        modifier = modifier,
        topMenu = { scale ->
            TopMenu(
                modifier = Modifier
                    .padding(horizontal = 16.dp)
                    .fillMaxWidth()
                    .align(Alignment.CenterHorizontally)
                    .alpha(1 - scale)
                    .statusBarsPadding(),
                title = album.title,
                color = MaterialTheme.colorScheme.onSurface,
                endIconResId = R.drawable.ic_share,
                onEndIconClick = {}
            )
        },
        albumInfo = { scrollScale ->
            AlbumInfo(
                modifier = Modifier.fillMaxWidth(),
                albumImageUrl = album.imageUrl,
                imageSize = 200.dp * scrollScale + 100.dp,
                title = album.title,
                subTitle = album.subTitle,
                useAnimation = true
            )
        },
        songList = { _, lazyListState ->
            when (songs) {
                is Resource.Success -> {
                    Box(
                        modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.BottomCenter
                    ) {
                        LazyColumn(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(bottom = 60.dp)
                                .align(Alignment.TopCenter),
                            state = lazyListState,
                            userScrollEnabled = false
                        ) {
                            items(songs.data!!) { song ->
                                MusicListItem(song = song, onClick = onItemClick)
                            }
                        }
                    }
                }

                is Resource.Loading -> {
                    Box(modifier = Modifier.fillMaxSize()) {
                        CircularProgressIndicator(
                            color = MaterialTheme.colorScheme.onBackground,
                            modifier = Modifier
                                .size(100.dp)
                                .fillMaxHeight()
                                .align(Alignment.Center)
                                .padding(16.dp)
                        )
                    }
                }

                is Resource.Error -> {
                }
            }
        }
    )
}

@Composable
fun MusicList(
    modifier: Modifier = Modifier,
    topMenu: @Composable ColumnScope.(scale: Float) -> Unit,
    albumInfo: @Composable BoxScope.(scale: Float) -> Unit,
    songList: @Composable (scale: Float, lazyListState: LazyListState) -> Unit
) {
    val scope = rememberCoroutineScope()
    var scrollScale by remember { mutableFloatStateOf(1f) }
    val songListState = rememberLazyListState()
    val albumDescriptionScrollState = rememberScrollState()
    var albumDescriptionHeight by remember { mutableIntStateOf(0) }

    val columnScrollableState = rememberScrollableState { delta ->
        scope.launch {
            if (delta > 0) { // Column Scroll down
                if (songListState.canScrollBackward) {
                    songListState.dispatchRawDelta(-delta)
                } else {
                    albumDescriptionScrollState.dispatchRawDelta(-delta)
                }
            } else { // Column Scroll up
                if (albumDescriptionHeight - albumDescriptionScrollState.value > 0) {
                    albumDescriptionScrollState.dispatchRawDelta(-delta)
                } else {
                    songListState.dispatchRawDelta(-delta)
                }
            }
        }
        delta
    }

    Column(
        modifier = modifier.scrollable(
            state = columnScrollableState,
            orientation = Orientation.Vertical,
            flingBehavior = ScrollableDefaults.flingBehavior()
        ),
        horizontalAlignment = Alignment.CenterHorizontally,
    ) {
        topMenu(scrollScale)
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .layout { measurable, constraints ->
                    val placeable = measurable.measure(constraints)
                    val offsetValue = IntOffset.Zero.copy(y = -albumDescriptionScrollState.value)

                    albumDescriptionHeight = placeable.height

                    layout(
                        width = placeable.width + offsetValue.x,
                        height = placeable.height + offsetValue.y,
                        placementBlock = {
                            placeable.placeRelativeWithLayer(
                                position = offsetValue,
                                zIndex = -1f,
                                layerBlock = {
                                    val scale =
                                        1 + (offsetValue.y.toFloat() / placeable.height.toFloat())

                                    scrollScale = scale.coerceIn(0.001f, 1f)

                                    this.alpha = scale
                                }
                            )
                        }
                    )
                },
            content = { albumInfo(scrollScale) }
        )
        songList(scrollScale, songListState)
    }
}

@Composable
fun MusicListItem(song: Song, onClick: (Song) -> Unit) {
    MusicListItem(
        author = "author",
        title = song.title,
        imageUrl = song.imageUrl,
        duration = "1:00",
        onLikeClick = {},
        onContentClick = { onClick(song) }
    )
}

@Composable
fun MusicListItem(
    author: String = "",
    title: String,
    imageUrl: String,
    duration: String = "",
    isLiked: Boolean = true,
    onLikeClick: () -> Unit,
    onContentClick: () -> Unit,
) {
    val likedIcon = rememberVectorPainter(image = Icons.Filled.Favorite)
    val notLikedIcon = rememberVectorPainter(image = Icons.Outlined.Favorite)

    Row(
        modifier = Modifier
            .height(80.dp)
            .fillMaxWidth()
            .clip(shape = RoundedCornerShape(percent = 5))
            .clickable { onContentClick() },
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
        IconButton(onClick = { onLikeClick() }) {
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
fun AlbumInfo(
    modifier: Modifier,
    albumImageUrl: String,
    imageSize: Dp,
    title: String,
    subTitle: String,
    useAnimation: Boolean
) {
    Column(modifier = modifier) {
        Box(
            modifier = Modifier
                .align(Alignment.CenterHorizontally)
                .size(imageSize)
                .clip(RoundedCornerShape(imageSize)),
            content = {
                AsyncImage(
                    modifier = Modifier
                        .fillMaxSize()
                        .aspectRatio(1f),
                    model = albumImageUrl,
                    contentDescription = null,
                )
            },
        )
        Box(modifier = Modifier.fillMaxWidth()) {
            Labels(title = title, author = subTitle, useAnimation = useAnimation)
        }
    }
}

@Composable
private fun BoxScope.Labels(
    title: String,
    author: String,
    useAnimation: Boolean
) {
    Column(
        modifier = Modifier
            .align(Alignment.Center)
            .padding(bottom = 16.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
    ) {
        Spacer(modifier = Modifier.height(16.dp))
        AnimatedText(
            text = title,
            useAnimation = useAnimation,
            animationDelay = 350L,
            style = MaterialTheme.typography.headlineLarge,
            textColor = MaterialTheme.colorScheme.onSurface
        )
        Spacer(modifier = Modifier.height(8.dp))
        Text(
            text = author,
            textAlign = TextAlign.Center,
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurface,
        )
    }
}


@Composable
fun TopMenu(
    modifier: Modifier = Modifier,
    title: String,
    color: Color = androidx.compose.material.MaterialTheme.colors.onSurface,
    @DrawableRes startIconResId: Int = -1,
    @DrawableRes endIconResId: Int = -1,
    onStartIconClick: () -> Unit = {},
    onEndIconClick: () -> Unit = {},
) {
    TopMenu(
        modifier = modifier,
        startIcon = if (startIconResId != -1) {
            {
                androidx.compose.material.IconButton(onClick = onStartIconClick) {
                    Image(
                        painter = painterResource(id = startIconResId),
                        contentDescription = "",
                        colorFilter = ColorFilter.tint(color),
                    )
                }
            }
        } else null,
        title = {
            androidx.compose.material.Text(
                text = title,
                modifier = Modifier.weight(1f),
                textAlign = TextAlign.Center,
                fontWeight = FontWeight.Bold,
                color = color,
            )
        },
        endIcon = if (endIconResId != -1) {
            {
                androidx.compose.material.IconButton(onClick = onEndIconClick) {
                    Image(
                        painter = painterResource(id = endIconResId),
                        contentDescription = "",
                        colorFilter = ColorFilter.tint(color),
                    )
                }
            }
        } else null
    )
}

@Composable
fun TopMenu(
    modifier: Modifier = Modifier,
    startIcon: @Composable (RowScope.() -> Unit)? = null,
    title: @Composable RowScope.() -> Unit,
    endIcon: @Composable (RowScope.() -> Unit)? = null,
) {
    Row(
        modifier = modifier,
        verticalAlignment = Alignment.CenterVertically,
    ) {
        if (startIcon != null) {
            startIcon()
        }
        title()
        if (endIcon != null) {
            endIcon()
        }
    }
}
