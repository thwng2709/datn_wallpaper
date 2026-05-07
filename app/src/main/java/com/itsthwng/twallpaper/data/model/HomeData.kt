package com.itsthwng.twallpaper.data.model

import com.google.gson.annotations.SerializedName

data class HomeData(

    @field:SerializedName("featured_wallpapers")
    val featuredWallpapers: List<SettingData.WallpapersItem>? = null ?: arrayListOf(),

    @field:SerializedName("latest_wallpapers")
    val latestWallpapers: List<SettingData.WallpapersItem>? = null ?: arrayListOf(),

    @field:SerializedName("message")
    val message: String? = null ?: "",

    @field:SerializedName("status")
    val status: Boolean? = null ?: false
)



