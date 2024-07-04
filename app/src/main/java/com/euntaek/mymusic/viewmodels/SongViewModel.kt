package com.euntaek.mymusic.viewmodels

import android.graphics.Bitmap
import androidx.compose.ui.graphics.Color
import androidx.lifecycle.ViewModel
import androidx.palette.graphics.Palette
import com.euntaek.mymusic.data.repository.Constants.UPDATE_PLAYER_POSITION_INTERVAL
import com.euntaek.mymusic.service.MusicService
import com.euntaek.mymusic.service.MusicServiceConnection
import com.euntaek.mymusic.utility.currentPlaybackPosition
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import javax.inject.Inject


@HiltViewModel
class SongViewModel @Inject constructor(
    musicServiceConnection: MusicServiceConnection
) : ViewModel() {

    private val playbackState = musicServiceConnection.playbackState

    private val _currentPlaybackPosition = MutableStateFlow(0L)
    val currentPlaybackPosition = _currentPlaybackPosition.asStateFlow()

    fun setCurrentPlaybackPosition(position:Long) {
        _currentPlaybackPosition.value = position
    }

    val currentPlayerPosition: Float
        get() {
            if (currentSongDuration > 0) {
                return _currentPlaybackPosition.value.toFloat() / currentSongDuration
            }
            return 0f
        }

    val currentSongDuration: Long
        get() = MusicService.currentSongDuration

    suspend fun updateCurrentPlaybackPosition() {
        val currentPosition = playbackState.value?.currentPlaybackPosition
        if (currentPosition != null && currentPosition != _currentPlaybackPosition.value) {
            _currentPlaybackPosition.value = currentPosition
        }
        delay(UPDATE_PLAYER_POSITION_INTERVAL)
        updateCurrentPlaybackPosition()
    }

    fun calculateColorPalette(drawable: Bitmap, onFinish: (Color) -> Unit) {
        Palette.from(drawable).generate { palette ->
            palette?.dominantSwatch?.rgb?.let { colorValue ->
                onFinish(Color(colorValue))
            }
        }
    }
}
