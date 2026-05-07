package com.itsthwng.twallpaper.ui.component.history.viewmodel

import androidx.lifecycle.viewModelScope
import com.itsthwng.twallpaper.data.model.SettingData
import com.itsthwng.twallpaper.data.model.isZipper
import com.itsthwng.twallpaper.repository.WallpapersRepository
import com.itsthwng.twallpaper.ui.base.BaseViewModel
import com.itsthwng.twallpaper.ui.component.history.adapter.HistoryWallpaperAdapter
import com.itsthwng.twallpaper.ui.component.history.model.EmptyStateMessage
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class HistoryViewModel @Inject constructor(
    private val wallpapersRepository: WallpapersRepository
) : BaseViewModel() {
    

    enum class FilterType {
        ALL, HOME, LOCK
    }

    private val _filterType = MutableStateFlow(FilterType.ALL)
    val filterType: StateFlow<FilterType> = _filterType.asStateFlow()
    
    private val _searchQuery = MutableStateFlow("")
    val searchQuery: StateFlow<String> = _searchQuery.asStateFlow()
    
    private val _isLoading = MutableStateFlow(false)
    val isLoading: StateFlow<Boolean> = _isLoading.asStateFlow()

    val favoriteWalls: StateFlow<List<SettingData.WallpapersItem>> =
        wallpapersRepository.observeFavoriteWallpapers()
            .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), emptyList())

    val wallpaperAdapter: HistoryWallpaperAdapter = HistoryWallpaperAdapter().apply {
        onFavClick = { wallpaper ->
            updateWallpaper(wallpaper.copy(isFavorite = !wallpaper.isFavorite))
        }
        onDeleteClick = { wallpaper ->
            // Delete will be handled by the fragment for undo functionality
        }
    }
    
    val historyWalls: StateFlow<List<SettingData.WallpapersItem>> = 
        combine(
            wallpapersRepository.observeHistoryWallpapers(),
            _filterType,
            _searchQuery
        ) { wallpapers, filter, query ->
            var filtered = when (filter) {
                FilterType.ALL -> wallpapers
                FilterType.HOME -> wallpapers.filter { it.isSelectedHome == 3 || it.isSelectedHome == 1 }
                FilterType.LOCK -> wallpapers.filter { it.isSelectedLock == 3 || it.isSelectedLock == 1 }
            }
            
            if (query.isNotEmpty()) {
                filtered = filtered.filter { wallpaper ->
                    wallpaper.name?.contains(query, ignoreCase = true) == true ||
                    wallpaper.tags?.contains(query, ignoreCase = true) == true ||
                    wallpaper.categoryId?.contains(query, ignoreCase = true) == true
                }
            }
            
            filtered
        }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), emptyList())
    
    // Track empty state type
    val emptyStateType: StateFlow<EmptyStateMessage?> = 
        combine(
            historyWalls,
            wallpapersRepository.observeHistoryWallpapers(),
            _filterType,
            _searchQuery
        ) { filtered, allHistory, filter, query ->
            when {
                filtered.isNotEmpty() -> null
                query.isNotEmpty() -> EmptyStateMessage.SearchEmpty
                filter != FilterType.ALL && allHistory.isNotEmpty() -> EmptyStateMessage.FilteredEmpty
                else -> EmptyStateMessage.NeverSetWallpaper
            }
        }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), null)

    init {
        // Show loading initially
        viewModelScope.launch {
            _isLoading.value = true
            // Wait for first data emission
            historyWalls.collect { 
                _isLoading.value = false
                return@collect // Stop after first emission
            }
        }
    }

    fun loadHistoryWallpapers() {
        // No longer needed - using Flow directly from repository
    }

    fun setFilterType(type: FilterType) {
        _filterType.value = type
    }
    
    fun setSearchQuery(query: String) {
        _searchQuery.value = query
    }

    fun updateWallpaper(item: SettingData.WallpapersItem) {
        viewModelScope.launch(Dispatchers.IO) {
            wallpapersRepository.updateWallpaper(item)
        }
    }
    
    fun removeFromHistory(id: Int, isZipper: Boolean = false) {
        viewModelScope.launch(Dispatchers.IO) {
            wallpapersRepository.removeFromHistory(id, isZipper)
        }
    }
    
    fun restoreToHistory(wallpaper: SettingData.WallpapersItem) {
        viewModelScope.launch(Dispatchers.IO) {
            // Update the wallpaper to mark it as viewed/in history
            val updatedWallpaper = wallpaper.copy(
                isSelectedHome = if (wallpaper.isSelectedHome == 3) 3 else wallpaper.isSelectedHome,
                isSelectedLock = if (wallpaper.isSelectedLock == 3) 3 else wallpaper.isSelectedLock
            )
            // If it wasn't in history (no 3 status), mark it as viewed
            if (wallpaper.isSelectedHome != 3 && wallpaper.isSelectedLock != 3) {
                wallpapersRepository.updateWallpaper(updatedWallpaper.copy(isSelectedHome = 3))
            } else {
                wallpapersRepository.updateWallpaper(updatedWallpaper)
            }
        }
    }
    
    fun clearAllHistory() {
        viewModelScope.launch(Dispatchers.IO) {
            historyWalls.value.forEach { wallpaper ->
                val isZipper = wallpaper.isZipper()
                wallpapersRepository.removeFromHistory(wallpaper.id ?: 0, isZipper)
            }
        }
    }
    
    
    fun toggleFavorite(wallpaper: SettingData.WallpapersItem) {
        viewModelScope.launch(Dispatchers.IO) {
            // Toggle favorite status, preserve history status
            val updatedWallpaper = wallpaper.copy(isFavorite = !wallpaper.isFavorite)
            wallpapersRepository.updateWallpaper(updatedWallpaper)
        }
    }
}