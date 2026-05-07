package com.itsthwng.twallpaper.ui.base

import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.itsthwng.twallpaper.local.LocalStorage
import com.itsthwng.twallpaper.repository.FileHelper
import io.reactivex.disposables.CompositeDisposable
import javax.inject.Inject

open class BaseViewModel : ViewModel() {

    @Inject
    lateinit var localStorage: LocalStorage

    @Inject
    lateinit var fileHelper: FileHelper

    open val composite = CompositeDisposable()
    private val _toastLiveData = MutableLiveData<Any>()
    val toastLiveData get() = _toastLiveData

    override fun onCleared() {
        composite.dispose()
        super.onCleared()
    }

    fun showToast(any: Any) {
        _toastLiveData.postValue(any)
    }

}