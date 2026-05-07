package com.itsthwng.twallpaper.data.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import com.itsthwng.twallpaper.data.entity.AppEntity

@Dao
abstract class ApplicationDao {
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    abstract fun insert(data: AppEntity): Long

    @Query("DELETE FROM app_entity")
    abstract fun deleteAll()

    @Query("DELETE FROM app_entity WHERE appId = :id")
    abstract fun deleteById(id: String)

    @Query("SELECT * FROM app_entity")
    abstract fun getAllData(): List<AppEntity>
}