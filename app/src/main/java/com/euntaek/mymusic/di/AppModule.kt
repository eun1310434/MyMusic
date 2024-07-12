package com.euntaek.mymusic.di

import android.content.Context
import com.bumptech.glide.Glide
import com.bumptech.glide.load.engine.DiskCacheStrategy
import com.bumptech.glide.request.RequestOptions
import com.euntaek.mymusic.R
import com.euntaek.mymusic.service.MusicServiceConnection
import com.euntaek.mymusic.usecase.GetAllAlbumsUseCase
import com.euntaek.mymusic.usecase.GetAllArtistsUseCase
import com.euntaek.mymusic.usecase.GetAllSongsUseCase
import com.euntaek.mymusic.usecase.GetAppInfoUseCase
import com.euntaek.mymusic.usecase.GetMusicDataUseCase
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton


@Module
@InstallIn(SingletonComponent::class)
object AppModule {
    @Singleton
    @Provides
    fun provideMusicServiceConnection(
        @ApplicationContext context: Context
    ) = MusicServiceConnection(context)

    @Singleton
    @Provides
    fun provideGlideInstance(
        @ApplicationContext context: Context
    ) = Glide.with(context).setDefaultRequestOptions(
        RequestOptions()
            .placeholder(R.drawable.ic_image)
            .error(R.drawable.ic_image)
            .diskCacheStrategy(DiskCacheStrategy.DATA)
    )

    @Singleton
    @Provides
    fun provideGetAllAlbumsUseCase() = GetAllAlbumsUseCase()

    @Singleton
    @Provides
    fun provideGetAllSongsUseCase() = GetAllSongsUseCase()

    @Singleton
    @Provides
    fun provideGetAllArtistsUseCase() = GetAllArtistsUseCase()

    @Singleton
    @Provides
    fun provideGetAppInfoUseCase() = GetAppInfoUseCase()

    @Singleton
    @Provides
    fun provideGetMusicDataUseCase(
        getAllAlbumsUseCase: GetAllAlbumsUseCase,
        getAllSongsUseCase: GetAllSongsUseCase,
        getAllArtistsUseCase: GetAllArtistsUseCase,
    ) = GetMusicDataUseCase(
        getAllAlbumsUseCase = getAllAlbumsUseCase,
        getAllSongsUseCase = getAllSongsUseCase,
        getAllArtistsUseCase = getAllArtistsUseCase
    )
}