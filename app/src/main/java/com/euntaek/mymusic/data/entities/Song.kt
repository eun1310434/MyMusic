package com.euntaek.mymusic.data.entities

import androidx.compose.runtime.Immutable
import androidx.compose.runtime.Stable

@Stable
@Immutable
data class Song(
    val mediaId: String = "",
    val appId: String = "",
    val order: Int = -1,
    val title: String = "",
    val subtitle: String = "",
    val songUrl: String = "",
    var imageUrl: String = ""
)