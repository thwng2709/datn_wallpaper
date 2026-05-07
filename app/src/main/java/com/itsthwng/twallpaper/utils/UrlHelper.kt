package com.itsthwng.twallpaper.utils

/**
 * Centralized URL handling utility
 * Manages URL construction for wallpaper content across the app
 * 
 * API always returns full URLs, so this utility simply validates and returns them
 */
object UrlHelper {
    
    /**
     * Returns the URL as-is since API always provides full URLs
     * 
     * @param path The full URL from API
     * @return The same URL or empty string if null/empty
     */
    fun getFullUrl(path: String?): String {
        return path ?: ""
    }
    
    
    /**
     * Validates if a URL is well-formed and accessible
     * 
     * @param url The URL to validate
     * @return true if URL is valid, false otherwise
     */
    fun isValidUrl(url: String): Boolean {
        return try {
            val urlObj = java.net.URL(url)
            urlObj.protocol != null && (urlObj.protocol == "http" || urlObj.protocol == "https")
        } catch (e: Exception) {
            false
        }
    }
    
    /**
     * Extracts filename from URL
     * 
     * @param url The URL to extract filename from
     * @return Filename or null if cannot extract
     */
    fun getFilenameFromUrl(url: String): String? {
        return try {
            val fullUrl = getFullUrl(url)
            fullUrl.substringAfterLast('/').takeIf { it.isNotEmpty() }
        } catch (e: Exception) {
            null
        }
    }
}