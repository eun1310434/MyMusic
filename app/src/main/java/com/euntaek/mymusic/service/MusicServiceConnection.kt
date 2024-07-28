package com.euntaek.mymusic.service

import android.content.ComponentName
import android.content.Context
import android.os.Bundle
import android.support.v4.media.MediaBrowserCompat
import android.support.v4.media.MediaMetadataCompat
import android.support.v4.media.session.MediaControllerCompat
import android.support.v4.media.session.MediaSessionCompat
import android.support.v4.media.session.PlaybackStateCompat
import com.euntaek.mymusic.data.entities.Song
import com.euntaek.mymusic.data.repository.Constants.NETWORK_FAILURE
import com.euntaek.mymusic.utility.toMediaMetadata
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import timber.log.Timber

class MusicServiceConnection(val context: Context) {
    private val _playbackState = MutableStateFlow<PlaybackStateCompat?>(null)
    val playbackState = _playbackState.asStateFlow()

    private var _currentPlayingMediaMeta = MutableStateFlow<MediaMetadataCompat?>(null)
    val currentPlayingMediaMeta = _currentPlayingMediaMeta.asStateFlow()

    private lateinit var mediaController: MediaControllerCompat

    val transportController: MediaControllerCompat.TransportControls
        get() = mediaController.transportControls

    private val mediaBrowserConnectionCallback: MediaBrowserCompat.ConnectionCallback =
        MediaBrowserConnectionCallback(onConnectedListener = { setMediaController() })

    private val mediaBrowser = MediaBrowserCompat(
        context,
        ComponentName(context, MusicService::class.java),
        mediaBrowserConnectionCallback,
        null
    ).apply {
        connect()
    }

    private fun setMediaController() {
        val callback = MediaControllerCallback(
            onSessionReadyListener = {

            },
            onPlaybackStateChangedListener = { state ->
                _playbackState.update { state }
            },
            onMetadataChangedListener = { metadata ->
                _currentPlayingMediaMeta.value = metadata
            },
            onSessionEventListener = { event, _ ->
                when (event) {
                    NETWORK_FAILURE -> Timber.tag("MusicServiceConnection")
                        .d("MediaControllerCallback::onSessionEvent::NETWORK_FAILURE")
                }
            },
            onSessionDestroyedListener = {
                mediaBrowserConnectionCallback.onConnectionSuspended()
            }
        )

        mediaController = MediaControllerCompat(context, mediaBrowser.sessionToken)
        mediaController.registerCallback(callback)
    }

    fun setCurrentPlayingSong(song: Song) {
        _currentPlayingMediaMeta.value = song.toMediaMetadata()
    }
}


private class MediaBrowserConnectionCallback(
    val onConnectedListener: (() -> Unit)? = null,
    val onConnectionSuspendedListener: (() -> Unit)? = null,
    val onConnectionFailedListener: (() -> Unit)? = null,
) : MediaBrowserCompat.ConnectionCallback() {
    override fun onConnected() {
        Timber.tag("MusicServiceConnection").d("MediaBrowserConnectionCallback::onConnected()")
        onConnectedListener?.invoke()
    }

    override fun onConnectionSuspended() {
        Timber.tag("MusicServiceConnection")
            .d("MediaBrowserConnectionCallback::onConnectionSuspended()")
        onConnectionSuspendedListener?.invoke()
    }

    override fun onConnectionFailed() {
        Timber.tag("MusicServiceConnection")
            .d("MediaBrowserConnectionCallback::onConnectionFailed()")
        onConnectionFailedListener?.invoke()
    }
}

private class MediaControllerCallback(
    val onSessionReadyListener: (() -> Unit)? = null,
    val onQueueChangedListener: ((queue: MutableList<MediaSessionCompat.QueueItem>?) -> Unit)? = null,
    val onPlaybackStateChangedListener: ((state: PlaybackStateCompat?) -> Unit)? = null,
    val onMetadataChangedListener: ((metadata: MediaMetadataCompat?) -> Unit)? = null,
    val onSessionEventListener: ((String?, Bundle?) -> Unit)? = null,
    val onSessionDestroyedListener: (() -> Unit)? = null
) : MediaControllerCompat.Callback() {
    override fun onSessionReady() {
        super.onSessionReady()
        Timber.tag("MusicServiceConnection").d("MediaControllerCallback::onSessionReady")
        onSessionReadyListener?.invoke()
    }

    override fun onShuffleModeChanged(shuffleMode: Int) {
        super.onShuffleModeChanged(shuffleMode)
        Timber.tag("MusicServiceConnection").d("MediaControllerCallback::onShuffleModeChanged")
    }

    override fun onRepeatModeChanged(repeatMode: Int) {
        super.onRepeatModeChanged(repeatMode)
        Timber.tag("MusicServiceConnection").d("MediaControllerCallback::onRepeatModeChanged")
    }

    override fun onCaptioningEnabledChanged(enabled: Boolean) {
        super.onCaptioningEnabledChanged(enabled)
        Timber.tag("MusicServiceConnection")
            .d("MediaControllerCallback::onCaptioningEnabledChanged")
    }

    override fun onAudioInfoChanged(info: MediaControllerCompat.PlaybackInfo?) {
        super.onAudioInfoChanged(info)
        Timber.tag("MusicServiceConnection").d("MediaControllerCallback::onAudioInfoChanged")
    }

    override fun onExtrasChanged(extras: Bundle?) {
        super.onExtrasChanged(extras)
        Timber.tag("MusicServiceConnection").d("MediaControllerCallback::onExtrasChanged")
    }

    override fun onQueueTitleChanged(title: CharSequence?) {
        super.onQueueTitleChanged(title)
        Timber.tag("MusicServiceConnection").d("MediaControllerCallback::onQueueTitleChanged")
    }

    override fun binderDied() {
        super.binderDied()
        Timber.tag("MusicServiceConnection").d("binderDied::onQueueTitleChanged")
    }

    override fun onQueueChanged(queue: MutableList<MediaSessionCompat.QueueItem>?) {
        super.onQueueChanged(queue)
        Timber.tag("MusicServiceConnection")
            .d("MediaControllerCallback::onQueueChanged: %s", queue.toString())
        onQueueChangedListener?.invoke(queue)
    }

    override fun onPlaybackStateChanged(state: PlaybackStateCompat?) {
        super.onPlaybackStateChanged(state)
        Timber.tag("MusicServiceConnection")
            .d("MediaControllerCallback::onPlaybackStateChanged: %s", state.toString())
        onPlaybackStateChangedListener?.invoke(state)
    }

    override fun onMetadataChanged(metadata: MediaMetadataCompat?) {
        super.onMetadataChanged(metadata)
        Timber.tag("MusicServiceConnection")
            .d("MediaControllerCallback::onMetadataChanged: %s", metadata?.description.toString())
        onMetadataChangedListener?.invoke(metadata)
    }

    override fun onSessionEvent(event: String?, extras: Bundle?) {
        super.onSessionEvent(event, extras)
        Timber.tag("MusicServiceConnection")
            .d("MediaControllerCallback::onSessionEvent: %s", event.toString())
        onSessionEventListener?.invoke(event, extras)
    }

    override fun onSessionDestroyed() {
        super.onSessionDestroyed()
        Timber.tag("MusicServiceConnection").d("MediaControllerCallback::onSessionDestroyed")
        onSessionDestroyedListener?.invoke()
    }
}