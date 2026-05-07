package com.itsthwng.twallpaper.utils

import android.content.Context

/**
 * Thread-safe state manager for Zipper Lock Screen Service
 * This replaces the unsafe static field approach
 */
object ZipperServiceState {
    private const val PREF_NAME = "zipper_service_state"
    private const val KEY_SERVICE_RUNNING = "service_running"
    private const val KEY_SERVICE_START_TIME = "service_start_time"
    private const val KEY_LOCK_DISMISSED_TIME = "lock_dismissed_time"

    @Synchronized
    fun setServiceRunning(context: Context, isRunning: Boolean) {
        val prefs = context.getSharedPreferences(PREF_NAME, Context.MODE_PRIVATE)
        prefs.edit().apply {
            putBoolean(KEY_SERVICE_RUNNING, isRunning)
            if (isRunning) {
                putLong(KEY_SERVICE_START_TIME, System.currentTimeMillis())
            } else {
                putLong(KEY_LOCK_DISMISSED_TIME, System.currentTimeMillis())
            }
            apply()
        }
    }

    @Synchronized
    fun isServiceRunning(context: Context): Boolean {
        val prefs = context.getSharedPreferences(PREF_NAME, Context.MODE_PRIVATE)
        return prefs.getBoolean(KEY_SERVICE_RUNNING, false)
    }

    @Synchronized
    fun getTimeSinceDismissed(context: Context): Long {
        val prefs = context.getSharedPreferences(PREF_NAME, Context.MODE_PRIVATE)
        val dismissedTime = prefs.getLong(KEY_LOCK_DISMISSED_TIME, 0)
        return if (dismissedTime > 0) {
            System.currentTimeMillis() - dismissedTime
        } else {
            Long.MAX_VALUE
        }
    }

    @Synchronized
    fun shouldShowLockScreen(context: Context): Boolean {
        // Don't show lock screen if it was just dismissed (within 2 seconds)
        // This prevents the screen from immediately showing again after unlock
        return getTimeSinceDismissed(context) > 2000
    }
}