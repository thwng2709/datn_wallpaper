package com.itsthwng.twallpaper.data.entity

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.PrimaryKey
import com.google.gson.annotations.SerializedName
import java.io.Serializable

@Entity(tableName = "category_entity")
data class CategoryEntity(
    @PrimaryKey(autoGenerate = false)
    @ColumnInfo(name = "categoryId")
    @SerializedName("categoryId")
    var categoryId: String = "",
    var image: String = "",
    var title: String = "",
    @ColumnInfo(name = "wallpapers_count")
    var wallpapersCount: Int = 0,
    var originalTitle: String = "",
    var type: Int = 0, // 0: Simple, 1: Live,
    @ColumnInfo(name = "position") val position: Int = 0
): Serializable