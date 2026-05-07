package com.itsthwng.twallpaper.ui.component.home.viewmodel

import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.viewModelScope
import com.itsthwng.twallpaper.data.model.SettingData
import com.itsthwng.twallpaper.repository.CategoriesRepository
import com.itsthwng.twallpaper.ui.base.BaseViewModel
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.stateIn
import javax.inject.Inject

@HiltViewModel
class CategoryViewModel @Inject constructor(
    private val categoriesRepository: CategoriesRepository
) : BaseViewModel() {

    var selected: MutableLiveData<Int> = MutableLiveData(1)

    // Categories
    val categories: StateFlow<List<SettingData.CategoriesItem>?> =
        categoriesRepository.observeCategories()
            .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), null)


    fun setSelectedType(i: Int) {
        selected.value = i
    }
}