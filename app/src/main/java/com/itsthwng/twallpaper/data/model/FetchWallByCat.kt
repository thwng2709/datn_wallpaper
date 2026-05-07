package com.itsthwng.twallpaper.data.model

import com.google.gson.annotations.SerializedName

data class FetchWallByCat(


    @field:SerializedName("data")
    val data: List<SettingData.WallpapersItem>? = null ?: arrayListOf(),

    @field:SerializedName("message")
    val message: String? = null ?: "",

    @field:SerializedName("status")
    val status: Boolean? = null ?: false
)



