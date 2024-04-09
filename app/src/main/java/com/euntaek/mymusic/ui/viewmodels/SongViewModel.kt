package com.euntaek.mymusic.ui.viewmodels

import android.graphics.Bitmap
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableLongStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.compose.ui.graphics.Color
import androidx.lifecycle.ViewModel
import androidx.palette.graphics.Palette
import com.euntaek.mymusic.data.remote.Constants.UPDATE_PLAYER_POSITION_INTERVAL
import com.euntaek.mymusic.service.MusicService
import com.euntaek.mymusic.service.MusicServiceConnection
import com.euntaek.mymusic.utility.currentPlaybackPosition
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.delay
import java.text.SimpleDateFormat
import java.util.Locale
import javax.inject.Inject


@HiltViewModel
class SongViewModel @Inject constructor(
    musicServiceConnection: MusicServiceConnection
) : ViewModel() {

    private val playbackState = musicServiceConnection.playbackState

    var currentPlaybackPosition by mutableStateOf(0L)

    val currentPlayerPosition: Float
        get() {
            if (currentSongDuration > 0) {
                return currentPlaybackPosition.toFloat() / currentSongDuration
            }
            return 0f
        }

    val currentPlaybackFormattedPosition: String
        get() = formatLong(currentPlaybackPosition)

    val currentSongDurationFormattedPosition: String
        get() = formatLong(currentSongDuration)


    val currentSongDuration: Long
        get() = MusicService.currentSongDuration

    suspend fun updateCurrentPlaybackPosition() {
        val currentPosition = playbackState.value?.currentPlaybackPosition
        if (currentPosition != null && currentPosition != currentPlaybackPosition) {
            currentPlaybackPosition = currentPosition
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

    private fun formatLong(value: Long): String {
        val dateFormat = SimpleDateFormat("mm:ss", Locale.getDefault())
        return dateFormat.format(value)
    }
}
