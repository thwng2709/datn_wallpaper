package com.itsthwng.twallpaper.ui.base

import android.os.Bundle
import androidx.fragment.app.Fragment
import kotlin.reflect.KClass

interface NavigationController {

    val peek: String?

    fun popToRoot(animate: Boolean = true)

    /**
     * nếu trả về bằng true thì đã pop fragment thành công và ngược lại là false
     */
    fun popFragment(
        clazz: KClass<out Fragment>? = null, tag: String? = null, animate: Boolean = false
    ): Boolean

    /**
     * nếu trả về bằng true thì đã pop fragment thành công và ngược lại là false
     */
    fun popFragment(fragment: Fragment, animate: Boolean): Boolean

    fun popFragment2(
        fragment: Fragment? = null,
        clazz: KClass<out Fragment>? = null,
        tag: String? = null,
        animateRightOrLeft: Boolean
    ): Boolean

    fun pushFragment(
        fragment: Fragment,
        bundle: Bundle? = null,
        tag: String? = null,
        animate: Boolean = true,
        viewId: Int = 0,
        singleton: Boolean = false,
        parentTag: String? = null
    )

    fun pushFragment(
        clazz: KClass<out Fragment>,
        bundle: Bundle? = null,
        tag: String? = null,
        animate: Boolean = true,
        viewId: Int = 0,
        singleton: Boolean = false
    )

    fun addFragment(
        viewId: Int,
        clazz: KClass<out Fragment>,
        bundle: Bundle? = null,
        tag: String? = null,
        singleton: Boolean = false
    )

    fun addFragment(
        viewId: Int, fragment: Fragment, bundle: Bundle?, tag: String?, singleton: Boolean
    )

    fun removeFragment(fragment: Fragment)
    fun removeFragment(tag: String)

    fun removeFragment(clazz: KClass<out Fragment>)

    fun replaceFragment(
        viewId: Int, fragment: Fragment, tag: String? = null, parentTag: String? = null
    )

    fun replaceFragment(
        viewId: Int, clazz: KClass<out Fragment>, tag: String? = null, parentTag: String? = null
    )

    fun <T : Fragment> findFragment(clazz: KClass<T>, tag: String? = null): T?

    val topFragment: Fragment?
    val isEmptyFragments: Boolean
    val sizeListFragments: Int

    companion object {
        const val PUSH_ANIMATE_FRAGMENT_TAG = "PUSH_ANIMATE_FRAGMENT_TAG"
        const val FRAGMENT_NAME_TAG = "FRAGMENT_NAME_TAG"
        const val PARENT_FRAGMENT_NAME_TAG = "PARENT_FRAGMENT_NAME_TAG"

    }

}