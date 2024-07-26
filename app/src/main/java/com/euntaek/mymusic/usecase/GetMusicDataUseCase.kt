package com.euntaek.mymusic.usecase

import com.euntaek.mymusic.App
import com.euntaek.mymusic.utility.Either
import com.euntaek.mymusic.utility.execUsesCase
import com.euntaek.mymusic.data.entities.MusicData
import timber.log.Timber
import javax.inject.Inject

class GetMusicDataUseCase @Inject constructor(
    private val getAllSongsUseCase: GetAllSongsUseCase,
    private val getAllAlbumsUseCase: GetAllAlbumsUseCase,
    private val getAllArtistsUseCase: GetAllArtistsUseCase,
) {
    suspend operator fun invoke(): Either<MusicData> {
        val appId = App.appId

        return try {
            var musicData = MusicData()

            execUsesCase(
                load = { getAllAlbumsUseCase() },
                success = { albums ->
                    musicData = musicData.copy(album = albums.find { it.appId == appId })
                }
            )

            execUsesCase(
                load = { getAllSongsUseCase() },
                success = { songs ->
                    musicData = musicData.copy(songs = songs.filter { it.appId == appId })
                }
            )

            execUsesCase(
                load = { getAllArtistsUseCase() },
                success = { artists ->
                    musicData = musicData.copy(artists = artists.filter { it.appId == appId })
                }
            )


            Either.Success(musicData)
        } catch (error: Exception) {
            Timber.tag("GetMusicDataUseCase").e(error)
            Either.Error(error)
        }
    }
}