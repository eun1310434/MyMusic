package com.euntaek.mymusic.ui.viewmodels

import android.graphics.Bitmap
import android.graphics.drawable.Drawable
import android.support.v4.media.MediaBrowserCompat
import android.support.v4.media.MediaMetadataCompat
import android.util.Log
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.euntaek.mymusic.data.entities.Album
import com.euntaek.mymusic.data.entities.Song
import com.euntaek.mymusic.data.remote.Constants
import com.euntaek.mymusic.data.remote.Constants.MEDIA_ROOT_ID
import com.euntaek.mymusic.data.remote.MusicDatabase
import com.euntaek.mymusic.service.MusicServiceConnection
import com.euntaek.mymusic.utility.Resource
import com.euntaek.mymusic.utility.isPlayEnabled
import com.euntaek.mymusic.utility.isPlaying
import com.euntaek.mymusic.utility.isPrepared
import com.google.firebase.firestore.FirebaseFirestore
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import kotlinx.coroutines.tasks.await
import timber.log.Timber
import javax.inject.Inject

@HiltViewModel
class MainViewModel @Inject constructor(
    private val musicServiceConnection: MusicServiceConnection,
) : ViewModel() {

    private val _album = MutableStateFlow<Album?>(null)
    var album = _album.asStateFlow()

    private val _mediaItems = MutableStateFlow<Resource<List<Song>>>(Resource.Loading(null))
    var mediaItems = mutableStateOf<Resource<List<Song>>>(Resource.Loading(null))

    var showPlayerFullScreen by mutableStateOf(false)

    val isConnected = musicServiceConnection.isConnected
    val networkError = musicServiceConnection.networkFailure
    val curPlayingSong = musicServiceConnection.nowPlaying

    val currentPlayingSong = musicServiceConnection.currentPlayingSong

    val songIsPlaying: Boolean
        get() = playbackState.value?.isPlaying == true

    val playbackState = musicServiceConnection.playbackState


    private val db = FirebaseFirestore.getInstance()
    private val albumCollection = db.collection(Constants.ALBUM_COLLECTION)

    init {
        mediaItems.value = (Resource.Loading(null))
        musicServiceConnection.subscribe(
            MEDIA_ROOT_ID,
            object : MediaBrowserCompat.SubscriptionCallback() {
                override fun onChildrenLoaded(
                    parentId: String,
                    children: MutableList<MediaBrowserCompat.MediaItem>
                ) {
                    super.onChildrenLoaded(parentId, children)
                    val items = children.map {
                        Song(
                            mediaId = it.mediaId!!,
                            title = it.description.title.toString(),
                            subtitle = it.description.subtitle.toString(),
                            songUrl = it.description.mediaUri.toString(),
                            imageUrl = it.description.iconUri.toString()
                        )
                    }
                    Timber.e("SubscriptionCallback")
                    mediaItems.value = Resource.Success(items)
                }
            })


        viewModelScope.launch(Dispatchers.IO) {
            try {
                val album = albumCollection
                    .get()
                    .await()
                    .toObjects(Album::class.java)
                _album.value = album.firstOrNull()
                Log.e("Test", "" + album.toString())

            } catch (e: Exception) {
                Log.e("Test", "getAlbum() error : " + e.message)
            }
        }

    }

    fun skipToNextSong() {
        musicServiceConnection.transportController.skipToNext()
    }

    fun skipToPreviousSong() {
        musicServiceConnection.transportController.skipToPrevious()
    }

    fun seekTo(pos: Float) {
        musicServiceConnection.transportController.seekTo(pos.toLong())
    }

    fun playOrToggleSong(mediaItem: Song, toggle: Boolean = false) {
        val isPrepared = playbackState.value?.isPrepared ?: false
        if (isPrepared && mediaItem.mediaId ==
            currentPlayingSong.value?.getString(MediaMetadataCompat.METADATA_KEY_MEDIA_ID)
//            curPlayingSong.value?.getString(METADATA_KEY_MEDIA_ID)
        ) {
            playbackState.value?.let { playbackState ->
                when {
                    playbackState.isPlaying -> {
                        if (toggle) musicServiceConnection.transportController.pause()
                    }

                    playbackState.isPlayEnabled -> {
                        musicServiceConnection.transportController.play()
                    }

                    else -> Unit
                }
            }
        } else {
            musicServiceConnection.transportController.playFromMediaId(mediaItem.mediaId, null)
        }
    }

    override fun onCleared() {
        super.onCleared()
        musicServiceConnection.unsubscribe(
            MEDIA_ROOT_ID,
            object : MediaBrowserCompat.SubscriptionCallback() {})
    }
}
