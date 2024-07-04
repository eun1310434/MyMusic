package com.euntaek.mymusic.data.entities

import androidx.compose.runtime.Immutable
import androidx.compose.runtime.Stable

@Stable
@Immutable
data class AppInfo(
    val id: String = "",
    val appName: String = "",
    val version: String = "",
    val playerGifs: List<String> = emptyList(),
    val youTubeMusicLink: String = "",
    val spotifyLink: String = "",
    val whatsappLink: String = "",
    val instagramLink: String = ""
)