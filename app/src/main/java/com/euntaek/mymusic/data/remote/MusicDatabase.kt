package com.euntaek.mymusic.data.remote

import android.util.Log
import com.euntaek.mymusic.data.entities.Album
import com.euntaek.mymusic.data.entities.Song
import com.euntaek.mymusic.data.remote.Constants.ALBUM_COLLECTION
import com.euntaek.mymusic.data.remote.Constants.SONG_COLLECTION
import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.coroutines.tasks.await

class MusicDatabase {
    private val db = FirebaseFirestore.getInstance()
    private val albumCollection = db.collection(ALBUM_COLLECTION)
    private val songCollection = db.collection(SONG_COLLECTION)

    suspend fun getAllSongs(): List<Song> {
        return try {
            songCollection
                .get()
                .await()
                .toObjects(Song::class.java)
        } catch (e: Exception) {
            Log.e("Test", "getAllSongs() error : " + e.message)
            emptyList()
        }
    }

    suspend fun getAlbums(): List<Album> {
        return try {
            albumCollection
                .get()
                .await()
                .toObjects(Album::class.java)
        } catch (e: Exception) {
            Log.e("Test", "getAlbum() error : " + e.message)
            emptyList()
        }
    }
}

object Constants {
    const val ALBUM_COLLECTION = "albums"
    const val SONG_COLLECTION = "songs"
    const val SERVICE_TAG = "MusicService"
    const val MEDIA_ROOT_ID = "root_id"
    const val NETWORK_FAILURE = "NETWORK_FAILURE"
    const val UPDATE_PLAYER_POSITION_INTERVAL = 100L
    const val NOTIFICATION_ID = 1
    const val NOTIFICATION_CHANNEL_ID = "music"
}