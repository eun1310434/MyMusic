package com.euntaek.mymusic.usecase

import com.euntaek.mymusic.core.Either
import com.euntaek.mymusic.data.entities.AppInfo
import com.euntaek.mymusic.data.repository.Constants
import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.coroutines.tasks.await
import timber.log.Timber

class GetAppInfoUseCase {
    private val db = FirebaseFirestore.getInstance()
    private val appInfoCollection = db.collection(Constants.APP_INFO_COLLECTION)
    suspend operator fun invoke(): Either<List<AppInfo>> {
        return try {
            val appInfo = appInfoCollection
                .get()
                .await()
                .toObjects(AppInfo::class.java)
            Either.Success(appInfo)
        } catch (error: Exception) {
            Timber.tag("GetAppInfoUseCase").e(error)
            Either.Error(error)
        }
    }
}