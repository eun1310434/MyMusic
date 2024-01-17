package com.euntaek.mymusic.service

import android.support.v4.media.MediaBrowserCompat
import android.support.v4.media.MediaDescriptionCompat
import android.support.v4.media.MediaMetadataCompat
import android.util.Log
import com.euntaek.mymusic.data.entities.Album
import com.euntaek.mymusic.data.entities.Song
import com.euntaek.mymusic.data.remote.MusicDatabase
import com.google.android.exoplayer2.MediaItem
import com.google.android.exoplayer2.source.ConcatenatingMediaSource
import com.google.android.exoplayer2.source.ProgressiveMediaSource
import com.google.android.exoplayer2.upstream.DefaultDataSourceFactory
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import javax.inject.Inject

class MusicSource @Inject constructor(
    private val musicDatabase: MusicDatabase
) {
    var album: Album? = null
    var songs = emptyList<MediaMetadataCompat>()

    suspend fun fetchMediaData() = withContext(Dispatchers.Main) {
        state = State.STATE_INITIALIZING
        Dispatchers.IO

//        val allAlbums = musicDatabase.getAlbums()
//        album = allAlbums.firstOrNull()
//        Log.e("Test", "" + allAlbums.toString())

        val allSongs = musicDatabase.getAllSongs()
        Log.e("Test", "" + allSongs.toString())
        songs = allSongs.map { song ->
            MediaMetadataCompat.Builder()
                .putString(MediaMetadataCompat.METADATA_KEY_TITLE, song.title)
                .putString(MediaMetadataCompat.METADATA_KEY_DISPLAY_TITLE, song.title)
                .putString(MediaMetadataCompat.METADATA_KEY_ARTIST, song.subtitle)
                .putString(MediaMetadataCompat.METADATA_KEY_DISPLAY_SUBTITLE, song.subtitle)
                .putString(MediaMetadataCompat.METADATA_KEY_MEDIA_ID, song.mediaId)
                .putString(MediaMetadataCompat.METADATA_KEY_MEDIA_URI, song.songUrl)
                .putString(MediaMetadataCompat.METADATA_KEY_DISPLAY_ICON_URI, song.imageUrl)
                .putString(MediaMetadataCompat.METADATA_KEY_ALBUM_ART_URI, song.imageUrl)
                .putString(MediaMetadataCompat.METADATA_KEY_DISPLAY_DESCRIPTION, song.subtitle)
                .build()
        }
        Dispatchers.Main
        state = State.STATE_INITIALIZED
    }

    fun asMediaSource(dataSourceFactory: DefaultDataSourceFactory): ConcatenatingMediaSource {
        val concatenatingMediaSource = ConcatenatingMediaSource()
        songs.forEach { song ->
            val mediaSource = ProgressiveMediaSource.Factory(dataSourceFactory)
                .createMediaSource(MediaItem.fromUri(song.getString(MediaMetadataCompat.METADATA_KEY_MEDIA_URI)))
            concatenatingMediaSource.addMediaSource(mediaSource)
        }
        return concatenatingMediaSource
    }

    fun asMediaItems() = songs.map { song ->
        val desc = MediaDescriptionCompat.Builder()
            .setMediaUri(song.description.mediaUri)
            .setTitle(song.description.title)
            .setSubtitle(song.description.subtitle)
            .setMediaId(song.description.mediaId)
            .setIconUri(song.description.iconUri)
            .build()
        MediaBrowserCompat.MediaItem(desc, MediaBrowserCompat.MediaItem.FLAG_PLAYABLE)
    }.toMutableList()

    private val onReadyListeners = mutableListOf<(Boolean) -> Unit>()

    private var state: State = State.STATE_CREATED
        set(value) {
            if (value == State.STATE_INITIALIZED || value == State.STATE_ERROR) {
                synchronized(onReadyListeners) {
                    field = value
                    onReadyListeners.forEach { listener ->
                        listener(state == State.STATE_INITIALIZED)
                    }
                }
            }
        }

    fun whenReady(action: (Boolean) -> Unit): Boolean {
        return if (state == State.STATE_CREATED || state == State.STATE_INITIALIZING) {
            onReadyListeners += action
            false
        } else {
            action(state == State.STATE_INITIALIZED)
            true
        }
    }
}

enum class State {
    STATE_CREATED,
    STATE_INITIALIZING,
    STATE_INITIALIZED,
    STATE_ERROR
}