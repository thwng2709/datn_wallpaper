package com.itsthwng.twallpaper.ui.component.home.fragment

import android.content.Intent
import android.provider.Settings
import android.view.View
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.Observer
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.repeatOnLifecycle
import androidx.recyclerview.widget.GridLayoutManager
import androidx.recyclerview.widget.LinearLayoutManager
import com.google.firebase.Firebase
import com.itsthwng.twallpaper.R
import com.itsthwng.twallpaper.data.CommonInfo
import com.itsthwng.twallpaper.data.model.SettingData
import com.itsthwng.twallpaper.databinding.FragmentCategoryBinding
import com.itsthwng.twallpaper.ui.base.BaseViewModelFragmentBinding
import com.itsthwng.twallpaper.ui.component.MainActivity
import com.itsthwng.twallpaper.ui.component.home.adapter.CategoryAdapter
import com.itsthwng.twallpaper.ui.component.home.viewmodel.CategoryViewModel
import com.itsthwng.twallpaper.utils.AppConfig.logEventTracking
import com.itsthwng.twallpaper.utils.Constants
import com.itsthwng.twallpaper.utils.Logger
import com.itsthwng.twallpaper.utils.NetworkUtils
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.launch

@AndroidEntryPoint
class CategoryFragment :
    BaseViewModelFragmentBinding<FragmentCategoryBinding, CategoryViewModel>() {

    lateinit var allCat: List<SettingData.CategoriesItem>
    lateinit var liveCat: List<SettingData.CategoriesItem>
    lateinit var simpleCat: List<SettingData.CategoriesItem>

    lateinit var categoryAdapter: CategoryAdapter

    override fun getContentViewId(): Int {
        return R.layout.fragment_category
    }

    override fun initializeViews() {
        // Inflate the layout for this fragment
        // Fix: Hiển thị tab Live mặc định để tránh vấn đề Firebase chậm
        // Sẽ được cập nhật khi có data từ Remote Config
        dataBinding.tvLive.visibility = View.VISIBLE
        categoryAdapter = CategoryAdapter(
            localStorage,
            requireActivity(),
        )
        dataBinding.rvCat.adapter = categoryAdapter


        initView()
        logTracking()

        checkNetworkFirst()
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
            initObserver()
        } else {
            showOffline(true)

            // 2) Chờ tới khi có mạng rồi start collectors (one-shot)
            val onceObserver = object : Observer<Boolean> {
                override fun onChanged(value: Boolean) {
                    if (value) {
                        connLiveData.removeObserver(this)
                        showOffline(false)
                        initObserver()
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
                initObserver()
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

    override fun registerListeners() {

    }

    override fun initializeData() {

    }

    private fun initObserver() {
        viewLifecycleOwner.lifecycleScope.launch {
            viewLifecycleOwner.repeatOnLifecycle(Lifecycle.State.STARTED) {
                launch {
                    viewModel.categories.collect { categories ->
                        if (categories == null) return@collect
                        if (categories.isNotEmpty()) {
                            allCat = categories.filter { it.wallpapers_count != 0 }
                            simpleCat =
                                categories.filter { categoriesItem -> categoriesItem.type == 0 && categoriesItem.wallpapers_count != 0 }
                            liveCat =
                                categories.filter { categoriesItem -> categoriesItem.type == 1 && categoriesItem.wallpapers_count != 0 }
                            when (viewModel.selected.value) {
                                0 -> {
                                    if (allCat.isEmpty()) {
                                        dataBinding.tvNoData.visibility = View.VISIBLE
                                        dataBinding.rvCat.visibility = View.GONE
                                        dataBinding.progressLoading.visibility = View.GONE
                                    } else {
                                        dataBinding.tvNoData.visibility = View.GONE
                                        dataBinding.rvCat.visibility = View.VISIBLE
                                        dataBinding.progressLoading.visibility = View.GONE
                                    }
                                    categoryAdapter.submitList(allCat)
                                    changeLayoutManager(allCat)
                                }

                                1 -> {
                                    if (simpleCat.isEmpty()) {
                                        dataBinding.tvNoData.visibility = View.VISIBLE
                                        dataBinding.rvCat.visibility = View.GONE
                                        dataBinding.progressLoading.visibility = View.GONE
                                    } else {
                                        dataBinding.tvNoData.visibility = View.GONE
                                        dataBinding.rvCat.visibility = View.VISIBLE
                                        dataBinding.progressLoading.visibility = View.GONE
                                    }
                                    categoryAdapter.submitList(simpleCat)
                                    changeLayoutManager(simpleCat)
                                }

                                2 -> {
                                    if (liveCat.isEmpty()) {
                                        dataBinding.tvNoData.visibility = View.VISIBLE
                                        dataBinding.rvCat.visibility = View.GONE
                                        dataBinding.progressLoading.visibility = View.GONE
                                    } else {
                                        dataBinding.tvNoData.visibility = View.GONE
                                        dataBinding.rvCat.visibility = View.VISIBLE
                                        dataBinding.progressLoading.visibility = View.GONE
                                    }
                                    categoryAdapter.submitList(liveCat)
                                    changeLayoutManager(liveCat)
                                }
                            }
                        } else {
                            dataBinding.tvNoData.visibility = View.VISIBLE
                            dataBinding.rvCat.visibility = View.GONE
                            dataBinding.progressLoading.visibility = View.GONE
                        }
                    }
                }
            }
        }
        viewModel.selected.observe(viewLifecycleOwner, Observer {
            when (it) {
                0 -> {
                    if (allCat.isEmpty()) {
                        dataBinding.tvNoData.visibility = View.VISIBLE
                        dataBinding.rvCat.visibility = View.GONE
                        dataBinding.progressLoading.visibility = View.GONE
                    } else {
                        dataBinding.tvNoData.visibility = View.GONE
                        dataBinding.rvCat.visibility = View.VISIBLE
                        dataBinding.progressLoading.visibility = View.GONE
                    }
                    categoryAdapter.submitList(allCat)
                    changeLayoutManager(allCat)
                }

                1 -> {
                    if (simpleCat.isEmpty()) {
                        dataBinding.tvNoData.visibility = View.VISIBLE
                        dataBinding.rvCat.visibility = View.GONE
                        dataBinding.progressLoading.visibility = View.GONE
                    } else {
                        dataBinding.tvNoData.visibility = View.GONE
                        dataBinding.rvCat.visibility = View.VISIBLE
                        dataBinding.progressLoading.visibility = View.GONE
                    }
                    categoryAdapter.submitList(simpleCat)
                    changeLayoutManager(simpleCat)
                }

                2 -> {
                    if (liveCat.isEmpty()) {
                        dataBinding.tvNoData.visibility = View.VISIBLE
                        dataBinding.rvCat.visibility = View.GONE
                        dataBinding.progressLoading.visibility = View.GONE
                    } else {
                        dataBinding.tvNoData.visibility = View.GONE
                        dataBinding.rvCat.visibility = View.VISIBLE
                        dataBinding.progressLoading.visibility = View.GONE
                    }
                    categoryAdapter.submitList(liveCat)
                    changeLayoutManager(liveCat)
                }
            }
            dataBinding.progressLoading.visibility = View.GONE
            dataBinding.rvCat.scrollToPosition(0)


            dataBinding.model = viewModel
        })
    }

    private fun initView() {
        allCat = localStorage.getListCategories().filter { it.wallpapers_count != 0 }
        simpleCat = localStorage.getListCategories()
            .filter { categoriesItem -> categoriesItem.type == 0 && categoriesItem.wallpapers_count != 0 }
        liveCat = localStorage.getListCategories()
            .filter { categoriesItem -> categoriesItem.type == 1 && categoriesItem.wallpapers_count != 0 }
    }

    override fun onResume() {
        super.onResume()
        dataBinding.rvCat.scrollToPosition(categoryAdapter.lastSelectedPos)
        dataBinding.tvLive.visibility = if (CommonInfo.show_live_categories) {
            View.VISIBLE
        } else {
            View.GONE
        }
    }

    private fun logTracking() {
        if (localStorage.isFirstOpenCategoryScreen) {
            localStorage.isFirstOpenCategoryScreen = false
            logEventTracking(Constants.EventKey.CATEGORY_OPEN_1ST)
        } else {
            logEventTracking(Constants.EventKey.CATEGORY_OPEN_2ND)
        }
    }

    private fun changeLayoutManager(list: List<SettingData.CategoriesItem>) {
        dataBinding.rvCat.layoutManager = if (list.size > 50) {
            GridLayoutManager(requireActivity(), 2)
        } else {
            LinearLayoutManager(requireActivity())
        }
    }

}