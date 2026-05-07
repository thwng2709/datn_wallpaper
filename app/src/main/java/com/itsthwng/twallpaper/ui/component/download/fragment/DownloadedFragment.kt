package com.itsthwng.twallpaper.ui.component.download.fragment

import android.content.Intent
import android.provider.Settings
import android.view.HapticFeedbackConstants
import android.view.View
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.Observer
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.repeatOnLifecycle
import com.google.firebase.Firebase
import com.itsthwng.twallpaper.R
import com.itsthwng.twallpaper.databinding.FragmentDownloadedBinding
import com.itsthwng.twallpaper.extension.toArrayList
import com.itsthwng.twallpaper.ui.base.BaseViewModelFragmentBinding
import com.itsthwng.twallpaper.ui.component.download.viewmodel.DownloadedViewModel
import com.itsthwng.twallpaper.ui.component.setting.view.SettingActivity
import com.itsthwng.twallpaper.utils.AppConfig
import com.itsthwng.twallpaper.utils.Constants
import com.itsthwng.twallpaper.utils.Global
import com.itsthwng.twallpaper.utils.Logger
import com.itsthwng.twallpaper.utils.NetworkUtils
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.launch

@AndroidEntryPoint
class DownloadedFragment :
    BaseViewModelFragmentBinding<FragmentDownloadedBinding, DownloadedViewModel>() {

    override fun getContentViewId(): Int {
        return R.layout.fragment_downloaded
    }

    override fun initializeViews() {
        checkNetworkFirst()
        logTracking()
        dataBinding.model = viewModel
    }

    private fun showOffline(isOffline: Boolean) {
        if (isOffline) {
            dataBinding.noInternetLayout.visibility = View.VISIBLE
        } else {
            dataBinding.progressLoading.visibility = View.VISIBLE
            dataBinding.noInternetLayout.visibility = View.GONE
        }
    }

    private fun checkNetworkFirst() {
        val connLiveData = (activity as SettingActivity).getConnectionLiveData()

        // 1) Trạng thái ngay lúc vào
        val initialConnected = NetworkUtils.isNetworkConnected()

        if (initialConnected) {
            showOffline(false)
            initObservers()
            doOnResume()
        } else {
            showOffline(true)

            // 2) Chờ tới khi có mạng rồi start collectors (one-shot)
            val onceObserver = object : Observer<Boolean> {
                override fun onChanged(value: Boolean) {
                    if (value) {
                        connLiveData.removeObserver(this)
                        showOffline(false)
                        initObservers()
                        doOnResume()
                    }
                }
            }
            connLiveData.observe(viewLifecycleOwner, onceObserver)
        }

        // 3) Nút Refresh: thử lại ngay
        dataBinding.btnRetry.setOnClickListener {
            val now = NetworkUtils.isNetworkConnected()
            if (now) {
                showOffline(false)
                initObservers()
                doOnResume()
            } else {
                showOffline(true)
            }
        }

        // 4) Nút Go to Settings: mở cài đặt mạng
        dataBinding.btnSetting.setOnClickListener {
            try {
                AppConfig.logEventTracking(Constants.HOME_REQUIRE_INTERNET_YES)
                startActivity(Intent(Settings.ACTION_WIFI_SETTINGS))
            } catch (e: Exception) {
                Logger.e(e.message)
            }
        }
    }


    private fun initObservers() {
        viewLifecycleOwner.lifecycleScope.launch {
            viewLifecycleOwner.lifecycle.repeatOnLifecycle(Lifecycle.State.CREATED) {
                launch {
                    // Collect giá trị Favourite riêng, để hiển thị trạng thái favourite hay ko
                    viewModel.favoriteWalls.collect { likedList ->
                        localStorage.favourites =
                            Global.listOfIntegerToString(likedList.filter { it.id != null }
                                .map { it.id!! }) ?: ""
                        viewModel.wallpaperAdapter.favList =
                            Global.convertStringToLis(localStorage.favourites).toArrayList()
                    }
                }
                launch {
                    // Collect giá trị ảnh đã tải về  -> Hiển thị làm dữ liệu cho adapter
                    viewModel.downloadedWalls.collect { likedList ->
                        dataBinding.progressLoading.visibility = View.GONE
                        if (likedList.isEmpty()) {
                            dataBinding.loutNoDownloaded.visibility = View.VISIBLE
                            dataBinding.rvImageByCategory.visibility = View.GONE
                        } else {
                            dataBinding.loutNoDownloaded.visibility = View.GONE
                            dataBinding.rvImageByCategory.visibility = View.VISIBLE
                            viewModel.wallpaperAdapter.removeWhenUpdateData(likedList.toMutableList())
                        }
                    }
                }
            }
        }
    }

    private fun doOnResume() {
        // Thêm onclick điều chỉnh trạng thái Favourite của ảnh
        viewModel.wallpaperAdapter.onFavClick = {
            viewModel.updateWallpaper(it)
        }
    }

    override fun onResume() {
        super.onResume()
    }

    override fun registerListeners() {
        dataBinding.btnBack.setOnClickListener {
            it.performHapticFeedback(HapticFeedbackConstants.VIRTUAL_KEY)
            activity?.onBackPressedDispatcher?.onBackPressed()
        }
    }


    private fun logTracking(){
        if(localStorage.isFirstOpenDownloadScreen){
            localStorage.isFirstOpenDownloadScreen = false
            AppConfig.logEventTracking(Constants.EventKey.DOWNLOAD_OPEN_1ST)
        } else {
            AppConfig.logEventTracking(Constants.EventKey.DOWNLOAD_OPEN_2ND)
        }
    }

    override fun initializeData() {

    }
}