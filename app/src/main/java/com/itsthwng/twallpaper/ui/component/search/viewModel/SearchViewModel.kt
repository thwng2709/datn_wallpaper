package com.itsthwng.twallpaper.ui.component.search.viewModel

import androidx.lifecycle.viewModelScope
import com.itsthwng.twallpaper.data.model.SettingData
import com.itsthwng.twallpaper.repository.WallpapersRepository
import com.itsthwng.twallpaper.ui.base.BaseViewModel
import com.itsthwng.twallpaper.ui.component.home.adapter.WallpaperAdapter
import com.itsthwng.twallpaper.utils.Constants
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class SearchViewModel @Inject constructor(
    private val wallpapersRepository: WallpapersRepository
): BaseViewModel() {

    var wallpaperAdapter = WallpaperAdapter()

    // --- State ---
    private val pageSize = Constants.PAGINATION_COUNT // ví dụ 20
    private var page = 1

    // query & filter
    private val _searchWord = MutableStateFlow("")
    val searchWord: StateFlow<String> = _searchWord

    // 2==all, 0=static images, 1=live images
    private val _filterType = MutableStateFlow(2)
    val filterType: StateFlow<Int> = _filterType

    // Kết quả tích lũy (append 20–20)
    private val _items = MutableStateFlow<List<SettingData.WallpapersItem>>(emptyList())
    val items: StateFlow<List<SettingData.WallpapersItem>> = _items

    // Loading flags cho UI
    private val _isInitialLoading = MutableStateFlow(false)
    val isInitialLoading: StateFlow<Boolean> = _isInitialLoading

    private val _isLoadMore = MutableStateFlow(false)
    val isLoadMore: StateFlow<Boolean> = _isLoadMore

    private val _noMoreData = MutableStateFlow(false)

    // Favorites observe (giữ như bạn đang có)
    val favoriteWalls: StateFlow<List<SettingData.WallpapersItem>> =
        wallpapersRepository.observeFavoriteWallpapers()
            .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), emptyList())

    // --- Public API ---
    /** Gọi khi người dùng nhập từ khóa mới */
    fun onQueryChanged(newQuery: String) {
        val q = newQuery.trim()
        if (q == _searchWord.value) return
        startSearch(q, _filterType.value)
    }

    /** Gọi khi người dùng đổi filter (2=all, 0=static, 1=live) */
    fun onFilterChanged(newFilter: Int) {
        if (newFilter == _filterType.value) return
        startSearch(_searchWord.value, newFilter)
    }

    /** Bắt đầu search mới (reset paging) */
    fun startSearch(query: String, filter: Int = 2) {
        _searchWord.value = query
        _filterType.value = filter

        // reset state
        page = 1
        _noMoreData.value = false
        _items.value = emptyList()
        
        // Clear fullDataList để không dùng data cũ khi search mới
        wallpaperAdapter.clearFullDataList()

        // nếu rỗng -> để UI show empty
        if (query.isBlank()) {
            _isInitialLoading.value = false
            _isLoadMore.value = false
            return
        }

        loadNextPage(initial = true)
    }

    /** Gọi khi cuộn gần cuối để lấy 20 item tiếp theo */
    fun loadNextPage(initial: Boolean = false) {
        if (_noMoreData.value || _isInitialLoading.value || _isLoadMore.value) return
        if (_searchWord.value.isBlank()) return

        if (initial) _isInitialLoading.value = true else _isLoadMore.value = true

        viewModelScope.launch(Dispatchers.IO) {
            try {
                val wallpaperType: Int? = _filterType.value.let { ft -> if (ft != 2) ft else null }
                val data = wallpapersRepository.searchPage(
                    keyword = _searchWord.value,
                    wallpaperType = wallpaperType,
                    page = page,
                    pageSize = pageSize
                )

                // update UI state
                val merged = _items.value + data
                _items.value = merged

                if (data.size < pageSize) {
                    _noMoreData.value = true
                } else {
                    page += 1
                }
            } catch (t: Throwable) {
                t.printStackTrace()
                // tuỳ bạn: có thể đặt _noMoreData=false để cho phép retry
            } finally {
                if (initial) _isInitialLoading.value = false else _isLoadMore.value = false
            }
        }
    }

    fun updateWallpaper(item: SettingData.WallpapersItem) {
        viewModelScope.launch(Dispatchers.IO) {
            wallpapersRepository.updateWallpaper(item)
        }
    }
}