package com.euntaek.mymusic.data.entities

import androidx.compose.runtime.Immutable
import androidx.compose.runtime.Stable
import kotlinx.collections.immutable.persistentListOf

@Stable
@Immutable
data class Song(
    val mediaId: String = "",
    val title: String = "",
    val subtitle: String = "",
    val songUrl: String = "",
    var imageUrl: String = ""
)