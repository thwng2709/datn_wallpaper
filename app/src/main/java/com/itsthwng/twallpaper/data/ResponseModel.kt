package com.itsthwng.twallpaper.data

import com.google.gson.annotations.SerializedName

class ResponseModel<T> constructor(
    @SerializedName("data")
    var data: MutableList<T> = mutableListOf()
) {

    @SerializedName("message")
    var message: String? = null

    @SerializedName("status")
    var status: Int? = null

    @SerializedName("pageId")
    var pageId: String? = null

    @SerializedName("hasnext")
    var hasNext: Boolean = false

    @SerializedName(value = "nextoffset")
    var nextOffset: Int? = null
}