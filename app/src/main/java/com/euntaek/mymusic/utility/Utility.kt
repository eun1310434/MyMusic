package com.euntaek.mymusic.utility

import java.text.SimpleDateFormat
import java.util.Locale

fun Long.formatLong(): String {
    val dateFormat = SimpleDateFormat("mm:ss", Locale.getDefault())
    return dateFormat.format(this)
}