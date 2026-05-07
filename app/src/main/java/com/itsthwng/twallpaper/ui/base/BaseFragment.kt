package com.itsthwng.twallpaper.ui.base

import android.annotation.SuppressLint
import android.content.res.Configuration
import android.content.res.Resources
import android.os.Bundle
import android.os.Handler
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.annotation.StringRes
import androidx.core.view.doOnLayout
import androidx.fragment.app.Fragment
import androidx.lifecycle.Observer
import androidx.navigation.NavController
import androidx.navigation.fragment.findNavController
import com.itsthwng.twallpaper.local.LocalStorage
import com.itsthwng.twallpaper.repository.FileHelper
import com.itsthwng.twallpaper.utils.AppConfig
import com.itsthwng.twallpaper.utils.Logger
import java.util.Locale
import javax.inject.Inject
import kotlin.reflect.KClass

abstract class BaseFragment : BaseView, Fragment(), NavigationCallback {

    @Inject
    lateinit var localStorage: LocalStorage

    @Inject
    lateinit var fileHelper: FileHelper

    protected val autoDisposable = AutoDisposable()

    private val mHandler = Handler()
    var mView: View? = null

    private lateinit var mNavigation: NavigationControllerImp
    val navigation: NavigationController? get() = if (this::mNavigation.isInitialized) mNavigation else null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        Logger.d(javaClass.name, "onCreate()...")
        mNavigation = NavigationControllerImp(childFragmentManager)
        mNavigation.callback = this
        autoDisposable.bindTo(lifecycle)
    }

    @SuppressLint("ClickableViewAccessibility")
    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?
    ): View? {
        Logger.d(javaClass.name, "onCreateView()...")
        updateLanguage()
        val view = inflater.inflate(getContentViewId(), null)
        mView = view
        view.isClickable = true
        view.isFocusable = true
        return view
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        Logger.d(javaClass.name, "onViewCreated()...")
        if (isAdded() && activity != null) {
            initializeViews()
            initializeData()
            registerListeners()
            view.doOnLayout {
                viewDidLoad()
                initDelayed()
            }
        }
    }

    open fun setupObserver(viewModel: BaseViewModel) {
        viewModel.toastLiveData.observe(viewLifecycleOwner, Observer {
            if (it is Int) {
                showToast(it)
            } else {
                showToast(it.toString())
            }
        })
    }

    open fun viewIsVisible() {
    }

    open fun viewDidLoad() {
        //Add if needed
    }

    fun showToast(str: String) {
        val con = activity ?: return
        Toast.makeText(con, str, Toast.LENGTH_SHORT).show()
    }

    open fun showToast(@StringRes id: Int) {
        val con = activity ?: return
        Toast.makeText(con, id, Toast.LENGTH_SHORT).show()
    }

    fun showLongToast(str: String) {
        val con = activity ?: return
        Toast.makeText(con, str, Toast.LENGTH_LONG).show()
    }

    open fun showLongToast(@StringRes id: Int) {
        val con = activity ?: return
        Toast.makeText(con, id, Toast.LENGTH_LONG).show()
    }

    override fun onResume() {
        super.onResume()
        Logger.d(">>>>>>>>OnResumeBase    " + this.javaClass.name)
        // Update resources for multi-language support
        context?.let {
            AppConfig.updateResources(it, localStorage.langCode)
        }
    }

    override fun onDestroyView() {
        mHandler.removeCallbacksAndMessages(null)
        super.onDestroyView()
    }

    fun postDelayed(run: Runnable, delayMillis: Long) {
        mHandler.postDelayed(run, delayMillis)
    }

    fun post(run: () -> Unit) {
        if (context == null) return
        mHandler.post(run)
    }

    fun postDelayed(run: () -> Unit, delayMillis: Long) {
        if (context == null) return
        mHandler.postDelayed(run, delayMillis)
    }

    fun removeAllCallbacks() {
        mHandler.removeCallbacksAndMessages(null)
    }

    fun removeCallbacks(runnable: Runnable) {
        mHandler.removeCallbacks(runnable)
    }

    override fun prepareToPushFragment() {
        // to do implement
    }

    override fun didPushFragment(fragment: Fragment) {

    }

    override fun didRemoveFragment(fragment: Fragment) {

    }

    open fun onBackPressedLoading() {
        //Add if needed
    }

    protected open fun initDelayed() {
        //Add if needed
    }

    /**
     * @return true if Back button was handled
     */
    open fun onBackPressed() = false

    fun hiddenKeyboard() {
        (activity as? BaseActivity)?.hiddenKeyboard()
    }

    fun showKeyboard(view: View? = null) {
        (activity as? BaseActivity)?.showKeyboard(view)
    }

    fun popChildFragment(): Boolean {
        if (mNavigation.popFragment()) {
            return true
        }
        val parent = parentFragment as? BaseFragment ?: return false
        return parent.navigation?.popFragment(this, animate = false) ?: false
    }

    fun findNavControllerSafety(): NavController? {
        return if (view != null) {
            findNavController()
        } else null
    }

    fun pushChildFragment(
        fragment: Fragment,
        bundle: Bundle? = null,
        tag: String? = null,
        animate: Boolean = false,
        viewId: Int = 0,
        singleton: Boolean = false,
        parentTag: String? = null
    ) {
        val id = if (viewId == 0) mView?.id ?: 0 else viewId
        mNavigation.pushFragment(
            fragment,
            bundle = bundle,
            tag = tag,
            animate = animate,
            viewId = id,
            singleton = singleton,
            parentTag = parentTag ?: arguments?.getString(NavigationController.FRAGMENT_NAME_TAG)
        )
    }

    fun pushChildFragment(
        clazz: KClass<out Fragment>,
        bundle: Bundle? = null,
        tag: String? = null,
        viewId: Int = 0,
        singleton: Boolean = false,
        parentTag: String? = null
    ) {
        pushChildFragment(
            fragment = clazz.java.newInstance(),
            bundle = bundle,
            tag = tag,
            viewId = viewId,
            singleton = singleton,
            parentTag = parentTag
        )
    }

    fun replaceChildFragment(
        viewId: Int, fragment: Fragment, tag: String? = null, parentTag: String? = null
    ) {
        mNavigation.replaceFragment(
            viewId,
            fragment,
            tag,
            parentTag ?: arguments?.getString(NavigationController.FRAGMENT_NAME_TAG)
        )
    }

    fun replaceChildFragment(
        viewId: Int, clazz: KClass<out Fragment>, tag: String? = null, parentTag: String? = null
    ) {
        mNavigation.replaceFragment(
            viewId,
            clazz,
            tag,
            parentTag ?: arguments?.getString(NavigationController.FRAGMENT_NAME_TAG)
        )
    }

    fun addChildFragment(
        viewId: Int,
        clazz: KClass<out Fragment>,
        bundle: Bundle? = null,
        tag: String? = null,
        singleton: Boolean = false
    ) {
        val id = if (viewId == 0) mView?.id ?: 0 else viewId
        mNavigation.addFragment(
            id, clazz = clazz, bundle = bundle, tag = tag, singleton = singleton
        )
    }

    fun addChildFragment(
        viewId: Int,
        fragment: Fragment,
        bundle: Bundle? = null,
        tag: String? = null,
        singleton: Boolean = false
    ) {
        val id = if (viewId == 0) mView?.id ?: 0 else viewId
        mNavigation.addFragment(id, fragment, bundle = bundle, tag = tag, singleton = singleton)
    }

    fun removeChildFragment(fragment: Fragment) {
        mNavigation.removeFragment(fragment)
    }

    fun removeChildFragment(tag: String) {
        mNavigation.removeFragment(tag)
    }

    fun removeChildFragment(clazz: KClass<out Fragment>) {
        mNavigation.removeFragment(clazz)
    }

    fun <F : Fragment> findChildFragment(clazz: KClass<F>, tag: String? = null): F? {
        return mNavigation.findFragment(clazz, tag)
    }

    protected fun updateLanguage() {
        if (localStorage.langCode.isNotBlank()) {
            val resources: Resources = resources
            val language = localStorage.langCode
            val locale: Locale
            if (language.contains("-")) {
                val splitLanguage = language.split("-")
                locale = Locale(splitLanguage[0], splitLanguage[1])
            } else {
                locale = Locale(language)
            }
            Locale.setDefault(locale)
            val config = Configuration()
            config.locale = locale
            resources.updateConfiguration(config, resources.displayMetrics)
        }
    }

}
