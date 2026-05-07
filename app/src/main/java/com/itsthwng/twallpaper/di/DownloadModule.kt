package com.itsthwng.twallpaper.di

import android.content.Context
import com.itsthwng.twallpaper.utils.permission.PermissionDialogManager
import com.itsthwng.twallpaper.utils.permission.PermissionHandler
import com.itsthwng.twallpaper.utils.wallpaper.WallpaperDownloader
import com.itsthwng.twallpaper.utils.wallpaper.WallpaperDownloaderImpl
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

/**
 * Dependency injection module for download and permission related components
 */
@Module
@InstallIn(SingletonComponent::class)
object DownloadModule {
    
    @Provides
    @Singleton
    fun provideWallpaperDownloader(
        @ApplicationContext context: Context
    ): WallpaperDownloader {
        return WallpaperDownloaderImpl(context)
    }
    
    @Provides
    @Singleton
    fun providePermissionDialogManager(): PermissionDialogManager {
        return PermissionDialogManager()
    }
    
    @Provides
    fun providePermissionHandler(
        dialogManager: PermissionDialogManager
    ): PermissionHandler {
        return PermissionHandler(dialogManager)
    }
}