package com.itsthwng.twallpaper.ui.component.setting.view

import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.provider.Settings
import androidx.navigation.fragment.NavHostFragment
import com.itsthwng.twallpaper.ui.component.setting.viewmodel.SettingViewModel
import com.google.firebase.Firebase
import com.itsthwng.twallpaper.App
import com.itsthwng.twallpaper.R
import com.itsthwng.twallpaper.databinding.ActivitySettingBinding
import com.itsthwng.twallpaper.local.LocalStorage
import com.itsthwng.twallpaper.ui.base.BaseActivityBinding
import com.itsthwng.twallpaper.ui.component.bottomSheet.RequestInternetBottomSheet
import com.itsthwng.twallpaper.ui.component.permission.GrantPermissionFragment
import com.itsthwng.twallpaper.utils.AppConfig
import com.itsthwng.twallpaper.utils.ConnectionLiveDataJava
import com.itsthwng.twallpaper.utils.Constants
import com.itsthwng.twallpaper.utils.Logger
import com.itsthwng.twallpaper.utils.NetworkUtils
import dagger.hilt.android.AndroidEntryPoint
import java.util.Locale
import javax.inject.Inject

@AndroidEntryPoint
class SettingActivity: BaseActivityBinding<ActivitySettingBinding, SettingViewModel>() {

    @Inject
    lateinit var localStorage: LocalStorage
    var isShowGrantPermission = false
    var isShowDownloadFragment = false
    private var requestInternetBottomSheet : RequestInternetBottomSheet =
        RequestInternetBottomSheet()
    private lateinit var connectionLiveData: ConnectionLiveDataJava

    override fun attachBaseContext(newBase: Context?) {
        // Get localStorage from application context since DI is not ready yet
        val appContext = newBase?.applicationContext
        val localStorage = if (appContext != null) {
            try {
                App.instance.localStorage
            } catch (e: Exception) {
                null
            }
        } else null

        val context = if (newBase != null && localStorage != null) {
            AppConfig.updateResourcesLocale(newBase, localStorage.langCode)
        } else {
            newBase
        }
        super.attachBaseContext(context)
    }

    override fun getContentViewId(): Int {
        return R.layout.activity_setting
    }

    override fun initializeViews() {

    }

    override fun registerListeners() {

    }

    override fun initializeData() {

    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        println("Current locale: ${Locale.getDefault().language}")
        val fragmentToOpen = intent.getStringExtra("fragmentToOpen")
        val navHostFragment = (supportFragmentManager.findFragmentById(R.id.settingNavHostFragment) as NavHostFragment)
        val inflater = navHostFragment.navController.navInflater
        val graph = inflater.inflate(R.navigation.nav_setting)
        if(!fragmentToOpen.isNullOrEmpty()) {
            when(fragmentToOpen) {
                GrantPermissionFragment::class.java.simpleName -> {
                    isShowGrantPermission = true
                    graph.setStartDestination(R.id.grantPermissionFragment2)
                }
                "DownloadedFragment" -> {
                    isShowDownloadFragment = true
                    graph.setStartDestination(R.id.downloadedFragment)
                }
                else -> {
                    graph.setStartDestination(R.id.settingFragment2)
                }
            }
        }
        navHostFragment.navController.graph = graph
        connectionLiveData = ConnectionLiveDataJava(this)
        // Sử dụng instance đã khởi tạo thay vì tạo mới
        connectionLiveData.observe(this) { networkCollected ->
            if(networkCollected){
                if (requestInternetBottomSheet != null && requestInternetBottomSheet.isVisible) {
                    requestInternetBottomSheet.dismiss()
                }
            } else {
                if (requestInternetBottomSheet != null && !requestInternetBottomSheet.isVisible) {
                    initRequestInternetBottomSheet()
                }
            }
        }

    }
    override fun onResume() {
        super.onResume()
        println("Current locale: ${Locale.getDefault().language}")

        if (!NetworkUtils.isNetworkConnected()) {
            if (requestInternetBottomSheet != null && !requestInternetBottomSheet.isVisible) {
                initRequestInternetBottomSheet()
            }
        } else {
            if (requestInternetBottomSheet != null && requestInternetBottomSheet.isVisible) {
                requestInternetBottomSheet.dismiss()
            }
        }
    }

    private fun initRequestInternetBottomSheet() {
        requestInternetBottomSheet = RequestInternetBottomSheet()
        requestInternetBottomSheet.updateLanguage(this, localStorage.langCode)
        requestInternetBottomSheet.isCancelable = false
        requestInternetBottomSheet.clickConfirmYes = {
            if(NetworkUtils.isNetworkConnected()){
                if (requestInternetBottomSheet != null && requestInternetBottomSheet.isVisible) {
                    requestInternetBottomSheet.dismiss()
                }
            } else {
                try {
                    AppConfig.logEventTracking(Constants.HOME_REQUIRE_INTERNET_YES)
                    startActivity(Intent(Settings.ACTION_WIFI_SETTINGS))
                } catch (e: Exception) {
                    Logger.e(e.message)
                }
            }
        }
        requestInternetBottomSheet.clickConfirmNo = {
            try {
                AppConfig.logEventTracking(Constants.HOME_REQUIRE_INTERNET_EXIT)
            } catch (e: Exception) {
                Logger.e(e.message)
            }
        }
        requestInternetBottomSheet.clickConfirmCancel = {
            try {
                AppConfig.logEventTracking(Constants.HOME_REQUEST_INTERNET_CANCEL)
            } catch (e: Exception) {
                Logger.e(e.message)
            }
        }
        if (!this.isFinishing) {
            if (requestInternetBottomSheet.isVisible) {
                requestInternetBottomSheet.dismiss()
            }
            this.supportFragmentManager.let {
                requestInternetBottomSheet.show(
                    it,
                    RequestInternetBottomSheet.TAG
                )
            }
        }
    }

    fun getConnectionLiveData(): ConnectionLiveDataJava {
        if (::connectionLiveData.isInitialized) {
            return connectionLiveData
        } else {
            connectionLiveData = ConnectionLiveDataJava(this)
            return connectionLiveData
        }
    }

    companion object{
        const val TAG = "SettingActivity"
    }
}