package com.itsthwng.twallpaper.ui.component.zipper.zipperlock.ui

import android.content.Context
import android.content.Intent
import android.content.SharedPreferences
import android.graphics.Bitmap
import android.graphics.Color
import android.os.Bundle
import android.provider.Settings
import android.view.View
import android.view.Window
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.widget.AppCompatButton
import androidx.core.content.edit
import androidx.core.graphics.drawable.toDrawable
import androidx.lifecycle.ViewModelProvider
import com.itsthwng.twallpaper.R
import com.itsthwng.twallpaper.databinding.ActivateLockBinding
import com.itsthwng.twallpaper.local.LocalData
import com.itsthwng.twallpaper.local.LocalStorage
import com.itsthwng.twallpaper.ui.component.zipper.lib.Glszl_GameAdapters.Glszl_GameAdapter
import com.itsthwng.twallpaper.ui.component.zipper.lib.Glszl_GameAdapters.Glszl_PreviewHelper
import com.itsthwng.twallpaper.ui.component.zipper.lib.Glszl_GameAdapters.ZipperCompatActivity
import com.itsthwng.twallpaper.ui.component.permission.OverlayPermissionActivity
import com.itsthwng.twallpaper.ui.component.zipper.ZipperViewModel
import com.itsthwng.twallpaper.ui.component.zipper.zipperlock.ZipLockMainActivity
import com.itsthwng.twallpaper.ui.component.zipper.zipperlock.utils.PrefKey
import com.itsthwng.twallpaper.ui.component.zipper.zipperlock.utils.StatusBarUtils
import com.itsthwng.twallpaper.utils.AppConfig
import com.itsthwng.twallpaper.utils.AppConfig.updateResources
import com.itsthwng.twallpaper.utils.Constants
import com.itsthwng.twallpaper.utils.Logger.e
import java.util.Objects

class ActivateZipLockActivity : ZipperCompatActivity() {
    private lateinit var dataBinding: ActivateLockBinding
    private lateinit var packagePrefs: SharedPreferences
    private lateinit var zipperViewModel: ZipperViewModel
    override fun attachBaseContext(newBase: Context?) {
        if (newBase != null) {
            val localStorage: LocalStorage = LocalData(newBase, "sharedPreferences")
            super.attachBaseContext(updateResources(newBase, localStorage.langCode))
        } else {
            super.attachBaseContext(null)
        }
    }

    override fun onCreate(bundle: Bundle?) {
        super.onCreate(bundle)
        dataBinding = ActivateLockBinding.inflate(layoutInflater)
        setContentView(dataBinding.root)
        zipperViewModel = ViewModelProvider(this)[ZipperViewModel::class.java]

        StatusBarUtils.setStatusBarColor(this)
        this.packagePrefs = getSharedPreferences(packageName, 0)

        // Đợi layout xong để lấy size chính xác
        dataBinding.img.post { // Lấy kích thước thực tế của ImageView
            val width = dataBinding.img.width
            val height = dataBinding.img.height

            // Tạo preview với kích thước phù hợp với ImageView
            val preview: Bitmap? = if (width > 0 && height > 0) {
                Glszl_PreviewHelper.createSafePreview(
                    this,
                    width,
                    height
                )
            } else {
                // Fallback nếu chưa có size
                Glszl_PreviewHelper.createDefaultPreview(this)
            }
            dataBinding.img.setImageBitmap(preview)
        }

        if (intent.extras != null) {
            val previewAfterSelection = intent.extras!!.getBoolean("preview_after_selection", false)

            // If coming from preview flow after selecting all styles, start preview immediately
            if (previewAfterSelection) {
                if (checkPermissionOverlay()) {
                    Glszl_LockScreenService.IsPreview = true
                    Glszl_LockScreenService.Start(this)
                }
            }
        }
        dataBinding.backBtn.setOnClickListener { finish() }
        dataBinding.activateBtn.setOnClickListener { doActivateAction() }
    }

    private fun doActivateAction() {
        if (this.checkPermissionOverlay()) {
            val str = PrefKey.ACTIVE
            val activateLockActivity = this
            if (Glszl_CheckBoxUpdater.toggleAndPersistState(false, str, activateLockActivity, true)) {
                val tempZipperId = Glszl_AppAdapter.getZipperTemp(this)
                val tempChainId = Glszl_AppAdapter.getChainTemp(this)
                val tempFontId = Glszl_AppAdapter.getFontTemp(this)
                val tempWallpaperId =
                    Glszl_AppAdapter.getWallpaperTemp(this)
                val tempWallpaperBgId =
                    Glszl_AppAdapter.getWallpaperBgTemp(this)
                val tempChainType =
                    Glszl_AppAdapter.getChainTypeTemp(this)
                Glszl_AppAdapter.SaveWallpaper(this, tempWallpaperId)
                Glszl_AppAdapter.SaveWallpaperBg(this, tempWallpaperBgId)
                Glszl_AppAdapter.SaveZipper(this, tempZipperId)
                Glszl_AppAdapter.SaveChain(this, tempChainId)
                Glszl_AppAdapter.SaveFont(this, tempFontId)
                Glszl_AppAdapter.SaveChainType(this, tempChainType)
                saveChain("chain_index", tempChainId)
                saveChain("zipper_index", tempZipperId)

                try {
                    AppConfig.logEventTracking(
                        Constants.EventKey.SET_ZIP + "PENDANT_" + tempZipperId,
                        Bundle()
                    )
                    AppConfig.logEventTracking(
                        Constants.EventKey.SET_ZIP + "CHAIN_" + tempChainId,
                        Bundle()
                    )
                    AppConfig.logEventTracking(
                        Constants.EventKey.SET_ZIP + "FOREGROUND" + tempWallpaperId,
                        Bundle()
                    )
                    AppConfig.logEventTracking(
                        Constants.EventKey.SET_ZIP + "BACKGROUND_" + tempWallpaperBgId,
                        Bundle()
                    )
                    AppConfig.logEventTracking(Constants.EventKey.ENABLE_ZIPPER, Bundle())
                } catch (e: Exception) {
                    e("LogEventTracking error: " + e.message)
                }

                this.saveActivatedLock(true)
                Glszl_AppAdapter.SetLock(Glszl_GameAdapter.ctx, "1")
                this.saveLock("lock_screen", 1)
                Glszl_LiveService.StartServiceIfNotNull(this)
                Glszl_JobUtil.scheduleJob(this)

                // Clear all temp values after successful activation
                Glszl_AppAdapter.SaveWallpaperBgTemp(this, -1)
                Glszl_AppAdapter.SaveWallpaperTemp(this, -1)
                Glszl_AppAdapter.SaveZipperTemp(this, -1)
                Glszl_AppAdapter.SaveChainTemp(this, -1)
                Glszl_AppAdapter.SaveFontTemp(this, -1)

                // End the "Set Now" flow
                ZipLockMainActivity.endSetNowFlow(this)
            }
            showSetSuccessDialogAndFinish()
        }
    }

    fun saveChain(str: String?, num: Int) {
        this.packagePrefs.edit(commit = true) {
            putInt(str, num)
        }
    }

    override fun onResume() {
        super.onResume()
    }

    /**
     * Hiển thị dialog thông báo set thành công và finish activity
     */
    private fun showSetSuccessDialogAndFinish() {
        // Kiểm tra xem Activity có còn tồn tại không
        if (isFinishing || isDestroyed) {
            return
        }

        val inflater = layoutInflater
        val dialogView = inflater.inflate(R.layout.layout_dialog_success, null)

        val dialog = AlertDialog.Builder(this)
            .setView(dialogView).create()
        Objects.requireNonNull<Window>(dialog.window).setBackgroundDrawable(
            Color.TRANSPARENT.toDrawable()
        )
        val tvDes = dialogView.findViewById<TextView>(R.id.tv_description)
        tvDes.text = getString(R.string.set_zipper_success)
        val btnOk = dialogView.findViewById<AppCompatButton>(R.id.btnOk)
        btnOk.setOnClickListener {
            dialog.dismiss()
            // Đảm bảo finish activity nếu dialog bị dismiss
            if (!isFinishing) {
                try {
                    AppConfig.logEventTracking(Constants.EventKey.GO_TO_ZIPPER_LOCKER, Bundle())
                } catch (e: Exception) {
                    e("LogEventTracking error: " + e.message)
                }
                // After activation, go back to MainActivity
                val intent =
                    Intent(this, ZipLockMainActivity::class.java)
                intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP or Intent.FLAG_ACTIVITY_SINGLE_TOP)
                this.startActivity(intent)
                this.finish()
            }
        }
        dialog.setOnDismissListener {
            if (!isFinishing) {
                // After activation, go back to MainActivity
                try {
                    AppConfig.logEventTracking(Constants.EventKey.GO_TO_ZIPPER_LOCKER, Bundle())
                } catch (e: Exception) {
                    e("LogEventTracking error: " + e.message)
                }
                val intent =
                    Intent(this, ZipLockMainActivity::class.java)
                intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP or Intent.FLAG_ACTIVITY_SINGLE_TOP)
                this.startActivity(intent)
                this.finish()
            }
        }
        val decorView = dialog.window!!.decorView
        decorView.systemUiVisibility =
            View.SYSTEM_UI_FLAG_FULLSCREEN or View.SYSTEM_UI_FLAG_HIDE_NAVIGATION
        dialog.show()
    }

    fun saveActivatedLock(z: Boolean) {
        this.packagePrefs.edit(commit = true) {
            putBoolean("activatedLock", z)
        }
    }

    fun saveLock(str: String?, num: Int) {
        this.packagePrefs.edit {
            putInt(str, num)
        }
    }

    fun checkPermissionOverlay(): Boolean {
        try {
            if (Settings.canDrawOverlays(this)) {
                return true
            }
            // Navigate to overlay permission screen and wait for result
            OverlayPermissionActivity.startForResult(this, true)
            return false
        } catch (_: Exception) {
            return true
        }
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)

        if (requestCode == OverlayPermissionActivity.REQUEST_OVERLAY_PERMISSION) {
            if (resultCode == RESULT_OK && data != null) {
                val permissionGranted = data.getBooleanExtra(
                    OverlayPermissionActivity.EXTRA_PERMISSION_GRANTED, false
                )
                if (!permissionGranted) {
                    try {
                        AppConfig.logEventTracking(
                            Constants.EventKey.DENY_OVERLAY_PERMISSION,
                            Bundle()
                        )
                    } catch (e: Exception) {
                        e("LogEventTracking error: " + e.message)
                    }
                    // Show toast if permission was not granted
                    Toast.makeText(
                        this,
                        getString(R.string.overlay_permission_denied_message),
                        Toast.LENGTH_LONG
                    ).show()
                } else {
                    try {
                        AppConfig.logEventTracking(
                            Constants.EventKey.GRANT_OVERLAY_PERMISSION,
                            Bundle()
                        )
                    } catch (e: Exception) {
                        e("LogEventTracking error: " + e.message)
                    }
                }
            } else if (resultCode == RESULT_CANCELED) {
                // User clicked "Not Now" or back
                Toast.makeText(
                    this,
                    getString(R.string.overlay_permission_denied_message),
                    Toast.LENGTH_LONG
                ).show()
            }
        }
    }
}
