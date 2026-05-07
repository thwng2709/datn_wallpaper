package com.itsthwng.twallpaper.ui.base

import android.os.Build
import android.os.Bundle
import android.view.View
import android.view.WindowInsets
import android.view.WindowInsetsController
import androidx.core.content.ContextCompat
import androidx.core.view.ViewCompat
import androidx.databinding.DataBindingUtil
import androidx.databinding.ViewDataBinding
import androidx.lifecycle.ViewModelProvider
import java.lang.reflect.ParameterizedType

abstract class BaseActivityBinding<T : ViewDataBinding, V : BaseViewModel> : BaseActivity() {

    open lateinit var dataBinding: T
    open lateinit var viewModel: V

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        try {
            dataBinding = DataBindingUtil.bind(view)!!
            dataBinding.lifecycleOwner = this
            @Suppress("UNCHECKED_CAST")
            val clazz: Class<V> =
                (this.javaClass.genericSuperclass as ParameterizedType).actualTypeArguments[1] as Class<V>
            viewModel = ViewModelProvider(this).get(clazz)
        } catch (e: Exception) {
            finish()
            return
        }

        viewModel.toastLiveData.observe(this) {
            if (it is Int) {
                showToast(it)
            } else {
                showToast(it.toString())
            }
        }
        onViewCreated(savedInstanceState)
        val decorView = window.decorView
        decorView.setOnApplyWindowInsetsListener { v: View, insets: WindowInsets? ->
            val defaultInsets = v.onApplyWindowInsets(insets)
            defaultInsets.replaceSystemWindowInsets(
                defaultInsets.systemWindowInsetLeft,
                0,
                defaultInsets.systemWindowInsetRight,
                defaultInsets.systemWindowInsetBottom
            )
        }
        ViewCompat.requestApplyInsets(decorView)
        window.statusBarColor = ContextCompat.getColor(this, android.R.color.transparent)
    }

    open fun onViewCreated(savedInstanceState: Bundle?) {
    }

    fun hideSystemBars() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R) {
            window.insetsController?.let {
                it.hide(WindowInsets.Type.statusBars() or WindowInsets.Type.navigationBars())
                it.systemBarsBehavior =
                    WindowInsetsController.BEHAVIOR_SHOW_TRANSIENT_BARS_BY_SWIPE
            }
        } else {
            @Suppress("DEPRECATION")
            window.decorView.systemUiVisibility =
                View.SYSTEM_UI_FLAG_IMMERSIVE_STICKY or
                        View.SYSTEM_UI_FLAG_FULLSCREEN or
                        View.SYSTEM_UI_FLAG_HIDE_NAVIGATION or
                        View.SYSTEM_UI_FLAG_LAYOUT_FULLSCREEN or
                        View.SYSTEM_UI_FLAG_LAYOUT_HIDE_NAVIGATION or
                        View.SYSTEM_UI_FLAG_LAYOUT_STABLE
        }
    }

    override fun onWindowFocusChanged(hasFocus: Boolean) {
        super.onWindowFocusChanged(hasFocus)
        hideSystemBars()
    }

    override fun onResume() {
        super.onResume()
        hideSystemBars()
    }

    override fun onDestroy() {
        if (this::dataBinding.isInitialized)
            dataBinding.unbind()
        super.onDestroy()
    }

    protected val isInitialized get() = this::dataBinding.isInitialized && this::viewModel.isInitialized

}