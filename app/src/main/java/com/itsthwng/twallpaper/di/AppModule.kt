package com.itsthwng.twallpaper.di

import android.annotation.SuppressLint
import android.content.Context
import android.os.Build
import android.provider.Settings
import com.itsthwng.twallpaper.local.PreferenceInfo
import com.itsthwng.twallpaper.data.DatabaseInfo
import com.itsthwng.twallpaper.local.MobileIdInfo
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import javax.inject.Named
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
class AppModule {

    @Singleton
    @Provides
    @PreferenceInfo
    fun preferencesName(): String {
        return "sharedPreferences"
    }

    @Singleton
    @Provides
    @DatabaseInfo
    fun databaseName(): String {
        return "wallpaper_database"
    }

    @SuppressLint("HardwareIds")
    @Singleton
    @Provides
    @MobileIdInfo
    fun providerMobileId(@ApplicationContext context: Context) = Settings.Secure.getString(
        context.contentResolver,
        Settings.Secure.ANDROID_ID
    ) + "_sdk" + Build.VERSION.SDK_INT

    @Singleton
    @Provides
    @Named("AppId")
    fun providerAppId() = "auto_clicker_pro"
}
