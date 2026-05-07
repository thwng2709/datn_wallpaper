package com.itsthwng.twallpaper.workManager.model

import androidx.annotation.Keep
import com.google.gson.annotations.SerializedName

@Keep
data class LocaleDataModel(
    val baseUrl: String ?= null,
    val categories: List<LocaleCategories>,
    @field:SerializedName("zipper_images")
    val zipperImages: List<LocaleZipperImages>?=null,
    val zippers: List<LocaleZipperImages>?=null,
    val chains: List<LocaleZipperImages>?=null,
)