package com.itsthwng.twallpaper.ui.base

import android.content.Context
import android.content.Intent
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.graphics.Color
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.provider.Settings
import android.view.View
import android.view.ViewGroup
import android.view.WindowManager
import android.view.inputmethod.InputMethodManager
import android.widget.EditText
import android.widget.Toast
import androidx.activity.enableEdgeToEdge
import androidx.annotation.StringRes
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat
import androidx.fragment.app.Fragment
import com.itsthwng.twallpaper.utils.Logger
import com.google.gson.internal.Primitives
import com.itsthwng.twallpaper.App
import com.itsthwng.twallpaper.R
import com.itsthwng.twallpaper.data.AppDatabase
import com.itsthwng.twallpaper.data.model.SettingData
import com.itsthwng.twallpaper.utils.permission.PermissionCallback
import com.itsthwng.twallpaper.utils.permission.PermissionDialogManager
import com.itsthwng.twallpaper.utils.permission.PermissionHandler
import com.itsthwng.twallpaper.utils.wallpaper.WallpaperDownloader
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import eightbitlab.com.blurview.BlurAlgorithm
import eightbitlab.com.blurview.BlurView
import eightbitlab.com.blurview.RenderEffectBlur
import eightbitlab.com.blurview.RenderScriptBlur
import java.io.BufferedInputStream
import java.io.IOException
import java.io.InputStream
import java.net.HttpURLConnection
import java.net.URL
import javax.inject.Inject
import kotlin.reflect.KClass
import com.itsthwng.twallpaper.utils.AppConfig

abstract class BaseActivity : BaseView, AppCompatActivity(), NavigationCallback {

    protected lateinit var view: View
    @Inject
    lateinit var database: AppDatabase
    
    @Inject
    lateinit var permissionDialogManager: PermissionDialogManager
    
    @Inject
    lateinit var permissionHandler: PermissionHandler
    
    @Inject
    lateinit var wallpaperDownloader: WallpaperDownloader

    private val mHandler = Handler(Looper.getMainLooper())
    open var delayMillis = 600L
    private var isLoading = false
    private lateinit var mNavigation: NavigationControllerImp
    val navigation: NavigationController get() = mNavigation
    
    private val activityScope = CoroutineScope(Dispatchers.Main)
    
    override fun attachBaseContext(newBase: Context?) {
        // Get localStorage from application context since DI is not ready yet
        val appContext = newBase?.applicationContext
        val localStorage = if (appContext != null) {
            try {
                App.instance.localStorage
            } catch (e: Exception) {
                null
            }
        } else null
        
        val context = if (newBase != null && localStorage != null) {
            AppConfig.updateResources(newBase, localStorage.langCode)
        } else {
            newBase
        }
        super.attachBaseContext(context)
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        mNavigation = NavigationControllerImp(supportFragmentManager)
        mNavigation.callback = this
        getWindow().getDecorView()
        super.onCreate(savedInstanceState)
        view = layoutInflater.inflate(getContentViewId(), null)
        setContentView(view)
        enableEdgeToEdge()
        
        // Initialize permission handler
        permissionHandler.init(this)
        
        init(view)
    }

    open fun setBlur(blurView: BlurView, rootView: ViewGroup) {
        try {
            // Kiểm tra xem BlurView và rootView có hợp lệ không
            if (blurView == null || rootView == null) {
                android.util.Log.w("BaseActivity", "setBlur: BlurView or rootView is null")
                return
            }
            
            // Kiểm tra xem activity có đang active không
            if (this.isFinishing || this.isDestroyed) {
                android.util.Log.w("BaseActivity", "setBlur: Activity is finishing or destroyed")
                return
            }
            
            val windowBackground = window.decorView.background
            val algorithm: BlurAlgorithm = getBlurAlgorithm()
            
            // Setup BlurView với error handling
            blurView.setupWith(rootView, algorithm)
                .setFrameClearDrawable(windowBackground)
                .setBlurRadius(20f)
                
            android.util.Log.d("BaseActivity", "BlurView setup completed successfully")
            
        } catch (e: Exception) {
            android.util.Log.e("BaseActivity", "setBlur failed: ${e.message}")
            // Ẩn BlurView nếu có lỗi để tránh crash
            try {
                blurView.visibility = android.view.View.GONE
            } catch (ex: Exception) {
                android.util.Log.e("BaseActivity", "Failed to hide BlurView: ${ex.message}")
            }
        }
    }

    open fun getBlurAlgorithm(): BlurAlgorithm {
        val algorithm: BlurAlgorithm
        algorithm = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
            RenderEffectBlur()
        } else {
            RenderScriptBlur(this)
        }
        return algorithm
    }

    interface OnDownload {
        fun onComplete()
        fun onError()
    }

    lateinit var onDownloadCallback: OnDownload

    // Deprecated - use downloadWallWithPermissionCheck instead
    @Deprecated("Use downloadWallWithPermissionCheck for proper permission handling")
    public fun downloadWall(wallpapersItem: SettingData.WallpapersItem, callback: OnDownload) {
        downloadWallWithPermissionCheck(wallpapersItem, callback)
    }

    // These methods are now handled by WallpaperDownloaderImpl
    // Keeping mLoad for backward compatibility if needed elsewhere
    @Deprecated("Use WallpaperDownloader instead")
    public fun mLoad(string: String): Bitmap? {
        val url: URL = URL(string)
        val connection: HttpURLConnection?
        try {
            connection = url.openConnection() as HttpURLConnection
            connection.connect()
            val inputStream: InputStream = connection.inputStream
            val bufferedInputStream = BufferedInputStream(inputStream)
            return BitmapFactory.decodeStream(bufferedInputStream)
        } catch (e: IOException) {
            e.printStackTrace()
        }
        return null
    }



    open fun init(view: View) {

    }

    fun setStatusBarColor(color: Int) {
        window.statusBarColor = color
    }

    var isTransparentStatusBar: Boolean
        set(value) {
            window.statusBarColor = if (value) Color.TRANSPARENT else ContextCompat.getColor(
                this,
                R.color.colorSecondary
            )
        }
        get() = window.statusBarColor == Color.TRANSPARENT


    open fun setTransparentNavigationBar(on: Boolean) {
        setWindowFlag(WindowManager.LayoutParams.FLAG_TRANSLUCENT_NAVIGATION, on)
        window.navigationBarColor = if (on) Color.TRANSPARENT else Color.BLACK
        window.statusBarColor = ContextCompat.getColor(this, R.color.colorSecondary)
    }

    open fun setWindowFlag(bits: Int, on: Boolean) {
        val winParams = window.attributes
        if (on) {
            winParams.flags = winParams.flags or bits
        } else {
            winParams.flags = winParams.flags and bits.inv()
        }
        window.attributes = winParams
    }

    open fun popToRoot(animate: Boolean = true) {
        val first = mNavigation.peek
        var last = mNavigation.peek
        while (last != null) {
            mNavigation.popFragment(tag = last, animate = false)
            last = mNavigation.peek
        }
        mNavigation.popFragment(tag = first, animate = animate)
    }

    override fun finish() {
//  overridePendingTransition(R.anim.slide_in_right, R.anim.slide_out_left)
        super.finish()
    }
    
    /**
     * Download wallpaper with automatic permission check
     * This is the main entry point for all download operations
     */
    fun downloadWallWithPermissionCheck(
        wallpapersItem: SettingData.WallpapersItem, 
        callback: OnDownload
    ) {
        permissionHandler.requestStoragePermission(this, object : PermissionCallback {
            override fun onGranted() {
                // Use the new wallpaper downloader
                activityScope.launch {
                    wallpaperDownloader.download(
                        wallpaper = wallpapersItem,
                        onProgress = { /* Can add progress UI here */ },
                        onComplete = {
//                            Toast.makeText(
//                                this@BaseActivity,
//                                getString(R.string.saved_to_gallery),
//                                Toast.LENGTH_SHORT
//                            ).show()
                            callback.onComplete()
                        },
                        onError = { errorMessage ->
                            callback.onError()
                        }
                    )
                }
            }
            
            override fun onDenied(showedRationale: Boolean) {
                callback.onError()
            }
            
            override fun onPermanentlyDenied() {
                // Open settings
                val intent = Intent(Settings.ACTION_APPLICATION_DETAILS_SETTINGS).apply {
                    data = Uri.fromParts("package", packageName, null)
                }
                startActivity(intent)
                callback.onError()
            }
        })
    }

    override fun onResume() {
        super.onResume()
        
        // Check if storage permission was revoked while app was in background
        permissionHandler.checkPermissionStatus(this) {
            // Permission was revoked, show a message to user
        }
    }

    override fun onPause() {
        super.onPause()
    }

    override fun onBackPressed() {
        try {
            if (supportFragmentManager.fragments.isNotEmpty()) {
                supportFragmentManager.fragments[0].childFragmentManager.fragments.forEach {
                    if (it is BaseFragment && it.onBackPressed()) {
                        return
                    }
                }
            }
        } catch (ex : Exception) {

        }
        if (shouldOverrideBackPressed()) {
            super.onBackPressed()
        }
    }


    inline fun <reified T : Fragment> findFragment(): T? {
        return supportFragmentManager.fragments.lastOrNull { it as? T != null } as? T
    }

    open fun <T : Fragment> findFragment(clazz: KClass<T>, tag: String? = null): T? {
        try {
            val name = tag ?: clazz.java.name
            val fg = supportFragmentManager.findFragmentByTag(name)
            if (fg != null) {
                return Primitives.wrap(clazz.java).cast(fg)
            }
        } catch (e: Exception) {

        }
        return null
    }

    open fun shouldOverrideBackPressed(): Boolean {
        return true
    }
//
//    open fun onBackPressedFragment() : Boolean {
//        return true
//    }

    override fun onDestroy() {
        mHandler.removeCallbacksAndMessages(null)
        permissionHandler.cleanup()
        wallpaperDownloader.cancelDownload()
        super.onDestroy()
    }

    fun showToast(str: String) {
        Toast.makeText(this, str, Toast.LENGTH_SHORT).show()
    }

    fun showToast(@StringRes id: Int) {
        Toast.makeText(this, id, Toast.LENGTH_SHORT).show()
    }

    fun showLongToast(str: String) {
        Toast.makeText(this, str, Toast.LENGTH_LONG).show()
    }

    fun showLongToast(@StringRes id: Int) {
        Toast.makeText(this, id, Toast.LENGTH_LONG).show()
    }

    open fun handleResponse(code: Int, cause: Any?) {
        Logger.d("Error status = " + code + " ; cause = " + cause?.toString())
    }

    open fun didClickConfirmNetwork() {

    }

    override fun prepareToPushFragment() {
        topFragment?.let {
            it.childFragmentManager.fragments.forEach { child ->
                (child as? BaseFragment)?.prepareToPushFragment()
            }
            it.prepareToPushFragment()
        }
    }

    val topFragment: BaseFragment?
        get() {
            var tf = navigation.topFragment as? BaseFragment
            while (tf != null) {
                val fg = tf.navigation?.topFragment as? BaseFragment
                if (fg == null) return tf
                else tf = fg
            }
            return tf
        }

    override fun didPushFragment(fragment: Fragment) {
        topFragment?.let {
            it.childFragmentManager.fragments.forEach { child ->
                (child as? BaseFragment)?.didPushFragment(fragment)
            }
            it.didPushFragment(fragment)
        }
    }

    override fun didRemoveFragment(fragment: Fragment) {
        topFragment?.let {
            it.childFragmentManager.fragments.forEach { child ->
                (child as? BaseFragment)?.didRemoveFragment(fragment)
            }
            it.didRemoveFragment(fragment)
        }
    }

    open fun onBackPressedLoading() {
        (navigation.topFragment as? BaseFragment)?.onBackPressedLoading()
    }

    fun hiddenKeyboard() {
        var viewFocus = view.findFocus()
        if (viewFocus == null) {
            viewFocus = findViewById(android.R.id.content) ?: return
        }

        val inputMethodManager =
            getSystemService(Context.INPUT_METHOD_SERVICE) as InputMethodManager
        inputMethodManager.hideSoftInputFromWindow(viewFocus.applicationWindowToken, 0)
        viewFocus.clearFocus()
    }

    fun showKeyboard(view: View? = null) {
        val inputMethodManager =
            getSystemService(Context.INPUT_METHOD_SERVICE) as InputMethodManager
        val v = view ?: this.currentFocus as? EditText
        if (v != null) {
            inputMethodManager.showSoftInput(v, InputMethodManager.SHOW_IMPLICIT)
        } else {
            inputMethodManager.toggleSoftInput(
                InputMethodManager.SHOW_FORCED,
                InputMethodManager.HIDE_IMPLICIT_ONLY
            )
        }
    }

    //endregion

    fun post(runnable: Runnable) {
        if (isFinishing || isDestroyed) return
        mHandler.post(runnable)
    }

    fun post(runnable: (() -> Unit)) {
        if (isFinishing || isDestroyed) return
        mHandler.post(runnable)
    }

    fun postDelayed(runnable: Runnable, delayMillis: Long) {
        if (isFinishing || isDestroyed) return
        mHandler.postDelayed(runnable, delayMillis)
    }

    fun removeCallbacks(runnable: Runnable) {
        mHandler.removeCallbacks(runnable)
    }

    val allFragments: List<BaseFragment> get() = supportFragmentManager.fragments.mapNotNull { it as? BaseFragment }
}
