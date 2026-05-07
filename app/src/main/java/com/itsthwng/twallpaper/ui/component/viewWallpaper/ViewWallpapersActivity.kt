package com.itsthwng.twallpaper.ui.component.viewWallpaper

import android.content.Context
import android.content.Intent
import android.graphics.drawable.Drawable
import android.os.Bundle
import android.provider.Settings
import android.util.Log
import android.view.View
import androidx.activity.result.contract.ActivityResultContracts
import androidx.core.app.ActivityOptionsCompat
import androidx.lifecycle.Observer
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.bumptech.glide.request.FutureTarget
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken
import com.itsthwng.twallpaper.R
import com.itsthwng.twallpaper.data.model.SettingData
import com.itsthwng.twallpaper.databinding.ActivityViewWallpapersBinding
import com.itsthwng.twallpaper.extension.setSafeOnClickListener
import com.itsthwng.twallpaper.extension.toArrayList
import com.itsthwng.twallpaper.local.LocalData
import com.itsthwng.twallpaper.local.LocalStorage
import com.itsthwng.twallpaper.ui.base.BaseActivityBinding
import com.itsthwng.twallpaper.ui.component.preview.PreviewActivity
import com.itsthwng.twallpaper.ui.component.viewWallpaper.adapter.ViewWallpapersAdapter
import com.itsthwng.twallpaper.ui.component.viewWallpaper.viewModel.ViewWallpapersViewModel
import com.itsthwng.twallpaper.utils.AppConfig
import com.itsthwng.twallpaper.utils.BitmapUtils
import com.itsthwng.twallpaper.utils.ConnectionLiveDataJava
import com.itsthwng.twallpaper.utils.Constants
import com.itsthwng.twallpaper.utils.Global
import com.itsthwng.twallpaper.utils.Logger
import com.itsthwng.twallpaper.utils.NetworkUtils
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.launch
import javax.inject.Inject

@AndroidEntryPoint
class ViewWallpapersActivity :
    BaseActivityBinding<ActivityViewWallpapersBinding, ViewWallpapersViewModel>() {

    @Inject
    lateinit var localStorage: LocalStorage


    lateinit var selectedWall: SettingData.WallpapersItem
    lateinit var viewWallpapersAdapter: ViewWallpapersAdapter
    private lateinit var connectionLiveData: ConnectionLiveDataJava

    var dataList: List<SettingData.WallpapersItem> = arrayListOf()
    var pos = 0

    // Thêm biến để track Glide request hiện tại
    private var currentGlideRequest: FutureTarget<Drawable>? = null

    // Thêm biến để track BlurView state
    private var isBlurViewActive = false
    private val openPreview = registerForActivityResult(
        ActivityResultContracts.StartActivityForResult()
    ) { result ->
        if (result.resultCode == RESULT_OK && result.data != null) {
            val id = result.data!!.getIntExtra("id", -1)
            val newAccess = result.data!!.getIntExtra("accessType", -1)
            val newDownloaded = result.data!!.getLongExtra("isDownloaded", 0L)

            if (id != -1 && newAccess != -1) {
                // Tìm item theo id and update
                val index = dataList.indexOfFirst { it.id == id }
                if (index != -1) {
                    // Update accessType if changed
                    if (dataList[index].accessType != newAccess) {
                        dataList[index].accessType = newAccess
                        viewWallpapersAdapter.updateItemAccessTypeOnly(id, newAccess)
                    }

                    // Update download status if changed
                    if (newDownloaded > 0 && dataList[index].isDownloaded != newDownloaded) {
                        dataList[index].isDownloaded = newDownloaded
                        viewWallpapersAdapter.updateDownloadStatus(index, newDownloaded)
                        viewModel.updatedWallpaperDownloadStatus(id, newDownloaded)
                    }

                    if (index == dataBinding.viewPager.getViewPager().currentItem) {
                        selectedWall = dataList[index]
                    }
                    // Nếu bạn dùng ViewPager2 (adapter thường):
//                    viewWallpapersAdapter.updateData(dataList)
                }
            }
        }
    }

    override fun attachBaseContext(newBase: Context?) {
        if (newBase != null) {
            val localStorage = LocalData(newBase, "sharedPreferences")
            super.attachBaseContext(
                AppConfig.updateResources(newBase, localStorage.langCode)
            )
        } else {
            super.attachBaseContext(null)
        }
    }

    override fun getContentViewId(): Int {
        return R.layout.activity_view_wallpapers
    }

    override fun initializeViews() {}

    override fun registerListeners() {}

    override fun initializeData() {}

    private fun handleDeepLink() {
        // Handle deep link from notification
        val deepLink = intent.getStringExtra("deepLink")
        val wallpaperData = intent.getStringExtra("wallpaperData")

        Log.d(TAG, "handleDeepLink called - deepLink: $deepLink, wallpaperData: $wallpaperData")

        if (deepLink == "myapp://view_wallpaper" && !wallpaperData.isNullOrEmpty()) {
            try {
                val wallpaper =
                    Gson().fromJson(wallpaperData, SettingData.WallpapersItem::class.java)

                // Create wallpaper list with single item
                dataList = listOf(wallpaper)
                pos = 0

                // Update adapter with the new data
                viewWallpapersAdapter.updateData(dataList)

                // Set current item to position 0
                dataBinding.viewPager.getViewPager().setCurrentItem(0, false)

                Log.d(TAG, "Deep link handled successfully: ${wallpaper.name}")
            } catch (e: Exception) {
                Log.e(TAG, "Error parsing wallpaper data: ${e.message}")
            }
        } else {
            Log.d(TAG, "No deep link or wallpaper data found")
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        connectionLiveData = ConnectionLiveDataJava(this)
        viewWallpapersAdapter = ViewWallpapersAdapter(
            localStorage,
            itemList = mutableListOf(), isNetworkConnected = NetworkUtils.isNetworkConnected()
        )
        dataBinding.viewPager.getViewPager().adapter = viewWallpapersAdapter

        // Handle deep link from notification AFTER adapter is initialized
        handleDeepLink()

        initConnectivityAndCollectors()
        initView()
        initListeners()
        initObservers()
        logTracking()
        dataBinding.model = viewModel
    }

    override fun onNewIntent(intent: Intent?) {
        super.onNewIntent(intent)
        Log.d(TAG, "onNewIntent called")

        // Update the intent to the new one
        setIntent(intent)

        // Handle deep link from notification
        handleDeepLink()
    }

    private fun initConnectivityAndCollectors() {
        // 1) Trạng thái ngay lúc vào
        val initialConnected = NetworkUtils.isNetworkConnected()

        if (initialConnected) {
            showOffline(false)
        } else {
            showOffline(true)

            // 2) Chờ tới khi có network rồi start collectors (one-shot)
            val onceObserver = object : Observer<Boolean> {
                override fun onChanged(value: Boolean) {
                    if (value) {
                        connectionLiveData.removeObserver(this)
                        showOffline(false)
                    }
                }
            }
            connectionLiveData.observe(this, onceObserver)
        }

        // 3) Nút Refresh: thử lại ngay
        dataBinding.btnConfirmYesImage.setOnClickListener {
            val now = NetworkUtils.isNetworkConnected()
            if (now) {
                showOffline(false)
            } else {
                showOffline(true)
            }
        }

        // 4) Nút Go to Settings: mở cài đặt connectivity
        dataBinding.btnSetting.setOnClickListener {
            try {
                AppConfig.logEventTracking(Constants.HOME_REQUIRE_INTERNET_YES)
                startActivity(Intent(Settings.ACTION_WIFI_SETTINGS))
            } catch (e: Exception) {
                Logger.e(e.message)
            }
        }
    }

    private fun showOffline(show: Boolean) {
        dataBinding.noInternetImageLayout.visibility = if (show) View.VISIBLE else View.GONE
    }

    private fun initObservers() {
        lifecycleScope.launch {
            viewModel.favoriteWalls.collect { likedList ->
                localStorage.favourites =
                    Global.listOfIntegerToString(likedList.filter { it.id != null }.map { it.id!! })
                        ?: ""
                viewWallpapersAdapter.updateFavList(
                    Global.convertStringToLis(localStorage.favourites).toArrayList()
                )
            }
        }
    }

    private var hasCalledDownload = false
    private fun startDownload(onDone: () -> Unit = {}) {
        hasCalledDownload = true
        // Show loader immediately when download button is clicked
        dataBinding.loutLoaderDownload.visibility = View.VISIBLE

        // Use the new permission-aware download method
        downloadWallWithPermissionCheck(
            dataList[dataBinding.viewPager.getViewPager().currentItem],
            object : OnDownload {
                override fun onComplete() {
                    dataBinding.loutLoaderDownload.visibility = View.GONE
                    val pos = dataBinding.viewPager.getViewPager().currentItem
                    viewModel.updatedWallpaperDownloadStatus(
                        dataList[pos].id!!,
                        System.currentTimeMillis()
                    )
                    runOnUiThread {
                        showToast(R.string.wallpaper_downloaded_successfully)
                    }
                    // Update data cho adapter
                    viewWallpapersAdapter.updateDownloadStatus(pos, System.currentTimeMillis())
                    hasCalledDownload = false
                    dataBinding.viewPager.resumeAutoSlide()
                    onDone()
                }

                override fun onError() {
                    // Hide loader if permission is denied or any error occurs
                    dataBinding.loutLoaderDownload.visibility = View.GONE
                    runOnUiThread {
                        showToast(R.string.error_saving_image)
                    }
                    hasCalledDownload = false
                    dataBinding.viewPager.resumeAutoSlide()
                    onDone()
                }
            })
    }

    private fun initListeners() {
        dataBinding.btnBack.setOnClickListener {
            onBackPressedDispatcher.onBackPressed()
        }
        dataBinding.loutLoaderDownload.setOnClickListener {

        }

        dataBinding.btnDownload.setSafeOnClickListener(800) {
            dataBinding.viewPager.pauseAutoSlide()
            selectedWall = dataList[dataBinding.viewPager.getViewPager().currentItem]
            try {
                val isFirstTimeDownloadWallpaper = localStorage.isFirstTimeDownloadWallpaper
                if (isFirstTimeDownloadWallpaper) {
                    AppConfig.logEventTracking(Constants.EventKey.DOWNLOAD_WALLPAPER_1ST)
                    localStorage.isFirstTimeDownloadWallpaper = false
                } else {
                    AppConfig.logEventTracking(Constants.EventKey.DOWNLOAD_WALLPAPER_2ND)
                }
            } catch (e: Exception) {
                Logger.e("LogEventTracking error: ${e.message}")
            }
            try {
                AppConfig.logEventTracking(Constants.EventKey.DOWNLOAD_WALLPAPER + "${selectedWall.id}")
            } catch (e: Exception) {
                Logger.e("LogEventTracking error: ${e.message}")
            }
            startDownload()
        }

        dataBinding.btnPreview.setSafeOnClickListener(800) {
            // Reset BlurView state before opening PreviewActivity
            isBlurViewActive = false

            val currentItem = dataList[dataBinding.viewPager.getViewPager().currentItem]
            val intent = Intent(this, PreviewActivity::class.java)

            intent.putExtra(
                Constants.wallpaper,
                Gson().toJson(currentItem)
            )
            val bundle = ActivityOptionsCompat.makeCustomAnimation(
                this,
                R.anim.slide_in_right, R.anim.slide_out_left
            )
            openPreview.launch(intent, bundle)
        }

        viewWallpapersAdapter.registerAdapterDataObserver(
            object : RecyclerView.AdapterDataObserver() {
                override fun onChanged() {
                    dataBinding.viewPager.recheckNow()
                }

                override fun onItemRangeInserted(positionStart: Int, itemCount: Int) {
                    dataBinding.viewPager.recheckNow()
                }

                override fun onItemRangeRemoved(positionStart: Int, itemCount: Int) {
                    dataBinding.viewPager.recheckNow()
                }
            })

        dataBinding.viewPager.getViewPager().registerOnPageChangeCallback(object :
            com.github.islamkhsh.viewpager2.ViewPager2.OnPageChangeCallback() {
            override fun onPageSelected(position: Int) {
                // Cancel request Glide cũ nếu có để prevent lỗi recycled bitmap
                currentGlideRequest?.let { futureTarget ->
                    if (!futureTarget.isDone) {
                        Glide.with(this@ViewWallpapersActivity).clear(futureTarget)
                        Log.d(TAG, "Cancelled previous Glide request for position $position")
                    }
                }
                updateButtonsVisibility(position)
                super.onPageSelected(position)
            }
        })
    }

    private fun initView() {
        dataBinding.viewPager.getViewPager().adapter = viewWallpapersAdapter
        viewWallpapersAdapter.onFavClick = {
            viewModel.updateWallpaper(it)
        }

        // Khởi tạo BlurView safely
        initializeBlurView()
        dataBinding.viewPager.pauseAutoSlide()
        pos = intent.getIntExtra(Constants.position, 0)
        val s = intent.getStringExtra(Constants.dataList)
        if (s != null) {
            dataList = Gson().fromJson(
                s, object : TypeToken<List<SettingData.WallpapersItem>?>() {}.type
            )
            viewWallpapersAdapter.updateData(dataList)
            viewWallpapersAdapter.updateFavList(
                Global.convertStringToLis(localStorage.favourites).toArrayList()
            )
        }
        dataBinding.viewPager.getViewPager().setCurrentItem(pos, false)
        updateButtonsVisibility(pos)
    }

    private fun initializeBlurView() {
        try {
            if (!isBlurViewValid()) {
                Log.w(TAG, "BlurView is not valid, skipping initialization")
                return
            }

            // Chỉ setup BlurView khi activity active
            if (!isFinishing && !isDestroyed) {
                // Sử dụng post để đảm bảo View is layout completely
                dataBinding.blurView.post {
                    if (!isFinishing && !isDestroyed && isBlurViewValid()) {
                        try {
                            setBlur(dataBinding.blurView, dataBinding.rootLout)
                            isBlurViewActive = true
                            Log.d(TAG, "BlurView initialized successfully after layout")
                        } catch (e: Exception) {
                            Log.e(TAG, "Failed to setup BlurView after layout: ${e.message}")
                            disableBlurView()
                        }
                    }
                }
            }
        } catch (e: Exception) {
            Log.e(TAG, "Failed to initialize BlurView: ${e.message}")
            // Ẩn BlurView nếu có lỗi
            dataBinding.blurView.visibility = View.GONE
            isBlurViewActive = false
        }
    }

    private fun isBlurViewValid(): Boolean {
        return try {
            dataBinding.blurView.context != null && !this.isFinishing
        } catch (e: Exception) {
            Log.e(TAG, "Error checking BlurView validity: ${e.message}")
            false
        }
    }

    private fun logTracking() {
        if (localStorage.isFirstOpenViewWallpaper) {
            localStorage.isFirstOpenViewWallpaper = false
            AppConfig.logEventTracking(Constants.EventKey.WALLPAPER_OPEN_1ST)
        } else {
            AppConfig.logEventTracking(Constants.EventKey.WALLPAPER_OPEN_2ND)
        }
    }

    // Method để update visibility của buttons dựa trên position
    private fun updateButtonsVisibility(position: Int) {
        dataBinding.btnDownload.visibility = View.VISIBLE
        dataBinding.btnPreview.visibility = View.VISIBLE
        Log.d(TAG, "Showing buttons for wallpaper position $position")
    }

    private fun disableBlurView() {
        try {
            isBlurViewActive = false
            if (isBlurViewValid()) {
                dataBinding.blurView.visibility = View.GONE
                Log.w(TAG, "BlurView disabled due to errors")
            }
        } catch (e: Exception) {
            Log.e(TAG, "Failed to disable BlurView: ${e.message}")
        }
    }

    override fun onPause() {
        super.onPause()
        try {
            dataBinding.viewPager.pauseAutoSlide()
        } catch (e: Exception) {
            Logger.e(TAG, "Exception during pauseAutoSlide: $e")
        }
        if (isBlurViewActive) {
            try {
                isBlurViewActive = false
                Log.d(TAG, "BlurView paused")
            } catch (e: Exception) {
                Log.e(TAG, "Failed to pause BlurView: ${e.message}")
            }
        }
    }

    override fun onResume() {
        super.onResume()
        try {
            dataBinding.viewPager.resumeAutoSlide()
        } catch (e: Exception) {
            Logger.e(TAG, "Exception during pauseAutoSlide: $e")
        }
        // Resume BlurView nếu cần
        if (!isBlurViewActive && isBlurViewValid()) {
            try {
                // Sử dụng post để đảm bảo View đã layout
                dataBinding.blurView.post {
                    if (!isFinishing && !isDestroyed && isBlurViewValid()) {
                        try {
                            setBlur(dataBinding.blurView, dataBinding.rootLout)
                            dataBinding.blurView.invalidate()
                            isBlurViewActive = true
                            Log.d(TAG, "BlurView resumed after layout")
                        } catch (e: Exception) {
                            Log.e(TAG, "Failed to resume BlurView after layout: ${e.message}")
                            disableBlurView()
                        }
                    }
                }
            } catch (e: Exception) {
                Log.e(TAG, "Failed to resume BlurView: ${e.message}")
            }
        }
    }

    override fun onDestroy() {
        super.onDestroy()

        // Disable BlurView before destroy
        isBlurViewActive = false

        // Clear ImageView safely
        BitmapUtils.clearImageViewSafely(dataBinding.imgBg)

        // Cleanup Glide request để avoid memory leak và lỗi recycled bitmap
        currentGlideRequest?.let { futureTarget ->
            try {
                if (!futureTarget.isDone) {
                    Glide.with(applicationContext).clear(futureTarget)
                    Log.d(TAG, "Cleaned up Glide request in onDestroy using application context")
                }
            } catch (e: Exception) {
                Logger.e(TAG, "Exception during Glide cleanup: $e")
            }
        }
        currentGlideRequest = null

        try {
            if (isBlurViewValid()) {
                Log.d(TAG, "BlurView will be cleaned up automatically")
            }
        } catch (e: Exception) {
            Log.e(TAG, "Failed to clear BlurView: ${e.message}")
        }
    }

    companion object {
        private const val TAG = "ViewWallpapersActivity"
    }
}