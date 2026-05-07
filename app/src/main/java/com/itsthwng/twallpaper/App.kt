package com.itsthwng.twallpaper

import android.annotation.SuppressLint
import android.app.Activity
import android.app.Application
import android.app.KeyguardManager
import android.content.Context
import android.content.Intent
import android.os.Build
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.util.Log
import androidx.annotation.RequiresApi
import androidx.hilt.work.HiltWorkerFactory
import androidx.lifecycle.DefaultLifecycleObserver
import androidx.lifecycle.LifecycleOwner
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ProcessLifecycleOwner
import androidx.work.Configuration
import com.downloader.PRDownloader
import com.itsthwng.twallpaper.data.CommonInfo
import com.itsthwng.twallpaper.local.LocalStorage
import com.itsthwng.twallpaper.local.MobileIdInfo
import com.itsthwng.twallpaper.remote.RemoteConfig
import com.itsthwng.twallpaper.server.Network
import com.itsthwng.twallpaper.ui.component.splash.view.SplashLoadingActivity
import com.itsthwng.twallpaper.utils.AppConfig
import com.itsthwng.twallpaper.utils.ConnectionLiveDataJava
import com.itsthwng.twallpaper.utils.NetworkUtils
import dagger.hilt.android.HiltAndroidApp
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import java.time.LocalDate
import javax.inject.Inject

@HiltAndroidApp
class App : Application(), Configuration.Provider, DefaultLifecycleObserver{
    @Inject
    lateinit var localStorage: LocalStorage

    @Inject
    lateinit var network: Network

    @Inject
    @MobileIdInfo
    lateinit var androidId: String

    @Inject lateinit var workerFactory: HiltWorkerFactory
    override val workManagerConfiguration: Configuration
        get() = Configuration.Builder().setWorkerFactory(workerFactory).build()

    private var mKL: KeyguardManager.KeyguardLock? = null
    private var mKM: KeyguardManager? = null
    var hasHandledColdStart = false
    private val initListeners = mutableListOf<() -> Unit>()
    var isInitialized = false
        private set

    var countClickSplash: Int = 0
    var isTurnOnDebugMode = false


    override fun onStart(owner: LifecycleOwner) {
        super.onStart(owner)
        // App vừa vào foreground
        if (AppConfig.shouldShowToday(this)) {
            // Tránh tự mở lại khi đang ở chính SplashActivity
            // => Đặt guard bằng Activity đang top
            localStorage.callOnboardingFlow = true
            localStorage.lastOpenDate = LocalDate.now().toString()
            val launch = {
                startActivity(
                    Intent(this, SplashLoadingActivity::class.java)
                        .addFlags(
                            Intent.FLAG_ACTIVITY_NEW_TASK or
                                    Intent.FLAG_ACTIVITY_CLEAR_TOP or
                                    Intent.FLAG_ACTIVITY_SINGLE_TOP
                        )
                        .putExtra("showOnboardingNewDay", true)
                )
            }

            val top = TopActivityHolder.currentActivity
            if (top == null) {
                // Đợi activity đầu tiên lên resume rồi mới check/launch
                Handler(Looper.getMainLooper()).post {
                    val t = TopActivityHolder.currentActivity
                    if (t !is SplashLoadingActivity) launch()
                }
            } else if (top !is SplashLoadingActivity) {
                launch()
            }
        }
    }

    override fun onTrimMemory(level: Int) {
        super.onTrimMemory(level)
        if(level == TRIM_MEMORY_UI_HIDDEN) {
            localStorage.lastOpenDate = LocalDate.now().toString()
        }
    }

    var isAppInBackground = false
    override fun onStop(owner: LifecycleOwner) {
        super.onStop(owner)
        isAppInBackground = true
    }
    override fun attachBaseContext(context: Context?) {
        super.attachBaseContext(context)
    }

    @SuppressLint("HardwareIds")
    override fun onCreate() {
        super<Application>.onCreate()
        ProcessLifecycleOwner.get().lifecycle.addObserver(this)
        instance = this
        TopActivityHolder.init(this)
        
        // Initialize PRDownloader for video downloads
        PRDownloader.initialize(applicationContext)

        setupAppConfig()

        CoroutineScope(Dispatchers.IO).launch {
            CommonInfo.initDefaults()
            CommonInfo.fetchRemoteConfig {
                isInitialized = true
                initListeners.forEach { it.invoke() }
                initListeners.clear()
            }
        }
        val keyguardManager = getSystemService(KEYGUARD_SERVICE) as KeyguardManager
        this.mKM = keyguardManager
        this.mKL = keyguardManager.newKeyguardLock("IN")
    }

    companion object {
        lateinit var instance: App
        fun getApplication(): App {
            return instance
        }
        var TAG = "Application"
    }

    override fun onConfigurationChanged(newConfig: android.content.res.Configuration) {
        super.onConfigurationChanged(newConfig)
        setupAppConfig()
    }

    private fun setupAppConfig() {
        CoroutineScope(Dispatchers.IO).launch {
            AppConfig.setup(applicationContext)
            RemoteConfig.ANDROID_ID = androidId
        }
    }

    fun doWhenInitialized(action: () -> Unit) {
        if (isInitialized) action()
        else initListeners.add(action)
    }
}


object TopActivityHolder : Application.ActivityLifecycleCallbacks {
    @Volatile var currentActivity: Activity? = null

    fun init(app: Application) {
        app.registerActivityLifecycleCallbacks(this)
    }

    override fun onActivityCreated(
        activity: Activity,
        savedInstanceState: Bundle?
    ) {

    }

    override fun onActivityStarted(activity: Activity) {
        currentActivity = activity
    }

    override fun onActivityResumed(activity: Activity) {
        currentActivity = activity
    }
    override fun onActivityPaused(activity: Activity) {

    }

    override fun onActivityStopped(activity: Activity) {
        if (currentActivity === activity) currentActivity = null
    }

    override fun onActivitySaveInstanceState(
        activity: Activity,
        outState: Bundle
    ) {}

    override fun onActivityDestroyed(activity: Activity) {}
    // các hàm khác có thể để trống
}