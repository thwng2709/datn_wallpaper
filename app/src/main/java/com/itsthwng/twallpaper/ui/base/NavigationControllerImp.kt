package com.itsthwng.twallpaper.ui.base

import android.os.Bundle
import android.os.Handler
import android.os.Looper
import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentManager
import androidx.fragment.app.FragmentTransaction
import com.itsthwng.twallpaper.R
import com.itsthwng.twallpaper.utils.Logger
import java.util.*
import kotlin.reflect.KClass

class NavigationControllerImp constructor(private val fragmentManager: FragmentManager) :
    NavigationController {

    private val listTagFragments = ArrayDeque<String>()
    var callback: NavigationCallback? = null

    /**
     * Retrieves, but does not remove, the head of the queue represented by
     * this deque, or returns {@code null} if this deque is empty.
     */

    override val peek: String?
        get() = listTagFragments.peek()

    /**
     * Retrieves and removes the head of the queue represented by this deque
     * (in other words, the first element of this deque), or returns
     * {@code null} if this deque is empty.
     */

    val poll: String?
        get() = listTagFragments.poll()

    override fun popToRoot(animate: Boolean) {
        val first = peek
        var last = peek
        while (last != null) {
            popFragment(tag = last, animate = false)
            last = peek
        }
        popFragment(tag = first, animate = animate)
    }

    fun popTopFragment(animate: Boolean = true): Boolean {
        val tag = peek ?: return false
        return popFragment(tag = tag, animate = animate)
    }

    override fun popFragment(
        clazz: KClass<out Fragment>?, tag: String?, animate: Boolean
    ): Boolean {
        var fragment: Fragment? = null
        if (clazz != null) {
            val n = clazz.java.name
            fragment = fragmentManager.fragments.lastOrNull() { it.javaClass.name == n }
        }
        if (fragment == null) {
            fragment = fragmentManager.findFragmentByTag(tag ?: peek) ?: return false
        }
        return popFragment(fragment, animate)
    }

    override fun popFragment(fragment: Fragment, animate: Boolean): Boolean {
        val tag = fragment.arguments?.getString(NavigationController.FRAGMENT_NAME_TAG)
        val removed = listTagFragments.remove(tag)
        //	if (!removed) return false
        val trans = fragmentManager.beginTransaction().disallowAddToBackStack()
        try {
            if (animate || fragment.arguments?.getBoolean(NavigationController.PUSH_ANIMATE_FRAGMENT_TAG) == true) {
                trans.setCustomAnimations(R.anim.slide_in_right, R.anim.slide_out_right)
            }
            trans.remove(fragment).commitNowAllowingStateLoss()
            callback?.didRemoveFragment(fragment)
        } catch (e: Exception) {
            Handler(Looper.getMainLooper()).post {
                try {
                    trans.remove(fragment).commitNowAllowingStateLoss()
                    callback?.didRemoveFragment(fragment)
                } catch (e: Exception) {
                }
            }
        }
        return true
    }

    override fun popFragment2(
        fragment: Fragment?, clazz: KClass<out Fragment>?, tag: String?, animateRightOrLeft: Boolean
    ): Boolean {
        var fg = fragment
        if (fg == null && clazz != null) {
            val n = clazz.java.name
            fg = fragmentManager.fragments.lastOrNull() { it.javaClass.name == n }
        }
        if (fg == null) {
            fg = fragmentManager.findFragmentByTag(tag ?: peek) ?: return false
        }
        if (!listTagFragments.remove(fg.arguments?.getString(NavigationController.FRAGMENT_NAME_TAG))) return false
        val trans = fragmentManager.beginTransaction().disallowAddToBackStack()
        try {
            if (animateRightOrLeft) {
                trans.setCustomAnimations(R.anim.slide_in_right, R.anim.slide_out_right)
            } else {
                trans.setCustomAnimations(R.anim.slide_in_left, R.anim.slide_out_left)
            }
            trans.remove(fg).commitNowAllowingStateLoss()
            callback?.didRemoveFragment(fg)
        } catch (e: Exception) {
            Handler().post {
                try {
                    trans.remove(fg).commitNowAllowingStateLoss()
                    callback?.didRemoveFragment(fg)
                } catch (e: Exception) {
                }
            }
        }
        return true
    }

    override fun pushFragment(
        fragment: Fragment,
        bundle: Bundle?,
        tag: String?,
        animate: Boolean,
        viewId: Int,
        singleton: Boolean,
        parentTag: String?
    ) {
        val mBundle = fragment.arguments ?: (bundle ?: Bundle())
        mBundle.putBoolean(NavigationController.PUSH_ANIMATE_FRAGMENT_TAG, animate)
        val mTag = tag ?: fragment.javaClass.name + getLastTag(singleton)
        mBundle.putString(NavigationController.FRAGMENT_NAME_TAG, mTag)
        mBundle.putString(NavigationController.PARENT_FRAGMENT_NAME_TAG, parentTag)
        fragment.arguments = mBundle
        val mViewId = if (viewId == 0) android.R.id.content else viewId
        if (singleton) {
            synchronized(mTag) {
                if (listTagFragments.contains(mTag) || fragment.isAdded) return
                push(fragment, animate, mViewId, mTag)
            }
        } else {
            push(fragment, animate, mViewId, mTag)
        }
    }

    override fun pushFragment(
        clazz: KClass<out Fragment>,
        bundle: Bundle?,
        tag: String?,
        animate: Boolean,
        viewId: Int,
        singleton: Boolean
    ) {
        pushFragment(
            fragment = clazz.java.newInstance(),
            bundle = bundle,
            tag = tag,
            animate = animate,
            viewId = viewId,
            singleton = singleton
        )
    }

    private fun push(fragment: Fragment, animate: Boolean, viewId: Int, tag: String): Boolean {
        try {
            val trans = fragmentManager.beginTransaction()
            if (animate) {
                trans.setCustomAnimations(R.anim.slide_in_right, R.anim.slide_out_right)
            }
            callback?.prepareToPushFragment()
            trans.add(viewId, fragment, tag)
                .setTransition(FragmentTransaction.TRANSIT_FRAGMENT_OPEN)
                .commitNowAllowingStateLoss()
            listTagFragments.push(tag)
            Logger.d(">>>>pushFrag $tag")
            callback?.didPushFragment(fragment)
            return true
        } catch (e: Exception) {
            Logger.e(e)
        }
        return false
    }

    override fun addFragment(
        viewId: Int, clazz: KClass<out Fragment>, bundle: Bundle?, tag: String?, singleton: Boolean
    ) {
        addFragment(viewId, clazz.java.newInstance(), bundle, tag, singleton)
    }

    override fun addFragment(
        viewId: Int, fragment: Fragment, bundle: Bundle?, tag: String?, singleton: Boolean
    ) {
        val mTag = tag ?: fragment::class.java.name + getLastTag(singleton)
        val mBundle = bundle ?: Bundle()
        mBundle.putString(NavigationController.FRAGMENT_NAME_TAG, mTag)
        fragment.arguments = mBundle
        try {
            if (singleton) {
                synchronized(mTag) {
                    if (findFragment(fragment::class, mTag) != null) return
                    fragmentManager.beginTransaction().disallowAddToBackStack()
                        .add(viewId, fragment, mTag).commitNowAllowingStateLoss()
                }
            } else {
                fragmentManager.beginTransaction().disallowAddToBackStack()
                    .add(viewId, fragment, mTag).commitNowAllowingStateLoss()
            }
        } catch (e: Exception) {
            Logger.e(e)
        }
    }

    private fun getLastTag(singleton: Boolean): String {
        return if (singleton) "" else "_${System.nanoTime()}"
    }

    override fun removeFragment(fragment: Fragment) {
        try {
            fragmentManager.beginTransaction().remove(fragment).commitNowAllowingStateLoss()
        } catch (e: Exception) {

        }
    }

    override fun removeFragment(tag: String) {
        val fragment = fragmentManager.findFragmentByTag(tag) ?: return
        removeFragment(fragment)
    }

    override fun removeFragment(clazz: KClass<out Fragment>) {
        val n = clazz.java.name
        val fragment = fragmentManager.fragments.firstOrNull() { it.javaClass.name == n } ?: return
        removeFragment(fragment)
    }

    override fun replaceFragment(
        viewId: Int, fragment: Fragment, tag: String?, parentTag: String?
    ) {
        try {
            val mBundle = fragment.arguments ?: Bundle()
            mBundle.putString(NavigationController.FRAGMENT_NAME_TAG, tag)
            mBundle.putString(NavigationController.PARENT_FRAGMENT_NAME_TAG, parentTag)
            fragment.arguments = mBundle
            fragmentManager.beginTransaction().replace(viewId, fragment, tag)
                .disallowAddToBackStack().commitNowAllowingStateLoss()
        } catch (e: Exception) {
            Logger.e(e)
        }
    }

    override fun replaceFragment(
        viewId: Int, clazz: KClass<out Fragment>, tag: String?, parentTag: String?
    ) {
        replaceFragment(viewId, clazz.java.newInstance(), tag, parentTag)
    }

    @Suppress("UNCHECKED_CAST")
    override fun <T : Fragment> findFragment(clazz: KClass<T>, tag: String?): T? {
        Logger.d("tag = $tag")
        val name = clazz.java.name
        val mTag = tag ?: listTagFragments.firstOrNull { it.contains(name) } ?: name
        return try {
            fragmentManager.findFragmentByTag(mTag) as? T?
        } catch (e: Exception) {
            Logger.d("exception = ${e.message}")
            null
        }
    }

    override val topFragment: Fragment?
        get() {
            return fragmentManager.findFragmentByTag(peek)
//            while (result != null) {
//                if (result is BaseFragment) {
//                    result.childFragmentManager.findFragmentByTag()
//                }
//            }
//            return result
        }

    override val isEmptyFragments: Boolean get() = listTagFragments.isEmpty()
    override val sizeListFragments get() = listTagFragments.size

//    //endregion

}