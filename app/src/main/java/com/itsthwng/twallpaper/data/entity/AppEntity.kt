package com.itsthwng.twallpaper.data.entity

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.PrimaryKey
import com.google.gson.annotations.SerializedName
import java.io.Serializable

@Entity(tableName = "app_entity")
data class AppEntity (
    @PrimaryKey(autoGenerate = false)
    @ColumnInfo(name = "appId")
    @SerializedName("appId")
    var appId: String = ""
): Serializable