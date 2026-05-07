package com.itsthwng.twallpaper.ui.component.home.viewmodel

import androidx.lifecycle.viewModelScope
import com.itsthwng.twallpaper.data.model.SettingData
import com.itsthwng.twallpaper.repository.CategoriesRepository
import com.itsthwng.twallpaper.repository.WallpapersRepository
import com.itsthwng.twallpaper.ui.base.BaseViewModel
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class HomeViewModel
@Inject constructor(
    private val categoriesRepository: CategoriesRepository,
    private val wallpapersRepository: WallpapersRepository
) : BaseViewModel() {


    // Fix: Lưu position của featured để khôi phục khi quay về
    var savedFeaturedPosition: Int = 0


    // Categories
    val categories: StateFlow<List<SettingData.CategoriesItem>> =
        categoriesRepository.observeCategories()
            .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), emptyList())

    // Tất cả wallpapers
    val allWalls: StateFlow<List<SettingData.WallpapersItem>> =
        wallpapersRepository.observeWallpapers()
            .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), emptyList())

    // Category đang chọn (null = xem tất cả)
    private val _selectedCategoryId = MutableStateFlow<String?>(null)
    val selectedCategoryId: StateFlow<String?> get() = _selectedCategoryId

    fun setCategory(id: String?) {
        _selectedCategoryId.value = id
    }

    // Wallpapers theo category (nếu id = null hoặc "new" -> lấy danh sách "mới")
    val wallsByCategory: StateFlow<List<SettingData.WallpapersItem>?> =
        _selectedCategoryId
            .flatMapLatest { id ->
                if (id == null || id.lowercase() == "new") {
                    wallpapersRepository.observeNewWallpapers()
                } else {
                    wallpapersRepository.observeWallpapersOrdered(id)
                }
            }
            .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), null)

    // Featured
    val featuredWalls: StateFlow<List<SettingData.WallpapersItem>?> =
        wallpapersRepository.observeRandomFeaturedWallpapers()
            .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), null)

    // Favorites
    val favoriteWalls: StateFlow<List<SettingData.WallpapersItem>> =
        wallpapersRepository.observeFavoriteWallpapers()
            .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), emptyList())

    // d) Refresh thủ công (khi bấm Refresh, có mạng)
    fun onManualRefresh() {
        // Tối thiểu: re-emit category hiện tại để flatMapLatest chạy lại.
        _selectedCategoryId.value = _selectedCategoryId.value
    }

    fun updateWallpaper(item: SettingData.WallpapersItem) {
        viewModelScope.launch(Dispatchers.IO) {
            wallpapersRepository.updateWallpaper(item)
        }
    }


    fun prewarmCategory(categoryId: String) {
        viewModelScope.launch(Dispatchers.IO) {
            runCatching {
                // loadPage will also seed repository ordered cache for this category.
                wallpapersRepository.loadPage(categoryId = categoryId, page = 1, pageSize = 1)
            }
        }
    }


    companion object {
        const val TAG = "HomeViewModel"
    }
}