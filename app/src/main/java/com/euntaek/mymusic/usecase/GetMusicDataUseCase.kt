package com.euntaek.mymusic.usecase

import com.euntaek.mymusic.App
import com.euntaek.mymusic.data.entities.MusicData
import timber.log.Timber
import javax.inject.Inject

class GetMusicDataUseCase @Inject constructor(
    private val getAllSongsUseCase: GetAllSongsUseCase,
    private val getAllAlbumsUseCase: GetAllAlbumsUseCase,
    private val getAllArtistsUseCase: GetAllArtistsUseCase,
) {
    suspend operator fun invoke(): Result<MusicData> {
        val appId = App.appId

        return try {
            var musicData = MusicData()
            getAllAlbumsUseCase().onSuccess { albums ->
                musicData = musicData.copy(album = albums.find { it.appId == appId })
            }
            getAllSongsUseCase().onSuccess { songs ->
                musicData = musicData.copy(songs = songs.filter { it.appId == appId })
            }
            getAllArtistsUseCase().onSuccess { artists ->
                musicData = musicData.copy(artists = artists.filter { it.appId == appId })
            }
            Result.success(musicData)
        } catch (error: Exception) {
            Timber.tag("GetMusicDataUseCase").e(error)
            Result.failure(error)
        }
    }
}