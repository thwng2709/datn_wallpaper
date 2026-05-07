package com.itsthwng.twallpaper.ui.component.wallpaperByCat

import android.os.Bundle
import android.util.Log
import android.view.View
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.google.gson.Gson
import com.itsthwng.twallpaper.R
import com.itsthwng.twallpaper.data.model.SettingData
import com.itsthwng.twallpaper.databinding.ActivityWallpaperByCatBinding
import com.itsthwng.twallpaper.extension.toArrayList
import com.itsthwng.twallpaper.local.LocalStorage
import com.itsthwng.twallpaper.ui.base.BaseActivityBinding
import com.itsthwng.twallpaper.ui.component.wallpaperByCat.viewModel.WallpaperByCatViewModel
import com.itsthwng.twallpaper.utils.AppConfig
import com.itsthwng.twallpaper.utils.Constants
import com.itsthwng.twallpaper.utils.Global
import dagger.hilt.android.AndroidEntryPoint
import io.reactivex.disposables.CompositeDisposable
import kotlinx.coroutines.launch
import javax.inject.Inject

@AndroidEntryPoint
class WallpaperByCatActivity: BaseActivityBinding<ActivityWallpaperByCatBinding, WallpaperByCatViewModel>() {
    
    @Inject
    lateinit var localStorage: LocalStorage
    
    var category: SettingData.CategoriesItem? = null
    var noMoreData = false
    
    var disposable = CompositeDisposable()

    override fun getContentViewId(): Int {
        return R.layout.activity_wallpaper_by_cat
    }

    override fun initializeViews() {}

    override fun registerListeners() {}

    override fun initializeData() {}

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        initView()
        initListeners()
        initObservers()
        logTracking()
        dataBinding.model = viewModel
    }

    private fun initObservers() {
        lifecycleScope.launch {
            launch {
                viewModel.showInitialLoading.collect {
                    dataBinding.sihmmer.visibility = if (it) View.VISIBLE else View.GONE
                }
            }
            launch {
                viewModel.showLoadMore.collect {
                    dataBinding.progressLoadMore.visibility = if (it) View.VISIBLE else View.GONE
                }
            }
        }
        lifecycleScope.launch {
            launch {
                viewModel.items.collect { list ->
                    val currentCount = viewModel.wallpaperAdapter.itemCount
                    when {
                        list.isEmpty() -> {
                            dataBinding.tvNoData.visibility = View.VISIBLE
                        }
                        
                        currentCount == 0 -> {
                            dataBinding.tvNoData.visibility = View.GONE
                            viewModel.wallpaperAdapter.updateData(list.toMutableList())
                        }
                        
                        list.size > currentCount -> {
                            val delta = list.subList(currentCount, list.size)
                            viewModel.wallpaperAdapter.loadMore(delta.toMutableList())
                        }
                        
                        else -> {
                            // dữ liệu giữ nguyên
                        }
                    }
                }
            }
            launch {
                viewModel.favoriteWalls.collect { likedList ->
                    if (likedList.isEmpty()) return@collect
                    localStorage.favourites =
                        Global.listOfIntegerToString(likedList.filter { it.id != null }
                            .map { it.id!! }) ?: ""
                    refreshFavList()
                }
            }
            launch {
                viewModel.wallsByCategory.collect { allDataList ->
                    viewModel.updateFullDataList(allDataList)
                    viewModel.updateAccessType(allDataList)
                }
            }
        }
    }

    var linearLayoutManager: LinearLayoutManager? = null
    var isLoading = false

    private fun initListeners() {

        dataBinding.rvImageByCategory.addOnScrollListener(object : RecyclerView.OnScrollListener() {
            override fun onScrollStateChanged(recyclerView: RecyclerView, newState: Int) {
                super.onScrollStateChanged(recyclerView, newState)
                if (newState == RecyclerView.SCROLL_STATE_IDLE) {
                    linearLayoutManager = recyclerView.layoutManager as LinearLayoutManager?
                    if (viewModel.wallpaperAdapter.itemCount - 1 === linearLayoutManager?.findLastVisibleItemPosition() && !isLoading) {
                        viewModel.loadNextPage(initial = false)
                    }
                }
            }
        })

        dataBinding.btnBack.setOnClickListener {
            onBackPressedDispatcher.onBackPressed()
        }
    }

    override fun onDestroy() {
        disposable.dispose()
        super.onDestroy()
    }

    private fun refreshFavList() {
        viewModel.wallpaperAdapter.refreshData(
            Global.convertStringToLis(
                localStorage.favourites
            )
        )
        Log.i(" onnnn set wall by cat", ": ${viewModel.wallpaperAdapter.favList}")
    }

    override fun onResume() {
        super.onResume()
    }

    private fun logTracking() {
        if (localStorage.isFirstOpenWallpaperByCat) {
            localStorage.isFirstOpenWallpaperByCat = false
            AppConfig.logEventTracking(Constants.EventKey.WALLPAPER_BY_CAT_OPEN_1ST)
        } else {
            AppConfig.logEventTracking(Constants.EventKey.WALLPAPER_BY_CAT_OPEN_2ND)
        }
    }
    
    private fun initView() {
        
        dataBinding.progressLoadMore.visibility = View.GONE
        
        val s = intent.getStringExtra(Constants.data)
        if (s != null) {
            category = Gson().fromJson(s, SettingData.CategoriesItem::class.java)
            viewModel.category = category
            viewModel.setCategoryId(category?.id)
        } else {
            dataBinding.tvNoData.visibility = View.VISIBLE
        }
        
        category?.let {
            
            viewModel.wallpaperAdapter.favList =
                Global.convertStringToLis(localStorage.favourites).toArrayList()
            
            viewModel.wallpaperAdapter.onFavClick = {
                viewModel.updateWallpaper(it)
            }
            viewModel.start(categoryId = it.id)
        }
    }
    
    companion object {
        private const val TAG = "WallpaperByCatActivity"
    }
}