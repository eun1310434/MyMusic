package com.euntaek.mymusic.utility

import android.support.v4.media.MediaMetadataCompat
import com.euntaek.mymusic.data.entities.Song
import java.text.SimpleDateFormat
import java.util.Locale

fun Long.formatLong(): String {
    val dateFormat = SimpleDateFormat("mm:ss", Locale.getDefault())
    return dateFormat.format(this)
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