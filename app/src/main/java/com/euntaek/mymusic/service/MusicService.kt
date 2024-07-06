package com.euntaek.mymusic.service

import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.os.ResultReceiver
import android.support.v4.media.MediaBrowserCompat
import android.support.v4.media.MediaDescriptionCompat
import android.support.v4.media.MediaMetadataCompat
import android.support.v4.media.session.MediaSessionCompat
import android.support.v4.media.session.PlaybackStateCompat
import androidx.core.content.ContextCompat
import androidx.media.MediaBrowserServiceCompat
import com.euntaek.mymusic.data.repository.Constants.MEDIA_ROOT_ID
import com.euntaek.mymusic.data.repository.Constants.NETWORK_FAILURE
import com.euntaek.mymusic.data.repository.Constants.NOTIFICATION_ID
import com.euntaek.mymusic.data.repository.Constants.SERVICE_TAG
import com.euntaek.mymusic.usecase.GetAllSongsUseCase
import com.euntaek.mymusic.utility.toMediaMetadata
import com.google.android.exoplayer2.MediaItem
import com.google.android.exoplayer2.Player
import com.google.android.exoplayer2.SimpleExoPlayer
import com.google.android.exoplayer2.ext.mediasession.MediaSessionConnector
import com.google.android.exoplayer2.ext.mediasession.TimelineQueueNavigator
import com.google.android.exoplayer2.source.ConcatenatingMediaSource
import com.google.android.exoplayer2.source.MediaSource
import com.google.android.exoplayer2.source.ProgressiveMediaSource
import com.google.android.exoplayer2.upstream.DefaultDataSourceFactory
import com.google.android.exoplayer2.util.Util
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.cancel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import javax.inject.Inject

@AndroidEntryPoint
class MusicService : MediaBrowserServiceCompat() {
    @Inject
    lateinit var exoPlayer: SimpleExoPlayer

    @Inject
    lateinit var getAllSongsUseCase: GetAllSongsUseCase

    private lateinit var musicNotificationManger: MusicNotificationManger

    private var _songs = MutableStateFlow(emptyList<MediaMetadataCompat>())

    private val serviceJob = Job()
    private val serviceScope = CoroutineScope(Dispatchers.Main + serviceJob)

    private lateinit var mediaSessionCompat: MediaSessionCompat

    private var isForegroundService = false

    private var currentPlayingSong: MediaMetadataCompat? = null

    private var isPlayerInitialize = false

    private lateinit var musicPlayerListener: Player.Listener

    companion object {
        var currentSongDuration = 0L
            private set
    }

    override fun onCreate() {
        super.onCreate()

        serviceScope.launch { fetchMediaData() }
        setMediaSessionCompat()

        //Set MusicNotificationManager
        musicNotificationManger = MusicNotificationManger(
            context = this,
            player = exoPlayer,
            sessionToken = mediaSessionCompat.sessionToken,
            onCancelled = { _, _ ->
                stopForeground(STOP_FOREGROUND_REMOVE)
                isForegroundService = false
                stopSelf()
            },
            onPosted = { _, notification, ongoing ->
                if (ongoing && !isForegroundService) {
                    ContextCompat.startForegroundService(
                        this,
                        Intent(applicationContext, this::class.java)
                    )
                    startForeground(NOTIFICATION_ID, notification)
                    isForegroundService = true
                }
                currentSongDuration = exoPlayer.duration
            }
        )

        setMediaSessionConnector(
            player = exoPlayer,
            mediaSessionCompat = mediaSessionCompat,
            onPrepareFromMediaId = { mediaId, playWhenReady ->
                currentPlayingSong = _songs.value.find { mediaId == it.description.mediaId }
                preparePlayer(
                    songs = _songs.value,
                    itemToPlay = currentPlayingSong,
                    playNow = true
                )
            },
            setMediaDescription = { windowIndex ->
                _songs.value[windowIndex].description
            }
        )

        setMusicPlayerEventListener()
    }


    private suspend fun fetchMediaData() {
        _songs.update { getAllSongsUseCase().map { it.toMediaMetadata() } }
    }


    private fun setMusicPlayerEventListener() {
        musicPlayerListener = object : Player.Listener {
            override fun onPlayWhenReadyChanged(playWhenReady: Boolean, reason: Int) {
                super.onPlayWhenReadyChanged(playWhenReady, reason)
                if (reason == Player.STATE_READY && !playWhenReady) {
                    stopForeground(STOP_FOREGROUND_DETACH)
                }
            }
        }

        exoPlayer.addListener(musicPlayerListener)
    }

    private fun setMediaSessionCompat() {
        val activityIntent = packageManager?.getLaunchIntentForPackage(packageName)?.let {
            PendingIntent.getActivity(this, 0, it, PendingIntent.FLAG_IMMUTABLE)
        }

        mediaSessionCompat = MediaSessionCompat(this, SERVICE_TAG).apply {
            setSessionActivity(activityIntent)
            isActive = true
        }

        sessionToken = mediaSessionCompat.sessionToken
    }

    private fun setMediaSessionConnector(
        player: Player,
        mediaSessionCompat: MediaSessionCompat,
        onPrepareFromMediaId: (mediaId: String, playWhenReady: Boolean) -> Unit,
        setMediaDescription: (Int) -> MediaDescriptionCompat
    ) {
        val mediaSessionConnector = MediaSessionConnector(mediaSessionCompat)
        mediaSessionConnector.setPlaybackPreparer(MusicPlaybackPrepared(onPrepareFromMediaId))
        mediaSessionConnector.setQueueNavigator(
            MusicQueueNavigator(
                mediaSessionCompat = mediaSessionCompat,
                setMediaDescription = setMediaDescription
            )
        )
        mediaSessionConnector.setPlayer(player)
    }

    private fun preparePlayer(
        songs: List<MediaMetadataCompat>,
        itemToPlay: MediaMetadataCompat?,
        playNow: Boolean
    ) {
        val curSongIndex = if (currentPlayingSong == null) 0 else songs.indexOf(itemToPlay)
        exoPlayer.setMediaSource(songs.getMediaSource(this))
        exoPlayer.prepare()
        exoPlayer.seekTo(curSongIndex, 0L)
        exoPlayer.playWhenReady = playNow
    }

    override fun onTaskRemoved(rootIntent: Intent?) {
        super.onTaskRemoved(rootIntent)
        exoPlayer.stop()
    }

    override fun onDestroy() {
        super.onDestroy()
        serviceScope.cancel()
        exoPlayer.removeListener(musicPlayerListener)
        exoPlayer.release()
    }

    override fun onGetRoot(
        clientPackageName: String,
        clientUid: Int,
        rootHints: Bundle?
    ): BrowserRoot {
        return BrowserRoot(MEDIA_ROOT_ID, null)
    }

    override fun onLoadChildren(
        parentId: String,
        result: Result<MutableList<MediaBrowserCompat.MediaItem>>
    ) {
        if (parentId == MEDIA_ROOT_ID) {
            mediaSessionCompat.sendSessionEvent(NETWORK_FAILURE, null)
            result.sendResult(null)
        }
    }
}

fun List<MediaMetadataCompat>.asMediaItems() = this.map { song ->
    val desc = MediaDescriptionCompat.Builder()
        .setMediaUri(song.description.mediaUri)
        .setTitle(song.description.title)
        .setSubtitle(song.description.subtitle)
        .setMediaId(song.description.mediaId)
        .setIconUri(song.description.iconUri)
        .build()

    MediaBrowserCompat.MediaItem(desc, MediaBrowserCompat.MediaItem.FLAG_PLAYABLE)
}.toMutableList()


private fun List<MediaMetadataCompat>.getMediaSource(
    context: Context
): MediaSource {
    val userAgent = Util.getUserAgent(context, "MyMusic")
    val dataSourceFactory = DefaultDataSourceFactory(context, userAgent)
    val concatenatingMediaSource = ConcatenatingMediaSource()

    this.forEach { song ->
        val songUri = song.getString(MediaMetadataCompat.METADATA_KEY_MEDIA_URI)
        val mediaItem = MediaItem.fromUri(songUri)
        val mediaSource = ProgressiveMediaSource
            .Factory(dataSourceFactory)
            .createMediaSource(mediaItem)
        concatenatingMediaSource.addMediaSource(mediaSource)
    }

    return concatenatingMediaSource
}

private class MusicQueueNavigator(
    mediaSessionCompat: MediaSessionCompat,
    val setMediaDescription: (Int) -> MediaDescriptionCompat
) : TimelineQueueNavigator(mediaSessionCompat) {
    override fun getMediaDescription(player: Player, windowIndex: Int): MediaDescriptionCompat {
        return setMediaDescription(windowIndex)
    }
}

private class MusicPlaybackPrepared(val onPrepareFromMediaId: (String, Boolean) -> Unit) :
    MediaSessionConnector.PlaybackPreparer {
    override fun onCommand(p0: Player, p1: String, p2: Bundle?, p3: ResultReceiver?): Boolean {
        return false
    }

    override fun getSupportedPrepareActions(): Long {
        return PlaybackStateCompat.ACTION_PREPARE_FROM_MEDIA_ID or
                PlaybackStateCompat.ACTION_PLAY_FROM_MEDIA_ID
    }

    override fun onPrepare(playWhenReady: Boolean) {
    }

    override fun onPrepareFromMediaId(mediaId: String, playWhenReady: Boolean, extras: Bundle?) {
        onPrepareFromMediaId(mediaId, playWhenReady)
    }

    override fun onPrepareFromSearch(query: String, playWhenReady: Boolean, extras: Bundle?) {
    }

    override fun onPrepareFromUri(uri: Uri, playWhenReady: Boolean, extras: Bundle?) {
    }
}