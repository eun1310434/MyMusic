package com.euntaek.mymusic.viewmodels

import android.support.v4.media.MediaMetadataCompat
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.euntaek.mymusic.App
import com.euntaek.mymusic.data.entities.Album
import com.euntaek.mymusic.data.entities.AppInfo
import com.euntaek.mymusic.data.entities.Artist
import com.euntaek.mymusic.data.entities.Song
import com.euntaek.mymusic.data.repository.Constants
import com.euntaek.mymusic.data.repository.Constants.UPDATE_PLAYER_POSITION_INTERVAL
import com.euntaek.mymusic.service.MusicService
import com.euntaek.mymusic.service.MusicServiceConnection
import com.euntaek.mymusic.utility.currentPlaybackPosition
import com.euntaek.mymusic.utility.isPlayEnabled
import com.euntaek.mymusic.utility.isPlaying
import com.euntaek.mymusic.utility.isPrepared
import com.google.firebase.firestore.FirebaseFirestore
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.flow.map
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

    val currentPlayingSong = musicServiceConnection.currentPlayingSong

    private val _playerBackgroundImage = MutableStateFlow<String?>(null)
    val playerBackgroundImage = _playerBackgroundImage.asStateFlow()

    private val _songs = MutableStateFlow<List<Song>?>(null)
    var songs = _songs.asStateFlow()

    private val _isPlayerFullScreenShowing = MutableStateFlow(false)
    var isPlayerFullScreenShowing = _isPlayerFullScreenShowing.asStateFlow()

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

    fun prepareFirstSong() {
        val firstSong = _songs.value?.firstOrNull()
        if (firstSong != null) {
            musicServiceConnection.setCurrentPlayingSong(firstSong)
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

    private val _currentPlaybackPosition = MutableStateFlow(0L)
    val currentPlaybackPosition = _currentPlaybackPosition.asStateFlow()

    suspend fun updateCurrentPlaybackPosition() {
        val currentPosition = playbackState.value?.currentPlaybackPosition
        if (currentPosition != null && currentPosition != _currentPlaybackPosition.value) {
            _currentPlaybackPosition.value = currentPosition
        }
        delay(UPDATE_PLAYER_POSITION_INTERVAL)
        updateCurrentPlaybackPosition()
    }

    fun showFullScreenPlayer() {
        _isPlayerFullScreenShowing.value = true
    }

    fun hideFullScreenPlayer() {
        _isPlayerFullScreenShowing.value = false
    }
}
