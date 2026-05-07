package com.itsthwng.twallpaper.ui.component

import android.Manifest
import android.annotation.SuppressLint
import android.app.ActivityOptions
import android.app.NotificationManager
import android.content.ContentValues
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.content.res.Configuration
import android.content.res.Resources
import android.os.Build
import android.os.Bundle
import android.os.PowerManager
import android.provider.Settings
import android.util.Log
import android.view.View
import android.widget.LinearLayout
import android.widget.RelativeLayout
import androidx.activity.result.ActivityResultLauncher
import androidx.activity.result.contract.ActivityResultContracts
import androidx.core.content.ContextCompat
import androidx.core.view.GravityCompat
import androidx.drawerlayout.widget.DrawerLayout
import androidx.fragment.app.DialogFragment
import androidx.fragment.app.Fragment
import com.google.firebase.Firebase
import com.itsthwng.twallpaper.App
import com.itsthwng.twallpaper.R
import com.itsthwng.twallpaper.data.CommonInfo
import com.itsthwng.twallpaper.databinding.ActivityMyHomeBinding
import com.itsthwng.twallpaper.di.SessionManager
import com.itsthwng.twallpaper.local.LocalStorage
import com.itsthwng.twallpaper.ui.base.AutoDisposable
import com.itsthwng.twallpaper.ui.base.BaseActivityBinding
import com.itsthwng.twallpaper.ui.component.bottomSheet.BottomSheetExitApp
import com.itsthwng.twallpaper.ui.component.bottomSheet.RequestInternetBottomSheet
import com.itsthwng.twallpaper.ui.component.history.fragment.HistoryFragment
import com.itsthwng.twallpaper.ui.component.home.adapter.HomePagerAdapter
import com.itsthwng.twallpaper.ui.component.home.fragment.CategoryFragment
import com.itsthwng.twallpaper.ui.component.home.fragment.FavouriteFragment
import com.itsthwng.twallpaper.ui.component.home.fragment.HomeFragment
import com.itsthwng.twallpaper.ui.component.permission.GrantPermissionFragment
import com.itsthwng.twallpaper.ui.component.search.SearchActivity
import com.itsthwng.twallpaper.ui.component.setting.view.SettingActivity
import com.itsthwng.twallpaper.ui.component.zipper.zipperlock.ZipLockMainActivity
import com.itsthwng.twallpaper.userId.GenerateUserId
import com.itsthwng.twallpaper.utils.AppConfig
import com.itsthwng.twallpaper.utils.CommonUtil
import com.itsthwng.twallpaper.utils.ConnectionLiveDataJava
import com.itsthwng.twallpaper.utils.Constants
import com.itsthwng.twallpaper.utils.Constants.PERMISSIONS
import com.itsthwng.twallpaper.utils.Logger
import com.itsthwng.twallpaper.utils.NetworkUtils
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import java.security.MessageDigest
import java.util.Locale
import javax.inject.Inject

@AndroidEntryPoint
class MainActivity : BaseActivityBinding<ActivityMyHomeBinding, MainViewModel>() {
    private val autoDisposable = AutoDisposable()
    @Inject
    lateinit var localStorage: LocalStorage
    @Inject
    lateinit var sessionManager: SessionManager

    lateinit var fragmentsList: ArrayList<Fragment>

    private var requestInternetBottomSheet: RequestInternetBottomSheet =
        RequestInternetBottomSheet()
    private var isAppInBackground: Boolean? = false
    private lateinit var connectionLiveData: ConnectionLiveDataJava

    private var languageCode = ""

    private lateinit var exitAppBottomSheet: BottomSheetExitApp

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

        Logger.d(
            "HienDV_Language",
            "attachBaseContext: ${localStorage?.langCode} and locale: ${Locale.getDefault().language}"
        )
        val context = if (newBase != null && localStorage != null) {
            AppConfig.updateResourcesLocale(newBase, localStorage.langCode)
        } else {
            newBase
        }
        Logger.d(
            "HienDV_Language",
            "attachBaseContext22: ${localStorage?.langCode} and locale: ${Locale.getDefault().language}"
        )
        super.attachBaseContext(context)
    }

    override fun onViewCreated(savedInstanceState: Bundle?) {
        super.onViewCreated(savedInstanceState)

        Logger.d(
            "HienDV_Language",
            "onViewCreated: ${localStorage.langCode} and locale: ${Locale.getDefault().language}"
        )
        Logger.d("HienDV_Language", "onViewCreated currentLanguage: ${getString(R.string.locale)}")
    }

    override fun onTrimMemory(level: Int) {
        super.onTrimMemory(level)
        if (level == TRIM_MEMORY_UI_HIDDEN) {
            isAppInBackground = true
        }
    }

    override fun onResume() {
        super.onResume()
        if (languageCode != localStorage.langCode) {
            Logger.d(
                "HienDV_Language",
                "onResume: ${localStorage.langCode} and locale: ${Locale.getDefault().language}"
            )
            val resources: Resources? = this.resources
            val tag = localStorage.langCode
            val locale = Locale.forLanguageTag(tag)
            Locale.setDefault(locale)
            val config = Configuration()
            config.locale = locale
            resources?.updateConfiguration(config, resources.displayMetrics)
            languageCode = localStorage.langCode // Cập nhật mã ngôn ngữ mới
            Logger.d(
                "HienDV_Language",
                "new onResume: ${localStorage.langCode} and locale: ${Locale.getDefault().language}"
            )
            recreateWithoutBlink()
        }
        if (!NetworkUtils.isNetworkConnected()) {
            if (requestInternetBottomSheet != null && !requestInternetBottomSheet.isVisible) {
                initRequestInternetBottomSheet()
            }
        } else {
            if (requestInternetBottomSheet != null && requestInternetBottomSheet.isVisible) {
                requestInternetBottomSheet.dismiss()
            }
        }
        CoroutineScope(Dispatchers.Main).launch {
            CommonInfo.fetchRemoteConfig {
                AppConfig.setCheckedConfig(CommonInfo.version_code_an_qc)
            }
        }

        // Đồng bộ ViewPager với selectedTab
        val currentTab = viewModel.selectedTab.value ?: 0
        if (dataBinding.vPager.currentItem != currentTab) {
            dataBinding.vPager.currentItem = currentTab
        }
        showHideRedDot(this)
        // Check permission changes when returning from Settings
        checkPermissionChangesFromSettings()
    }

    private fun logSigningSha256Safe() {
        try {
            val pm = packageManager

            val pkgInfo = if (Build.VERSION.SDK_INT >= 33) {
                pm.getPackageInfo(
                    packageName,
                    PackageManager.PackageInfoFlags.of(
                        PackageManager.GET_SIGNING_CERTIFICATES.toLong()
                    )
                )
            } else {
                @Suppress("DEPRECATION")
                pm.getPackageInfo(
                    packageName,
                    if (Build.VERSION.SDK_INT >= 28)
                        PackageManager.GET_SIGNING_CERTIFICATES
                    else
                        PackageManager.GET_SIGNATURES
                )
            }

            val signatures: Array<android.content.pm.Signature>? =
                if (Build.VERSION.SDK_INT >= 28) {
                    // Tránh chạm vào SigningInfo trên máy < 28
                    val si = pkgInfo.signingInfo
                    val arr = when {
                        si == null -> null
                        si.hasMultipleSigners() -> si.apkContentsSigners
                        else -> si.signingCertificateHistory
                    }
                    arr
                } else {
                    @Suppress("DEPRECATION")
                    pkgInfo.signatures
                }

            val firstSig = signatures?.firstOrNull() ?: return
            val sha = sha256ColonSeparated(firstSig.toByteArray())
            Log.d("SigningSHA256", sha)
        } catch (e: NoSuchFieldError) {
            // Trường hợp verifier cố resolve field không tồn tại trên máy cũ
            Log.w("SigningSHA256", "signingInfo not available on this device", e)
        } catch (t: Throwable) {
            Log.w("SigningSHA256", "Failed to compute signing SHA-256", t)
        }
    }

    private fun sha256ColonSeparated(bytes: ByteArray): String {
        val md = MessageDigest.getInstance("SHA-256")
        val digest = md.digest(bytes)
        return digest.joinToString(":") { "%02X".format(it) }
    }

    fun getConnectionLiveData(): ConnectionLiveDataJava {
        if (::connectionLiveData.isInitialized) {
            return connectionLiveData
        } else {
            connectionLiveData = ConnectionLiveDataJava(this)
            return connectionLiveData
        }
    }

    private val requestPermissionLauncher: ActivityResultLauncher<String> =
        registerForActivityResult(ActivityResultContracts.RequestPermission()) { result ->
            if (result) {
                try {
                    AppConfig.logEventTracking(Constants.EventKey.GRANT_NOTI_PERMISSION)
                } catch (e: Exception) {
                    Logger.e("LogEventTracking error: " + e.message)
                }
                // Permission granted - enable notifications
                runOnUiThread {
                    dataBinding.navBar.switchNoti.isChecked = true
                }
                enableNotifications()
            } else {
                try {
                    AppConfig.logEventTracking(Constants.EventKey.DENY_NOTI_PERMISSION)
                } catch (e: Exception) {
                    Logger.e("LogEventTracking error: " + e.message)
                }
                // Permission denied - keep switch off
                runOnUiThread {
                    dataBinding.navBar.switchNoti.isChecked = false
                }
                localStorage.isNotification = false

                // Check if permanently denied
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU &&
                    !shouldShowRequestPermissionRationale(Manifest.permission.POST_NOTIFICATIONS)
                ) {
                    // Suggest going to settings
                } else {
                    // Permission denied
                }
            }
            // Sync switch state after permission result
            syncNotificationSwitchState()
            Log.i(
                "TAG",
                ": $result"
            )
        }

    private fun checkNotificationPermission() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            if (ContextCompat.checkSelfPermission(
                    this,
                    Manifest.permission.POST_NOTIFICATIONS
                ) != PackageManager.PERMISSION_GRANTED
            ) {
                requestPermissionLauncher.launch(Manifest.permission.POST_NOTIFICATIONS)
            }
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)


        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.P) {
            logSigningSha256Safe()
        }

        languageCode = localStorage.langCode // Lưu mã ngôn ngữ hiện tại khi tạo Activity
        autoDisposable.bindTo(lifecycle)

        connectionLiveData = ConnectionLiveDataJava(this)
        // Sử dụng instance đã khởi tạo thay vì tạo mới
        connectionLiveData.observe(this) { networkCollected ->
            if (networkCollected) {
                if (requestInternetBottomSheet != null && requestInternetBottomSheet.isVisible) {
                    requestInternetBottomSheet.dismiss()
                }
            } else {
                if (requestInternetBottomSheet != null && !requestInternetBottomSheet.isVisible) {
                    initRequestInternetBottomSheet()
                }
            }
        }
        if (localStorage.userId.isBlank()) {
            localStorage.userId = GenerateUserId.generate()
        }
        Logger.d(
            "HienDV_Language",
            "onCreate: ${localStorage.langCode} and locale: ${Locale.getDefault().language}"
        )
        Logger.d("HienDV_Language", "onCreate currentLanguage: ${getString(R.string.locale)}")
        viewModel.fetchCategories()

        initView()
        initisteners()
        initNavbarListeners()
        initObserver()
        dataBinding.model = viewModel
        checkNotificationPermission()
        handleNotificationClickTracking()
    }

    private fun initObserver() {
        dataBinding.drawerLout.addDrawerListener(object : DrawerLayout.DrawerListener {
            override fun onDrawerSlide(drawerView: View, slideOffset: Float) {
                dataBinding.fullscreenBlur.visibility = View.VISIBLE
                dataBinding.fullscreenBlur.alpha = slideOffset
                Log.i("TAG", "onDrawerSlide: $slideOffset")
            }

            override fun onDrawerOpened(drawerView: View) {
                dataBinding.fullscreenBlur.visibility = View.VISIBLE
                dataBinding.fullscreenBlur.alpha = 1f
            }

            override fun onDrawerClosed(drawerView: View) {
                dataBinding.fullscreenBlur.visibility = View.GONE
            }

            override fun onDrawerStateChanged(newState: Int) {
            }
        })
        viewModel.navOpen.observe(this) {
            if (it) {
                dataBinding.drawerLout.openDrawer(GravityCompat.START)
            } else {
                dataBinding.drawerLout.closeDrawer(GravityCompat.START)

            }
        }

        viewModel.coordinatedExpanded.observe(this) {
            updateLogoAppVisibility()
        }
        viewModel.selectedTab.observe(this) {
            navigateTab(it)
            updateLogoAppVisibility()
        }
    }

    private fun updateLogoAppVisibility() {
        val selectedTab = viewModel.selectedTab.value ?: 0
        val isExpanded = viewModel.coordinatedExpanded.value ?: false

        when (selectedTab) {
            0 -> dataBinding.logoImg.visibility = if (isExpanded) View.GONE else View.VISIBLE
            else -> dataBinding.logoImg.visibility = View.VISIBLE
        }
    }

    private fun navigateTab(position: Int) {
        if (position <= fragmentsList.size - 1) {
            dataBinding.vPager.currentItem = position
        } else if (position == 3) {
            startActivity(Intent(this, ZipLockMainActivity::class.java))
        }
        Log.i(ContentValues.TAG, "initObserver: $position")
        removeBackgroundTint()
        // Reset tất cả layout weight về 1
        resetAllTabWeights()

        when (position) {
            0 -> {
                // Hiển thị text cho Home tab
                dataBinding.txtHome.visibility = View.VISIBLE
                dataBinding.txtCategory.visibility = View.GONE
                dataBinding.txtFavourite.visibility = View.GONE
                dataBinding.txtZipper.visibility = View.GONE
                dataBinding.txtHistory.visibility = View.GONE
                // Mở rộng tab Home, thu nhỏ các tab khác
                expandTab(dataBinding.parentHome, dataBinding.loutHome)

            }

            1 -> {
                // Hiển thị text cho Category tab
                dataBinding.txtHome.visibility = View.GONE
                dataBinding.txtCategory.visibility = View.VISIBLE
                dataBinding.txtFavourite.visibility = View.GONE
                dataBinding.txtZipper.visibility = View.GONE
                dataBinding.txtHistory.visibility = View.GONE
                // Mở rộng tab Category, thu nhỏ các tab khác
                expandTab(dataBinding.parentCat, dataBinding.loutCat)

            }

            2 -> {
                // Hiển thị text cho Favourite tab
                dataBinding.txtHome.visibility = View.GONE
                dataBinding.txtCategory.visibility = View.GONE
                dataBinding.txtFavourite.visibility = View.VISIBLE
                dataBinding.txtZipper.visibility = View.GONE
                dataBinding.txtHistory.visibility = View.GONE
                // Mở rộng tab Favourite, thu nhỏ các tab khác
                expandTab(dataBinding.parentFav, dataBinding.loutFav)

            }

            3 -> {
                // Hiển thị text cho History tab
                dataBinding.txtHome.visibility = View.GONE
                dataBinding.txtCategory.visibility = View.GONE
                dataBinding.txtFavourite.visibility = View.GONE
                dataBinding.txtZipper.visibility = View.GONE
                dataBinding.txtHistory.visibility = View.VISIBLE

                // Mở rộng tab History, thu nhỏ các tab khác
                expandTab(dataBinding.parentHistory, dataBinding.loutHis)
            }

            4 -> {
                // Hiển thị text cho Zipper tab
                dataBinding.txtHome.visibility = View.GONE
                dataBinding.txtCategory.visibility = View.GONE
                dataBinding.txtFavourite.visibility = View.GONE
                dataBinding.txtZipper.visibility = View.VISIBLE
                dataBinding.txtHistory.visibility = View.GONE

                // Mở rộng tab Zipper, thu nhỏ các tab khác
                expandTab(dataBinding.parentZipper, dataBinding.loutZipper)
            }
        }


    }

    // Hàm reset tất cả tab về weight = 1
    private fun resetAllTabWeights() {
        val layoutParams1 = dataBinding.parentHome.layoutParams as LinearLayout.LayoutParams
        layoutParams1.weight = 1f
        dataBinding.parentHome.layoutParams = layoutParams1

        val layoutParams2 = dataBinding.parentCat.layoutParams as LinearLayout.LayoutParams
        layoutParams2.weight = 1f
        dataBinding.parentCat.layoutParams = layoutParams2

        val layoutParams3 = dataBinding.parentFav.layoutParams as LinearLayout.LayoutParams
        layoutParams3.weight = 1f
        dataBinding.parentFav.layoutParams = layoutParams3

        val layoutParams5 = dataBinding.parentHistory.layoutParams as LinearLayout.LayoutParams
        layoutParams5.weight = 1f
        dataBinding.parentHistory.layoutParams = layoutParams5

        val layoutParams4 = dataBinding.parentZipper.layoutParams as LinearLayout.LayoutParams
        layoutParams4.weight = 1f
        dataBinding.parentZipper.layoutParams = layoutParams4
    }

    // Hàm mở rộng tab được chọn với tỷ lệ cố định
    private fun expandTab(selectedParent: RelativeLayout, selectedLout: View) {
        // Tab được chọn: weight = 2 (chiếm 2/6)
        // Các tab khác: weight = 1 (mỗi tab chiếm 1/6)
        val layoutParams1 = dataBinding.parentHome.layoutParams as LinearLayout.LayoutParams
        layoutParams1.weight = if (selectedParent == dataBinding.parentHome) 2f else 1f
        dataBinding.parentHome.layoutParams = layoutParams1

        val layoutParams2 = dataBinding.parentCat.layoutParams as LinearLayout.LayoutParams
        layoutParams2.weight = if (selectedParent == dataBinding.parentCat) 2f else 1f
        dataBinding.parentCat.layoutParams = layoutParams2

        val layoutParams3 = dataBinding.parentFav.layoutParams as LinearLayout.LayoutParams
        layoutParams3.weight = if (selectedParent == dataBinding.parentFav) 2f else 1f
        dataBinding.parentFav.layoutParams = layoutParams3

        val layoutParams5 = dataBinding.parentHistory.layoutParams as LinearLayout.LayoutParams
        layoutParams5.weight = if (selectedParent == dataBinding.parentHistory) 2f else 1f
        dataBinding.parentHistory.layoutParams = layoutParams5

        val layoutParams4 = dataBinding.parentZipper.layoutParams as LinearLayout.LayoutParams
        layoutParams4.weight = if (selectedParent == dataBinding.parentZipper) 2f else 1f
        dataBinding.parentZipper.layoutParams = layoutParams4

        // Thiết lập background cho tab được chọn
        selectedLout.backgroundTintList =
            ContextCompat.getColorStateList(this, R.color.color_theme_blue)
    }

    private fun removeBackgroundTint() {
        // Ẩn tất cả text
        dataBinding.txtHome.visibility = View.GONE
        dataBinding.txtCategory.visibility = View.GONE
        dataBinding.txtFavourite.visibility = View.GONE
        dataBinding.txtZipper.visibility = View.GONE
        dataBinding.txtHistory.visibility = View.GONE

        // Reset tất cả layout weight về 1
        resetAllTabWeights()

        // Xóa background tint
        dataBinding.loutHome.backgroundTintList = null
        dataBinding.loutCat.backgroundTintList = null
        dataBinding.loutFav.backgroundTintList = null
        dataBinding.loutZipper.backgroundTintList = null
        dataBinding.loutHis.backgroundTintList = null
    }

    private fun initView() {
        dataBinding.drawerLout.setScrimColor(
            ContextCompat.getColor(
                this,
                android.R.color.transparent
            )
        )
        dataBinding.drawerLout.setDrawerElevation(0f)

        dataBinding.vPager.isUserInputEnabled = false

        setBlur(dataBinding.blurView, dataBinding.rootLout)

        fragmentsList = ArrayList()

        fragmentsList.add(HomeFragment())
        fragmentsList.add(CategoryFragment())
        fragmentsList.add(FavouriteFragment())
        fragmentsList.add(HistoryFragment())


        val adapter = HomePagerAdapter(
            supportFragmentManager,
            lifecycle
        )
        adapter.fragments = fragmentsList
        dataBinding.vPager.adapter = adapter
        dataBinding.vPager.offscreenPageLimit = 1
        viewModel.selectedTab.value = 0

        setBlur(dataBinding.fullscreenBlur, dataBinding.rootLout)
//        if(!CommonInfo.show_promotion_home || CommonInfo.promotion_app_home.isNullOrEmpty()){
//            dataBinding.imgAdsOtherApp.visibility = View.GONE
//        } else {
//            val remotePackageList = CommonInfo.promotion_app_home.trim()
//            val appInfo = PromotionApp.getAll().find { it.packageName in remotePackageList }
//            if(appInfo == null){
//                dataBinding.imgAdsOtherApp.visibility = View.GONE
//            } else {
//                val appPromotion = AppInfo(
//                    packageName = appInfo.packageName,
//                    name = appInfo.name,
//                    description = appInfo.description,
//                    iconUrl = appInfo.iconUrl
//                )
//                dataBinding.imgAdsOtherApp.visibility = View.VISIBLE
//                dataBinding.ivAdsOtherApp.setImageResource(appPromotion.iconUrl)
//            }
//        }
    }

    private fun initNavbarListeners() {
        // Initialize notification switch state
        initNotificationSwitch()

        dataBinding.navBar.rlPermission.setOnClickListener {
            if (CommonUtil.isDoubleClick()) return@setOnClickListener
            try {
                val isFirstTimeClickPermissionNavBar = localStorage.isFirstTimeClickPermissionNavBar
                if (isFirstTimeClickPermissionNavBar) {
                    AppConfig.logEventTracking(Constants.EventKey.MAIN_SL_PERMISSION_MENU_1ST)
                    localStorage.isFirstTimeClickPermissionNavBar = false
                } else {
                    AppConfig.logEventTracking(Constants.EventKey.MAIN_SL_PERMISSION_MENU_2ND)
                }
            } catch (e: Exception) {
                Logger.e("LogEventTracking error: ${e.message}")
            }
            try {
                AppConfig.logEventTracking(Constants.EventKey.GO_TO_GRANT_PERMISSION)
            } catch (e: Exception) {
                Logger.e("LogEventTracking error: ${e.message}")
            }
            val intent = Intent(this, SettingActivity::class.java)
                .putExtra("fragmentToOpen", GrantPermissionFragment::class.java.simpleName)
            startActivity(intent)
            overridePendingTransition(R.anim.slide_in_right, R.anim.slide_out_left)
            closeDrawer()
        }

        dataBinding.fullscreenBlur.setOnClickListener {
            closeDrawer()
        }

        dataBinding.navBar.btnDownload.setOnClickListener {
            if (CommonUtil.isDoubleClick()) return@setOnClickListener
            try {
                val isFirstTimeClickDownloadNavBar = localStorage.isFirstTimeClickDownloadNavBar
                if (isFirstTimeClickDownloadNavBar) {
                    AppConfig.logEventTracking(Constants.EventKey.MAIN_SL_DOWNLOAD_MENU_1ST)
                    localStorage.isFirstTimeClickDownloadNavBar = false
                } else {
                    AppConfig.logEventTracking(Constants.EventKey.MAIN_SL_DOWNLOAD_MENU_2ND)
                }
            } catch (e: Exception) {
                Logger.e("LogEventTracking error: ${e.message}")
            }
            try {
                AppConfig.logEventTracking(Constants.EventKey.GO_TO_DOWNLOAD)
            } catch (e: Exception) {
                Logger.e("LogEventTracking error: ${e.message}")
            }
            val intent = Intent(this, SettingActivity::class.java)
                .putExtra("fragmentToOpen", "DownloadedFragment")
            startActivity(intent)
            overridePendingTransition(R.anim.slide_in_right, R.anim.slide_out_left)
            closeDrawer()
        }

//        dataBinding.navBar.btnHistory.setOnClickListener {
//            if (CommonUtil.isDoubleClick()) return@setOnClickListener
//            localStorage.countClickHomeNavigate += 1
//            if(localStorage.countClickHomeNavigate >= CommonInfo.count_click_home_navigate){
//                showInterNavigationBar(Constants.NAVIGATE_HISTORY)
//            } else if(localStorage.countClickHomeNavigate == CommonInfo.count_click_home_navigate - 1) {
//                loadInterNavigationBar()
//                handleWhenLoadInterDone(Constants.NAVIGATE_HISTORY)
//            } else {
//                handleWhenLoadInterDone(Constants.NAVIGATE_HISTORY)
//            }
//        }


        dataBinding.navBar.btnSetting.setOnClickListener {
            if (CommonUtil.isDoubleClick()) return@setOnClickListener
            try {
                val isFirstTimeClickSettingNavBar = localStorage.isFirstTimeClickSettingNavBar
                if (isFirstTimeClickSettingNavBar) {
                    AppConfig.logEventTracking(Constants.EventKey.MAIN_SL_SETTING_MENU_1ST)
                    localStorage.isFirstTimeClickSettingNavBar = false
                } else {
                    AppConfig.logEventTracking(Constants.EventKey.MAIN_SL_SETTING_MENU_2ND)
                }
            } catch (e: Exception) {
                Logger.e("LogEventTracking error: ${e.message}")
            }
            try {
                AppConfig.logEventTracking(Constants.EventKey.GO_TO_SETTING)
            } catch (e: Exception) {
                Logger.e("LogEventTracking error: ${e.message}")
            }
            val intent = Intent(this, SettingActivity::class.java)
            startActivity(intent)
            overridePendingTransition(R.anim.slide_in_right, R.anim.slide_out_left)
            closeDrawer()
        }

        dataBinding.navBar.btnPremium.setOnClickListener {
            // TODO: Delete this button
            closeDrawer()
        }

        // Notification menu item click listener (AC-001)
        dataBinding.navBar.btnNoti.setOnClickListener {
            if (CommonUtil.isDoubleClick()) return@setOnClickListener
            try {
                AppConfig.logEventTracking("MAIN_SL_NOTIFICATION_MENU")
            } catch (e: Exception) {
                Logger.e("LogEventTracking error: ${e.message}")
            }
            closeDrawer()
            handleNotificationPermissionClick()
        }
    }

    private fun closeDrawer() {
        dataBinding.fullscreenBlur.visibility = View.GONE
        dataBinding.drawerLout.closeDrawer(GravityCompat.START)
        viewModel.updateNavOpen(false)

    }

    private fun initisteners() {
        dataBinding.parentZipper.setOnClickListener {
            try {
                val isFirstTimeClickZipper = localStorage.isFirstClickZipperMain
                if (isFirstTimeClickZipper) {
                    AppConfig.logEventTracking(Constants.EventKey.MAIN_SL_ZIPPER_1ST)
                    localStorage.isFirstClickZipperMain = false
                } else {
                    AppConfig.logEventTracking(Constants.EventKey.MAIN_SL_ZIPPER_2ND)
                }
            } catch (e: Exception) {
                Logger.e("LogEventTracking error: ${e.message}")
            }
            handleWhenLoadInterDone(Constants.NAVIGATE_ZIPPER_LOCKER)
        }
        dataBinding.loutFav.setOnClickListener {
            try {
                val isFirstTimeClickFavourite = localStorage.isFirstClickFavouriteMain
                if (isFirstTimeClickFavourite) {
                    AppConfig.logEventTracking(Constants.EventKey.MAIN_SL_FAVOURITE_1ST)
                    localStorage.isFirstClickFavouriteMain = false
                } else {
                    AppConfig.logEventTracking(Constants.EventKey.MAIN_SL_FAVOURITE_2ND)
                }
            } catch (e: Exception) {
                Logger.e("LogEventTracking error: ${e.message}")
            }
            handleWhenLoadInterDone(Constants.NAVIGATE_FAVOURITE)

        }

        dataBinding.parentHistory.setOnClickListener {
            try {
                val isFirstTimeClickHistory = localStorage.isFirstClickHistoryMain
                if (isFirstTimeClickHistory) {
                    AppConfig.logEventTracking(Constants.EventKey.MAIN_SL_HISTORY_1ST)
                    localStorage.isFirstClickHistoryMain = false
                } else {
                    AppConfig.logEventTracking(Constants.EventKey.MAIN_SL_HISTORY_2ND)
                }
            } catch (e: Exception) {
                Logger.e("LogEventTracking error: ${e.message}")
            }
            handleWhenLoadInterDone(Constants.NAVIGATE_HISTORY)
        }

        dataBinding.parentCat.setOnClickListener {
            try {
                val isFirstTimeClickCategory = localStorage.isFirstClickCategoryMain
                if (isFirstTimeClickCategory) {
                    AppConfig.logEventTracking(Constants.EventKey.MAIN_SL_CATEGORY_1ST)
                    localStorage.isFirstClickCategoryMain = false
                } else {
                    AppConfig.logEventTracking(Constants.EventKey.MAIN_SL_CATEGORY_2ND)
                }
            } catch (e: Exception) {
                Logger.e("LogEventTracking error: ${e.message}")
            }
            handleWhenLoadInterDone(1)
        }

        dataBinding.parentHome.setOnClickListener {
            try {
                val isFirstTimeClickHome = localStorage.isFirstClickHomeMain
                if (isFirstTimeClickHome) {
                    AppConfig.logEventTracking(Constants.EventKey.MAIN_SL_HOME_1ST)
                    localStorage.isFirstClickHomeMain = false
                } else {
                    AppConfig.logEventTracking(Constants.EventKey.MAIN_SL_HOME_2ND)
                }
            } catch (e: Exception) {
                Logger.e("LogEventTracking error: ${e.message}")
            }
            handleWhenLoadInterDone(0)
        }

        dataBinding.loutLoader.setOnClickListener {

        }
        dataBinding.btnSearch.setOnClickListener {
            try {
                val isFirstTimeClickSearch = localStorage.isFirstClickSearchMain
                if (isFirstTimeClickSearch) {
                    AppConfig.logEventTracking(Constants.EventKey.MAIN_SL_SEARCH_1ST)
                    localStorage.isFirstClickSearchMain = false
                } else {
                    AppConfig.logEventTracking(Constants.EventKey.MAIN_SL_SEARCH_2ND)
                }
            } catch (e: Exception) {
                Logger.e("LogEventTracking error: ${e.message}")
            }
            try {
                AppConfig.logEventTracking(Constants.EventKey.GO_TO_SEARCH)
            } catch (e: Exception) {
                Logger.e("LogEventTracking error: ${e.message}")
            }
            startActivity(Intent(this, SearchActivity::class.java))
        }
    }

    @SuppressLint("MissingSuperCall")
    override fun onBackPressed() {
        if (viewModel.navOpen.value!!) {
            viewModel.navOpen.value = false
        } else {
            showExitAppDialog()
        }
    }

    private fun showExitAppDialog() {
        exitAppBottomSheet = BottomSheetExitApp()
        exitAppBottomSheet.updateLanguage(this, localStorage.langCode)
        exitAppBottomSheet.setStyle(DialogFragment.STYLE_NORMAL, R.style.AppBottomSheetDialogTheme)

        exitAppBottomSheet.clickConfirmYes = {
            sessionManager.refreshSession()
            this.finish()
        }
        if (!this.isFinishing) {
            this.supportFragmentManager.let {
                exitAppBottomSheet.show(it, BottomSheetExitApp.TAG)
            }
        }
    }

    override fun onStop() {
        super.onStop()
        localStorage.lastTimeExitApp = System.currentTimeMillis()
    }

    override fun getContentViewId() = R.layout.activity_my_home

    override fun initializeViews() {
        Logger.d("initialize MainActivity")
    }

    override fun registerListeners() {
    }

    override fun initializeData() {
    }

    override fun onDestroy() {
        super.onDestroy()
        if (requestInternetBottomSheet != null && requestInternetBottomSheet.isVisible) {
            requestInternetBottomSheet.dismiss()
        }
    }

    private fun recreateWithoutBlink() {
        val intent = Intent(this, this::class.java).apply {
            addFlags(Intent.FLAG_ACTIVITY_NO_ANIMATION)
        }
        val option = ActivityOptions.makeCustomAnimation(this, 0, 0)
        finish()
        startActivity(intent, option.toBundle())
    }

    private fun initRequestInternetBottomSheet() {
        requestInternetBottomSheet = RequestInternetBottomSheet()
        requestInternetBottomSheet.updateLanguage(this, localStorage.langCode)
        requestInternetBottomSheet.isCancelable = false
        requestInternetBottomSheet.clickConfirmYes = {
            if (NetworkUtils.isNetworkConnected()) {
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

    // Show permission dot

    private fun hasManageFilePermission(): Boolean {
        return if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R) {
//            Environment.isExternalStorageManager()
            true
        } else {
            CommonUtil.hasPermissions(
                permissions = PERMISSIONS,
                activity = this
            )
        }
    }

    private fun hasSaveAsPermissions(): Boolean {
        return if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            PERMISSIONS.all { permission ->
                ContextCompat.checkSelfPermission(
                    this,
                    permission
                ) == PackageManager.PERMISSION_GRANTED
            }
        } else if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R) {
            PERMISSIONS.all { permission ->
                ContextCompat.checkSelfPermission(
                    this,
                    permission
                ) == PackageManager.PERMISSION_GRANTED
            }
        } else {
            CommonUtil.hasPermissions(
                permissions = PERMISSIONS,
                activity = this
            )
        }
    }

    private fun hasNotificationPermissions(): Boolean {
        return if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            ContextCompat.checkSelfPermission(
                this,
                Manifest.permission.POST_NOTIFICATIONS
            ) == PackageManager.PERMISSION_GRANTED
        } else {
            // For Android < 13, permission is not required
            true
        }
    }

    /**
     * Check if system notifications are actually enabled
     * This checks the NotificationManager status, not just permission
     */
    private fun areSystemNotificationsEnabled(): Boolean {
        val notificationManager =
            getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager

        // First check if notifications are enabled globally
        if (!notificationManager.areNotificationsEnabled()) {
            return false
        }

        // For Android O+, also check channel importance
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val channel = notificationManager.getNotificationChannel(Constants.FCM_CHANNEL_ID)
            if (channel != null) {
                return channel.importance != NotificationManager.IMPORTANCE_NONE
            }
        }

        return true
    }

    /**
     * Check if notifications can be shown (both permission and system status)
     */
    private fun canShowNotifications(): Boolean {
        return hasNotificationPermissions() && areSystemNotificationsEnabled()
    }


    private fun isIgnoringBatteryOptimizations(context: Context): Boolean {
        val powerManager = context.getSystemService(Context.POWER_SERVICE) as PowerManager
        return powerManager.isIgnoringBatteryOptimizations(context.packageName)
    }

    private fun hasDownloadPermission(): Boolean {
        return if (Build.VERSION.SDK_INT <= Build.VERSION_CODES.Q) {
            hasSaveAsPermissions()
        } else {
            hasManageFilePermission()
        }
    }

    private fun hasOverlayPermission(context: Context): Boolean {
        return Settings.canDrawOverlays(context)
    }

    private fun isAllPermissionGrant(context: Context): Boolean {
        return hasOverlayPermission(context) && hasDownloadPermission() && canShowNotifications() && isIgnoringBatteryOptimizations(
            context
        )
    }

    private fun showHideRedDot(context: Context) {
        dataBinding.notificationDot.visibility =
            if (isAllPermissionGrant(context)) View.GONE else View.VISIBLE
        dataBinding.navBar.notificationDot.visibility =
            if (isAllPermissionGrant(context)) View.GONE else View.VISIBLE
    }

    private fun handleWhenLoadInterDone(position: Int) {
        runOnUiThread {
            dataBinding.loutLoader.visibility = View.GONE
        }
        when (position) {
            Constants.NAVIGATE_ZIPPER_LOCKER -> {
                startActivity(Intent(this, ZipLockMainActivity::class.java))
                overridePendingTransition(R.anim.slide_in_right, R.anim.slide_out_left)
            }

            else -> {
                viewModel.selectedTab.value = position
            }
        }
        logEventGoTo(position)
    }

    private fun logEventGoTo(position: Int) {
        when (position) {
            0 -> {
                try {
                    AppConfig.logEventTracking(Constants.EventKey.GO_TO_HOME)
                } catch (e: Exception) {
                    Logger.e("LogEventTracking error: ${e.message}")
                }
            }

            1 -> {
                try {
                    AppConfig.logEventTracking(Constants.EventKey.GO_TO_CATEGORY)
                } catch (e: Exception) {
                    Logger.e("LogEventTracking error: ${e.message}")
                }
            }

            Constants.NAVIGATE_ZIPPER_LOCKER -> {
                try {
                    AppConfig.logEventTracking(Constants.EventKey.GO_TO_ZIPPER_LOCKER)
                } catch (e: Exception) {
                    Logger.e("LogEventTracking error: ${e.message}")
                }
            }

            Constants.NAVIGATE_FAVOURITE -> {
                try {
                    AppConfig.logEventTracking(Constants.EventKey.GO_TO_FAVOURITE)
                } catch (e: Exception) {
                    Logger.e("LogEventTracking error: ${e.message}")
                }
            }

            Constants.NAVIGATE_HISTORY -> {
                try {
                    AppConfig.logEventTracking(Constants.EventKey.GO_TO_HISTORY)
                } catch (e: Exception) {
                    Logger.e("LogEventTracking error: ${e.message}")
                }
            }
        }
    }

    fun updateSelectedTab(position: Int) {
        viewModel.selectedTab.value = position
    }

    private fun initNotificationSwitch() {
        // Sync switch with current state
        syncNotificationSwitchState()

        // Make switch act as indicator only - always open settings when clicked
        dataBinding.navBar.switchNoti.setOnClickListener {
            Logger.d("Notification switch clicked - opening settings")
            openAppNotificationSettings()
        }

        // Prevent switch from being toggled directly
        dataBinding.navBar.switchNoti.setOnCheckedChangeListener { _, isChecked ->
            // Always sync with system state, ignore manual changes
            val systemState = canShowNotifications()
            if (isChecked != systemState) {
                dataBinding.navBar.switchNoti.isChecked = systemState
            }
        }
    }

    private fun syncNotificationSwitchState() {
        val systemEnabled = canShowNotifications()

        runOnUiThread {
            // Switch is clickable (to open settings) but shows system state
            dataBinding.navBar.switchNoti.isEnabled = true
            // Switch reflects ONLY system notification status
            dataBinding.navBar.switchNoti.isChecked = systemEnabled

            // Update Firebase subscription based on system status
            if (systemEnabled && !(localStorage.isNotification)) {
                // System enabled, sync Firebase
                localStorage.isNotification = true
            } else if (!systemEnabled && (localStorage.isNotification)) {
                // System disabled, sync Firebase
                localStorage.isNotification = false
            }
        }
    }


    private fun guideUserToEnablePermission() {
        when {
            // Android 13+ permission flow
            Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU -> {
                handleAndroid13PermissionFlow()
            }
            // Android < 13 - notifications enabled by default
            else -> {
                enableNotifications()
                dataBinding.navBar.switchNoti.isChecked = true
            }
        }
    }

    private fun handleAndroid13PermissionFlow() {
        when {
            // Can show rationale - user denied once
            shouldShowRequestPermissionRationale(Manifest.permission.POST_NOTIFICATIONS) -> {
                showPermissionRationaleDialog()
            }

            // Check if we've requested before
            localStorage.hasRequestedNotificationPermission -> {
                // User selected "Don't ask again" - guide to settings
                showGoToSettingsDialog()
            }

            // First time request
            else -> {
                localStorage.hasRequestedNotificationPermission = true
                requestPermissionLauncher.launch(Manifest.permission.POST_NOTIFICATIONS)
            }
        }
    }

    private fun showPermissionRationaleDialog() {
        androidx.appcompat.app.AlertDialog.Builder(this)
            .setTitle(getString(R.string.notification_permission_title))
            .setMessage(getString(R.string.notification_permission_message))
            .setPositiveButton(getString(R.string.allow)) { _, _ ->
                requestPermissionLauncher.launch(Manifest.permission.POST_NOTIFICATIONS)
            }
            .setNegativeButton(getString(R.string.not_now)) { dialog, _ ->
                dialog.dismiss()
            }
            .setCancelable(false)
            .show()
    }

    private fun showGoToSettingsDialog() {
        androidx.appcompat.app.AlertDialog.Builder(this)
            .setTitle(getString(R.string.notification_permission_title))
            .setMessage(getString(R.string.notification_permission_settings_message))
            .setPositiveButton(getString(R.string.open_settings)) { _, _ ->
                openAppNotificationSettings()
                AppConfig.logEventTracking("NOTIFICATION_OPEN_SETTINGS_FROM_DIALOG")
            }
            .setNegativeButton(getString(R.string.cancel)) { dialog, _ ->
                dialog.dismiss()
            }
            .setCancelable(true)
            .show()
    }

    private fun enableNotifications() {
        localStorage.isNotification = true
        AppConfig.logEventTracking(Constants.PreferencesKey.NOTIFICATION_ENABLED)
    }

    private fun checkPermissionChangesFromSettings() {
        // Always sync with system state when returning from settings
        val prevState = dataBinding.navBar.switchNoti.isChecked
        syncNotificationSwitchState()
        val newState = canShowNotifications()

        Logger.d("Returned from settings - prev: $prevState, new: $newState")

        // Track state changes
        if (newState && !prevState) {
            AppConfig.logEventTracking(Constants.PreferencesKey.NOTIFICATION_ENABLED)
        } else if (!newState && prevState) {
            AppConfig.logEventTracking(Constants.PreferencesKey.NOTIFICATION_DISABLED)
        }
     }


    private fun handleNotificationClickTracking() {
        // Handle notification click tracking (AC-019, AC-033)
        intent?.let { intent ->
            if (intent.getBooleanExtra("notification_clicked", false)) {
                val timestamp = intent.getLongExtra("notification_timestamp", 0)
                Logger.d(TAG, "App opened from notification at timestamp: $timestamp")

                try {
                    AppConfig.logEventTracking("NOTIFICATION_CLICKED")
                } catch (e: Exception) {
                    Logger.e("Error tracking notification click: ${e.message}")
                }
            }
        }
    }

    private fun handleNotificationPermissionClick() {
        // Always open settings for notification control
        openAppNotificationSettings()
    }

    private fun openAppNotificationSettings() {
        try {
            val intent = when {
                Build.VERSION.SDK_INT >= Build.VERSION_CODES.O -> {
                    Intent(Settings.ACTION_APP_NOTIFICATION_SETTINGS).apply {
                        putExtra(Settings.EXTRA_APP_PACKAGE, packageName)
                    }
                }

                else -> {
                    Intent(Settings.ACTION_APPLICATION_DETAILS_SETTINGS).apply {
                        data = android.net.Uri.fromParts("package", packageName, null)
                    }
                }
            }
            startActivity(intent)
            AppConfig.logEventTracking("OPEN_NOTIFICATION_SETTINGS")
        } catch (e: Exception) {
            Logger.e("Cannot open notification settings: ${e.message}")
            // Fallback to general app settings
            try {
                val intent = Intent(Settings.ACTION_APPLICATION_DETAILS_SETTINGS).apply {
                    data = android.net.Uri.fromParts("package", packageName, null)
                }
                startActivity(intent)
            } catch (ex: Exception) {
                Logger.e("Cannot open app settings: ${ex.message}")
            }
        }
    }

    companion object {
        const val TAG = "MyHomeActivity"
    }
}