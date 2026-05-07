package com.itsthwng.twallpaper.data

import androidx.room.Database
import androidx.room.RoomDatabase
import com.itsthwng.twallpaper.data.dao.ApplicationDao
import com.itsthwng.twallpaper.data.dao.CategoryDao
import com.itsthwng.twallpaper.data.dao.WallpaperDao
import com.itsthwng.twallpaper.data.dao.ZipperDao
import com.itsthwng.twallpaper.data.entity.AppEntity
import com.itsthwng.twallpaper.data.entity.CategoryEntity
import com.itsthwng.twallpaper.data.entity.WallpaperEntity
import com.itsthwng.twallpaper.data.entity.ZipperImageEntity

@Database(
    entities = [AppEntity::class, WallpaperEntity::class, CategoryEntity::class, ZipperImageEntity::class], 
    version = 10,
    autoMigrations = [
        // Add auto migrations here for future schema changes
        // Example: AutoMigration(from = 3, to = 4)
    ],
    exportSchema = true
)
abstract class AppDatabase : RoomDatabase() {
    abstract fun applicationDao(): ApplicationDao
    abstract fun wallpaperDao(): WallpaperDao
    abstract fun categoryDao(): CategoryDao
    abstract fun zipperDao(): ZipperDao
}