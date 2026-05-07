package com.itsthwng.twallpaper.workManager.model

import androidx.annotation.Keep
import com.google.gson.annotations.SerializedName
import com.itsthwng.twallpaper.data.model.SettingData

@Keep
data class LocaleCategories(
    @field:SerializedName("thumbnail")
    val image: String? = null ?: "",

    @field:SerializedName("id")
    val id: String? = null ?: "",

    @field:SerializedName("name")
    val title: String? = null ?: "",

    @field:SerializedName("type")
    val type: Int = 0, // 0: Simple, 1: Live

    @field:SerializedName("originalName")
    var originalTitle: String?= "",

    val images: List<LocaleImages> = emptyList<LocaleImages>()
)

fun LocaleCategories.toCategoryItem(baseUrl: String = ""): SettingData.CategoriesItem {
    val content = if(this.image.isNullOrEmpty()) ""
    else if(this.image.contains("mp4") || this.image.contains("gif")) baseUrl + this.image
    else baseUrl + this.image
    return SettingData.CategoriesItem(
        id = id ?: "",
        title = title ?: "",
        image = content,
        type = type,
        originalTitle = originalTitle ?: "",
        wallpapers_count = images.size ?: 0
    )
}