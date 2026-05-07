package com.itsthwng.twallpaper.ui.component.home.fragment

import android.app.Activity
import android.content.Intent
import android.os.Build
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.provider.Settings
import android.util.DisplayMetrics
import android.util.Log
import android.view.View
import android.view.ViewGroup
import android.view.WindowInsets
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.repeatOnLifecycle
import androidx.recyclerview.widget.GridLayoutManager
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.PagerSnapHelper
import androidx.recyclerview.widget.RecyclerView
import androidx.recyclerview.widget.SimpleItemAnimator
import androidx.recyclerview.widget.SnapHelper
import androidx.work.WorkInfo
import androidx.work.WorkManager
import com.google.firebase.Firebase
import com.google.gson.Gson
import com.itsthwng.twallpaper.BuildConfig
import com.itsthwng.twallpaper.R
import com.itsthwng.twallpaper.data.CommonInfo
import com.itsthwng.twallpaper.data.model.SettingData
import com.itsthwng.twallpaper.databinding.FragmentHomeBinding
import com.itsthwng.twallpaper.extension.toArrayList
import com.itsthwng.twallpaper.ui.base.BaseViewModelFragmentBinding
import com.itsthwng.twallpaper.ui.component.MainActivity
import com.itsthwng.twallpaper.ui.component.MainViewModel
import com.itsthwng.twallpaper.ui.component.bottomSheet.UpdateNewVersionBottomSheet
import com.itsthwng.twallpaper.ui.component.home.adapter.AdsAdapter
import com.itsthwng.twallpaper.ui.component.home.adapter.FeatureDotsAdapter
import com.itsthwng.twallpaper.ui.component.home.adapter.FeatureImagesAdapter
import com.itsthwng.twallpaper.ui.component.home.adapter.HomeCatAdapter
import com.itsthwng.twallpaper.ui.component.home.viewmodel.HomeViewModel
import com.itsthwng.twallpaper.utils.AppConfig
import com.itsthwng.twallpaper.utils.Constants
import com.itsthwng.twallpaper.utils.Global
import com.itsthwng.twallpaper.utils.Logger
import com.itsthwng.twallpaper.utils.NetworkUtils
import com.itsthwng.twallpaper.utils.wallpaper.CategoryHelper
import com.itsthwng.twallpaper.workManager.OrderConf
import dagger.hilt.android.AndroidEntryPoint
import io.reactivex.disposables.CompositeDisposable
import kotlinx.coroutines.Runnable
import kotlinx.coroutines.launch
import kotlin.math.abs

@AndroidEntryPoint
class HomeFragment : BaseViewModelFragmentBinding<FragmentHomeBinding, HomeViewModel>(), Runnable {
    private var gson = Gson()
    private var wallsByCategory: List<SettingData.WallpapersItem> = listOf()
    lateinit var mainViewModel: MainViewModel
    lateinit var handler: Handler
    var reversed = false
    var scrollingPos = 0
    var wallListMap = HashMap<String, List<SettingData.WallpapersItem>>()
    private var wallsSignatureMap = HashMap<String, Int>()
    lateinit var disposable: CompositeDisposable
    private var strNew: String = ""
    private var justClickFav = false
    private var justPause = false
    private lateinit var updateNewVersionBottomSheet: UpdateNewVersionBottomSheet
    private lateinit var wallpaperAdapter: AdsAdapter
    // Home adapters are owned by Activity instead of ViewModel.
    val featureImagesAdapter: FeatureImagesAdapter by lazy { FeatureImagesAdapter() }
    val featureDotsAdapter: FeatureDotsAdapter by lazy { FeatureDotsAdapter() }
    val catAdapter: HomeCatAdapter by lazy { HomeCatAdapter() }

    private var workerStatus: WorkInfo.State? = null
    private var workerProgress: Int? = null
    private var hasPrewarmedCategories = false

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
    }

    override fun getContentViewId(): Int {
        return R.layout.fragment_home
    }

    override fun initializeViews() {
        localStorage.isFirstOpen = false

        if (localStorage.isFirstOpenHome) {
            AppConfig.logEventTracking(Constants.EventKey.HOME_OPEN_1ST)
            localStorage.isFirstOpenHome = false
        } else {
            AppConfig.logEventTracking(Constants.EventKey.HOME_OPEN_2ND)
        }
        mainViewModel = ViewModelProvider(requireActivity())[MainViewModel::class.java]
        disposable = CompositeDisposable()
        WorkManager.getInstance(requireContext())
            .getWorkInfosForUniqueWorkLiveData("LOCALE_SYNC")
            .observe(viewLifecycleOwner) { workInfos ->
                if (workInfos.isNotEmpty()) {
                    val workInfo = workInfos[0]
                    workerStatus = workInfos[0].state
                    workerProgress = when (workInfo.state) {
                        WorkInfo.State.RUNNING -> {
                            workInfo.progress.getInt("progress", 0)
                        }
                        WorkInfo.State.SUCCEEDED -> {
                            100
                        }
                        WorkInfo.State.FAILED -> {
                            -1
                        }
                        else -> {
                            null
                        }
                    }
                }
            }

        strNew = getString(R.string.str_new)
        initView()
        initListener()
        initConnectivityAndCollectors()
    }

    private fun initConnectivityAndCollectors() {
        val connLiveData = (activity as MainActivity).getConnectionLiveData()

        // 1) Trạng thái ngay lúc vào
        val initialConnected = NetworkUtils.isNetworkConnected()

        if (initialConnected) {
            showOffline(false)
            initObserver()
            if (::wallpaperAdapter.isInitialized) {
                wallpaperAdapter.updateNetwork(true)
            }
        } else {
            showOffline(true)

            // 2) Chờ tới khi có mạng rồi start collectors (one-shot)
            val onceObserver = object : Observer<Boolean> {
                override fun onChanged(value: Boolean) {
                    if (value) {
                        connLiveData.removeObserver(this)
                        showOffline(false)
                        if (::wallpaperAdapter.isInitialized) {
                            wallpaperAdapter.updateNetwork(true)
                        }
                        initObserver()
                    }
                }
            }
            connLiveData.observe(viewLifecycleOwner, onceObserver)
        }

        // 3) Nút Refresh: thử lại ngay
        dataBinding.btnConfirmYesImage.setOnClickListener {
            val now = NetworkUtils.isNetworkConnected()
            if (now) {
                showOffline(false)
                if (::wallpaperAdapter.isInitialized) {
                    wallpaperAdapter.updateNetwork(true)
                }
                initObserver()
                viewModel.onManualRefresh() // nếu muốn "kick" reload
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

    private fun initObserver() {
        viewLifecycleOwner.lifecycleScope.launch {
            viewLifecycleOwner.repeatOnLifecycle(Lifecycle.State.STARTED) {
                // 1) Wallpapers theo category (hoặc tất cả nếu null)
                launch {
                    viewModel.wallsByCategory.collect { it ->
                        if (it == null || (justPause && wallpaperAdapter.hasData())) {
                            justPause = false
                            return@collect
                        }
                        if (it.isNotEmpty()) {
                            val categoryKey = catId.lowercase()
                            val newSignature = buildIdSignature(it)
                            if (wallsSignatureMap[categoryKey] == newSignature && wallpaperAdapter.hasData()) {
                                return@collect
                            }
                            var sortedList = it
                            if (catId == "new") {
                                try {
                                    val orderJson =
                                        CommonInfo.categoryOrderJson // file JSON thứ tự + aliases
                                    val orderConf = gson.fromJson(orderJson, OrderConf::class.java)
                                    if (orderConf != null && !orderConf.categories_order.isNullOrEmpty()) {
                                        sortedList = CategoryHelper.sortWallpapersByCategoryOrder(
                                            it,
                                            orderConf
                                        )
                                    }
                                } catch (e: Exception) {
                                    Log.e("Home", "error: ${e.message}")
                                }
                            }
                            if (wallListMap.contains(categoryKey)) {
                                wallListMap[categoryKey] = sortedList
                            } else {
                                wallListMap[categoryKey] = sortedList
                            }
                            wallsSignatureMap[categoryKey] = newSignature
                            dataBinding.tvNoData.visibility = View.GONE
                            dataBinding.ivNoData.visibility = View.GONE
                            dataBinding.rvImageByCategory.visibility = View.VISIBLE
                            wallsByCategory = sortedList
                            wallpaperAdapter.updateData(sortedList)
                            // Scroll về position 0 sau khi Flow emit data mới
                            if (!justClickFav) {
                                dataBinding.rvImageByCategory.scrollToPosition(0)
                            } else {
                                justClickFav = false
                            }
                            if (workerStatus == WorkInfo.State.RUNNING && workerProgress != null && workerProgress!! <= 50 && !wallpaperAdapter.hasData()) {
                                dataBinding.progressLoadMore.visibility = View.VISIBLE
                                dataBinding.sihmmer.visibility = View.VISIBLE
                                dataBinding.rvCatName.visibility = View.GONE
                            } else {
                                dataBinding.progressLoadMore.visibility = View.GONE
                                dataBinding.sihmmer.stopShimmer()
                                dataBinding.sihmmer.visibility = View.GONE
                                dataBinding.noInternetImageLayout.visibility = View.GONE
                                dataBinding.tvNoData.visibility = View.GONE
                                dataBinding.ivNoData.visibility = View.GONE
                                dataBinding.rvImageByCategory.visibility = View.VISIBLE
                                dataBinding.rvCatName.visibility = View.VISIBLE
                            }
                        } else {
                            try {
                                if (workerStatus != WorkInfo.State.RUNNING && workerStatus != null) {
                                    dataBinding.rvImageByCategory.visibility = View.GONE
                                    dataBinding.rvCatName.visibility = View.GONE
                                    dataBinding.tvNoData.visibility = View.VISIBLE
                                    dataBinding.ivNoData.visibility = View.VISIBLE
                                    dataBinding.progressLoadMore.visibility = View.GONE
                                    dataBinding.sihmmer.visibility = View.GONE
                                    dataBinding.sihmmer.stopShimmer()
                                    dataBinding.noInternetImageLayout.visibility = View.GONE
                                } else {
                                    dataBinding.rvImageByCategory.visibility = View.GONE
                                    dataBinding.rvCatName.visibility = View.GONE
                                    dataBinding.tvNoData.visibility = View.GONE
                                    dataBinding.ivNoData.visibility = View.GONE
                                    dataBinding.progressLoadMore.visibility = View.VISIBLE
                                    dataBinding.sihmmer.visibility = View.VISIBLE
                                }
                            } catch (_: Exception) {
                                dataBinding.rvImageByCategory.visibility = View.GONE
                                dataBinding.rvCatName.visibility = View.GONE
                                dataBinding.tvNoData.visibility = View.VISIBLE
                                dataBinding.ivNoData.visibility = View.VISIBLE
                                dataBinding.progressLoadMore.visibility = View.GONE
                                dataBinding.sihmmer.visibility = View.GONE
                                dataBinding.sihmmer.stopShimmer()
                                dataBinding.noInternetImageLayout.visibility = View.GONE
                            }
                        }
                    }
                }
                // 2) Featured
                launch {
                    viewModel.featuredWalls.collect { featured ->
                        if (featured == null || (justPause && featureImagesAdapter.hasData())) {
                            justPause = false
                            return@collect
                        }

                        featureImagesAdapter.updateData(featured)
                        val dotlist = MutableList(featureImagesAdapter.mList.size) { " " }
                        featureDotsAdapter.updateData(dotlist)
                        handler.postDelayed({
                            dataBinding.rvDots.minimumWidth = dataBinding.rvDots.width
                        }, 2000)
                        if (featured.isEmpty()) {
                            try {
                                if (workerStatus != WorkInfo.State.RUNNING && workerStatus != null) {
                                    dataBinding.progressFeature.visibility = View.GONE
                                    dataBinding.rvFeatured.visibility = View.GONE
                                    dataBinding.tvNoDataFeature.visibility = View.VISIBLE
                                } else {
                                    dataBinding.tvNoDataFeature.visibility = View.GONE
                                    dataBinding.progressFeature.visibility = View.VISIBLE
                                }
                            } catch (_: Exception) {
                                dataBinding.rvFeatured.visibility = View.GONE
                                dataBinding.tvNoDataFeature.visibility = View.VISIBLE
                            }
                        } else {
                            dataBinding.progressFeature.visibility = View.GONE
                            dataBinding.rvFeatured.visibility = View.VISIBLE
                            dataBinding.tvNoDataFeature.visibility = View.GONE
                        }
                    }
                }

                launch {
                    viewModel.categories.collect { categories ->
                        if (categories.isNotEmpty()) {
                            localStorage.saveCategories(categories)
                            catAdapter.updateData(
                                categories.toMutableList(),
                                strNew
                            )

                            if (!hasPrewarmedCategories) {
                                hasPrewarmedCategories = true
                                categories.asSequence()
                                    .mapNotNull { category -> category.id?.lowercase() }
                                    .filter { id -> id != "new" }
                                    .distinct()
                                    .take(2)
                                    .forEach { id -> viewModel.prewarmCategory(id) }
                            }
                        }
                    }
                }

                launch {
                    viewModel.favoriteWalls.collect { likedList ->
                        localStorage.favourites =
                            Global.listOfIntegerToString(likedList.filter { it.id != null }
                                .map { it.id!! }) ?: ""
                        // Chỉ gọi updateFavList một lần với dữ liệu từ database
                        wallpaperAdapter.updateFavList(likedList.filter { it.id != null }
                            .map { it.id!! })
                    }
                }
            }
        }
    }

    private fun showOffline(show: Boolean) {
        dataBinding.noInternetImageLayout.visibility = if (show) View.VISIBLE else View.GONE

        if (show) {
            // Lần đầu vào mà offline: tắt progress để tránh hiểu nhầm
            dataBinding.progressLoadMore.visibility = View.GONE
            dataBinding.progressFeature.visibility = View.GONE

            // KHÔNG động vào rvFeatured/rvImageByCategory:
            // - Nếu đã có data từ trước (VD quay lại Fragment) thì vẫn hiển thị bình thường.
            // - Nếu chưa có data (lần đầu) thì list vốn đang rỗng; overlay sẽ che.
        }
    }

    override fun registerListeners() {
    }

    override fun initializeData() {
    }

    var scrolledByUser = false
    private fun initListener() {

        dataBinding.rvFeatured.addOnScrollListener(object : RecyclerView.OnScrollListener() {
            override fun onScrollStateChanged(recyclerView: RecyclerView, newState: Int) {
                super.onScrollStateChanged(recyclerView, newState)

                if (newState == RecyclerView.SCROLL_STATE_DRAGGING) {
                    scrolledByUser = true
                }

                if (newState == RecyclerView.SCROLL_STATE_IDLE) {
                    if (scrolledByUser) {
                        handler.removeCallbacks(this@HomeFragment)
                        val itemPoition =
                            (dataBinding.rvFeatured.layoutManager as LinearLayoutManager?)!!.findFirstCompletelyVisibleItemPosition()

                        scrollingPos = itemPoition
                        reversed = scrollingPos + 1 > featureImagesAdapter.itemCount - 1

                        scrollToPos(true)
                    }
                    scrolledByUser = false
                }


            }
        })


        dataBinding.appBar.addOnOffsetChangedListener { appBarLayout, verticalOffset ->

            mainViewModel.coordinatedExpanded.value =
                abs(verticalOffset) - appBarLayout.totalScrollRange <= -50
        }

        catAdapter.onItemClick = object : HomeCatAdapter.OnItemClick {
            override fun onClick(item: SettingData.CategoriesItem) {

                val key = item.id!!.lowercase()
                catId = key

                // Scroll về position 0 ngay lập tức khi click category
                dataBinding.rvImageByCategory.scrollToPosition(0)
//                dataBinding.nestedScrollView.scrollTo(0, 0)

                // 1) Cập nhật state trong VM để mọi collector đều nhất quán
                viewModel.setCategory(key)

                // 2) Nếu đã có cache thì hiển thị ngay (optimistic UI), vẫn để Flow lo chuyện reload
                wallListMap[key]?.let { cached ->
                    dataBinding.tvNoData.visibility = View.GONE
                    dataBinding.ivNoData.visibility = View.GONE
                    val show = if (key == "new" && cached.size > Constants.PAGINATION_COUNT)
                        cached.take(Constants.PAGINATION_COUNT).toMutableList()
                    else cached

                    wallpaperAdapter.updateData(show)
                    return
                }

                // 3) Chưa có cache -> trigger load theo Flow
                noMoreData = false
                disposable.clear()

            }
        }

    }

    var noMoreData = false

    var catId = "new"

    private fun initView() {

        val snapHelper: SnapHelper = PagerSnapHelper()
        snapHelper.attachToRecyclerView(dataBinding.rvFeatured)

        handler = Handler(Looper.getMainLooper())


        catAdapter.updateData(
            localStorage.getListCategories().toMutableList(),
            strNew
        )
        dataBinding.rvFeatured.adapter = featureImagesAdapter
        dataBinding.rvDots.adapter = featureDotsAdapter
        dataBinding.rvCatName.adapter = catAdapter
        dataBinding.sihmmer.visibility = View.VISIBLE
        dataBinding.sihmmer.startShimmer()
        dataBinding.progressLoadMore.visibility = View.VISIBLE

        wallpaperAdapter = AdsAdapter(
            localStorage,
            mutableListOf(),
            isNetworkConnected = NetworkUtils.isNetworkConnected()
        )
//        wallpaperAdapter.favList =
//            Global.convertStringToLis(localStorage.favourites).toArrayList()
        wallpaperAdapter.updateFavList(
            Global.convertStringToLis(localStorage.favourites).toArrayList()
        )
        wallpaperAdapter.onFavClick = {
            justClickFav = true
            viewModel.updateWallpaper(it)
        }
        dataBinding.rvImageByCategory.apply {
            setHasFixedSize(true)
            (itemAnimator as? SimpleItemAnimator)?.supportsChangeAnimations = false
            itemAnimator = null                          // loại bỏ animation tốn 100ms+
            val grid = GridLayoutManager(context, 2)
            grid.initialPrefetchItemCount = 6
            // Nếu có footer khác span, set SpanSizeLookup phù hợp
            layoutManager = grid
            // Chia sẻ RecycledViewPool nếu có RV khác (featured, dots…)
        }
        dataBinding.rvImageByCategory.adapter = wallpaperAdapter

        val oneThird = (getWindowHeightPx(requireActivity()) / 3f).toInt()

        // Đặt container và RV cùng chiều cao, RV match_parent để lấp đầy box
        dataBinding.llFeature.updateHeight(oneThird)
        dataBinding.rvFeatured.updateHeight(ViewGroup.LayoutParams.MATCH_PARENT)

    }

    override fun run() {
        val count = featureImagesAdapter.itemCount
        if (count == 0) return

        if (reversed) {
            if (scrollingPos - 1 < 0) {
                Log.i("TAG", "run: 1")
                scrollingPos += 1
                reversed = false
            } else {
                Log.i("TAG", "run: 2")
                scrollingPos -= 1
            }
        } else {
            if (scrollingPos + 1 > featureImagesAdapter.itemCount - 1) {
                scrollingPos -= 1
                reversed = true
                Log.i("TAG", "run: 3")
            } else {
                scrollingPos += 1
                reversed = false
                Log.i("TAG", "run: 4")
            }
        }

        scrollingPos = scrollingPos.coerceIn(0, count - 1)
        scrollToPos(false)
    }

    private fun scrollToPos(fromUser: Boolean) {
        if (featureImagesAdapter.itemCount == 0 || featureDotsAdapter.itemCount == 0) return

        if (!fromUser) {
            dataBinding.rvFeatured.smoothScrollToPosition(scrollingPos)
        }
        featureDotsAdapter.scrollToPos(scrollingPos)
        dataBinding.rvDots.scrollToPosition(scrollingPos)
        handler.postDelayed(this, 5000)
    }

    @Suppress("DEPRECATION")
    private fun getWindowHeightPx(activity: Activity): Int {
        return if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R) {
            val wm = activity.windowManager.currentWindowMetrics
            val insets = wm.windowInsets
                .getInsetsIgnoringVisibility(
                    WindowInsets.Type.systemBars() or WindowInsets.Type.displayCutout()
                )
            val bounds = wm.bounds
            // chiều cao khả dụng (trừ status/nav bar để UI chuẩn xác hơn)
            bounds.height() - insets.top - insets.bottom
        } else {
            val dm = DisplayMetrics()
            activity.windowManager.defaultDisplay.getMetrics(dm)
            dm.heightPixels
        }
    }

    private fun View.updateHeight(heightPx: Int) {
        layoutParams = layoutParams.apply { height = heightPx }
        requestLayout()
    }


    override fun onDestroy() {
        super.onDestroy()
        handler.removeCallbacks(this)
    }

    override fun onDestroyView() {
        super.onDestroyView()
        println("HomeFragment onDestroyView")
    }

    override fun onPause() {
        super.onPause()
        handler.removeCallbacks(this)

        // Fix: Lưu position hiện tại vào ViewModel khi thoát khỏi màn hình
        if (featureImagesAdapter.itemCount > 0) {
            val currentPosition =
                (dataBinding.rvFeatured.layoutManager as LinearLayoutManager?)?.findFirstCompletelyVisibleItemPosition()
                    ?: 0
            viewModel.savedFeaturedPosition =
                currentPosition.coerceIn(0, featureImagesAdapter.itemCount - 1)
        }
    }

    override fun onStop() {
        super.onStop()
        justPause = true
    }

    override fun onResume() {
        super.onResume()
        checkUpdate()

        // Fix: Enable click khi quay lại fragment
        wallpaperAdapter.enableClick()
        if (featureImagesAdapter.itemCount != 0) {
            // Fix: Khôi phục position đã lưu từ ViewModel hoặc sử dụng position hiện tại
            val savedPosition = viewModel.savedFeaturedPosition
            val currentPosition =
                (dataBinding.rvFeatured.layoutManager as LinearLayoutManager?)?.findFirstCompletelyVisibleItemPosition()
                    ?: 0

            // Sử dụng position đã lưu nếu có và hợp lệ, nếu không thì dùng position hiện tại
            scrollingPos =
                if (savedPosition >= 0 && savedPosition < featureImagesAdapter.itemCount) {
                    savedPosition
                } else {
                    currentPosition.coerceIn(0, featureImagesAdapter.itemCount - 1)
                }

            // Scroll đến position đã khôi phục
            dataBinding.rvFeatured.scrollToPosition(scrollingPos)

            // Đồng bộ dots với position đã khôi phục
            featureDotsAdapter.scrollToPos(scrollingPos)
            dataBinding.rvDots.scrollToPosition(scrollingPos)

            handler.postDelayed(this, 3000)
        }

        justPause = false
    }

    private fun buildIdSignature(list: List<SettingData.WallpapersItem>): Int {
        var acc = 1
        list.forEach { item ->
            acc = 31 * acc + (item.id ?: 0)
        }
        return 31 * acc + list.size
    }

    private fun checkUpdate() {
        if (CommonInfo.currentVersionApp > BuildConfig.VERSION_CODE) {
            updateNewVersionBottomSheet = UpdateNewVersionBottomSheet()
            updateNewVersionBottomSheet.isCancelable = !CommonInfo.isForceUpdate
            updateNewVersionBottomSheet.updateLanguage(context, localStorage.langCode)
            updateNewVersionBottomSheet.clickConfirmYes = {
                AppConfig.logEventTracking(Constants.BUNDLE_ANALYTICS_DIALOG_UPDATE_YES)
                context?.let { AppConfig.openApp(it) }
            }
            updateNewVersionBottomSheet.clickConfirmNo = {
                AppConfig.logEventTracking(Constants.BUNDLE_ANALYTICS_DIALOG_UPDATE_NO)
            }
            updateNewVersionBottomSheet.clickConfirmCancel = {
                AppConfig.logEventTracking(Constants.BUNDLE_ANALYTICS_DIALOG_UPDATE_CANCEL)
            }
            if (activity?.isFinishing == false) {
                activity?.supportFragmentManager?.let {
                    updateNewVersionBottomSheet.show(
                        it,
                        UpdateNewVersionBottomSheet.TAG
                    )
                }
            }
        }
    }
}