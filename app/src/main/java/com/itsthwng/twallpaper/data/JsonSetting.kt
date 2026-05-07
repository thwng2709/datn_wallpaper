package com.itsthwng.twallpaper.data

import com.google.gson.reflect.TypeToken
import com.squareup.moshi.Json
import java.lang.reflect.Type

data class JsonSetting(
    @field:Json(name = "commonInfo")
    var commonInfo: CommonInfo? = null,

    ) {
    companion object {
        val type: Type
            get() = object : TypeToken<JsonSetting?>() {}.type
    }
}