package com.itsthwng.twallpaper.ui.base

interface BaseView {
    fun getContentViewId(): Int

    fun initializeViews()

    fun registerListeners()

    fun initializeData()
}