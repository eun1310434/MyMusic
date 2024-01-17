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

val songs = persistentListOf(
    Song("1", "Aurora", "All Is Soft Inside"),
    Song("2", "Aurora", "Queendom"),
    Song("3", "Aurora", "Gentle Earthquakes"),
    Song("4", "Aurora", "Awakening"),
    Song("5", "Aurora", "All Is Soft Inside"),
    Song("6", "Aurora", "Queendom"),
    Song("7", "Aurora", "Gentle Earthquakes"),
    Song("8", "Aurora", "Awakening")
)