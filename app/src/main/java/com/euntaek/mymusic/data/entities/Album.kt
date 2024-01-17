package com.euntaek.mymusic.data.entities

import androidx.compose.runtime.Immutable
import androidx.compose.runtime.Stable

@Stable
@Immutable
data class Album(
    val author: String = "",
    val id: String = "",
    val imageUrl: String = "",
    val subTitle: String = "",
    val title: String = ""
)