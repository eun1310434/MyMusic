package com.euntaek.mymusic.data.entities

data class Song(
    val mediaId: String = "",
    val appId: String = "",
    val order: Int = -1,
    val title: String = "",
    val subtitle: String = "",
    val songUrl: String = "",
    val imageUrl: String = "",
    val duration: Long? = null,
)