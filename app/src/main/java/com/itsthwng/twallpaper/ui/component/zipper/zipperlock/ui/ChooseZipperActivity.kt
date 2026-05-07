package com.itsthwng.twallpaper.ui.component.zipper.zipperlock.ui

import android.annotation.SuppressLint
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.graphics.Color
import android.net.Uri
import android.os.Bundle
import android.provider.Settings
import android.util.Log
import android.view.MotionEvent
import android.view.View
import android.view.Window
import android.widget.ImageView
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.widget.AppCompatButton
import androidx.core.graphics.drawable.toDrawable
import androidx.lifecycle.ViewModelProvider
import androidx.viewpager.widget.ViewPager.OnPageChangeListener
import com.itsthwng.twallpaper.R
import com.itsthwng.twallpaper.data.entity.ZipperImageEntity
import com.itsthwng.twallpaper.databinding.ActivityChooseZipperBinding
import com.itsthwng.twallpaper.extension.setSafeOnClickListener
import com.itsthwng.twallpaper.local.LocalData
import com.itsthwng.twallpaper.local.LocalStorage
import com.itsthwng.twallpaper.repository.DownloadRepository
import com.itsthwng.twallpaper.ui.component.zipper.lib.Glszl_GameAdapters.ZipperCompatActivity
import com.itsthwng.twallpaper.ui.component.permission.OverlayPermissionActivity
import com.itsthwng.twallpaper.ui.component.permission.OverlayPermissionActivity.Companion.startForResult
import com.itsthwng.twallpaper.ui.component.zipper.DownloadInternalCallback
import com.itsthwng.twallpaper.ui.component.zipper.FileNameUtils
import com.itsthwng.twallpaper.ui.component.zipper.PrivateImageStore
import com.itsthwng.twallpaper.ui.component.zipper.ZipperViewModel
import com.itsthwng.twallpaper.ui.component.zipper.zipperlock.ZipLockMainActivity
import com.itsthwng.twallpaper.ui.component.zipper.zipperlock.ui.adapter.ZipperViewPagerAdapter
import com.itsthwng.twallpaper.ui.component.zipper.zipperlock.utils.StatusBarUtils
import com.itsthwng.twallpaper.utils.AppConfig
import com.itsthwng.twallpaper.utils.Constants
import com.itsthwng.twallpaper.utils.Logger.e
import com.itsthwng.twallpaper.utils.download.DownloadManager
import dagger.hilt.android.AndroidEntryPoint
import java.util.Objects
import javax.inject.Inject
import kotlin.math.abs
import kotlin.math.max
import kotlin.math.min
import kotlin.math.sqrt

@AndroidEntryPoint
class ChooseZipperActivity : ZipperCompatActivity() {
    private lateinit var dataBinding: ActivityChooseZipperBinding
    private lateinit var zipperViewModel: ZipperViewModel
    private lateinit var adapter: ZipperViewPagerAdapter
    private lateinit var utils: FileNameUtils
    private var currentWallpaperId = -1
    private var currentTempZipperId = -1
    private var currentWallpaperBgId = -1
    private var currentCoinsNeed = 0
    private var currentPosition = 0
    private var next = false
    private var personalization = false
    private var setCount = 0
    private var type = 0

    // Download related
    @Inject
    lateinit var downloadRepository: DownloadRepository

    override fun attachBaseContext(newBase: Context?) {
        if (newBase != null) {
            val localStorage: LocalStorage = LocalData(newBase, "sharedPreferences")
            super.attachBaseContext(AppConfig.updateResources(newBase, localStorage.langCode))
        } else {
            super.attachBaseContext(null)
        }
    }

    override fun onCreate(bundle: Bundle?) {
        super.onCreate(bundle)
        dataBinding = ActivityChooseZipperBinding.inflate(layoutInflater)
        setContentView(dataBinding.root)
        zipperViewModel = ViewModelProvider(this)[ZipperViewModel::class.java]

        StatusBarUtils.setStatusBarColor(this)
        currentWallpaperId = Glszl_AppAdapter.getSelectedWallpaperNumber(this)
        currentWallpaperBgId = Glszl_AppAdapter.getSelectedWallpaperBgNumber(this)
        initialize()
        setContext()
        setupObservers()
        setWallpapers()
        setButtonListeners()
        utils = FileNameUtils()
    }

    override fun onStart() {
        super.onStart()
    }

    private fun initialize() {
        dataBinding.loadingProgressBar.visibility = View.GONE
        setCount = 0
        currentPosition = 0
    }

    private fun setupObservers() {
        zipperViewModel.zippersByType.observe(this) { zippers ->
            if (zippers != null && !zippers.isEmpty()) {
                adapter.updateData(zippers)
                if (type == 0) {
                    if (currentWallpaperId >= 1) {
                        dataBinding.viewpager.setCurrentItem(currentWallpaperId - 1, true)
                    }
                } else if (type == 1) {
                    if (currentWallpaperBgId >= 1) {
                        dataBinding.viewpager.setCurrentItem(currentWallpaperBgId - 1, true)
                    }
                }
                updateUIForZippers()
            }
        }

        zipperViewModel.error.observe(this) { error: String? ->
            if (!error.isNullOrEmpty()) {
                // Hiển thị thông báo lỗi
                // Có thể sử dụng Toast hoặc AlertDialog
                zipperViewModel.clearError()
            }
        }
    }

    private fun updateUIForZippers() {
        val zippers = zipperViewModel.zippers.getValue()
        if (!zippers.isNullOrEmpty()) {
            // Show buttons and cards when we have data
            updateUiVisibility()
        }
    }

    private fun setWallpapers() {
        // Load zippers from database with specific type
        zipperViewModel.loadZippersByType(Constants.ZIPPER_IMAGE)

        // Create adapter with empty list initially, will be updated by observer
        adapter = ZipperViewPagerAdapter(
            this,
            emptyList(),
            currentWallpaperId,
            currentWallpaperBgId,
            type
        )
        dataBinding.viewpager.setAdapter(adapter)
        adapter.attachTo(dataBinding.viewpager)
        if (type == 0) {
            if (currentWallpaperId >= 1) {
                adapter.updateSelectedPosition(currentWallpaperId - 1)
            }
        } else if (type == 1) {
            if (currentWallpaperBgId >= 1) {
                adapter.updateSelectedPosition(currentWallpaperBgId - 1)
            }
        }

        dataBinding.viewpager.apply {
            clipChildren = false
            setPageMargin(0)
            setOffscreenPageLimit(0)
        }

        if (isTablet(this)) {
            val i2 = (getResources().displayMetrics.widthPixels / 4.0f).toInt()
            dataBinding.viewpager.setPadding(i2, 0, i2, dpToPx(20, this))
        }
        dataBinding.viewpager.setPageTransformer(true) { page, position ->
            // position: 0 = giữa; -1 = bên trái; 1 = bên phải
            val overlay = page.findViewById<View?>(R.id.dimOverlay) // View mờ phủ lên ảnh
            val container = page.findViewById<View?>(R.id.imageContainer) // Frame/Card chứa ảnh

            val p = abs(position)

            // Độ mờ: giữa 0f, ra rìa ~0.55f
            val dim = min(0.55f * p, 0.55f)
            if (overlay != null) overlay.alpha = dim

            // Scale nhẹ: giữa 1.0, hai bên ~0.92
            val scale = 1f - (0.08f * p)
            page.scaleX = scale
            page.scaleY = scale

            // Elevation cao hơn cho trang giữa (nếu cần đổ bóng)
            if (container != null) {
                val elev = max(10f - 8f * p, 0f)
                container.elevation = elev
            }
        }

        dataBinding.viewpager.addOnPageChangeListener(object : OnPageChangeListener {
            var index: Int = 0

            override fun onPageScrolled(i: Int, f: Float, i2: Int) {}

            override fun onPageSelected(i: Int) {
                index = i
                currentPosition = i

                // Update adapter selection when page scrolled
                adapter.updateSelectedPosition(i)


                // Update currentTempZipperId based on selected zipper
                val selectedZipper = adapter.getSelectedZipper()
                if (selectedZipper != null) {
                    currentTempZipperId = selectedZipper.id
                    currentCoinsNeed = selectedZipper.pricePoints
                }

                // Update UI based on current position
                updateUIForCurrentPosition()

                if (type == 0) {
                    dataBinding.titleText.text = getString(R.string.set_foreground)
                } else if (type == 1) {
                    dataBinding.titleText.text = getString(R.string.set_background)
                }
            }

            override fun onPageScrollStateChanged(i: Int) {
//                if (i == 1) {
//                    heart_zipper_checkImage.setVisibility(View.GONE);
//                } else if (i == 0) {
//                    heart_zipper_checkImage.setVisibility(View.VISIBLE);
//                } else {
//                    if (index % 5 == 0 && index != 0) {
//                        heart_zipper_checkImage.setVisibility(View.GONE);
//                    }
//                }
            }
        })
    }

    private fun updateUIForCurrentPosition() {
        val currentZipper = adapter.getSelectedZipper()
        if (currentZipper != null) {
            // Show UI elements when we have valid data
            updateUiVisibility()
        }
    }

    private fun updateUiVisibility() {
        dataBinding.previewBtn.visibility = View.VISIBLE
        dataBinding.setBtn.visibility = View.VISIBLE
        dataBinding.previewCard.visibility = View.VISIBLE
        dataBinding.setCard.visibility = View.VISIBLE
        dataBinding.previewTxt.visibility = View.VISIBLE
        dataBinding.setTxt.visibility = View.VISIBLE
    }

    fun buttonEffect(imageView: ImageView) {
        imageView.setOnTouchListener(object : View.OnTouchListener {
            override fun onTouch(view: View?, motionEvent: MotionEvent): Boolean {
                when (motionEvent.action) {
                    MotionEvent.ACTION_DOWN -> {
                        imageView.invalidate()
                        return false
                    }

                    MotionEvent.ACTION_UP -> {
                        imageView.invalidate()
                        return false
                    }

                    else -> return false
                }
            }
        })
    }

    fun buttonEffect2(imageView: ImageView) {
        imageView.setOnTouchListener(object : View.OnTouchListener {
            override fun onTouch(view: View?, motionEvent: MotionEvent): Boolean {
                when (motionEvent.action) {
                    MotionEvent.ACTION_DOWN -> {
                        dataBinding.setCard.invalidate()
                        return false
                    }

                    MotionEvent.ACTION_UP -> {
                        dataBinding.setCard.invalidate()
                        return false
                    }

                    else -> return false
                }
            }
        })
    }

    private fun setButtonListeners() {
        buttonEffect(dataBinding.previewBtn)
        buttonEffect2(dataBinding.setBtn)

        dataBinding.backBtn.setOnClickListener { back() }

        dataBinding.previewBtn.setOnClickListener {
            // Kiểm tra quyền trước khi cho phép preview
            val currentZipper = adapter.getSelectedZipper()
            if (currentZipper != null) {
                handlePreviewAction(currentZipper)
            }
        }

        dataBinding.setBtn.setSafeOnClickListener {
            val currentZipper = adapter.getSelectedZipper()
            if (currentZipper != null) {
                if (this.next) {
                    handleSetAction()
                } else {
                    handleSetAction()
                }
            }
        }

        if (type == 0) {
            dataBinding.titleText.text = getString(R.string.set_foreground)
        } else if (type == 1) {
            dataBinding.titleText.text = getString(R.string.set_background)
        }
    }

    private fun handleSetAction() {
        val currentZipper = adapter.getSelectedZipper()
        var imageType = ""
        if (type == 0) {
            imageType = "FOREGROUND"
        } else if (type == 1) {
            imageType = "BACKGROUND"
        }
        if (currentZipper != null) {
            try {
                AppConfig.logEventTracking(
                    Constants.EventKey.SELECT_ZIP + imageType + "_" + currentZipper.id
                )
            } catch (e: Exception) {
                e("LogEventTracking error: " + e.message)
            }
        }

        if (this.next) {
            handleWhenLoadInterDone()
        } else {
            handleWhenLoadInterDone()
        }
    }

    private fun prepareDataBeforePreview() {
        if (this.next) {
            val tempZipperId = Glszl_AppAdapter.getZipperTemp(this@ChooseZipperActivity)
            val tempChainId = Glszl_AppAdapter.getChainTemp(this@ChooseZipperActivity)
            val tempFontId = Glszl_AppAdapter.getFontTemp(this@ChooseZipperActivity)
            val tempWallpaperId = Glszl_AppAdapter.getWallpaperTemp(this@ChooseZipperActivity)
            val tempWallpaperBgId =
                Glszl_AppAdapter.getWallpaperBgTemp(this@ChooseZipperActivity)

            var currentZipper =
                Glszl_AppAdapter.getSelectedZiperNumber(this@ChooseZipperActivity)
            var currentChain =
                Glszl_AppAdapter.getSelectedChainNumber(this@ChooseZipperActivity)
            var currentFont =
                Glszl_AppAdapter.getSelectedFontNumber(this@ChooseZipperActivity)
            var currentWallpaper =
                Glszl_AppAdapter.getSelectedWallpaperNumber(this@ChooseZipperActivity)
            var currentWallpaperBg =
                Glszl_AppAdapter.getSelectedWallpaperBgNumber(this@ChooseZipperActivity)

            // Validate and use default values if needed
            if (currentZipper <= 0) currentZipper = 1
            if (currentChain <= 0) currentChain = 1
            if (currentFont <= 0) currentFont = 1
            if (currentWallpaper <= 0) currentWallpaper = 1
            if (currentWallpaperBg <= 0) currentWallpaperBg = 1

            // IMPORTANT: Update the current selection for preview based on what's being selected
            // If we're previewing a new selection, use the temp ID instead of the saved value
            if (type == 0 && currentTempZipperId > 0) {
                // Previewing foreground - use the newly selected ID
                currentWallpaper = currentTempZipperId
                Glszl_AppAdapter.SaveWallpaperTemp(applicationContext, currentTempZipperId)
            } else if (type == 1 && currentTempZipperId > 0) {
                // Previewing background - use the newly selected ID
                currentWallpaperBg = currentTempZipperId
                Glszl_AppAdapter.SaveWallpaperBgTemp(applicationContext, currentTempZipperId)
            }

            // Use temp values if they exist (from previous selections in the flow)
            if (tempZipperId > 0) currentZipper = tempZipperId
            if (tempChainId > 0) currentChain = tempChainId
            if (tempFontId > 0) currentFont = tempFontId
            if (tempWallpaperId > 0 && type != 0) currentWallpaper = tempWallpaperId
            if (tempWallpaperBgId > 0 && type != 1) currentWallpaperBg = tempWallpaperBgId

            Glszl_LockScreenService.currentChainId = currentChain
            Glszl_LockScreenService.currentFontId = currentFont
            Glszl_LockScreenService.currentWallpaperId = currentWallpaper
            Glszl_LockScreenService.currentWallpaperBgId = currentWallpaperBg
            Glszl_LockScreenService.currentZipperId = currentZipper

            // Save temp values for next screen
            if (!(tempZipperId == 0 || tempZipperId == -1)) {
                Glszl_AppAdapter.SaveZipperTemp(this@ChooseZipperActivity, tempZipperId)
            }
            if (!(tempFontId == 0 || tempFontId == -1)) {
                Glszl_AppAdapter.SaveFontTemp(this@ChooseZipperActivity, tempFontId)
            }
            if (!(tempChainId == 0 || tempChainId == -1)) {
                Glszl_AppAdapter.SaveChainTemp(this@ChooseZipperActivity, tempChainId)
            }
        } else {
            // Not in "next" flow - standard preview
            // Get current saved values
            var currentZipper =
                Glszl_AppAdapter.getSelectedZiperNumber(this@ChooseZipperActivity)
            var currentChain =
                Glszl_AppAdapter.getSelectedChainNumber(this@ChooseZipperActivity)
            var currentFont =
                Glszl_AppAdapter.getSelectedFontNumber(this@ChooseZipperActivity)
            var currentWallpaper =
                Glszl_AppAdapter.getSelectedWallpaperNumber(this@ChooseZipperActivity)
            var currentWallpaperBg =
                Glszl_AppAdapter.getSelectedWallpaperBgNumber(this@ChooseZipperActivity)

            // Ensure we have valid values
            if (currentZipper <= 0) currentZipper = 1
            if (currentChain <= 0) currentChain = 1
            if (currentFont <= 0) currentFont = 1
            if (currentWallpaper <= 0) currentWallpaper = 1
            if (currentWallpaperBg <= 0) currentWallpaperBg = 1

            // Update the current selection for preview
            if (type == 0 && currentTempZipperId > 0) {
                currentWallpaper = currentTempZipperId
                Glszl_AppAdapter.SaveWallpaperTemp(applicationContext, currentTempZipperId)
            } else if (type == 1 && currentTempZipperId > 0) {
                currentWallpaperBg = currentTempZipperId
                Glszl_AppAdapter.SaveWallpaperBgTemp(applicationContext, currentTempZipperId)
            }

            // Set values for preview
            Glszl_LockScreenService.currentChainId = currentChain
            Glszl_LockScreenService.currentFontId = currentFont
            Glszl_LockScreenService.currentWallpaperId = currentWallpaper
            Glszl_LockScreenService.currentWallpaperBgId = currentWallpaperBg
            Glszl_LockScreenService.currentZipperId = currentZipper
        }
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
        decorView.systemUiVisibility =
            View.SYSTEM_UI_FLAG_FULLSCREEN or View.SYSTEM_UI_FLAG_HIDE_NAVIGATION
        dialog.show()
    }

    /**
     * Xử lý action preview
     */
    private fun handlePreviewAction(zipper: ZipperImageEntity) {
        // Log để debug
        Log.d(
            TAG,
            "Preview action - Zipper ID: " + zipper.id + ", Position: " +
                    currentPosition + ", Content: " + zipper.content
        )

        if (!zipper.content.isEmpty()) {
            // Kiểm tra xem ảnh đã có trong store chưa theo ID
            if (PrivateImageStore.isFileDownloadedById(
                    this,
                    zipper.id,
                    DownloadManager.FOLDER_ZIPPER_IMAGE,
                    zipper.content
                )
            ) {
                // Ảnh đã có, thực hiện logic preview
                Log.d(TAG, "File already exists, executing preview logic")
                executePreviewLogic(zipper)
            } else {
                // Ảnh chưa có, download trước
                Log.d(TAG, "File not found, starting download")
                downloadAndPreview(zipper)
            }
        } else {
            // Không có URL, thực hiện logic preview trực tiếp
            Log.d(TAG, "No URL, executing preview logic directly")
            executePreviewLogic(zipper)
        }
    }

    /**
     * Xử lý action set
     */
    private fun handleSetAction(zipper: ZipperImageEntity) {
        // Log để debug
        Log.d(
            TAG, "Set action - Zipper ID: " + zipper.id +
                    ", Position: " + currentPosition +
                    ", Content: " + zipper.content
        )
        showLoadingProgress()

        if (!zipper.content.isEmpty()) {
            // Kiểm tra xem ảnh đã có trong store chưa theo ID
            if (PrivateImageStore.isFileDownloadedById(
                    this,
                    zipper.id,
                    DownloadManager.FOLDER_ZIPPER_IMAGE,
                    zipper.content
                )
            ) {
                // Ảnh đã có, thực hiện logic set
                Log.d(TAG, "File already exists, executing set logic")
                executeSetLogic(zipper)
            } else {
                // Ảnh chưa có, download trước
                Log.d(TAG, "File not found, starting download")
                downloadAndSet(zipper)
            }
        } else {
            // Không có URL, thực hiện logic set trực tiếp
            Log.d(TAG, "No URL, executing set logic directly")
            executeSetLogic(zipper)
        }
    }

    /**
     * Hiển thị progressbar loading
     */
    private fun showLoadingProgress() {
        runOnUiThread { dataBinding.loadingProgressBar.visibility = View.VISIBLE }
    }

    /**
     * Ẩn progressbar loading
     */
    private fun hideLoadingProgress() {
        runOnUiThread { dataBinding.loadingProgressBar.visibility = View.GONE }
    }


    /**
     * Download ảnh và thực hiện preview
     */
    private fun downloadAndPreview(zipper: ZipperImageEntity) {
        try {
            // Sử dụng callback pattern thay vì Flow theo ID
            downloadImageWithCallbackById(zipper, object : DownloadInternalCallback {
                override fun onProgress(progress: Int) {
                    // Có thể hiển thị progress nếu cần
                }

                override fun onSuccess(filePath: String?) {
                    // Download thành công, thực hiện preview
                    runOnUiThread { executePreviewLogic(zipper) }
                }

                override fun onFailed(error: String?) {
                    // Download thất bại, thực hiện preview trực tiếp
                    runOnUiThread { executePreviewLogic(zipper) }
                }
            })
        } catch (e: Exception) {
            // Nếu có lỗi, thực hiện preview trực tiếp
            runOnUiThread { executePreviewLogic(zipper) }
        }
    }

    /**
     * Download ảnh và thực hiện set
     */
    private fun downloadAndSet(zipper: ZipperImageEntity) {
        try {
            // Sử dụng callback pattern thay vì Flow theo ID
            downloadImageWithCallbackById(zipper, object : DownloadInternalCallback {
                override fun onProgress(progress: Int) {
                    // Có thể hiển thị progress nếu cần
                }

                override fun onSuccess(filePath: String?) {
                    // Download thành công, thực hiện set
                    runOnUiThread { executeSetLogic(zipper) }
                }

                override fun onFailed(error: String?) {
                    // Download thất bại, thực hiện set trực tiếp
                    runOnUiThread { executeSetLogic(zipper) }
                }
            })
        } catch (e: Exception) {
            // Nếu có lỗi, thực hiện set trực tiếp
            runOnUiThread { executeSetLogic(zipper) }
        }
    }

    /**
     * Download ảnh với callback pattern theo ID
     */
    private fun downloadImageWithCallbackById(
        zipper: ZipperImageEntity,
        callback: DownloadInternalCallback
    ) {
        try {
            val fileName = utils.generateFileNameById(
                zipper.id,
                FileNameUtils.FOLDER_ZIPPER_IMAGE,
                zipper.content
            )
            PrivateImageStore.downloadToInternalFilesAsyncGuardedBySets(
                this@ChooseZipperActivity,
                zipper.content, fileName, callback
            )
            //            // Sử dụng DownloadManager trực tiếp với callback pattern theo ID
//            DownloadManager downloadManager = new DownloadManager();
//            downloadManager.downloadImageWithCallbackById(this, zipper.getId(), zipper.getContent(), DownloadManager.FOLDER_ZIPPER_IMAGE, callback);
        } catch (e: Exception) {
            callback.onFailed(e.message)
        }
    }


    /**
     * Thực hiện logic preview
     */
    private fun executePreviewLogic(zipper: ZipperImageEntity) {
        currentTempZipperId = zipper.id

        if (checkPermissionOverlay()) {
            showLoadingProgress()
            Glszl_LockScreenService.IsPreview = true
            prepareDataBeforePreview()
            Glszl_UserDataAdapter.setIsPreview(true)
            Glszl_LockScreenService.Start(this@ChooseZipperActivity)
            hideLoadingProgress()
        }
    }

    /**
     * Thực hiện logic set
     */
    private fun executeSetLogic(zipper: ZipperImageEntity) {
        if (!next) {
            if (type == 0) {
                AppConfig.logEventTracking(Constants.EventKey.SET_ZIP + "FOREGROUND_" + zipper.id)

                Glszl_AppAdapter.SaveWallpaper(applicationContext, zipper.id)
                currentWallpaperId = zipper.id
                setCount++
                if (setCount > 0) {
                    val sharedPreferencisUtil =
                        Glszl_SharedPreferencisUtil(this)
                    sharedPreferencisUtil.setShowAd(sharedPreferencisUtil.getShowAd() + 1)
                }
                hideLoadingProgress()
                // Hiển thị dialog trước khi finish
                showSetSuccessDialogAndFinish()
            } else if (type == 1) {
                try {
                    AppConfig.logEventTracking(
                        Constants.EventKey.SET_ZIP + "BACKGROUND_" + zipper.id
                    )
                } catch (e: Exception) {
                    e("LogEventTracking error: " + e.message)
                }
                Glszl_AppAdapter.SaveWallpaperBg(applicationContext, zipper.id)
                currentWallpaperBgId = zipper.id
                setCount++
                if (setCount > 0) {
                    val sharedPreferencisUtil =
                        Glszl_SharedPreferencisUtil(this@ChooseZipperActivity)
                    sharedPreferencisUtil.setShowAd(sharedPreferencisUtil.getShowAd() + 1)
                }
                hideLoadingProgress()
                // Hiển thị dialog trước khi finish
                showSetSuccessDialogAndFinish()
            }
        } else {
            if (type == 0) {
                Glszl_AppAdapter.SaveWallpaperTemp(applicationContext, zipper.id)
                Glszl_AppAdapter.SaveWallpaperCoinsTemp(applicationContext, zipper.pricePoints)
                try {
                    AppConfig.logEventTracking(Constants.EventKey.GO_TO_ZIPPER_BACKGROUND, Bundle())
                } catch (e: Exception) {
                    e("LogEventTracking error: " + e.message)
                }
                currentWallpaperId = zipper.id
                val enableBackground = Glszl_AppAdapter.isShowBackground(this)
                if (enableBackground) {
                    val intent = Intent(
                        this@ChooseZipperActivity,
                        ChooseZipperActivity::class.java
                    )
                    intent.putExtra("type", 1)
                    intent.putExtra("next", true)
                    startActivity(intent)
                    overridePendingTransition(R.anim.slide_in_right, R.anim.slide_out_left)
                } else {
                    val intent2 =
                        Intent(this@ChooseZipperActivity, FontChooserActivity::class.java)
                    intent2.putExtra("next", true)
                    startActivity(intent2)
                    overridePendingTransition(R.anim.slide_in_right, R.anim.slide_out_left)
                }
                //                finish();
            } else if (type == 1) {
                Glszl_AppAdapter.SaveWallpaperBgTemp(applicationContext, zipper.id)
                Glszl_AppAdapter.SaveWallpaperBgCoinsTemp(
                    applicationContext,
                    zipper.pricePoints
                )
                try {
                    AppConfig.logEventTracking(Constants.EventKey.GO_TO_ZIPPER_FONT, Bundle())
                } catch (e: Exception) {
                    e("LogEventTracking error: " + e.message)
                }
                currentWallpaperBgId = zipper.id
                val intent2 =
                    Intent(this@ChooseZipperActivity, FontChooserActivity::class.java)
                intent2.putExtra("next", true)
                startActivity(intent2)
                overridePendingTransition(R.anim.slide_in_right, R.anim.slide_out_left)
            }
            hideLoadingProgress()
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
        if (type == 0) {
            // foreground
            tvDes.text = getString(R.string.set_foreground_success)
        } else if (type == 1) {
            // background
            tvDes.text = getString(R.string.set_background_success)
        }
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
        decorView.systemUiVisibility =
            View.SYSTEM_UI_FLAG_FULLSCREEN or View.SYSTEM_UI_FLAG_HIDE_NAVIGATION
        dialog.show()
    }

    private fun setContext() {
        type =
            if (intent.extras != null) intent.extras!!.getInt("type", 0) else 0
        next = if (intent.extras != null) intent.extras!!
            .getBoolean("next", true) else true
        personalization = intent.extras != null && intent.extras!!
            .getBoolean(ZipLockPersonalisation.IS_CUSTOMIZE_KEY, false)
        if (personalization) {
            dataBinding.setTxt.setText(R.string.set)
        }
    }

    @SuppressLint("ResourceType")
    fun checkPermissionOverlay(): Boolean {
        try {
            if (Settings.canDrawOverlays(this)) {
                return true
            }
            try {
                AppConfig.logEventTracking(Constants.EventKey.GO_TO_OVERLAY_PERMISSION, Bundle())
            } catch (e: Exception) {
                e("LogEventTracking error: " + e.message)
            }
            // Navigate to overlay permission screen instead of showing dialog
//            com.itsthwng.twallpaper.ui.component.permission.OverlayPermissionActivity.Companion.start(this, true);
            startForResult(
                this, true
            )
            // Don't finish() here - wait for result
            return false
        } catch (unused: Exception) {
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
                    doPreviewWhenHadOverlayPermission()
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

    private fun doPreviewWhenHadOverlayPermission() {
        // Sử dụng vị trí hiện tại để lấy chain chính xác
        val currentZipper = adapter.getSelectedZipper()

        if (currentZipper != null) {
            executePreviewLogic(currentZipper)
        }
    }

    private fun back() {
        if (personalization) {
            finish()
            return
        }

        // If this is the first screen in "Set Now" flow (type=0, next=true)
        // End the flow when user backs out
        if (next && type == 0) {
            ZipLockMainActivity.endSetNowFlow(this)
            // Clear temp values
            Glszl_AppAdapter.SaveWallpaperBgTemp(this, -1)
            Glszl_AppAdapter.SaveWallpaperTemp(this, -1)
            Glszl_AppAdapter.SaveZipperTemp(this, -1)
            Glszl_AppAdapter.SaveChainTemp(this, -1)
            Glszl_AppAdapter.SaveFontTemp(this, -1)
        }

        finish()
    }

    override fun onDestroy() {
        super.onDestroy()
    }

    private fun handleWhenLoadInterDone() {
        // Sử dụng vị trí hiện tại để lấy zipper chính xác
        val currentZipper = adapter.getSelectedZipper()
        if (currentZipper != null) {
            // Kiểm tra danh sách file đã download
            val downloadedFiles = downloadRepository.getDownloadedFiles(
                this,
                DownloadManager.FOLDER_ZIPPER_IMAGE
            )
            Log.d(TAG, "Downloaded files count: " + downloadedFiles.size)
            for (file in downloadedFiles) {
                Log.d(TAG, "Downloaded file: " + file.name)
            }

            handleSetAction(currentZipper)
        }
    }

    companion object {
        private const val STORAGE_PERMISSION_REQUEST_CODE = 100
        const val TAG = "ChooseZipperActivity"

        fun isTablet(context: Context): Boolean {
            val displayMetrics = context.resources.displayMetrics
            val f = displayMetrics.heightPixels / displayMetrics.ydpi
            val f2 = displayMetrics.widthPixels / displayMetrics.xdpi
            return sqrt((f2 * f2 + f * f).toDouble()) >= 7
        }

        fun dpToPx(i: Int, context: Context): Int {
            return Math.round(i * context.resources.displayMetrics.density)
        }
    }
}
