package com.itsthwng.twallpaper.notification.model

import com.google.gson.annotations.Expose
import com.google.gson.annotations.SerializedName

data class NotifyContent(
    @SerializedName("c")
    @Expose
    var content: String?,
    @SerializedName("t")
    @Expose
    var title: String?
)