package com.euntaek.mymusic.ui.home

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.AnimatedVisibilityScope
import androidx.compose.animation.ExperimentalSharedTransitionApi
import androidx.compose.animation.SharedTransitionScope
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.collectIsDraggedAsState
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.wrapContentHeight
import androidx.compose.foundation.layout.wrapContentSize
import androidx.compose.foundation.pager.HorizontalPager
import androidx.compose.foundation.pager.rememberPagerState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.euntaek.mymusic.data.entities.Artist
import com.euntaek.ui.CachedAsyncImage
import kotlinx.coroutines.delay


@OptIn(ExperimentalSharedTransitionApi::class)
@Composable
fun SharedTransitionScope.AlbumInfo(
    title: String,
    artists: List<Artist>,
    animatedVisibilityScope: AnimatedVisibilityScope,
    scale: Float,
    onImageClick: (String) -> Unit //index
) {
    var isReversed by remember { mutableStateOf(false) }
    val horizontalPagerState = rememberPagerState(pageCount = { artists.size })
    val isDragged by horizontalPagerState.interactionSource.collectIsDraggedAsState()

    if (!isDragged) {
        LaunchedEffect(Unit) {
            with(horizontalPagerState) {
                repeat(
                    times = Int.MAX_VALUE,
                    action = {
                        delay(timeMillis = 5000)
                        horizontalPagerState.animateScrollToPage(
                            page = if (isReversed) currentPage - 1 else currentPage + 1
                        )

                        if (currentPage == pageCount - 1 || currentPage == 0) {
                            isReversed = !isReversed
                        }
                    }
                )
            }
        }
    }

    Box(
        modifier = Modifier
            .fillMaxWidth()
            .padding(bottom = 10.dp)
            .background(color = MaterialTheme.colorScheme.secondaryContainer)
            .shadow(
                elevation = 5.dp,
                shape = RoundedCornerShape(bottomStart = 30.dp * scale, bottomEnd = 30.dp * scale)
            )
    ) {
        HorizontalPager(
            modifier = Modifier
                .fillMaxHeight(0.6f)
                .fillMaxWidth(),
            state = horizontalPagerState,
            beyondViewportPageCount = 4
        ) { page ->
            val artist = artists[page]
            CachedAsyncImage(
                modifier = Modifier
                    .clickable { onImageClick(artist.id) }
                    .fillMaxWidth()
                    .sharedElement(
                        sharedContentState = rememberSharedContentState(key = "artistId/${artist.id}"),
                        animatedVisibilityScope = animatedVisibilityScope,
                        boundsTransform = { _, _ ->
                            tween(durationMillis = 1000)
                        }
                    ),
                contentScale = ContentScale.Crop,
                imageUrl = artist.image
            )
        }

        AnimatedVisibility(
            modifier = Modifier
                .wrapContentHeight()
                .fillMaxWidth()
                .align(Alignment.TopCenter)
                .padding(13.dp),
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
            Row(
                modifier = Modifier.wrapContentSize(),
                horizontalArrangement = Arrangement.Center
            ) {
                repeat(horizontalPagerState.pageCount) { iteration ->
                    val color =
                        if (horizontalPagerState.currentPage == iteration) MaterialTheme.colorScheme.onSecondaryContainer else MaterialTheme.colorScheme.secondaryContainer
                    Box(
                        modifier = Modifier
                            .padding(2.dp)
                            .clip(CircleShape)
                            .background(color)
                            .size(5.dp)
                    )
                }
            }
        }

        AnimatedVisibility(
            modifier = Modifier
                .wrapContentSize()
                .padding(vertical = 8.dp, horizontal = 32.dp)
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
            Text(
                modifier = Modifier.wrapContentSize(),
                text = title,
                style = MaterialTheme.typography.displayLarge.copy(fontWeight = FontWeight.Bold),
                color = MaterialTheme.colorScheme.onPrimary.copy(alpha = scale)
            )
        }
    }
}
