package com.itsthwng.twallpaper.ui.component.wallpaperByCat.viewModel

import androidx.lifecycle.viewModelScope
import com.itsthwng.twallpaper.data.model.SettingData
import com.itsthwng.twallpaper.repository.WallpapersRepository
import com.itsthwng.twallpaper.ui.base.BaseViewModel
import com.itsthwng.twallpaper.ui.component.home.adapter.WallpaperAdapter
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import javax.inject.Inject
import kotlin.text.lowercase

@HiltViewModel
class WallpaperByCatViewModel @Inject constructor(
    private val wallpapersRepository: WallpapersRepository
): BaseViewModel() {
    var category: SettingData.CategoriesItem? = null

    var wallpaperAdapter = WallpaperAdapter()

    // Favorites
    val favoriteWalls: StateFlow<List<SettingData.WallpapersItem>> =
        wallpapersRepository.observeFavoriteWallpapers()
            .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), emptyList())

    fun updateWallpaper(item: SettingData.WallpapersItem) {
        viewModelScope.launch(Dispatchers.IO) {
            wallpapersRepository.updateWallpaper(item)
        }
    }
    
    fun updateFullDataList(list: List<SettingData.WallpapersItem>) {
        wallpaperAdapter.updateFullDataList(list)
    }

    fun updateAccessType(allList: List<SettingData.WallpapersItem>) {
        wallpaperAdapter.updateAccessTypeData(allList)
    }

    // Category đang chọn (null = xem tất cả)
    private val _selectedCategoryId = MutableStateFlow<String?>(null)
    val selectedCategoryId: StateFlow<String?> get() = _selectedCategoryId

    fun setCategoryId(id: String?) { _selectedCategoryId.value = id }

    // Wallpapers theo category
    val wallsByCategory: StateFlow<List<SettingData.WallpapersItem>> =
        _selectedCategoryId
            .flatMapLatest { id ->
                if (id == null || id.lowercase() == "new") wallpapersRepository.observeNewWallpapers()
                else wallpapersRepository.observeWallpapersOrdered(id)
            }
            .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), emptyList())

    private val _items = MutableStateFlow<List<SettingData.WallpapersItem>>(emptyList())
    val items: StateFlow<List<SettingData.WallpapersItem>> = _items

    private val pageSize = 20
    private var page = 1
    private var isLoading = false
    private var noMore = false
    private var currentCategoryId: String? = null

    private val _showInitialLoading = MutableStateFlow(false)
    val showInitialLoading: StateFlow<Boolean> = _showInitialLoading

    private val _showLoadMore = MutableStateFlow(false)
    val showLoadMore: StateFlow<Boolean> = _showLoadMore

    fun start(categoryId: String?) {
        // đổi category → reset
        if (currentCategoryId != categoryId) {
            currentCategoryId = categoryId
            page = 1
            noMore = false
            _items.value = emptyList()
            loadNextPage(initial = true)
        }
    }

    fun loadNextPage(initial: Boolean = false) {
        if (isLoading || noMore) return
        isLoading = true
        if (initial) _showInitialLoading.value = true else _showLoadMore.value = true

        viewModelScope.launch(Dispatchers.IO) {
            runCatching {
                wallpapersRepository.loadPage(currentCategoryId, page, pageSize)
            }.onSuccess { pageData ->
                val merged = _items.value + pageData
                _items.value = merged
                if (pageData.size < pageSize) noMore = true else page += 1
            }.onFailure {
                // có thể set noMore=false để cho retry
            }
            isLoading = false
            _showInitialLoading.value = false
            _showLoadMore.value = false
        }
    }

}