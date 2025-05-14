package com.euntaek.mymusic.ui.profile

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.AnimatedVisibilityScope
import androidx.compose.animation.ExperimentalSharedTransitionApi
import androidx.compose.animation.SharedTransitionScope
import androidx.compose.animation.core.FastOutLinearInEasing
import androidx.compose.animation.core.LinearOutSlowInEasing
import androidx.compose.animation.core.tween
import androidx.compose.animation.slideIn
import androidx.compose.animation.slideOut
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.IntOffset
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.euntaek.ui.CachedAsyncImage
import com.euntaek.mymusic.ui.player.SmallMusicPlayerDefaults
import com.euntaek.mymusic.viewmodels.MainViewModel


@OptIn(ExperimentalSharedTransitionApi::class)
@Composable
fun SharedTransitionScope.ProfileDetailScreen(
    viewModel: MainViewModel,
    animatedVisibilityScope: AnimatedVisibilityScope,
    artistId: String
) {

    val state = rememberLazyListState()
    val musicData by viewModel.musicData.collectAsStateWithLifecycle()
    val artist = musicData?.artists?.firstOrNull { it.id == artistId }
    Box(modifier = Modifier.fillMaxSize()) {
        com.euntaek.ui.CachedAsyncImage(
            imageUrl = artist?.image.orEmpty(),
            contentScale = ContentScale.Crop,
            modifier = Modifier
                .height((LocalConfiguration.current.screenHeightDp * 0.75).dp)
                .sharedElement(
                    sharedContentState = rememberSharedContentState(key = "artistId/${artist?.id}"),
                    animatedVisibilityScope = animatedVisibilityScope,
                    boundsTransform = { _, _ ->
                        tween(durationMillis = 1000)
                    }
                )
        )
        AnimatedVisibility(
            modifier = Modifier.fillMaxSize(),
            visible = !isTransitionActive,
            enter = slideIn(tween(500, easing = LinearOutSlowInEasing)) {
                IntOffset(0, 100)
            },
            exit = slideOut(tween(100, easing = FastOutLinearInEasing)) {
                IntOffset(0, 1000)
            },
        ) {
            LazyColumn(
                modifier = Modifier.fillMaxSize(),
                state = state
            ) {
                item {
                    Spacer(modifier = Modifier.height((LocalConfiguration.current.screenHeightDp * 0.7).dp))
                }
                item {
                    BottomSheetContent(
                        title = artist?.name.orEmpty(),
                        subTitle = artist?.type.orEmpty(),
                        content = artist?.description.orEmpty()
                    )
                }
            }
        }
    }
}

@Composable
fun BottomSheetContent(
    title: String,
    subTitle: String,
    content: String,
) {
    Column(
        modifier = Modifier
            .clip(shape = RoundedCornerShape(topStart = 30.dp, topEnd = 30.dp))
            .background(MaterialTheme.colorScheme.background)
            .fillMaxWidth()
    ) {
        Box(
            modifier = Modifier
                .padding(10.dp)
                .align(Alignment.CenterHorizontally)
                .size(width = 30.dp, height = 3.dp)
                .clip(RoundedCornerShape(5.dp))
                .background(
                    color = MaterialTheme.colorScheme.secondary.copy(alpha = 0.75f)
                )
        )
        Text(
            text = title,
            style = MaterialTheme.typography.titleLarge.copy(fontWeight = FontWeight.Bold),
            modifier = Modifier
                .padding(horizontal = 20.dp)
                .align(Alignment.Start),
            color = MaterialTheme.colorScheme.onBackground
        )
        Spacer(modifier = Modifier.height(5.dp))
        Text(
            text = subTitle,
            style = MaterialTheme.typography.bodyMedium,
            modifier = Modifier
                .padding(horizontal = 20.dp)
                .align(Alignment.Start),
            color = MaterialTheme.colorScheme.onBackground.copy(alpha = 0.75f)
        )
        Spacer(modifier = Modifier.height(20.dp))
        Text(
            modifier = Modifier
                .padding(horizontal = 20.dp)
                .background(MaterialTheme.colorScheme.background),
            style = MaterialTheme.typography.bodyLarge,
            text = content,
            color = MaterialTheme.colorScheme.onBackground
        )
        Spacer(modifier = Modifier.height(SmallMusicPlayerDefaults.Height * 2))
    }
}


//    Column(
//        modifier = Modifier
//            .fillMaxSize(),
//        horizontalAlignment = Alignment.CenterHorizontally
//    ) {
//        CachedAsyncImage(
//            imageUrl = artist?.images?.get(index).orEmpty(),
//            contentScale = ContentScale.Crop,
//            modifier = Modifier
//                .fillMaxHeight(0.75f)
//                .sharedElement(
//                    state = rememberSharedContentState(key = "index/$index"),
//                    animatedVisibilityScope = animatedVisibilityScope,
//                    boundsTransform = { _, _ ->
//                        tween(durationMillis = 1000)
//                    }
//                )
//        )
//        Spacer(modifier = Modifier.height(16.dp))
//        Text(
//            text = text,
//            modifier = Modifier
//                .weight(1f)
//                .sharedElement(
//                    state = rememberSharedContentState(key = "text/$text"),
//                    animatedVisibilityScope = animatedVisibilityScope,
//                    boundsTransform = { _, _ ->
//                        tween(durationMillis = 1000)
//                    }
//                )
//        )
//    }