package com.itsthwng.twallpaper.ui.component.home.viewmodel


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
class FavouriteViewModel @Inject constructor(
    private val wallpapersRepository: WallpapersRepository
) : BaseViewModel() {

    var wallpaperAdapter = WallpaperAdapter()

    // Favorites
    val favoriteWalls: StateFlow<List<SettingData.WallpapersItem>?> =
        wallpapersRepository.observeFavoriteWallpapers()
            .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), null)

    fun updateWallpaper(item: SettingData.WallpapersItem) {
        viewModelScope.launch(Dispatchers.IO) {
            wallpapersRepository.updateWallpaper(item)
        }
    }
}