package com.itsthwng.twallpaper.utils

import android.Manifest
import android.content.Context
import android.content.pm.PackageManager
import android.os.Build
import android.os.Environment
import androidx.core.content.ContextCompat

/**
 * Centralized permission management for the app
 * Handles storage permissions across different Android versions
 * 
 * Android Permission Evolution:
 * - Android 9 and below: WRITE_EXTERNAL_STORAGE required
 * - Android 10-12: Scoped storage (no permission for MediaStore)
 * - Android 13+: READ_MEDIA_IMAGES for reading images
 */
object PermissionManager {
    
    /**
     * Check if app has all required storage permissions
     */
    fun hasStoragePermission(context: Context): Boolean {
        return when {
            Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU -> {
                // Android 13+ doesn't need permission to save to MediaStore
                // but needs READ_MEDIA_IMAGES to read images
                true // We only save, not read in this app
            }
            Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q -> {
                // Android 10-12 uses scoped storage
                true
            }
            else -> {
                // Android 9 and below needs WRITE_EXTERNAL_STORAGE
                ContextCompat.checkSelfPermission(
                    context,
                    Manifest.permission.WRITE_EXTERNAL_STORAGE
                ) == PackageManager.PERMISSION_GRANTED
            }
        }
    }
    
    /**
     * Check if app has permission to read media files
     * Required for Android 13+ when reading images from MediaStore
     */
    fun hasMediaReadPermission(context: Context): Boolean {
        return when {
            Build.VERSION.SDK_INT >= 34 -> {
                // Android 14+ - check for either full or partial media permission
                val hasFullPermission = ContextCompat.checkSelfPermission(
                    context,
                    Manifest.permission.READ_MEDIA_IMAGES
                ) == PackageManager.PERMISSION_GRANTED
                
                val hasPartialPermission = ContextCompat.checkSelfPermission(
                    context,
                    "android.permission.READ_MEDIA_VISUAL_USER_SELECTED"
                ) == PackageManager.PERMISSION_GRANTED
                
                hasFullPermission || hasPartialPermission
            }
            Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU -> {
                ContextCompat.checkSelfPermission(
                    context,
                    Manifest.permission.READ_MEDIA_IMAGES
                ) == PackageManager.PERMISSION_GRANTED
            }
            else -> {
                // For older versions, storage permission covers reading
                hasStoragePermission(context)
            }
        }
    }
    
    /**
     * Check if app has manage all files permission (Android 11+)
     * This is only needed if app wants to access all files outside scoped storage
     */
    fun hasManageFilesPermission(): Boolean {
        return if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R) {
            Environment.isExternalStorageManager()
        } else {
            // Not applicable for Android 10 and below
            true
        }
    }
    
    /**
     * Get the storage permission needed for saving files
     * Returns null for Android Q+ since no permission is needed for MediaStore writes
     */
    fun getStoragePermission(): String? {
        return when {
            Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q -> {
                null // No permission needed for saving to MediaStore
            }
            else -> {
                Manifest.permission.WRITE_EXTERNAL_STORAGE
            }
        }
    }
    
    /**
     * Get permissions needed for reading media files
     */
    fun getMediaReadPermissions(): Array<String> {
        return when {
            Build.VERSION.SDK_INT >= 34 -> {
                // Android 14+ supports partial media permissions
                arrayOf(
                    Manifest.permission.READ_MEDIA_IMAGES,
                    if (Build.VERSION.SDK_INT >= 34) {
                        "android.permission.READ_MEDIA_VISUAL_USER_SELECTED"
                    } else {
                        Manifest.permission.READ_MEDIA_IMAGES
                    }
                )
            }
            Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU -> {
                arrayOf(Manifest.permission.READ_MEDIA_IMAGES)
            }
            Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q -> {
                emptyArray() // Scoped storage handles this
            }
            else -> {
                arrayOf(Manifest.permission.READ_EXTERNAL_STORAGE)
            }
        }
    }
    
    /**
     * Check if we should show permission rationale
     * This helps determine if user has denied permission before
     */
    fun shouldShowStoragePermissionRationale(activity: android.app.Activity): Boolean {
        val permission = getStoragePermission() ?: return false
        return activity.shouldShowRequestPermissionRationale(permission)
    }
    
    /**
     * Check if we should show rationale for media read permission
     */
    fun shouldShowMediaReadPermissionRationale(activity: android.app.Activity): Boolean {
        return if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            activity.shouldShowRequestPermissionRationale(Manifest.permission.READ_MEDIA_IMAGES)
        } else {
            shouldShowStoragePermissionRationale(activity)
        }
    }
    
    /**
     * Get all storage permissions array for legacy support
     */
    fun getStoragePermissions(): Array<String> {
        return when {
            Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q -> {
                emptyArray() // No permissions needed for saving
            }
            else -> {
                arrayOf(Manifest.permission.WRITE_EXTERNAL_STORAGE)
            }
        }
    }
    
    /**
     * Extension function to check storage permission
     * Using different name to avoid JVM signature clash
     */
    fun Context.checkStoragePermission(): Boolean = hasStoragePermission(this)
    
    /**
     * Extension function to check media read permission
     * Using different name to avoid JVM signature clash
     */
    fun Context.checkMediaReadPermission(): Boolean = hasMediaReadPermission(this)
}