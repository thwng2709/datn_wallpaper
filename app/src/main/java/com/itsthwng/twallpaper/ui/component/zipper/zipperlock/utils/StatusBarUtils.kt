package com.itsthwng.twallpaper.ui.component.zipper.zipperlock.utils

import android.app.Activity
import com.itsthwng.twallpaper.R

object StatusBarUtils {
    @JvmStatic
    fun setStatusBarColor(activity: Activity) {
        val window = activity.window
        window.addFlags(Int.MIN_VALUE)
        window.statusBarColor = activity.resources.getColor(R.color.statusBarColor)
    }
}