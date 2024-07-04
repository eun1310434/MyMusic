package com.euntaek.mymusic.data.entities

import androidx.compose.runtime.Immutable
import androidx.compose.runtime.Stable

@Stable
@Immutable
data class Album(
    val id: String = "",
    val appId: String = "",
    val albumId: String = "",
    val artistId: String = "",
    val description: String = "",
    val name: String = "",
    val image: String = ""
)