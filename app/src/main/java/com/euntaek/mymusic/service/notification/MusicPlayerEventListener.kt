package com.euntaek.mymusic.service.notification

import com.euntaek.mymusic.service.MusicService
import com.google.android.exoplayer2.Player

class MusicPlayerEventListener(
    private val musicService: MusicService
) : Player.Listener {

    override fun onPlayWhenReadyChanged(playWhenReady: Boolean, reason: Int) {
        super.onPlayWhenReadyChanged(playWhenReady, reason)
        if (reason == Player.STATE_READY && !playWhenReady) {
            musicService.stopForeground(false)
        }
    }
}