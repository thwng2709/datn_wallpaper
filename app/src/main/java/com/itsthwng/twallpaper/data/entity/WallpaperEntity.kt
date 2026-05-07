package com.itsthwng.twallpaper.data.entity

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.PrimaryKey
import com.google.gson.annotations.SerializedName
import java.io.Serializable

@Entity(tableName = "wallpapers_entity")
data class WallpaperEntity(
    @PrimaryKey(autoGenerate = false)
    @ColumnInfo(name = "wallpaperId")
    @SerializedName("wallpaperId")
    var wallpaperId: Int = 0,

    @ColumnInfo(name = "access_type")
    @SerializedName("access_type")
    var accessType: Int = 0, // 0=premium, 1=lock, 2=free

    @ColumnInfo(name = "thumbnail")
    @SerializedName("thumbnail")
    var thumbnail: String = "",

    @ColumnInfo(name = "name")
    @SerializedName("name")
    var name: String = "",

    @ColumnInfo(name = "category_id")
    @SerializedName("category_id")
    var categoryId: String = "",

    @ColumnInfo(name = "wallpaper_type")
    @SerializedName("wallpaper_type")
    var wallpaperType: Int = 0, // 0: Images, 1: Live Images

    @ColumnInfo(name = "created_at")
    var createdAt: Long = 0,

    @ColumnInfo(name = "updated_at")
    var updatedAt: Long = 0,

    @ColumnInfo(name = "content")
    @SerializedName("content")
    var content: String = "",

    @ColumnInfo(name = "is_featured")
    @SerializedName("is_featured")
    var isFeatured: Int = 0,

    @ColumnInfo(name = "tags")
    @SerializedName("tags")
    var tags: String = "",

    @ColumnInfo(name = "is_favorite")
    @SerializedName("is_favorite")
    var isFavorite: Int = 0,

    @ColumnInfo(name = "is_downloaded")
    @SerializedName("is_downloaded")
    var isDownloaded: Long = 0, // Save as time downloaded, 0=not downloaded, >0=timestamp

    @ColumnInfo(name = "isSelectedLock") var isSelectedLock: Int = 0,  // 0 none, 1 current, 3 history
    @ColumnInfo(name = "isSelectedHome") var isSelectedHome: Int = 0,  // 0 none, 1 current, 3 history
    
    @ColumnInfo(name = "lastSetLockTime") var lastSetLockTime: Long = 0L,  // Timestamp when last set as Lock
    @ColumnInfo(name = "lastSetHomeTime") var lastSetHomeTime: Long = 0L,   // Timestamp when last set as Home
    
    @ColumnInfo(name = "is_test_data")
    var isTestData: Boolean = false,  // Flag to identify test data
    @ColumnInfo(name = "original_name")
    var originalName: String = "", // Store original name if modified,

    @ColumnInfo(name = "no_shuffle") var noShuffle: Boolean = false,

    @ColumnInfo(name = "price_points") var pricePoints: Int = 0,
    @ColumnInfo(name = "price_tier") var priceTier: Int = 0,
    @ColumnInfo(name = "is_for_you") var isForYou: Int = 0, // 0 = false, 1 = true
): Serializable

object PriceTier {
    const val NORMAL = 0
    const val MEDIUM = 1
    const val HIGH = 2

    fun random(): Int {
        val values = listOf(NORMAL, MEDIUM, HIGH)
        return values.random()
    }
}