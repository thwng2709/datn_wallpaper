package com.itsthwng.twallpaper.utils

import android.content.Context
import android.content.SharedPreferences
import com.itsthwng.twallpaper.BuildConfig
import dagger.hilt.android.qualifiers.ApplicationContext
import javax.inject.Inject
import javax.inject.Singleton

/**
 * Developer settings for controlling app behavior during development/testing
 */
@Singleton
class DeveloperSettings @Inject constructor(
    @ApplicationContext private val context: Context
) {
    private val prefs: SharedPreferences = context.getSharedPreferences(
        "developer_settings",
        Context.MODE_PRIVATE
    )
    
    companion object {
        private const val KEY_SHOW_TEST_DATA = "show_test_data"
    }
    
    /**
     * Controls whether test data should be shown in the app
     * Default: true in debug builds, false in release builds
     */
    var showTestData: Boolean
        get() = prefs.getBoolean(KEY_SHOW_TEST_DATA, BuildConfig.DEBUG)
        set(value) = prefs.edit().putBoolean(KEY_SHOW_TEST_DATA, value).apply()
    
    /**
     * Check if we should include test data in queries
     * This returns false in production to always hide test data from end users
     */
    fun shouldIncludeTestData(): Boolean {
        return if (BuildConfig.DEBUG) {
            showTestData
        } else {
            false // Never show test data in release builds
        }
    }
    
    /**
     * Reset all developer settings to default values
     */
    fun resetToDefaults() {
        prefs.edit().clear().apply()
    }
}