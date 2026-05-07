package com.itsthwng.twallpaper.di

import com.itsthwng.twallpaper.BuildConfig
import com.itsthwng.twallpaper.server.Network
import com.itsthwng.twallpaper.server.NetworkConnectivity
import com.squareup.moshi.Moshi
import com.itsthwng.twallpaper.local.GlideOkHttp
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import okhttp3.*
import okhttp3.logging.HttpLoggingInterceptor
import java.util.concurrent.TimeUnit
import javax.inject.Singleton

@InstallIn(SingletonComponent::class)
@Module
object NetworkModule {
    const val TIME_OUT: Long = 9_000
    const val EXPIRED_TIME_CACHE: Long = 1 * 1000 * 3600 * 24 // 1 DAYS
    private val userAgent = "TPcom/3.0 " + System.getProperty("http.agent")

    @Provides
    @Singleton
    fun provideNetworkConnectivity(netWork: Network): NetworkConnectivity = netWork

    /** Client CHUYÊN cho Glide: nhẹ, ổn định tải hàng loạt ảnh */
    @Provides
    @Singleton
    @GlideOkHttp
    fun provideGlideOkHttpClient(
        interceptor: HttpLoggingInterceptor
    ): OkHttpClient = OkHttpClient.Builder()
        // Logging nhẹ để debug mạng khi cần
        .addInterceptor(interceptor)
        // Không thêm business interceptor nếu ảnh là public CDN (nhẹ & nhanh hơn)
        .retryOnConnectionFailure(true)
        .followRedirects(true)
        // Pool lớn + keep-alive lâu để tái sử dụng socket, giảm "Socket is closed"
        .connectionPool(ConnectionPool(12, 5, TimeUnit.MINUTES))
        // Hạn mức đồng thời hợp lý (tránh burst quá lớn tới 1 host)
        .dispatcher(Dispatcher().apply {
            maxRequests = 32
            maxRequestsPerHost = 8
        })
        // Timeout “dễ thở” cho tải ảnh lớn/xa
        .connectTimeout(15, TimeUnit.SECONDS)
        .readTimeout(25, TimeUnit.SECONDS)
        .writeTimeout(25, TimeUnit.SECONDS)
        // KHÔNG đặt callTimeout cho Glide
        .build()

    @Provides
    fun provideLoggingInterceptor() = HttpLoggingInterceptor().apply {
        level =
            if (BuildConfig.DEBUG) HttpLoggingInterceptor.Level.BODY else HttpLoggingInterceptor.Level.NONE
    }

    private fun getMoshi(): Moshi {
        return Moshi.Builder().build()
    }
}