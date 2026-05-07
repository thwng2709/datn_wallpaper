package com.itsthwng.twallpaper.ui.component.splash.view

import android.content.Intent
import android.os.Bundle
import android.provider.Settings
import android.view.View
import com.itsthwng.twallpaper.R
import com.itsthwng.twallpaper.databinding.ActivitySplashBinding
import com.itsthwng.twallpaper.local.LocalStorage
import com.itsthwng.twallpaper.ui.base.AutoDisposable
import com.itsthwng.twallpaper.ui.base.BaseActivityBinding
import com.itsthwng.twallpaper.ui.component.bottomSheet.RequestInternetBottomSheet
import com.itsthwng.twallpaper.ui.component.splash.viewmodel.TopicViewModel
import com.itsthwng.twallpaper.utils.AppConfig
import com.itsthwng.twallpaper.utils.ConnectionLiveDataJava
import com.itsthwng.twallpaper.utils.Constants
import com.itsthwng.twallpaper.utils.Logger
import com.itsthwng.twallpaper.utils.NetworkUtils
import dagger.hilt.android.AndroidEntryPoint
import io.reactivex.disposables.CompositeDisposable
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import javax.inject.Inject

@AndroidEntryPoint
class SplashLoadingActivity : BaseActivityBinding<ActivitySplashBinding, TopicViewModel>() {
    @Inject
    lateinit var localStorage: LocalStorage
    private val autoDisposable = AutoDisposable()
    lateinit var disposable: CompositeDisposable
    private var requestInternetBottomSheet: RequestInternetBottomSheet =
        RequestInternetBottomSheet()

    override fun getContentViewId(): Int {
        return R.layout.activity_splash
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        autoDisposable.bindTo(lifecycle)
        disposable = CompositeDisposable()

        val fragment =
            this.supportFragmentManager.findFragmentById(R.id.mainNavHostFragment)?.childFragmentManager?.fragments?.get(
                0
            )
        val connectionLiveData = ConnectionLiveDataJava(this)
        connectionLiveData.observe(this) {
            if (!it && (fragment is IntroFragment || fragment is AskLanguageFragment)) {
                dataBinding.llInternetConnect.visibility = View.GONE
                dataBinding.llInternetDisconnect.visibility = View.VISIBLE
                if (requestInternetBottomSheet != null && !requestInternetBottomSheet.isVisible) {
                    initRequestInternetBottomSheet()
                }
            } else {
                if (requestInternetBottomSheet != null && requestInternetBottomSheet.isVisible) {
                    requestInternetBottomSheet.dismiss()
                }
                dataBinding.llInternetConnect.visibility = View.GONE
                dataBinding.llInternetDisconnect.visibility = View.GONE
            }
        }
    }

    private fun initRequestInternetBottomSheet() {
        requestInternetBottomSheet = RequestInternetBottomSheet()
        requestInternetBottomSheet.updateLanguage(this, localStorage.langCode)
        requestInternetBottomSheet.isCancelable = false
        requestInternetBottomSheet.clickConfirmYes = {
            try {
                AppConfig.logEventTracking(Constants.MAIN_REQUIRE_INTERNET_YES)
                startActivity(Intent(Settings.ACTION_WIFI_SETTINGS))
            } catch (e: Exception) {
                Logger.e(e.message)
            }
        }
        requestInternetBottomSheet.clickConfirmNo = {
            try {
                AppConfig.logEventTracking(Constants.MAIN_REQUIRE_INTERNET_EXIT)
            } catch (e: Exception) {
                Logger.e(e.message)
            }
        }
        requestInternetBottomSheet.clickConfirmCancel = {
            try {
                AppConfig.logEventTracking(Constants.MAIN_REQUEST_INTERNET_CANCEL)
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


    override fun initializeViews() {
    }

    override fun registerListeners() {

    }

    override fun initializeData() {

    }

    override fun onDestroy() {
        super.onDestroy()
    }

    override fun onResume() {
        super.onResume()
    }

    override fun onBackPressed() {
        // Kiểm tra fragment hiện tại để xử lý Back button phù hợp
        val currentFragment =
            supportFragmentManager.fragments.getOrNull(0)?.childFragmentManager?.fragments?.get(0)

        when (currentFragment) {
            is AskLanguageFragment, is AskLanguageFragment2 -> {
                finish()
            }

            else -> {
                // Xử lý Back button bình thường cho các fragment khác
                super.onBackPressed()
            }
        }
    }

    fun checkShowRequestInternet() {
        try {
            dataBinding.let {
                val fragment =
                    this.supportFragmentManager.findFragmentById(R.id.mainNavHostFragment)?.childFragmentManager?.fragments?.get(
                        0
                    )
                if (!NetworkUtils.isNetworkConnected()) {
                    dataBinding.llInternetConnect.visibility = View.GONE
                    dataBinding.llInternetDisconnect.visibility = View.VISIBLE
                    if (requestInternetBottomSheet != null
                        && !requestInternetBottomSheet.isVisible
                        && (fragment is IntroFragment || fragment is AskLanguageFragment)
                    ) {
                        initRequestInternetBottomSheet()
                    }
                } else {
                    dataBinding.llInternetConnect.visibility = View.GONE
                    dataBinding.llInternetDisconnect.visibility = View.GONE
                }
            }
        } catch (ex: Exception) {
            Logger.e(ex.message)
        }
    }

    companion object {
        const val TAG = "SplashLoadingActivity"
    }
}