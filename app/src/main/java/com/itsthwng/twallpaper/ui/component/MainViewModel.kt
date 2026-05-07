package com.itsthwng.twallpaper.ui.component

import androidx.databinding.ObservableBoolean
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.viewModelScope
import com.itsthwng.twallpaper.ui.base.BaseViewModel
import com.itsthwng.twallpaper.utils.AppConfig
import com.itsthwng.twallpaper.utils.Constants
import com.itsthwng.twallpaper.utils.FirebaseHelper
import com.itsthwng.twallpaper.utils.Logger
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
open class MainViewModel @Inject constructor() : BaseViewModel() {

    var selectedTab: MutableLiveData<Int> = MutableLiveData(0)
    var selectedNav: MutableLiveData<Int> = MutableLiveData(-1)
    var navOpen: MutableLiveData<Boolean> = MutableLiveData(false)
    var isNavCatOpen: ObservableBoolean = ObservableBoolean(false)
    var coordinatedExpanded: MutableLiveData<Boolean> = MutableLiveData(false)

    fun fetchCategories() {
        viewModelScope.launch {
            try {
                val listCategories = FirebaseHelper.getCategories(localStorage.langCode)
                Logger.d("TopicViewModel", "Fetched categories: ${listCategories.size} items")
                localStorage.saveCategories(listCategories)
            } catch (e: Exception) {
                e.printStackTrace()
                // Handle error, maybe show a message to the user
            }
        }
    }

    fun updateNavOpen(open: Boolean) {
        navOpen.value = open
    }

    open fun onTabClick(i: Int) {
        if (i == 4) {
            try {
                val isFirstTimeClickMenu = localStorage.isFirstClickMenuMain
                if (isFirstTimeClickMenu) {
                    AppConfig.logEventTracking(Constants.EventKey.MAIN_SL_MENU_1ST)
                    localStorage.isFirstClickMenuMain = false
                } else {
                    AppConfig.logEventTracking(Constants.EventKey.MAIN_SL_MENU_2ND)
                }
            } catch (e: Exception) {
                Logger.e("LogEventTracking error: ${e.message}")
            }
            navOpen.value = true
            return
        }
        if (selectedTab.value != i) {
            navOpen.value = false
            selectedTab.value = i
        }
    }

    open fun onNavItemClick(i: Int) {
        if (i == 2) {
            isNavCatOpen.set(!isNavCatOpen.get())
            return
        }
        selectedNav.value = i
    }
}