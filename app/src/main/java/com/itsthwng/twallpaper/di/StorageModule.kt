package com.itsthwng.twallpaper.di

import android.content.Context
import androidx.room.Room
import com.itsthwng.twallpaper.data.AppDatabase
import com.itsthwng.twallpaper.data.DatabaseInfo
import com.itsthwng.twallpaper.data.DatabaseMigrations
import com.itsthwng.twallpaper.local.LocalData
import com.itsthwng.twallpaper.local.LocalStorage
import com.itsthwng.twallpaper.repository.CategoriesRepository
import com.itsthwng.twallpaper.repository.CategoriesRepositoryImp
import com.itsthwng.twallpaper.repository.FileHelper
import com.itsthwng.twallpaper.repository.FileHelperImpl
import com.itsthwng.twallpaper.repository.WallpapersRepository
import com.itsthwng.twallpaper.repository.WallpapersRepositoryImp
import com.itsthwng.twallpaper.repository.ZipperRepository
import com.itsthwng.twallpaper.repository.ZipperRepositoryImp
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
class StorageModule {

    @Singleton
    @Provides
    fun fileHelper(fileHelper: FileHelperImpl): FileHelper = fileHelper

    @Singleton
    @Provides
    fun appDatabase(
        @ApplicationContext context: Context, @DatabaseInfo dbName: String
    ): AppDatabase {
        return Room.databaseBuilder(context, AppDatabase::class.java, dbName)
            .addMigrations(*DatabaseMigrations.getAllMigrations())
            .apply {
                // In development, allow destructive migration for easier testing
                if (com.itsthwng.twallpaper.BuildConfig.DEBUG) {
                    fallbackToDestructiveMigration()
                }
                // In production, force proper migrations
            }
            .build()
    }

    @Provides
    @Singleton
    fun provideLocalRepository(localStorage: LocalData): LocalStorage = localStorage

    @Singleton
    @Provides
    fun provideApplicationDao(db: AppDatabase) = db.applicationDao()

    @Singleton
    @Provides
    fun provideWallpaperDao(db: AppDatabase) = db.wallpaperDao()

    @Singleton
    @Provides
    fun provideCategoryDao(db: AppDatabase) = db.categoryDao()

    @Singleton
    @Provides
    fun provideZipperDao(db: AppDatabase) = db.zipperDao()

    @Singleton
    @Provides
    fun provideWallpaperRepository(wallpapersRepository: WallpapersRepositoryImp): WallpapersRepository = wallpapersRepository

    @Singleton
    @Provides
    fun provideCategoryRepository(categoriesRepository: CategoriesRepositoryImp): CategoriesRepository = categoriesRepository

    @Singleton
    @Provides
    fun provideZipperRepository(zipperRepository: ZipperRepositoryImp): ZipperRepository = zipperRepository

    // Legacy migrations - moved to DatabaseMigrations.kt
    // Keep empty for now to avoid breaking existing code

}