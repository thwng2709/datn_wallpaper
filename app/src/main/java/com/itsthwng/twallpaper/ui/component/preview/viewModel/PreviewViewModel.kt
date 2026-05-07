package com.itsthwng.twallpaper.ui.component.preview.viewModel

import androidx.lifecycle.viewModelScope
import com.itsthwng.twallpaper.data.entity.ZipperImageEntity
import com.itsthwng.twallpaper.data.model.SettingData
import com.itsthwng.twallpaper.repository.WallpapersRepository
import com.itsthwng.twallpaper.repository.ZipperRepository
import com.itsthwng.twallpaper.ui.base.BaseViewModel
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class PreviewViewModel @Inject constructor(
    private val wallpaperRepository: WallpapersRepository,
    private val zipperRepository: ZipperRepository
): BaseViewModel() {

    fun updateSelectedWallpaper(wallpapersItem: SettingData.WallpapersItem, type: Int) {
        viewModelScope.launch(Dispatchers.IO) {
            wallpaperRepository.updateSelectedWallpaper(wallpapersItem, type)
        }
    }

    fun updateWallpaper(item: SettingData.WallpapersItem) {
        viewModelScope.launch(Dispatchers.IO) {
            wallpaperRepository.updateWallpaper(item)
        }
    }

    fun updateDownloadedWallpaperStatus(id: Int, isDownloaded: Long) {
        viewModelScope.launch(Dispatchers.IO) {
            wallpaperRepository.updateWallpaperDownloadStatus(id, isDownloaded)
        }
    }

    fun updateSelectedZipper(zipper: ZipperImageEntity, type: Int) {
        viewModelScope.launch(Dispatchers.IO) {
            zipperRepository.updateSelectedZipper(zipper, type)
        }
    }
}