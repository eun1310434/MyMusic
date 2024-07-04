package com.euntaek.mymusic.ui.components

import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import coil.compose.AsyncImage
import coil.decode.ImageDecoderDecoder
import coil.request.CachePolicy
import coil.request.ImageRequest

@Composable
fun CachedAsyncImage(
    modifier: Modifier,
    contentScale: ContentScale = ContentScale.Fit,
    imageUrl: String
) {
    val context = LocalContext.current

    val imageRequest = ImageRequest.Builder(context)
        .data(imageUrl)
        .memoryCacheKey(imageUrl)
        .diskCacheKey(imageUrl)
        .diskCachePolicy(CachePolicy.ENABLED)
        .memoryCachePolicy(CachePolicy.ENABLED)
        .decoderFactory(ImageDecoderDecoder.Factory())
        .build()

    AsyncImage(
        modifier = modifier,
        contentScale = contentScale,
        model = imageRequest,
        contentDescription = null
    )
}