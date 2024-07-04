package com.euntaek.mymusic.usecase

import com.euntaek.mymusic.data.entities.Song
import com.euntaek.mymusic.data.repository.Constants
import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.coroutines.tasks.await
import timber.log.Timber

class GetAllSongsUseCase {
    private val db = FirebaseFirestore.getInstance()
    private val songCollection = db.collection(Constants.SONG_COLLECTION)
    suspend operator fun invoke(): List<Song> {
        return try {
            songCollection
                .get()
                .await()
                .toObjects(Song::class.java)
                .sortedBy { it.order }
        } catch (e: Exception) {
            Timber.e("getAllSongs: " + e.message)
            emptyList()
        }
    }
}