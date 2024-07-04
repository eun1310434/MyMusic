package com.euntaek.mymusic.data.entities

import androidx.compose.runtime.Immutable
import androidx.compose.runtime.Stable

@Stable
@Immutable
data class Artist(
    val id: String = "",
    val appId: String = "",
    val name: String = "",
    val description: String = "",
    val type: String = "",
    val image: String = ""
)