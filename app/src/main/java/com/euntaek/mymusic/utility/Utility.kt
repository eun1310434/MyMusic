package com.euntaek.mymusic.utility

import android.support.v4.media.MediaMetadataCompat
import com.euntaek.mymusic.data.entities.Song
import timber.log.Timber
import java.text.SimpleDateFormat
import java.util.Locale

fun Long.formatLong(): String {
    return if (this > -1) {
        SimpleDateFormat("mm:ss", Locale.getDefault()).format(this)
    } else ""
}

fun Song.toMediaMetadata(): MediaMetadataCompat = MediaMetadataCompat.Builder()
    .putString(MediaMetadataCompat.METADATA_KEY_TITLE, this.title)
    .putString(MediaMetadataCompat.METADATA_KEY_DISPLAY_TITLE, this.title)
    .putString(MediaMetadataCompat.METADATA_KEY_ARTIST, this.subtitle)
    .putString(MediaMetadataCompat.METADATA_KEY_DISPLAY_SUBTITLE, this.subtitle)
    .putString(MediaMetadataCompat.METADATA_KEY_MEDIA_ID, this.mediaId)
    .putString(MediaMetadataCompat.METADATA_KEY_MEDIA_URI, this.songUrl)
    .putString(MediaMetadataCompat.METADATA_KEY_DISPLAY_ICON_URI, this.imageUrl)
    .putString(MediaMetadataCompat.METADATA_KEY_ALBUM_ART_URI, this.imageUrl)
    .putString(MediaMetadataCompat.METADATA_KEY_DISPLAY_DESCRIPTION, this.subtitle)
    .build()


fun MediaMetadataCompat.toSong(): Song? {
    return description?.let {
        Timber.e(it.title.toString()+"  " +  this.getLong(MediaMetadataCompat.METADATA_KEY_DURATION))
        Song(
            mediaId = it.mediaId.orEmpty(),
            title = it.title.toString(),
            subtitle = it.subtitle.toString(),
            songUrl = it.mediaUri.toString(),
            imageUrl = it.iconUri.toString(),
            duration = this.getLong(MediaMetadataCompat.METADATA_KEY_DURATION)
        )
    }
}

suspend fun <T> execUsesCase(
    load: suspend () -> Either<T>,
    success: (T) -> Unit,
    error: (Exception) -> Unit = {}
) {
    when (val result = load()) {
        is Either.Success -> success(result.data)
        is Either.Error -> error(result.exception)
    }
}
