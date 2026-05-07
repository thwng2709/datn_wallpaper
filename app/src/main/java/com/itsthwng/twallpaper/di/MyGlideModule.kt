package com.itsthwng.twallpaper.di

import android.content.Context
import com.bumptech.glide.Glide
import com.bumptech.glide.Registry
import com.bumptech.glide.annotation.GlideModule
import com.bumptech.glide.integration.okhttp3.OkHttpUrlLoader
import com.bumptech.glide.load.model.GlideUrl
import com.bumptech.glide.module.AppGlideModule
import com.itsthwng.twallpaper.local.GlideOkHttp
import dagger.hilt.EntryPoint
import dagger.hilt.InstallIn
import dagger.hilt.android.EntryPointAccessors
import dagger.hilt.components.SingletonComponent
import okhttp3.OkHttpClient
import java.io.InputStream

@GlideModule
class MyGlideModule : AppGlideModule() {

    @EntryPoint
    @InstallIn(SingletonComponent::class)
    interface GlideDeps {
        @GlideOkHttp
        fun glideOkHttpClient(): OkHttpClient
    }

    override fun registerComponents(
        context: Context,
        glide: Glide,
        registry: Registry
    ) {
        val appContext = context.applicationContext
        val entryPoint = EntryPointAccessors.fromApplication(appContext, GlideDeps::class.java)
        val client = entryPoint.glideOkHttpClient()

        val factory = OkHttpUrlLoader.Factory(client)
        registry.replace(
            GlideUrl::class.java,
            InputStream::class.java,
            factory
        )
    }

    override fun isManifestParsingEnabled() = false
}
