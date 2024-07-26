package com.euntaek.mymusic.usecase

import com.euntaek.mymusic.utility.Either
import com.euntaek.mymusic.data.entities.Album
import com.euntaek.mymusic.data.repository.Constants
import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.coroutines.tasks.await
import timber.log.Timber

class GetAllAlbumsUseCase {
    private val db = FirebaseFirestore.getInstance()
    private val albumCollection = db.collection(Constants.ALBUM_COLLECTION)
    suspend operator fun invoke(): Either<List<Album>> {
        return try {
            val albums = albumCollection
                .get()
                .await()
                .toObjects(Album::class.java)
            Either.Success(albums)
        } catch (error: Exception) {
            Timber.tag("GetAllAlbumsUseCase").e(error)
            Either.Error(error)
        }
    }
}