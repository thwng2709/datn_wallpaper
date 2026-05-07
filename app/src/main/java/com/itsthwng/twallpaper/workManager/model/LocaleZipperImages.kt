package com.itsthwng.twallpaper.workManager.model

import androidx.annotation.Keep
import com.google.gson.annotations.SerializedName
import com.itsthwng.twallpaper.data.entity.PriceTier
import com.itsthwng.twallpaper.data.entity.ZipperImageEntity

@Keep
data class LocaleZipperImages(
    @field:SerializedName("accessType")//0=premium,1=lock,2=free
    val accessType: Int? = null ?: 0,

    @field:SerializedName("categoryId")
    val categoryId: String? = null ?: "",

    @field:SerializedName("id")
    val id: Int? = null ?: 0,

    @field:SerializedName("url")
    val content: String? = null ?: "",

    val name: String? = null ?: "",

    val fileName: String? = null ?: "",
    val ordinalNumber: Int? = null ?: 0,

    @field:SerializedName("urlLeft")
    val contentLeft: String? = null ?: "",

    @field:SerializedName("urlRight")
    val contentRight: String? = null ?: "",

    val chainType: Int? = null ?: 0
)

fun LocaleZipperImages.toZipperImageEntity(type: String? = null, baseUrl: String = ""): ZipperImageEntity {
    val priceTier = PriceTier.random()
    return ZipperImageEntity(
        id = id ?: 0,
        name = name ?: "",
        content = if(!this.content.isNullOrEmpty()) baseUrl + this.content else "",
        fileName = fileName ?: "",
        categoryId = categoryId ?: "",
        accessType = 0,
        contentLeft = if(!this.contentLeft.isNullOrEmpty()) baseUrl + this.contentLeft else "",
        contentRight = if(!this.contentRight.isNullOrEmpty()) baseUrl + this.contentRight else "",
        ordinalNumber = ordinalNumber ?: 0,
        type = type,
        chainType = chainType ?: 0,
        pricePoints = 0,
        priceTier = priceTier
    )
}