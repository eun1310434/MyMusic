package com.euntaek.mymusic.ui.profile

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.AnimatedVisibilityScope
import androidx.compose.animation.ExperimentalSharedTransitionApi
import androidx.compose.animation.SharedTransitionScope
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.GridItemSpan
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.euntaek.mymusic.data.entities.Artist
import com.euntaek.mymusic.ui.components.CachedAsyncImage
import com.euntaek.mymusic.ui.player.SmallMusicPlayerDefaults
import com.euntaek.mymusic.viewmodels.MainViewModel


@OptIn(ExperimentalSharedTransitionApi::class)
@Composable
fun SharedTransitionScope.ProfileScreen(
    viewModel: MainViewModel,
    animatedVisibilityScope: AnimatedVisibilityScope,
    onItemClick: (String) -> Unit //artistId
) {
    val musicData by viewModel.musicData.collectAsStateWithLifecycle()

    val columnsCount = 2
    val paddingDp = 5.dp

    LazyVerticalGrid(
        modifier = Modifier.fillMaxSize(),
        columns = GridCells.Fixed(columnsCount),
        contentPadding = PaddingValues(paddingDp),
        horizontalArrangement = Arrangement.spacedBy(paddingDp),
        verticalArrangement = Arrangement.spacedBy(paddingDp)
    ) {
        musicData?.artists?.forEachIndexed { index, artist ->
            if (index == 0) {
                item(span = { GridItemSpan(columnsCount) }) {
                    ProfileItem(
                        modifier = Modifier
                            .clickable { onItemClick(artist.id) }
                            .fillMaxWidth(),
                        artist = artist,
                        contentScale = ContentScale.Fit,
                        animatedVisibilityScope = animatedVisibilityScope
                    )
                }
            } else {
                item {
                    ProfileItem(
                        modifier = Modifier
                            .clickable { onItemClick(artist.id) }
                            .aspectRatio(0.75f),
                        artist = artist,
                        contentScale = ContentScale.Crop,
                        animatedVisibilityScope = animatedVisibilityScope,
                    )
                }
            }
        }
        item(span = { GridItemSpan(columnsCount) }) {
            Spacer(modifier = Modifier.height(SmallMusicPlayerDefaults.Height))
        }
    }
}

@OptIn(ExperimentalSharedTransitionApi::class)
@Composable
fun SharedTransitionScope.ProfileItem(
    modifier: Modifier,
    artist: Artist,
    contentScale: ContentScale,
    animatedVisibilityScope: AnimatedVisibilityScope
) {
    val gradientColors = listOf(
        MaterialTheme.colorScheme.background,
        Color.Transparent,
        Color.Transparent
    )
    Box(
        modifier = modifier
            .clip(shape = RoundedCornerShape(percent = 5))
    ) {
        CachedAsyncImage(
            modifier = Modifier
                .fillMaxSize()
                .sharedElement(
                    sharedContentState = rememberSharedContentState(key = "artistId/${artist.id}"),
                    animatedVisibilityScope = animatedVisibilityScope,
                    boundsTransform = { _, _ ->
                        tween(durationMillis = 1000)
                    }
                ),
            contentScale = contentScale,
            imageUrl = artist.image
        )
        AnimatedVisibility(
            modifier = Modifier
                .fillMaxWidth()
                .padding(10.dp)
                .align(Alignment.BottomCenter),
            visible = !isTransitionActive,
            enter = fadeIn(
                // Overwrites the initial value of alpha to 0.4f for fade in, 0 by default
                initialAlpha = 0.4f
            ),
            exit = fadeOut(
                // Overwrites the default animation with tween
                animationSpec = tween(durationMillis = 250)
            )
        ) {
            Column(
                modifier = Modifier.background(Brush.horizontalGradient(colors = gradientColors)),
                horizontalAlignment = Alignment.Start,
            ) {
                Text(
                    text = artist.name,
                    style = MaterialTheme.typography.bodyLarge.copy(fontWeight = FontWeight.Bold),
                    color = MaterialTheme.colorScheme.onBackground,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
                //Spacer(modifier = Modifier.height(5.dp))
                Text(
                    text = artist.type,
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onBackground.copy(alpha = 0.75f),
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
            }
        }
    }
}