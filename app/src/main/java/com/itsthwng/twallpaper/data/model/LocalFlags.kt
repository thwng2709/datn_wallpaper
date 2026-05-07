package com.itsthwng.twallpaper.data.model

data class LocalFlags(
    val wallpaperId: Int,
    val isFavorite: Int = 0,
    val isSelectedLock: Int,
    val isSelectedHome: Int,
    val accessType: Int,
    val isDownloaded: Long = 0,
    val isForYou: Int = 0
)
