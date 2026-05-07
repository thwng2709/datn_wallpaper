package com.itsthwng.twallpaper.utils.wallpaper

import com.itsthwng.twallpaper.data.model.SettingData

/**
 * Interface for wallpaper download operations
 * Provides abstraction for different download implementations
 */
interface WallpaperDownloader {
    
    /**
     * Download a wallpaper
     * @param wallpaper The wallpaper item to download
     * @param onProgress Progress callback (0-100)
     * @param onComplete Success callback
     * @param onError Error callback with error message
     */
    suspend fun download(
        wallpaper: SettingData.WallpapersItem,
        onProgress: (Int) -> Unit = {},
        onComplete: () -> Unit,
        onError: (String) -> Unit
    )
    
    /**
     * Cancel ongoing download
     */
    fun cancelDownload()
    
    /**
     * Check if a download is in progress
     */
    fun isDownloading(): Boolean
}

/**
 * Result wrapper for download operations
 */
sealed class DownloadResult {
    object Success : DownloadResult()
    data class Error(val message: String) : DownloadResult()
    data class Progress(val percentage: Int) : DownloadResult()
}