package com.itsthwng.twallpaper.ui.component.zipper.zipperlock.ui

import android.annotation.SuppressLint
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.graphics.Color
import android.net.Uri
import android.os.Bundle
import android.provider.Settings
import android.view.MotionEvent
import android.view.View
import android.view.Window
import android.widget.ImageView
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.widget.AppCompatButton
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.itsthwng.twallpaper.local.LocalData
import com.itsthwng.twallpaper.local.LocalStorage
import com.itsthwng.twallpaper.ui.component.zipper.lib.Glszl_GameAdapters.ZipperCompatActivity
import com.itsthwng.twallpaper.ui.component.permission.OverlayPermissionActivity.Companion.start
import com.itsthwng.twallpaper.utils.AppConfig
import com.itsthwng.twallpaper.utils.Constants
import com.itsthwng.twallpaper.utils.Logger.e
import java.util.Objects
import java.util.concurrent.Executors
import androidx.core.graphics.drawable.toDrawable
import com.itsthwng.twallpaper.R
import com.itsthwng.twallpaper.databinding.ActivityFontStyleChooserBinding
import com.itsthwng.twallpaper.extension.setSafeOnClickListener
import com.itsthwng.twallpaper.ui.component.zipper.zipperlock.ui.adapter.FontStyleAdapter
import com.itsthwng.twallpaper.ui.component.zipper.zipperlock.utils.StatusBarUtils

// Font Style
class FontChooserActivity : ZipperCompatActivity() {
    private lateinit var dataBinding: ActivityFontStyleChooserBinding
    private lateinit var adapter: FontStyleAdapter
    private var currentFontId = -1
    private var currentPosition = 0
    private val fonts = listOf(
        "A_Valentine_Story",
        "Abraham",
        "Almonte_Woodgrain",
        "Aluna",
        "android_7",
        "Arizonia_Regular",
        "ArnoProRegular",
        "Auttie",
        "Balloon_Pops",
        "Baratta",
        "Blue_highway_bd",
        "Burnstown_Dam",
        "Click_Medium_Stroked",
        "Crack_Man_Front",
        "Foo",
        "GSTIGNRM",
        "Maulydia",
        "Melloner_Happy_Bold",
        "Mellson",
        "Montserrat_Regular",
        "Mystery",
        "Nasalization",
        "Neuropol",
        "Raleway_Light",
        "Sun_island",
        "TitilliumText22L003"
    )
    private var next = false
    private var setCount = 0
    var type: Int = 0

    override fun attachBaseContext(newBase: Context?) {
        if (newBase != null) {
            val localStorage: LocalStorage = LocalData(newBase, "sharedPreferences")
            super.attachBaseContext(AppConfig.updateResources(newBase, localStorage.langCode))
        } else {
            super.attachBaseContext(null)
        }
    }

    private fun buttonEffect(imageView: ImageView) {
        imageView.setOnTouchListener(object : View.OnTouchListener {
            override fun onTouch(view: View?, motionEvent: MotionEvent): Boolean {
                val action = motionEvent.action
                if (action == 0) {
                    imageView.invalidate()
                    return false
                } else if (action != 1) {
                    return false
                } else {
                    imageView.invalidate()
                    return false
                }
            }
        })
    }

    private fun buttonEffect2(imageView: ImageView) {
        imageView.setOnTouchListener(object : View.OnTouchListener {
            override fun onTouch(view: View?, motionEvent: MotionEvent): Boolean {
                val action = motionEvent.action
                if (action == 0) {
                    dataBinding.setCard.invalidate()
                    return false
                } else if (action != 1) {
                    return false
                } else {
                    dataBinding.setCard.invalidate()
                    return false
                }
            }
        })
    }

    private fun initialize() {
        this.currentPosition = Glszl_AppAdapter.getSelectedFontNumber(this)
        this.currentFontId = Glszl_AppAdapter.getSelectedFontNumber(this)
        if (intent.extras != null) {
            this.next = intent.extras!!.getBoolean("next", false)
            val isCustomize = intent.extras!!.getBoolean(
                ZipLockPersonalisation.IS_CUSTOMIZE_KEY,
                false
            )
            if (isCustomize) {
                val setText = findViewById<TextView>(R.id.set_txt)
                setText.setText(R.string.set)
            }
        }
    }

    private fun setButtonListeners() {
        buttonEffect(dataBinding.previewBtn)
        buttonEffect2(dataBinding.setBtn)

        dataBinding.backBtn.setSafeOnClickListener { finish() }
        dataBinding.previewBtn.setSafeOnClickListener { doPreviewWhenHadStoragePermission() }
        dataBinding.setBtn.setSafeOnClickListener { handleWhenLoadInterDone() }
        dataBinding.titleText.text = getString(R.string.set_font)
    }

    private fun doPreviewWhenHadStoragePermission() {
        // For preview, check overlay permission first

        if (checkPermissionOverlay()) {
            // Permission granted, start preview
            Glszl_LockScreenService.IsPreview = true
            prepareDataBeforePreview()
            Glszl_UserDataAdapter.setIsPreview(true)
            Glszl_LockScreenService.Start(this)
        }
        // If permission not granted, checkPermissionOverlay will handle navigation to permission screen
    }

    private fun prepareDataBeforePreview() {
        if (this.next) {
            val tempZipperId = Glszl_AppAdapter.getZipperTemp(this)
            val tempChainId = Glszl_AppAdapter.getChainTemp(this)
            val tempWallpaperId = Glszl_AppAdapter.getWallpaperTemp(this@FontChooserActivity)
            val tempWallpaperBgId = Glszl_AppAdapter.getWallpaperBgTemp(this@FontChooserActivity)

            var currentZipper = Glszl_AppAdapter.getSelectedZiperNumber(this@FontChooserActivity)
            var currentChain = Glszl_AppAdapter.getSelectedChainNumber(this@FontChooserActivity)
            var currentFont = Glszl_AppAdapter.getSelectedFontNumber(this@FontChooserActivity)
            var currentWallpaper =
                Glszl_AppAdapter.getSelectedWallpaperNumber(this@FontChooserActivity)
            var currentWallpaperBg =
                Glszl_AppAdapter.getSelectedWallpaperBgNumber(this@FontChooserActivity)

            // Validate and use defaults if needed
            if (currentZipper <= 0) currentZipper = 1
            if (currentChain <= 0) currentChain = 1
            if (currentFont <= 0) currentFont = 1
            if (currentWallpaper <= 0) currentWallpaper = 1
            if (currentWallpaperBg <= 0) currentWallpaperBg = 1

            if (!(tempZipperId == 0 || tempZipperId == -1)) {
                Glszl_AppAdapter.SaveZipperTemp(this, tempZipperId)
            }
            if (!(tempChainId == 0 || tempChainId == -1)) {
                Glszl_AppAdapter.SaveChainTemp(this, tempChainId)
            }
            if (!(tempWallpaperId == 0 || tempWallpaperId == -1)) {
                Glszl_AppAdapter.SaveWallpaperTemp(this, tempWallpaperId)
            }
            if (!(tempWallpaperBgId == 0 || tempWallpaperBgId == -1)) {
                Glszl_AppAdapter.SaveWallpaperBgTemp(this, tempWallpaperBgId)
            }

            // Save current font selection
            if (currentPosition > 0) {
                Glszl_AppAdapter.SaveFontTemp(this, currentPosition)
            }
        } else {
            if (currentFontId <= 0) currentFontId = 1
            Glszl_AppAdapter.SaveFontTemp(this, currentPosition)
        }
    }

    private fun setFonts() {
        dataBinding.rvFont.setLayoutManager(LinearLayoutManager(this, RecyclerView.VERTICAL, false))
        val fontStyleAdapter = FontStyleAdapter(this, this.currentPosition) { pos ->
            currentPosition = pos + 1
            adapter.update(pos)
        }
        fontStyleAdapter.submitList(this.fonts)
        this.adapter = fontStyleAdapter
        dataBinding.rvFont.setAdapter(this.adapter)

        if (currentFontId >= 1) {
            dataBinding.rvFont.scrollToPosition(currentFontId - 1)
        }
    }

    override fun onResume() {
        super.onResume()
    }

    /**
     * Xử lý kết quả yêu cầu quyền
     */
    override fun onRequestPermissionsResult(
        requestCode: Int,
        permissions: Array<String?>,
        grantResults: IntArray
    ) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)

        if (requestCode == STORAGE_PERMISSION_REQUEST_CODE) {
            if (grantResults.isNotEmpty() && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                // Quyền được cấp, hiển thị thông báo thành công
                Toast.makeText(
                    this,
                    "Đã cấp quyền truy cập storage. Bạn có thể sử dụng tính năng zipper.",
                    Toast.LENGTH_SHORT
                ).show()
                doPreviewWhenHadStoragePermission()
            } else {
                // Quyền bị từ chối, hiển thị hướng dẫn vào cài đặt
                showPermissionDeniedDialog()
            }
        }
    }

    /**
     * Hiển thị dialog khi quyền bị từ chối
     */
    private fun showPermissionDeniedDialog() {
        val inflater = layoutInflater
        val dialogView = inflater.inflate(R.layout.layout_dialog_storage_permission, null)

        val dialog = AlertDialog.Builder(this)
            .setView(dialogView).create()
        Objects.requireNonNull<Window>(dialog.window).setBackgroundDrawable(
            Color.TRANSPARENT.toDrawable()
        )

        val btnOk = dialogView.findViewById<AppCompatButton>(R.id.btnOk)
        val btnCancel = dialogView.findViewById<TextView>(R.id.btnCancel)

        btnOk.setOnClickListener {
            val intent = Intent(Settings.ACTION_APPLICATION_DETAILS_SETTINGS)
            val uri = Uri.fromParts("package", packageName, null)
            intent.data = uri
            startActivity(intent)
            dialog.dismiss()
        }
        btnCancel.setOnClickListener { dialog.dismiss() }
        val decorView = dialog.window!!.decorView
        decorView.systemUiVisibility = View.SYSTEM_UI_FLAG_FULLSCREEN or View.SYSTEM_UI_FLAG_HIDE_NAVIGATION
        dialog.show()
    }


    override fun onCreate(bundle: Bundle?) {
        super.onCreate(bundle)
        dataBinding = ActivityFontStyleChooserBinding.inflate(layoutInflater)
        setContentView(dataBinding.root)

        StatusBarUtils.setStatusBarColor(this)
        initialize()
        setFonts()
        setButtonListeners()
        this.adapter.update(Glszl_AppAdapter.getSelectedFontNumber(this) - 1)
    }

    override fun onStart() {
        super.onStart()
    }

    public override fun onDestroy() {
        super.onDestroy()
        System.gc()
    }

    @SuppressLint("ResourceType")
    fun checkPermissionOverlay(): Boolean {
        try {
            if (Settings.canDrawOverlays(this)) {
                return true
            }
            try {
                AppConfig.logEventTracking(Constants.EventKey.GO_TO_OVERLAY_PERMISSION)
            } catch (e: Exception) {
                e("LogEventTracking error: " + e.message)
            }
            // Navigate to overlay permission screen instead of showing dialog
            start(this, true)
            // Don't finish() here - wait for result
            return false
        } catch (_: Exception) {
            return true
        }
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
        tvDes.text = getString(R.string.set_font_success)
        val btnOk = dialogView.findViewById<AppCompatButton>(R.id.btnOk)
        btnOk.setOnClickListener {
            dialog.dismiss()
            // Finish activity sau khi dialog được dismiss
            finish()
        }
        dialog.setOnDismissListener {
            if (!isFinishing) {
                finish()
            }
        }
        val decorView = dialog.window!!.decorView
        decorView.systemUiVisibility = View.SYSTEM_UI_FLAG_FULLSCREEN or View.SYSTEM_UI_FLAG_HIDE_NAVIGATION
        dialog.show()
    }


    private fun handleWhenLoadInterDone() {
        if (next) {
            val fontStyleChooser = this@FontChooserActivity
            Glszl_AppAdapter.SaveFontTemp(fontStyleChooser, fontStyleChooser.currentPosition)
            try {
                AppConfig.logEventTracking(Constants.EventKey.GO_TO_ZIPPER_STYLE, Bundle())
            } catch (e: Exception) {
                e("LogEventTracking error: " + e.message)
            }
            val intent = Intent(this, ChainStyleChooserActivity::class.java)
            intent.putExtra("next", true)
            startActivity(intent)
            overridePendingTransition(
                R.anim.slide_in_right,
                R.anim.slide_out_left
            )
            return
        }
        Glszl_AppAdapter.SaveFont(this, currentPosition)
        access(this)
        if (setCount > 0) {
            Glszl_SharedPreferencisUtil(this)
        }
        showSetSuccessDialogAndFinish()
    }

    companion object {
        private const val STORAGE_PERMISSION_REQUEST_CODE = 100
        fun access(fontStyleChooser: FontChooserActivity): Int {
            val i = fontStyleChooser.setCount
            fontStyleChooser.setCount = i + 1
            return i
        }
    }
}
