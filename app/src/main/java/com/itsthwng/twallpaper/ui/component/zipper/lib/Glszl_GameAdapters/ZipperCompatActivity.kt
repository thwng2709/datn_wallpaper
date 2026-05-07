package com.itsthwng.twallpaper.ui.component.zipper.lib.Glszl_GameAdapters

import android.os.Build
import android.os.Bundle
import android.view.View
import android.view.WindowInsets
import android.view.WindowInsetsController
import androidx.appcompat.app.AppCompatActivity
import com.itsthwng.twallpaper.local.LocalStorage
import com.itsthwng.twallpaper.utils.AppConfig.updateLanguage
import dagger.hilt.android.AndroidEntryPoint
import javax.inject.Inject

@AndroidEntryPoint
open class ZipperCompatActivity : AppCompatActivity() {
    @Inject
    lateinit var localStorage: LocalStorage

    public override fun onCreate(bundle: Bundle?) {
        super.onCreate(bundle)
    }

    fun hideSystemBars() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R) {
            val insetsController = window.insetsController
            if (insetsController != null) {
                insetsController.hide(WindowInsets.Type.statusBars() or WindowInsets.Type.navigationBars())
                insetsController.systemBarsBehavior = WindowInsetsController.BEHAVIOR_SHOW_TRANSIENT_BARS_BY_SWIPE
            }
        } else {
            window.decorView.setSystemUiVisibility(
                (View.SYSTEM_UI_FLAG_IMMERSIVE_STICKY
                        or View.SYSTEM_UI_FLAG_FULLSCREEN
                        or View.SYSTEM_UI_FLAG_HIDE_NAVIGATION
                        or View.SYSTEM_UI_FLAG_LAYOUT_FULLSCREEN
                        or View.SYSTEM_UI_FLAG_LAYOUT_HIDE_NAVIGATION
                        or View.SYSTEM_UI_FLAG_LAYOUT_STABLE)
            )
        }
    }

    override fun onWindowFocusChanged(hasFocus: Boolean) {
        super.onWindowFocusChanged(hasFocus)
        hideSystemBars()
    }

    public override fun onResume() {
        super.onResume()
        updateLanguage(localStorage.langCode, getResources())
        hideSystemBars()
    }

    public override fun onPause() {
        super.onPause()
    }

    override fun setContentView(i: Int) {
        super.setContentView(i)
    }
}
