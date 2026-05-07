package com.itsthwng.twallpaper.data.model

import com.itsthwng.twallpaper.data.CommonInfo
import com.itsthwng.twallpaper.data.entity.CategoryEntity
import com.itsthwng.twallpaper.data.entity.PriceTier
import com.itsthwng.twallpaper.data.entity.WallpaperEntity
import com.itsthwng.twallpaper.data.entity.ZipperImageEntity

/**
 * Constants for wallpaper tags
 */
object WallpaperTags {
    const val ZIPPER_PREFIX = "zipper"
    const val ZIPPER_TAG_SEPARATOR = ","
}

fun SettingData.WallpapersItem.toWallpaperEntity(): WallpaperEntity {
    val testKeywords = listOf("test", "demo", "sample", "dummy", "placeholder", "example")
    val nameLower = name?.lowercase() ?: ""
    val tagsLower = tags?.lowercase() ?: ""

    val isTestData = testKeywords.any { keyword ->
        nameLower.contains(keyword) || tagsLower.contains(keyword)
    }

    return WallpaperEntity(
        wallpaperId = id ?: 0,
        accessType = accessType ?: 0,
        thumbnail = thumbnail ?: "",
        categoryId = categoryId ?: "",
        name = name ?: "",
        wallpaperType = wallpaperType ?: 0,
        content = content ?: "",
        isFeatured = isFeatured ?: 0,
        tags = tags ?: "",
        isFavorite = isFavorite?.let { if (it) 1 else 0 } ?: 0, // Convert Boolean to Int
        createdAt = createdAt ?: System.currentTimeMillis(),
        updatedAt = updatedAt ?: System.currentTimeMillis(),
        isSelectedHome = isSelectedHome ?: 0,  // Preserve history status
        isSelectedLock = isSelectedLock ?: 0,   // Preserve history status
        isDownloaded = isDownloaded ?: 0L,
        isTestData = isTestData,
        originalName = fileName,
        pricePoints = pricePoints,
        priceTier = priceTier,
        isForYou = if (isForYou) 1 else 0 // Convert Boolean to Int
    )
}

fun SettingData.CategoriesItem.toCategoryEntity(): CategoryEntity {
    return CategoryEntity(
        categoryId = id ?: "",
        title = title ?: "",
        image = image ?: "",
        type = type ?: 0, // 0: Images, 1: Live Images
        wallpapersCount = wallpapers_count ?: 0,
        originalTitle = originalTitle ?: ""
    )
}

fun CategoryEntity.toCategoriesItem(): SettingData.CategoriesItem {
    return SettingData.CategoriesItem(
        id = categoryId,
        title = title,
        image = image,
        type = type,
        wallpapers_count = wallpapersCount,
        originalTitle = originalTitle,
    )
}

fun WallpaperEntity.toWallpapersItem(): SettingData.WallpapersItem {
    return SettingData.WallpapersItem(
        id = wallpaperId,
        accessType = accessType,
        thumbnail = thumbnail,
        categoryId = categoryId,
        name = name,
        wallpaperType = wallpaperType,
        content = content,
        isFeatured = isFeatured,
        tags = tags,
        createdAt = createdAt,
        updatedAt = updatedAt,
        isFavorite = isFavorite == 1, // Convert from Int to Boolean
        isSelectedHome = isSelectedHome,
        isSelectedLock = isSelectedLock,
        isDownloaded = isDownloaded ?: 0L, // Ensure non-null value
        fileName = originalName,
        pricePoints = pricePoints,
        priceTier = getPriceFromTierWithRemoteConfig(priceTier),
        isForYou = isForYou == 1 // Convert from Int to Boolean
    )
}

fun getPriceFromTierWithRemoteConfig(tier: Int): Int{
    return when(tier){
        PriceTier.NORMAL -> CommonInfo.priceNormal.toInt()
        PriceTier.MEDIUM -> CommonInfo.priceMedium.toInt()
        PriceTier.HIGH -> CommonInfo.priceHigh.toInt()
        else -> CommonInfo.priceNormal.toInt()
    }
    return CommonInfo.priceNormal.toInt()
}

/**
 * Extension function to check if a WallpapersItem is a Zipper
 */
fun SettingData.WallpapersItem.isZipper(): Boolean {
    return tags?.startsWith(WallpaperTags.ZIPPER_PREFIX, ignoreCase = true) == true
}

/**
 * Convert WallpapersItem to ZipperImageEntity
 * Used when routing Zipper items through unified history
 */
fun SettingData.WallpapersItem.toZipperEntity(): ZipperImageEntity {
    return ZipperImageEntity(
        id = this.id ?: 0,
        categoryId = this.categoryId ?: "",
        name = this.name ?: "",
        content = this.content ?: "",
        accessType = this.accessType ?: 0,
        fileName = this.fileName,
        ordinalNumber = this.id ?: 0, // Preserve ordinalNumber using ID
        contentLeft = this.homeScreen ?: this.content ?: "",
        contentRight = this.lockScreen ?: this.content ?: "",
        type = this.tags?.removePrefix("${WallpaperTags.ZIPPER_PREFIX}${WallpaperTags.ZIPPER_TAG_SEPARATOR}") ?: "",
        isSelectedHome = this.isSelectedHome ?: 0,
        isSelectedLock = this.isSelectedLock ?: 0
    )
}

/**
 * Convert ZipperImageEntity to WallpapersItem for unified History display
 */
fun ZipperImageEntity.toWallpapersItem(): SettingData.WallpapersItem {
    return SettingData.WallpapersItem(
        id = id,
        accessType = accessType,
        thumbnail = contentLeft.ifEmpty { content }, // Use contentLeft as thumbnail, fallback to content
        categoryId = categoryId,
        name = name,
        wallpaperType = 0, // Treat as static image type for compatibility
        content = content,
        isFeatured = 0,
        tags = "${WallpaperTags.ZIPPER_PREFIX}${WallpaperTags.ZIPPER_TAG_SEPARATOR}${type.orEmpty()}", // Add zipper tag
        createdAt = 0L,
        updatedAt = 0L,
        isFavorite = false, // Zippers don't have favorite feature yet
        isSelectedHome = isSelectedHome,
        isSelectedLock = isSelectedLock,
        isDownloaded = 0L,
        fileName = fileName,
        homeScreen = contentLeft,
        lockScreen = contentRight
    )
}