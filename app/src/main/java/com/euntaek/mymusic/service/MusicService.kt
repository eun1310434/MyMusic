package com.euntaek.mymusic.service

import android.app.PendingIntent
import android.content.Intent
import android.os.Bundle
import android.support.v4.media.MediaBrowserCompat
import android.support.v4.media.MediaDescriptionCompat
import android.support.v4.media.MediaMetadataCompat
import android.support.v4.media.session.MediaSessionCompat
import androidx.media.MediaBrowserServiceCompat
import com.euntaek.mymusic.data.repository.Constants.MEDIA_ROOT_ID
import com.euntaek.mymusic.data.repository.Constants.NETWORK_FAILURE
import com.euntaek.mymusic.data.repository.Constants.SERVICE_TAG
import com.euntaek.mymusic.service.notification.MusicNotificationManger
import com.euntaek.mymusic.service.notification.MusicPlaybackPrepared
import com.euntaek.mymusic.service.notification.MusicPlayerEventListener
import com.euntaek.mymusic.service.notification.MusicPlayerNotificationListener
import com.google.android.exoplayer2.Player
import com.google.android.exoplayer2.SimpleExoPlayer
import com.google.android.exoplayer2.ext.mediasession.MediaSessionConnector
import com.google.android.exoplayer2.ext.mediasession.TimelineQueueNavigator
import com.google.android.exoplayer2.upstream.DefaultDataSourceFactory
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.cancel
import kotlinx.coroutines.launch
import javax.inject.Inject

@AndroidEntryPoint
class MusicService : MediaBrowserServiceCompat() {

    @Inject
    lateinit var dataSourceFactory: DefaultDataSourceFactory

    @Inject
    lateinit var exoPlayer: SimpleExoPlayer

    @Inject
    lateinit var musicSource: MusicSource

    private lateinit var musicNotificationManger: MusicNotificationManger

    private val serviceJob = Job()
    private val serviceScope = CoroutineScope(Dispatchers.Main + serviceJob)

    private lateinit var mediaSessionCompat: MediaSessionCompat
    private lateinit var mediaSessionConnector: MediaSessionConnector

    var isForegroundService = false

    private var currentPlayingSong: MediaMetadataCompat? = null

    private var isPlayerInitialize = false

    private lateinit var musicPlayerListener: MusicPlayerEventListener

    companion object {
        var currentSongDuration = 0L
            private set
    }

    override fun onCreate() {
        super.onCreate()
        serviceScope.launch { musicSource.fetchMediaData() }
        setMediaSessionCompat()
        setMusicNotificationManger()
        setMediaSessionConnector()
        setMusicPlayerEventListener()
    }

    private fun setMusicPlayerEventListener() {
        musicPlayerListener = MusicPlayerEventListener(this)
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

    private fun setMediaSessionConnector() {
        val musicPlaybackPreparer = MusicPlaybackPrepared { mediaId ->
            musicSource.whenReady {
                currentPlayingSong = musicSource.songs.find { mediaId == it.description.mediaId }
                preparePlayer(
                    songs = musicSource.songs,
                    itemToPlay = currentPlayingSong,
                    playNow = true
                )
            }
        }
        mediaSessionConnector = MediaSessionConnector(mediaSessionCompat)
        mediaSessionConnector.setPlaybackPreparer(musicPlaybackPreparer)
        mediaSessionConnector.setQueueNavigator(MusicQueueNavigator())
        mediaSessionConnector.setPlayer(exoPlayer)
    }

    private fun setMusicNotificationManger() {
        musicNotificationManger = MusicNotificationManger(
            context = this,
            sessionToken = mediaSessionCompat.sessionToken,
            notificationListener = MusicPlayerNotificationListener(this)
        ) {
            currentSongDuration = exoPlayer.duration
        }
        musicNotificationManger.showNotification(exoPlayer)
    }


    private inner class MusicQueueNavigator : TimelineQueueNavigator(mediaSessionCompat) {
        override fun getMediaDescription(player: Player, windowIndex: Int): MediaDescriptionCompat {
            return musicSource.songs[windowIndex].description
        }
    }

    private fun preparePlayer(
        songs: List<MediaMetadataCompat>,
        itemToPlay: MediaMetadataCompat?,
        playNow: Boolean
    ) {
        val curSongIndex = if (currentPlayingSong == null) 0 else songs.indexOf(itemToPlay)
        exoPlayer.setMediaSource(musicSource.asMediaSource(dataSourceFactory))
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
        when (parentId) {
            MEDIA_ROOT_ID -> {
                val resultsSent = musicSource.whenReady { isInitialized ->
                    if (isInitialized) {
                        result.sendResult(musicSource.asMediaItems())
                        if (!isPlayerInitialize && musicSource.songs.isNotEmpty()) {
                            preparePlayer(musicSource.songs, musicSource.songs[0], false)
                            isPlayerInitialize = true
                        }
                    } else {
                        mediaSessionCompat.sendSessionEvent(NETWORK_FAILURE, null)
                        result.sendResult(null)
                    }
                }
                if (!resultsSent) {
                    result.detach()
                }
            }
        }
    }
}