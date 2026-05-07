package com.itsthwng.twallpaper.notification.model

import com.google.gson.annotations.Expose
import com.google.gson.annotations.SerializedName

data class NotifyData(
    @SerializedName("a")
    @Expose
    var clickAction: String?,
    @SerializedName("nc")
    @Expose
    var notifyContent: List<NotifyContent>?,
    @SerializedName("id")
    @Expose
    var id: String?
)
