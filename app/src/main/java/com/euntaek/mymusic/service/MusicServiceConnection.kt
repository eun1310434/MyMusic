package com.euntaek.mymusic.service

import android.content.ComponentName
import android.content.Context
import android.os.Bundle
import android.support.v4.media.MediaBrowserCompat
import android.support.v4.media.MediaMetadataCompat
import android.support.v4.media.session.MediaControllerCompat
import android.support.v4.media.session.PlaybackStateCompat
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import com.euntaek.mymusic.data.repository.Constants.NETWORK_FAILURE
import com.euntaek.mymusic.utility.Event
import com.euntaek.mymusic.utility.Resource
import kotlinx.coroutines.flow.MutableStateFlow

class MusicServiceConnection(context: Context) {
    private val _isConnected = MutableLiveData<Event<Resource<Boolean>>>()
    val isConnected: LiveData<Event<Resource<Boolean>>> = _isConnected

    private val _networkFailure = MutableLiveData<Event<Resource<Boolean>>>()
    val networkFailure: LiveData<Event<Resource<Boolean>>> = _networkFailure

    private val _playbackState = MutableLiveData<PlaybackStateCompat?>()
    val playbackState: LiveData<PlaybackStateCompat?> = _playbackState

    var currentPlayingSong = MutableStateFlow<MediaMetadataCompat?>(null)

    private val _nowPlaying = MutableLiveData<MediaMetadataCompat?>()
    val nowPlaying: LiveData<MediaMetadataCompat?> = _nowPlaying

    private lateinit var mediaController: MediaControllerCompat

    val transportController: MediaControllerCompat.TransportControls
        get() = mediaController.transportControls

    private val mediaBrowserConnectionCallback = MediaBrowserConnectionCallback(context)
    private val mediaBrowser = MediaBrowserCompat(
        context,
        ComponentName(context, MusicService::class.java),
        mediaBrowserConnectionCallback,
        null
    ).apply { connect() }

    fun subscribe(parentId: String, callback: MediaBrowserCompat.SubscriptionCallback) {
        mediaBrowser.subscribe(parentId, callback)
    }

    fun unsubscribe(parentId: String, callback: MediaBrowserCompat.SubscriptionCallback) {
        mediaBrowser.unsubscribe(parentId, callback)
    }

    private inner class MediaBrowserConnectionCallback(
        private val context: Context
    ) : MediaBrowserCompat.ConnectionCallback() {

        override fun onConnected() {
            mediaController = MediaControllerCompat(context, mediaBrowser.sessionToken)
                .apply { registerCallback(MediaControllerCallback()) }
            _isConnected.postValue(Event(Resource.Success(true)))
        }

        override fun onConnectionSuspended() {
            _isConnected.postValue(
                Event(
                    Resource.Error(
                        message = "The connection was suspended",
                        data = false
                    )
                )
            )
        }

        override fun onConnectionFailed() {
            _isConnected.postValue(
                Event(
                    Resource.Error(
                        message = "Couldn't connect to media browser",
                        data = false
                    )
                )
            )
        }
    }

    private inner class MediaControllerCallback : MediaControllerCompat.Callback() {
        override fun onPlaybackStateChanged(state: PlaybackStateCompat?) {
            _playbackState.postValue(state)
        }

        override fun onMetadataChanged(metadata: MediaMetadataCompat?) {
            super.onMetadataChanged(metadata)
            currentPlayingSong.value = metadata
            _nowPlaying.postValue(metadata)
        }

        override fun onSessionEvent(event: String?, extras: Bundle?) {
            super.onSessionEvent(event, extras)
            when (event) {
                NETWORK_FAILURE -> _networkFailure.postValue(
                    Event(
                        Resource.Error(
                            message = "Couldn't connect to the server. Please check your internet connection.",
                            data = null
                        )
                    )
                )
            }
        }

        override fun onSessionDestroyed() {
            mediaBrowserConnectionCallback.onConnectionSuspended()
        }
    }
}