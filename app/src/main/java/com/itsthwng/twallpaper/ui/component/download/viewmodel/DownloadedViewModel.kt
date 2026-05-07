package com.itsthwng.twallpaper.ui.component.download.viewmodel

import androidx.lifecycle.viewModelScope
import com.itsthwng.twallpaper.data.model.SettingData
import com.itsthwng.twallpaper.repository.WallpapersRepository
import com.itsthwng.twallpaper.ui.base.BaseViewModel
import com.itsthwng.twallpaper.ui.component.home.adapter.WallpaperAdapter
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class DownloadedViewModel @Inject constructor(
    private val wallpapersRepository: WallpapersRepository
): BaseViewModel() {

    var wallpaperAdapter = WallpaperAdapter()

    // Downloaded
    val downloadedWalls: StateFlow<List<SettingData.WallpapersItem>> =
        wallpapersRepository.observeDownloadedWallpapers()
            .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), emptyList())

    fun updateWallpaper(item: SettingData.WallpapersItem) {
        viewModelScope.launch(Dispatchers.IO) {
            wallpapersRepository.updateWallpaper(item)
        }
    }

    // Favorites
    val favoriteWalls: StateFlow<List<SettingData.WallpapersItem>> =
        wallpapersRepository.observeFavoriteWallpapers()
            .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), emptyList())
}