package com.itsthwng.twallpaper.repository

import com.itsthwng.twallpaper.data.model.SettingData
import kotlinx.coroutines.flow.Flow

interface WallpapersRepository {
    suspend fun getWallpapers(): List<SettingData.WallpapersItem>
    suspend fun getWallPapersByCategory(categoryId: String): List<SettingData.WallpapersItem>
    suspend fun getFeaturesWallpapers(): List<SettingData.WallpapersItem>
    suspend fun getFavoriteWallpapers(): List<SettingData.WallpapersItem>
    suspend fun getDownloadedWallpapers(): List<SettingData.WallpapersItem>

    suspend fun updateWallpaper(wallpaper: SettingData.WallpapersItem)

    suspend fun saveWallpapers(wallpapers: List<SettingData.WallpapersItem>)

    suspend fun deleteAllWallpapers()
    suspend fun deleteWallpaperById(id: String)

    fun observeWallpapers(): Flow<List<SettingData.WallpapersItem>>
    fun observeNewWallpapers(): Flow<List<SettingData.WallpapersItem>>
    fun observeWallpapersByCategory(categoryId: String): Flow<List<SettingData.WallpapersItem>>
    fun observeFeaturedWallpapers(): Flow<List<SettingData.WallpapersItem>>
    fun observeRandomFeaturedWallpapers(onlyCategoryIds: Set<String>? = null): Flow<List<SettingData.WallpapersItem>>
    fun observeFavoriteWallpapers(): Flow<List<SettingData.WallpapersItem>>
    fun observeDownloadedWallpapers(): Flow<List<SettingData.WallpapersItem>>
    fun observeSearchKeyword(
        keyword: String,
        wallpaperType: Int? = null  // null = không lọc
    ): Flow<List<SettingData.WallpapersItem>>
    suspend fun loadPage(categoryId: String?, page: Int, pageSize: Int): List<SettingData.WallpapersItem>
    suspend fun updateSelectedWallpaper(wallpapersItem: SettingData.WallpapersItem, type: Int = 1)
    suspend fun updateWallpaperDownloadStatus(id: Int, isDownloaded: Long)

    suspend fun searchPage(
        keyword: String,
        wallpaperType: Int?,  // null = không lọc
        page: Int,
        pageSize: Int
    ): List<SettingData.WallpapersItem>

    suspend fun countSearch(keyword: String, wallpaperType: Int?): Int
    suspend fun getHistoryWallpapers(): List<SettingData.WallpapersItem>
    fun observeHistoryWallpapers(): Flow<List<SettingData.WallpapersItem>>
    fun observeCurrentWallpapers(): Flow<List<SettingData.WallpapersItem>>
    fun observeWallpapersOrdered(categoryId: String): Flow<List<SettingData.WallpapersItem>>
    suspend fun removeFromHistory(wallpaperId: Int, isZipper: Boolean = false)
}