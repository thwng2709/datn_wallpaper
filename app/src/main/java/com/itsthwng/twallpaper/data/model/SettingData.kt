package com.itsthwng.twallpaper.data.model

import com.google.gson.annotations.SerializedName

data class SettingData(
    @field:SerializedName("data")
    val data: DataItem? = null ?: DataItem(),

    @field:SerializedName("message")
    val message: String? = null ?: "",

    @field:SerializedName("status")
    val status: Boolean? = null ?: false,

    @field:SerializedName("categories")
    val categories: List<CategoriesItem>? = null ?: mutableListOf(),
) {
	data class WallpapersItem(

        @field:SerializedName("accessType")//0=premium,1=lock,2=free
        var accessType: Int? = null ?: 0,

        @field:SerializedName("thumbnail")
        val thumbnail: String? = null ?: "",

        @field:SerializedName("categoryId")
        val categoryId: String? = null ?: "",

        @field:SerializedName("updated_at")
        val updatedAt: Long?= null ?: 0L,

        @field:SerializedName("created_at")
        val createdAt: Long?= null ?: 0L,

        @field:SerializedName("wallpaperType")
        val wallpaperType: Int? = null ?: 0, // 0: Images (PNG, JPG, etc.), 1: Live Images (Video, GIF, etc.)

        @field:SerializedName("id")
        val id: Int? = null ?: 0,

        @field:SerializedName("content")
        val content: String? = null ?: "", // could be video

        @field:SerializedName("isFeatured")
        val isFeatured: Int? = null ?: 0,

        @field:SerializedName("tags")
        val tags: String? = null ?: "",

        val name: String? = null ?: "",

        val lockScreen: String ?= null ?: "",

        val homeScreen: String ?= null ?: "",

        var isFavorite: Boolean = false, // Local favorite status, not from server

        var isSelectedHome: Int? = 0, // 0 none, 1 current, 3 history
        var isSelectedLock: Int? = 0,   // 0 none, 1 current, 3 history
        var isDownloaded: Long?= 0,
        var fileName: String = "",
        var pricePoints: Int = 0,
        var priceTier: Int = 0,
        var isForYou: Boolean = false
    )

    data class DataItem(

        @field:SerializedName("updated_at")
        val updatedAt: String? = null ?: "",

        @field:SerializedName("created_at")
        val createdAt: String? = null ?: "",

        @field:SerializedName("id")
        val id: Int? = null ?: 0,

        @field:SerializedName("app_name")
        val app_name: String? = null ?: "",


        )

	data class CategoriesItem(

        @field:SerializedName("image")
        val image: String? = null ?: "",

        @field:SerializedName("updated_at")
        val updatedAt: String? = null ?: "",

        @field:SerializedName("created_at")
        val createdAt: String? = null ?: "",

        @field:SerializedName("id")
        val id: String? = null ?: "",

        @field:SerializedName("title")
        val title: String? = null ?: "",

        @field:SerializedName("type")
        val type: Int = 0, // 0: Simple, 1: Live

        @field:SerializedName("wallpapers_count")
        var wallpapers_count: Int? = null ?: 0,

        @field:SerializedName("originalTitle")
        var originalTitle: String?= ""
    )
}


