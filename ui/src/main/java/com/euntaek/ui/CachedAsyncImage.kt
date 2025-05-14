package com.euntaek.ui

import android.net.Uri
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import coil.compose.AsyncImage
import coil.decode.ImageDecoderDecoder
import coil.request.CachePolicy
import coil.request.ImageRequest
/**
 * A composable that executes an [ImageRequest] asynchronously and renders the result.
 *
 * @param imageUrl The URL of the image to load, mapped to a [Uri].
 * @param modifier The [Modifier] used to adjust the layout or draw decoration content.
 * @param contentScale An optional parameter used to determine the aspect ratio scaling
 * if the bounds are a different size from the intrinsic size of the [CachedAsyncImage].
 * @param isGIF Enables the use of ImageDecoder to decode GIFs, animated WebPs, and animated HEIFs.
 */
@Composable
fun CachedAsyncImage(
    imageUrl: String,
    modifier: Modifier = Modifier,
    contentScale: ContentScale = ContentScale.Fit,
    isGIF: Boolean = false
) {
    val context = LocalContext.current

    val imageRequest = ImageRequest.Builder(context)
        .data(imageUrl)
        .diskCacheKey(imageUrl)
        .diskCachePolicy(CachePolicy.ENABLED)
        .memoryCacheKey(imageUrl)
        .memoryCachePolicy(CachePolicy.ENABLED)
        .apply {
            if (isGIF) this.decoderFactory(ImageDecoderDecoder.Factory())
        }
        .build()

    AsyncImage(
        modifier = modifier,
        contentScale = contentScale,
        model = imageRequest,
        contentDescription = null
    )
}