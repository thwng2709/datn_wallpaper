package com.itsthwng.twallpaper.ui.base

import android.os.Bundle
import android.view.View
import androidx.databinding.DataBindingUtil
import androidx.databinding.ViewDataBinding
import com.itsthwng.twallpaper.extension.popFragment
import com.itsthwng.twallpaper.utils.Logger

abstract class BaseFragmentBinding<T : ViewDataBinding> : BaseFragment() {

    open lateinit var dataBinding: T

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        try {
            dataBinding = DataBindingUtil.bind(view)!!
            dataBinding.lifecycleOwner = this
        } catch (e: Exception) {
            Logger.e(e)
            popFragment()
            return
        }
        if (isInitialized) {
            super.onViewCreated(view, savedInstanceState)
        }
    }

    protected val isInitialized get() = this::dataBinding.isInitialized

}