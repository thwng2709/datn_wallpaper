package com.itsthwng.twallpaper.extension

import androidx.fragment.app.Fragment
import com.itsthwng.twallpaper.ui.base.BaseActivity
import kotlin.reflect.KClass

fun Fragment.popFragment(clazz: KClass<out Fragment>? = null, animate: Boolean = false): Boolean {
    val activity = activity as? BaseActivity ?: return false
    if (clazz == null) {
        return activity.navigation.popFragment(this, animate)
    }
    return activity.navigation.popFragment(clazz = clazz, animate = animate)
}