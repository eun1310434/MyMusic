package com.euntaek.mymusic.usecase

import com.euntaek.mymusic.data.entities.Song
import com.euntaek.mymusic.data.repository.Constants
import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.coroutines.tasks.await
import timber.log.Timber

class GetAllSongsUseCase {
    private val db = FirebaseFirestore.getInstance()
    private val songCollection = db.collection(Constants.SONG_COLLECTION)
    suspend operator fun invoke(): Result<List<Song>> {
        return try {
            val songs = songCollection
                .get()
                .await()
                .toObjects(Song::class.java)
                .sortedBy { it.order }
            Result.success(songs)
        } catch (error: Exception) {
            Timber.tag("GetAllSongsUseCase").e(error)
            Result.failure(error)
        }
    }
}