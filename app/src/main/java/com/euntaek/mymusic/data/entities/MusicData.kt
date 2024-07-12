package com.euntaek.mymusic.data.entities

import androidx.compose.runtime.Immutable
import androidx.compose.runtime.Stable

@Stable
@Immutable
data class MusicData(
    val album: Album? = null,
    val artists: List<Artist> = emptyList(),
    val songs: List<Song> = emptyList()
)