package com.itsthwng.twallpaper.ui.component.zipper.zipperlock.ui

import android.content.Context
import android.content.Intent
import android.os.Bundle
import androidx.core.app.ActivityOptionsCompat
import com.itsthwng.twallpaper.R
import com.itsthwng.twallpaper.databinding.ActivityPersonalisationBinding
import com.itsthwng.twallpaper.extension.setSafeOnClickListener
import com.itsthwng.twallpaper.local.LocalData
import com.itsthwng.twallpaper.local.LocalStorage
import com.itsthwng.twallpaper.ui.component.zipper.lib.Glszl_GameAdapters.ZipperCompatActivity
import com.itsthwng.twallpaper.ui.component.zipper.zipperlock.utils.StatusBarUtils
import com.itsthwng.twallpaper.utils.AppConfig
import com.itsthwng.twallpaper.utils.AppConfig.updateResources
import com.itsthwng.twallpaper.utils.Constants

class ZipLockPersonalisation : ZipperCompatActivity() {
    private lateinit var dataBinding: ActivityPersonalisationBinding

    override fun attachBaseContext(newBase: Context?) {
        if (newBase != null) {
            val localStorage: LocalStorage = LocalData(newBase, "sharedPreferences")
            super.attachBaseContext(updateResources(newBase, localStorage.langCode))
        } else {
            super.attachBaseContext(null)
        }
    }

    private fun setButtons() {
        val bundle = ActivityOptionsCompat.makeCustomAnimation(
            this,
            R.anim.slide_in_right,
            R.anim.slide_out_left
        ).toBundle()

        dataBinding.backBtn.setSafeOnClickListener {
            finish()
        }

        dataBinding.zipperWallpaper.setOnClickListener {
            AppConfig.logEventTracking(Constants.EventKey.GO_TO_CUSTOM_ZIPPER_FOREGROUND)
            val intent = Intent(this, ChooseZipperActivity::class.java)
            intent.putExtra(IS_CUSTOMIZE_KEY, true)
            intent.putExtra("type", 0)
            intent.putExtra("next", false)
            startActivity(intent, bundle)
        }

        dataBinding.backgroundWallpaper.setSafeOnClickListener {
            AppConfig.logEventTracking(Constants.EventKey.GO_TO_CUSTOM_ZIPPER_BACKGROUND)
            val intent = Intent(this, ChooseZipperActivity::class.java)
            intent.putExtra("type", 1)
            intent.putExtra("next", false)
            intent.putExtra(IS_CUSTOMIZE_KEY, true)
            startActivity(intent, bundle)
        }

        dataBinding.fontStyle.setSafeOnClickListener {
            AppConfig.logEventTracking(Constants.EventKey.GO_TO_CUSTOM_ZIPPER_FONT)
            val intent = Intent(this, FontChooserActivity::class.java)
            intent.putExtra(IS_CUSTOMIZE_KEY, true)
            startActivity(intent, bundle)
        }

        dataBinding.setZipperStyle.setSafeOnClickListener {
            AppConfig.logEventTracking(Constants.EventKey.GO_TO_CUSTOM_ZIPPER_STYLE)
            val intent = Intent(this, ChainStyleChooserActivity::class.java)
            intent.putExtra(IS_CUSTOMIZE_KEY, true)
            startActivity(intent, bundle)
        }

        dataBinding.setPendantStyle.setSafeOnClickListener {
            AppConfig.logEventTracking(Constants.EventKey.GO_TO_CUSTOM_PENDANT_STYLE)
            val intent = Intent(this, PendantStyleChooserActivity::class.java)
            intent.putExtra(IS_CUSTOMIZE_KEY, true)
            startActivity(intent, bundle)
        }
    }

    override fun onCreate(bundle: Bundle?) {
        super.onCreate(bundle)
        dataBinding = ActivityPersonalisationBinding.inflate(layoutInflater)

        setContentView(dataBinding.root)

        setButtons()
        StatusBarUtils.setStatusBarColor(this)
    }

    override fun onResume() {
        super.onResume()
        clearAllTempValues()
    }

    private fun clearAllTempValues() {
        Glszl_AppAdapter.SaveWallpaperBgTemp(this, -1)
        Glszl_AppAdapter.SaveWallpaperTemp(this, -1)
        Glszl_AppAdapter.SaveZipperTemp(this, -1)
        Glszl_AppAdapter.SaveChainTemp(this, -1)
        Glszl_AppAdapter.SaveFontTemp(this, -1)
    }

    public override fun onDestroy() {
        super.onDestroy()
        System.gc()
    }

    companion object {
        const val IS_CUSTOMIZE_KEY: String = "isCustomize"
    }
}
