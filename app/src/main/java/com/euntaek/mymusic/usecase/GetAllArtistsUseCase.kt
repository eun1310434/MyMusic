package com.euntaek.mymusic.usecase

import com.euntaek.mymusic.utility.Either
import com.euntaek.mymusic.data.entities.Artist
import com.euntaek.mymusic.data.repository.Constants
import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.coroutines.tasks.await
import timber.log.Timber

class GetAllArtistsUseCase {
    private val db = FirebaseFirestore.getInstance()
    private val albumCollection = db.collection(Constants.ARTIST_COLLECTION)
    suspend operator fun invoke(): Either<List<Artist>> {
        return try {
            val artists = albumCollection
                .get()
                .await()
                .toObjects(Artist::class.java)
            Either.Success(artists)
        } catch (error: Exception) {
            Timber.tag("GetAllArtistsUseCase").e(error)
            Either.Error(error)
        }
    }
}