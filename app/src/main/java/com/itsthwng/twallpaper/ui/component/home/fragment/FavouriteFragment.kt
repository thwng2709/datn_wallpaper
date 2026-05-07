package com.itsthwng.twallpaper.ui.component.home.fragment

import android.content.Intent
import android.provider.Settings
import android.util.Log
import android.view.View
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.Observer
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.repeatOnLifecycle
import com.google.firebase.Firebase
import com.itsthwng.twallpaper.R
import com.itsthwng.twallpaper.databinding.FragmentFavouriteBinding
import com.itsthwng.twallpaper.extension.toArrayList
import com.itsthwng.twallpaper.ui.base.BaseViewModelFragmentBinding
import com.itsthwng.twallpaper.ui.component.MainActivity
import com.itsthwng.twallpaper.ui.component.home.viewmodel.FavouriteViewModel
import com.itsthwng.twallpaper.utils.AppConfig.logEventTracking
import com.itsthwng.twallpaper.utils.Constants
import com.itsthwng.twallpaper.utils.Global
import com.itsthwng.twallpaper.utils.Logger
import com.itsthwng.twallpaper.utils.NetworkUtils
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.launch

@AndroidEntryPoint
class FavouriteFragment :
    BaseViewModelFragmentBinding<FragmentFavouriteBinding, FavouriteViewModel>() {

    override fun getContentViewId(): Int {
        return R.layout.fragment_favourite
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
        val connLiveData = (activity as MainActivity).getConnectionLiveData()

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
                logEventTracking(Constants.HOME_REQUIRE_INTERNET_YES)
                startActivity(Intent(Settings.ACTION_WIFI_SETTINGS))
            } catch (e: Exception) {
                Logger.e(e.message)
            }
        }
    }


    private fun initObservers() {
        viewLifecycleOwner.lifecycleScope.launch {
            viewLifecycleOwner.lifecycle.repeatOnLifecycle(Lifecycle.State.STARTED) {
                launch {
                    viewModel.favoriteWalls.collect { likedList ->
                        if (likedList == null) return@collect
                        dataBinding.progressLoading.visibility = View.GONE
                        localStorage.favourites =
                            Global.listOfIntegerToString(likedList.filter { it.id != null }
                                .map { it.id!! }) ?: ""
                        viewModel.wallpaperAdapter.favList =
                            Global.convertStringToLis(localStorage.favourites).toArrayList()
                        if (likedList.isEmpty()) {
                            dataBinding.loutNoFav.visibility = View.VISIBLE
                            dataBinding.rvImageByCategory.visibility = View.GONE
                        } else {
                            dataBinding.loutNoFav.visibility = View.GONE
                            dataBinding.rvImageByCategory.visibility = View.VISIBLE
                            viewModel.wallpaperAdapter.removeWhenUpdateData(likedList.toMutableList())
                        }
                    }
                }
            }
        }
    }

    private fun doOnResume() {
        val s = localStorage.favourites
        val list = Global.convertStringToLis(s)
        Log.i("TAG", "initView: $s")
        Log.i("TAG", "initView: $list")

        viewModel.wallpaperAdapter.favList =
            Global.convertStringToLis(localStorage.favourites).toArrayList()
        viewModel.wallpaperAdapter.onFavClick = {
            viewModel.updateWallpaper(it)
        }

        if (list.isEmpty()) {
            viewModel.wallpaperAdapter.clear()
            dataBinding.rvImageByCategory.adapter = null
            dataBinding.rvImageByCategory.adapter = viewModel.wallpaperAdapter

            dataBinding.loutNoFav.visibility = View.VISIBLE
        } else {
//            getFavourites(s)
        }
        Log.i(" onnnn set fav", ": ${viewModel.wallpaperAdapter.favList}")
    }

    private fun logTracking() {
        if (localStorage.isFirstOpenFavouriteScreen) {
            localStorage.isFirstOpenFavouriteScreen = false
            logEventTracking(Constants.EventKey.FAVOURITE_OPEN_1ST)
        } else {
            logEventTracking(Constants.EventKey.FAVOURITE_OPEN_2ND)
        }
    }

    override fun registerListeners() {

    }

    override fun initializeData() {

    }
}