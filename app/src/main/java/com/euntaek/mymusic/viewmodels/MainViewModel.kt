package com.euntaek.mymusic.viewmodels

import android.support.v4.media.MediaBrowserCompat
import android.support.v4.media.MediaMetadataCompat
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.euntaek.mymusic.App
import com.euntaek.mymusic.data.entities.Album
import com.euntaek.mymusic.data.entities.AppInfo
import com.euntaek.mymusic.data.entities.Artist
import com.euntaek.mymusic.data.entities.Song
import com.euntaek.mymusic.data.repository.Constants
import com.euntaek.mymusic.data.repository.Constants.MEDIA_ROOT_ID
import com.euntaek.mymusic.service.MusicServiceConnection
import com.euntaek.mymusic.ui.player.toSong
import com.euntaek.mymusic.utility.isPlayEnabled
import com.euntaek.mymusic.utility.isPlaying
import com.euntaek.mymusic.utility.isPrepared
import com.google.firebase.firestore.FirebaseFirestore
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.collectLatest
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

    private val _artists = MutableStateFlow(emptyList<Artist>())
    var artists = _artists.asStateFlow()

    private val _appInfo = MutableStateFlow<AppInfo?>(null)
    var appInfo = _appInfo.asStateFlow()

    val currentPlayingSong = musicServiceConnection.currentPlayingSong.asStateFlow()

    private val _playerBackgroundImage = MutableStateFlow<String?>(null)
    val playerBackgroundImage = _playerBackgroundImage.asStateFlow()

    private val _songs = MutableStateFlow<List<Song>?>(null)
    var songs = _songs.asStateFlow()

    private val _isPlayerFullScreenShowing = MutableStateFlow(false)
    var isPlayerFullScreenShowing = _isPlayerFullScreenShowing.asStateFlow()

    fun showFullScreenPlayer() {
        _isPlayerFullScreenShowing.value = true
    }

    fun hideFullScreenPlayer() {
        _isPlayerFullScreenShowing.value = false
    }

    //TODO
//    val isConnected = musicServiceConnection.isConnected
//    val networkError = musicServiceConnection.networkFailure
//    val curPlayingSong = musicServiceConnection.nowPlaying


    val songIsPlaying: Boolean
        get() = playbackState.value?.isPlaying == true

    val playbackState = musicServiceConnection.playbackState


    private val db = FirebaseFirestore.getInstance()
    private val albumCollection = db.collection(Constants.ALBUM_COLLECTION)
    private val songCollection = db.collection(Constants.SONG_COLLECTION)
    private val artistCollection = db.collection(Constants.ARTIST_COLLECTION)
    private val appInfoCollection = db.collection(Constants.APP_INFO_COLLECTION)

    init {
        viewModelScope.launch {
            currentPlayingSong.collectLatest {
                _playerBackgroundImage.value = _appInfo.value?.playerGifs?.random()
            }
        }

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
                }
            })

        getSongsInfo(appId = App.appId)
        getAlbumInfo(appId = App.appId)
        getArtistInfo(appId = App.appId)
        getAppInfo(appId = App.appId)
    }

    private fun getAlbumInfo(appId: String) {
        viewModelScope.launch(Dispatchers.IO) {
            try {
                val album = albumCollection
                    .get()
                    .await()
                    .toObjects(Album::class.java)
                _album.value = album.find { it.appId == appId }

            } catch (e: Exception) {
                Timber.tag("getAlbumInfo()").e(e)
            }
        }
    }

    private fun getSongsInfo(appId: String) {
        viewModelScope.launch(Dispatchers.IO) {
            try {
                val songs = songCollection
                    .get()
                    .await()
                    .toObjects(Song::class.java)
                    .sortedBy { it.order }
                _songs.value = songs.filter { it.appId == appId }

            } catch (e: Exception) {
                Timber.tag("getSongsInfo()").e(e)
            }
        }
    }

    private fun getArtistInfo(appId: String) {
        viewModelScope.launch(Dispatchers.IO) {
            try {
                val artist = artistCollection
                    .get()
                    .await()
                    .toObjects(Artist::class.java)
                _artists.value = artist.filter { it.appId == appId }
            } catch (e: Exception) {
                Timber.tag("getArtistInfo()").e(e)
            }
        }
    }


    fun getAppInfo(appId: String) {
        viewModelScope.launch(Dispatchers.IO) {
            try {
                val appInfo = appInfoCollection
                    .get()
                    .await()
                    .toObjects(AppInfo::class.java)
                _appInfo.value = appInfo.firstOrNull { it.id == appId }

            } catch (e: Exception) {
                Timber.tag("getAppInfo()").e(e)
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

    fun playOrToggleCurrentSong() {
        val currentSong = currentPlayingSong.value?.toSong()
        if (currentSong != null) {
            playOrToggleSong(mediaItem = currentSong, toggle = true)
        }
    }

    fun playOrToggleSong(mediaItem: Song, toggle: Boolean = false) {
        val isPrepared = playbackState.value?.isPrepared ?: false
        if (isPrepared && mediaItem.mediaId ==
            currentPlayingSong.value?.getString(MediaMetadataCompat.METADATA_KEY_MEDIA_ID)
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
