package com.itsthwng.twallpaper.workManager.model

import androidx.annotation.Keep
import com.google.gson.annotations.SerializedName
import com.itsthwng.twallpaper.data.CommonInfo
import com.itsthwng.twallpaper.data.entity.PriceTier
import com.itsthwng.twallpaper.data.entity.ZipperImageEntity
import com.itsthwng.twallpaper.data.model.SettingData
import com.itsthwng.twallpaper.data.model.getPriceFromTierWithRemoteConfig
import com.itsthwng.twallpaper.utils.Constants

@Keep
data class LocaleImages(
    @field:SerializedName("accessType")//0=premium,1=lock,2=free
    val accessType: Int? = null ?: 2,

    @field:SerializedName("thumbnail")
    val thumbnail: String? = null ?: "",

    @field:SerializedName("categoryId")
    val categoryId: String? = null ?: "",

   @field:SerializedName("wallpaperType")
    val wallpaperType: Int? = null ?: 0, // 0: Images (PNG, JPG, etc.), 1: Live Images (Video, GIF, etc.)

    @field:SerializedName("id")
    val id: Int? = null ?: 0,

    @field:SerializedName("url")
    val content: String? = null ?: "", // could be video

    @field:SerializedName("isFeatured")
    val isFeatured: Int? = null ?: 0,

    @field:SerializedName("tags")
    val tags: String? = null,

    val name: String? = null ?: "",

    val fileName: String? = null ?: "",
    val ordinalNumber: Int? = null ?: 0,
    val lockScreen: String? = null,
    val homeScreen: String? = null
)

fun LocaleImages.toWallpaperItem(baseUrl: String = ""): SettingData.WallpapersItem {
    val content = if(this.content.isNullOrEmpty()) ""
    else if(wallpaperType == 0) baseUrl + this.content
    else baseUrl + this.content
    val priceTier = PriceTier.random()
    return SettingData.WallpapersItem(
        accessType = 2,
        thumbnail = if(!this.thumbnail.isNullOrEmpty()) baseUrl + this.thumbnail else content,
        categoryId = this.categoryId ?: "",
        wallpaperType = this.wallpaperType ?: 0,
        id = this.id ?: 0,
        content = content,
        isFeatured = this.isFeatured ?: 0,
        tags = this.tags ?: categoryId ?: "",
        name = this.name ?: "",
        lockScreen = this.lockScreen,
        homeScreen = this.homeScreen,
        fileName = this.fileName ?: "",
        pricePoints = 0,
        priceTier = priceTier
    )
}

fun SettingData.WallpapersItem.toZipperImageEntity(type: String? = null): ZipperImageEntity {
    return ZipperImageEntity(
        id = this.id ?: 0,
        name = this.name ?: "",
        content = this.content ?: "",
        fileName = this.fileName ?: "",
        categoryId = this.categoryId ?: "",
        accessType = 2,
        ordinalNumber = 0,
        type = type,
        pricePoints = 0,
        priceTier = this.priceTier
    )
}

fun LocaleImages.toZipperImageEntity(type: String? = null, baseUrl: String = ""): ZipperImageEntity {
    val priceTier = PriceTier.random()
    val pricePoints = if(accessType == 1) getPriceFromTierWithRemoteConfig(priceTier) else if(accessType == 0) CommonInfo.priceHigh.toInt() else 0
    return ZipperImageEntity(
        id = this.id ?: 0,
        name = this.name ?: "",
        content = if(!this.content.isNullOrEmpty()) baseUrl + this.content else "",
        fileName = this.fileName ?: "",
        categoryId = Constants.ZIPPER_IMAGE ?: "",
        accessType = this.accessType ?: 0,
        ordinalNumber = this.ordinalNumber ?: 0,
        type = type,
        pricePoints = pricePoints,
        priceTier = priceTier
    )
}
