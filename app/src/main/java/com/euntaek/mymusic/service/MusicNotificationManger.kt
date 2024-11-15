package com.euntaek.mymusic.service

import android.annotation.SuppressLint
import android.app.Notification
import android.app.PendingIntent
import android.content.Context
import android.graphics.Bitmap
import android.graphics.drawable.BitmapDrawable
import android.graphics.drawable.Drawable
import android.net.Uri
import android.support.v4.media.session.MediaControllerCompat
import android.support.v4.media.session.MediaSessionCompat
import coil.Coil.imageLoader
import coil.request.CachePolicy
import coil.request.ImageRequest
import com.euntaek.mymusic.R
import com.euntaek.mymusic.data.repository.Constants.NOTIFICATION_CHANNEL_ID
import com.euntaek.mymusic.data.repository.Constants.NOTIFICATION_ID
import com.google.android.exoplayer2.Player
import com.google.android.exoplayer2.ui.PlayerNotificationManager

class MusicNotificationManger(
    private val context: Context,
    player: Player,
    sessionToken: MediaSessionCompat.Token,
    onCancelled: (Int, Boolean) -> Unit,
    onPosted: (Int, Notification, Boolean) -> Unit
) {
    init {
        val mediaController = MediaControllerCompat(context, sessionToken)

        val descriptionAdapter = DescriptionAdapter(
            loadBitmap = { callback ->
                loadBitmap(
                    context = context,
                    uri = mediaController.metadata.description.iconUri,
                    callback = callback
                )
                null
            },
            mediaController = mediaController
        )

        val notificationListener = MusicPlayerNotificationListener(
            onCancelled = onCancelled,
            onPosted = onPosted
        )

        val notificationManager = PlayerNotificationManager.Builder(
            context,
            NOTIFICATION_ID,
            NOTIFICATION_CHANNEL_ID
        ).setMediaDescriptionAdapter(descriptionAdapter)
            .setSmallIconResourceId(R.drawable.ic_music)
            .setNotificationListener(notificationListener)
            .setChannelNameResourceId(R.string.notification_channel_name)
            .build()

        notificationManager.setMediaSessionToken(sessionToken)
        notificationManager.setPlayer(player)
    }

    private fun loadBitmap(
        context: Context,
        uri: Uri?,
        callback: PlayerNotificationManager.BitmapCallback
    ) {
        val imageLoader = imageLoader(context) // or create your own instance
        val request = ImageRequest.Builder(context)
            .data(uri)
            .diskCacheKey(uri.toString())
            .diskCachePolicy(CachePolicy.ENABLED)
            .memoryCacheKey(uri.toString())
            .memoryCachePolicy(CachePolicy.ENABLED)
            .target(
                onSuccess = { result ->
                    val bitmap = (result as BitmapDrawable).bitmap
                    callback.onBitmap(bitmap)
                }
            )
            .build()

        imageLoader.enqueue(request)
    }

    private inner class DescriptionAdapter(
        private val loadBitmap: (PlayerNotificationManager.BitmapCallback) -> Bitmap?,
        private val mediaController: MediaControllerCompat
    ) : PlayerNotificationManager.MediaDescriptionAdapter {

        override fun getCurrentContentTitle(player: Player): CharSequence {
            return mediaController.metadata.description.title.toString()
        }

        override fun createCurrentContentIntent(player: Player): PendingIntent? {
            return mediaController.sessionActivity
        }

        override fun getCurrentContentText(player: Player): CharSequence {
            return mediaController.metadata.description.subtitle.toString()
        }

        override fun getCurrentLargeIcon(
            player: Player,
            callback: PlayerNotificationManager.BitmapCallback
        ): Bitmap? {
            return loadBitmap(callback)
        }
    }

    private inner class MusicPlayerNotificationListener(
        private val onCancelled: (Int, Boolean) -> Unit,
        private val onPosted: (Int, Notification, Boolean) -> Unit
    ) : PlayerNotificationManager.NotificationListener {

        override fun onNotificationCancelled(notificationId: Int, dismissedByUser: Boolean) {
            super.onNotificationCancelled(notificationId, dismissedByUser)
            onCancelled(notificationId, dismissedByUser)
        }

        @SuppressLint("ForegroundServiceType")
        override fun onNotificationPosted(
            notificationId: Int,
            notification: Notification,
            ongoing: Boolean
        ) {
            super.onNotificationPosted(notificationId, notification, ongoing)
            onPosted(notificationId, notification, ongoing)
        }
    }
}