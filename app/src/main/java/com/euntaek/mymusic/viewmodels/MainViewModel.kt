package com.euntaek.mymusic.viewmodels

import android.support.v4.media.MediaMetadataCompat
import androidx.lifecycle.viewModelScope
import com.euntaek.mymusic.App
import com.euntaek.mymusic.BuildConfig
import com.euntaek.mymusic.data.entities.AppInfo
import com.euntaek.mymusic.data.entities.MusicData
import com.euntaek.mymusic.data.entities.Song
import com.euntaek.mymusic.data.repository.Constants.UPDATE_PLAYER_POSITION_INTERVAL
import com.euntaek.mymusic.service.MusicServiceConnection
import com.euntaek.mymusic.usecase.GetAppInfoUseCase
import com.euntaek.mymusic.usecase.GetMusicDataUseCase
import com.euntaek.mymusic.utility.currentPlaybackPosition
import com.euntaek.mymusic.utility.execUsesCase
import com.euntaek.mymusic.utility.isPlayEnabled
import com.euntaek.mymusic.utility.isPlaying
import com.euntaek.mymusic.utility.isPrepared
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import javax.inject.Inject


@HiltViewModel
class MainViewModel @Inject constructor(
    private val musicServiceConnection: MusicServiceConnection,
    private val getMusicDataUseCase: GetMusicDataUseCase,
    private val getAppInfoUseCase: GetAppInfoUseCase
) : BaseViewModel() {
    private val _musicData: MutableStateFlow<MusicData?> = MutableStateFlow(null)
    val musicData = _musicData.asStateFlow()

    private val _appInfo = MutableStateFlow<AppInfo?>(null)
    val appInfo = _appInfo.asStateFlow()

    val isSettingUpdateMarkEnabled = _appInfo.mapState {
        it?.version != BuildConfig.VERSION_NAME
    }

    val currentPlayingSong = musicServiceConnection.currentPlayingSong

    val playerBackgroundImage = currentPlayingSong.mapState {
        _appInfo.value?.playerGifs?.random()
    }

    private val _isPlayerFullScreenShowing = MutableStateFlow(false)
    val isPlayerFullScreenShowing = _isPlayerFullScreenShowing.asStateFlow()

    val songIsPlaying: Boolean
        get() = playbackState.value?.isPlaying == true

    val playbackState = musicServiceConnection.playbackState

    init {
        getMusicData()
        getAppInfo(appId = App.appId)
    }

    private fun getMusicData() {
        viewModelScope.launch(Dispatchers.IO) {
            execUsesCase(
                load = { getMusicDataUseCase() },
                success = { musicData -> _musicData.update { musicData } }
            )
        }
    }


    fun getAppInfo(appId: String) {
        viewModelScope.launch(Dispatchers.IO) {
            execUsesCase(
                load = { getAppInfoUseCase() },
                success = { appInfo -> _appInfo.value = appInfo.firstOrNull { it.id == appId } }
            )
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
        val firstSong = _musicData.value?.songs?.firstOrNull()
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
