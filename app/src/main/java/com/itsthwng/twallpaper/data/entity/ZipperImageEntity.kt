package com.itsthwng.twallpaper.data.entity

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.Index
import androidx.room.PrimaryKey
import com.google.gson.annotations.SerializedName
import com.itsthwng.twallpaper.utils.Constants

@Entity(
    tableName = "zipper_image_entity",
    indices = [
        Index(value = ["isSelectedHome"]),
        Index(value = ["isSelectedLock"]),
        Index(value = ["categoryId"])
    ]
)
data class ZipperImageEntity(
    var accessType: Int = 0,

    val categoryId: String = "",

    @PrimaryKey(autoGenerate = false)
    @ColumnInfo(name = "zipperId")
    @SerializedName("zipperId")
    val id: Int,

    val content: String = "",

    val name: String = "",

    val fileName: String = "",

    val ordinalNumber: Int = 0,

    val contentLeft: String = "",

    val contentRight: String = "",

    val type: String ?= "",
    val chainType: Int = 0,

    // History tracking fields (immutable as per data class best practice)
    var isSelectedHome: Int = 0, // 0: none, 1: current, 3: history
    var isSelectedLock: Int = 0,  // 0: none, 1: current, 3: history
    val pricePoints: Int = 0,
    val priceTier: Int = 0
) {
    fun setNewAccessType(newAccessType: Int): ZipperImageEntity = copy(accessType = newAccessType)
    fun setToFree(): ZipperImageEntity = copy(accessType = Constants.IMAGE_TYPE_FREE, pricePoints = 0)
}
