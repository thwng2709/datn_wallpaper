package com.itsthwng.twallpaper.ui.component.zipper.zipperlock.ui

import android.annotation.SuppressLint
import android.content.Context
import android.content.Intent
import android.content.SharedPreferences
import android.content.pm.PackageManager
import android.graphics.Color
import android.net.Uri
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.provider.Settings
import android.view.MotionEvent
import android.view.View
import android.view.Window
import android.widget.AdapterView
import android.widget.ImageView
import android.widget.ListAdapter
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.widget.AppCompatButton
import androidx.core.content.edit
import androidx.core.graphics.drawable.toDrawable
import androidx.core.graphics.toColorInt
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProvider
import com.itsthwng.twallpaper.R
import com.itsthwng.twallpaper.data.entity.ZipperImageEntity
import com.itsthwng.twallpaper.databinding.ActivityPendantStyleBinding
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
import com.itsthwng.twallpaper.ui.component.zipper.zipperlock.utils.StatusBarUtils.setStatusBarColor
import com.itsthwng.twallpaper.ui.component.zipper.zipperlock.ui.adapter.ChainStyleGridAdapter
import com.itsthwng.twallpaper.utils.AppConfig.logEventTracking
import com.itsthwng.twallpaper.utils.AppConfig.updateResources
import com.itsthwng.twallpaper.utils.Constants
import com.itsthwng.twallpaper.utils.Logger.e
import com.itsthwng.twallpaper.utils.download.DownloadManager
import dagger.hilt.android.AndroidEntryPoint
import java.util.Objects
import java.util.concurrent.Executors
import javax.inject.Inject

// Pendant Style
@AndroidEntryPoint
class PendantStyleChooserActivity : ZipperCompatActivity() {
    private lateinit var dataBinding: ActivityPendantStyleBinding
    private lateinit var adapter: ChainStyleGridAdapter
    private lateinit var zipperViewModel: ZipperViewModel
    private val zipperList: MutableList<ZipperImageEntity> = ArrayList()
    private var currentZipperId = -1
    var chains: List<String> = listOf(
        "zipper",
        "zipper1",
        "zipper2",
        "zipper3",
        "zipper4",
        "zipper5",
        "zipper6",
        "zipper7",
        "zipper8",
        "zipper9",
        "zipper10",
        "zipper11",
        "zipper12",
        "zipper13",
        "zipper14",
        "zipper15",
        "zipper16",
        "zipper17",
        "zipper18",
        "zipper19"
    )
    private var currentPosition = 0
    private var currentTempZipperId = 0
    private var next = false
    var packagePrefs: SharedPreferences? = null
    var type: Int = -1
    private var utils: FileNameUtils? = null

    // Download related
    @JvmField
    @Inject
    var downloadRepository: DownloadRepository? = null



    override fun attachBaseContext(newBase: Context?) {
        if (newBase != null) {
            val localStorage: LocalStorage = LocalData(newBase, "sharedPreferences")
            super.attachBaseContext(updateResources(newBase, localStorage.langCode))
        } else {
            super.attachBaseContext(null)
        }
    }

    private fun initialize() {
        // Khởi tạo progressbar
        dataBinding.loadingProgressBar.visibility = View.GONE // Ẩn mặc định
        // Đọc zipper_index từ SharedPreferences (giờ sẽ là ID thay vì vị trí)
        loadZipper("zipper_index")

        // currentPosition sẽ được set sau khi load data và tìm được vị trí tương ứng
        this.currentPosition = 0 // Mặc định là 0 (item đầu tiên)

        if (intent.extras != null) {
            this.next = intent.extras!!.getBoolean("next", false)
            val isCustomize = intent.extras!!.getBoolean(
                ZipLockPersonalisation.IS_CUSTOMIZE_KEY,
                false
            )
            if (isCustomize) {
                val setTxt = findViewById<TextView>(R.id.set_txt)
                setTxt.setText(R.string.set)
            }
        }
    }

    private fun setupObservers() {
        zipperViewModel.zippersByType.observe(this) { zippers ->
            if (zippers != null && !zippers.isEmpty()) {
                zipperList.clear()
                zipperList.addAll(zippers)
                // Debug log
                println("Data loaded: " + zipperList.size + " items")

                // Sau khi load data, tìm vị trí của zipper đã lưu
                findAndSetCurrentPosition()

                setZippers()
            } else {
                // Debug log khi không có data
                println("No data received from ViewModel")
                // Fallback về data cũ nếu cần
                setZippersWithFallback()
            }
        }

        zipperViewModel.error.observe(this, Observer { error: String? ->
            if (!error.isNullOrEmpty()) {
                println("Error loading data: $error")
                zipperViewModel.clearError()
                // Fallback về data cũ khi có lỗi
                setZippersWithFallback()
            }
        })
    }

    /**
     * Tìm vị trí của zipper đã lưu trong list hiện tại
     */
    private fun findAndSetCurrentPosition() {
        if (zipperList.isEmpty()) {
            this.currentPosition = 0
            return
        }

        // Đọc zipper_index từ SharedPreferences (giờ sẽ là ID)
        val savedZipperId = loadZipper("zipper_index")
        println("Looking for zipper with ID: $savedZipperId")

        if (savedZipperId > 0) {
            // Tìm vị trí của zipper có ID này trong list
            for (i in zipperList.indices) {
                val zipper = zipperList[i]
                if (zipper.id == savedZipperId) {
                    this.currentPosition = i
                    println("Found zipper at position: $i with ID: $savedZipperId")
                    return
                }
            }
        }

        // Nếu không tìm thấy hoặc savedZipperId = 0, set về item đầu tiên
        this.currentPosition = 0
        println("Zipper not found, setting to first item (position 0)")
    }

    private fun loadZipperData() {
        println("Loading zipper data with Constants.ZIPPERS...")
        zipperViewModel.loadZippersByType(Constants.ZIPPERS)
    }

    private fun setZippers() {
        if (zipperList.isEmpty()) {
            println("ZipperList is empty, using fallback")
            setZippersWithFallback()
            return
        }

        println("Setting zippers with " + zipperList.size + " items")

        val zipperStyleGridViewAdapter = ChainStyleGridAdapter(
            this,
            zipperList = zipperList,
            selectedPosition = this.currentPosition,
            isUsingZipperList = true
        )
        this.adapter = zipperStyleGridViewAdapter
        dataBinding.gridView.adapter = zipperStyleGridViewAdapter as ListAdapter
        dataBinding.gridView.onItemClickListener =
            AdapterView.OnItemClickListener { _, _, pos, _ ->
                this.currentPosition = pos
                this.adapter.update(pos)

                // Lưu zipper_index theo ID thay vì vị trí
                if (!zipperList.isEmpty() && pos < zipperList.size) {
                    val selectedZipper = zipperList[pos]
                    println("Zipper item clicked - Position: " + pos + ", Zipper ID: " + selectedZipper.id)
                }
            }
        if (currentZipperId >= 1) {
            dataBinding.gridView.post { dataBinding.gridView.setSelection(currentPosition) }
        }
    }

    private fun setZippersWithFallback() {
        println("Using fallback data with " + this.chains.size + " items")

        // Với fallback data, vẫn sử dụng logic mới
        findAndSetCurrentPositionForFallback()

        val zipperStyleGridViewAdapter = ChainStyleGridAdapter(
            this,
            chains = this.chains,
            selectedPosition = this.currentPosition,
            isUsingZipperList = false
        )
        this.adapter = zipperStyleGridViewAdapter
        dataBinding.gridView.adapter = zipperStyleGridViewAdapter as ListAdapter
        dataBinding.gridView.onItemClickListener =
            AdapterView.OnItemClickListener { _, _, i, _ ->
                this.currentPosition = i
                this.adapter.update(i)

                println("Fallback item clicked - Position: " + i + ", Saving as: " + (i + 1))
            }
    }

    /**
     * Tìm vị trí cho fallback data (khi không có zipperList)
     */
    private fun findAndSetCurrentPositionForFallback() {
        val savedZipperId = loadZipper("zipper_index")

        if (savedZipperId > 0 && savedZipperId <= this.chains.size) {
            // Với fallback data, zipper_index vẫn là vị trí (1-based)
            this.currentPosition = savedZipperId - 1 // Chuyển về 0-based
            println("Fallback: Using saved position: " + this.currentPosition)
        } else {
            this.currentPosition = 0
            println("Fallback: Setting to first item (position 0)")
        }
    }

    fun buttonEffect(imageView: ImageView) {
        imageView.setOnTouchListener(object : View.OnTouchListener {
            @SuppressLint("ClickableViewAccessibility")
            override fun onTouch(view: View?, motionEvent: MotionEvent): Boolean {
                val action = motionEvent.action
                if (action == 0) {
//                    imageView.setImageDrawable(Glszl_ZipperStyleChooser.this.getResources().getDrawable(R.drawable.buttons2));
                    imageView.invalidate()
                    return false
                } else if (action != 1) {
                    return false
                } else {
//                    imageView.setImageDrawable(Glszl_ZipperStyleChooser.this.getResources().getDrawable(R.drawable.buttons));
                    imageView.invalidate()
                    return false
                }
            }
        })
    }

    fun buttonEffect2(imageView: ImageView) {
        imageView.setOnTouchListener(object : View.OnTouchListener {
            override fun onTouch(view: View?, motionEvent: MotionEvent): Boolean {
                val action = motionEvent.action
                if (action == 0) {
                    dataBinding.cardView.invalidate()
                    return false
                } else if (action != 1) {
                    return false
                } else {
                    dataBinding.cardView.invalidate()
                    return false
                }
            }
        })
    }

    private fun initializeButtons() {
        buttonEffect(dataBinding.previewBtn)
        buttonEffect2(dataBinding.setBtn)

        dataBinding.backBtn.setOnClickListener { finish() }
        dataBinding.previewBtn.setOnClickListener { doPreviewWhenHadStoragePermission() }
        dataBinding.setBtn.setSafeOnClickListener {
            var currentZipper: ZipperImageEntity? = null
            if (!zipperList.isEmpty() && currentPosition < zipperList.size) {
                currentZipper = zipperList[currentPosition]
            }
            if (currentZipper != null) {
                try {
                    logEventTracking(Constants.EventKey.SELECT_ZIP + "PENDANT_" + currentZipper.id)
                } catch (e: Exception) {
                    e("LogEventTracking error: " + e.message)
                }
            }
            handleWhenLoadInterDone()
        }
    }

    private fun doSetWhenHadStoragePermission() {
        if (this.checkPermissionOverlay(SET_TYPE)) {
            Glszl_LockScreenService.IsPreview = true
            prepareDataBeforePreview(false)
            try {
                logEventTracking(Constants.EventKey.GO_TO_ACTIVATE_LOCK, Bundle())
            } catch (e: Exception) {
                e("LogEventTracking error: " + e.message)
            }
            Glszl_UserDataAdapter.setIsPreview(true)
            Glszl_LockScreenService.Start(this)
            // Chuyển đến bước tiếp theo
            val intent =
                Intent(this, ActivateZipLockActivity::class.java)
            intent.putExtra("next", true)
            startActivity(intent)
            overridePendingTransition(R.anim.slide_in_right, R.anim.slide_out_left)
        }
    }

    private fun prepareDataBeforePreview(isPreview: Boolean) {
        if (!isPreview) {
            if (this.next) {
                val tempZipperId = Glszl_AppAdapter.getZipperTemp(this)
                val tempChainId = Glszl_AppAdapter.getChainTemp(this)
                val tempFontId = Glszl_AppAdapter.getFontTemp(this)
                val tempWallpaperId = Glszl_AppAdapter.getWallpaperTemp(this)
                val tempWallpaperBgId =
                    Glszl_AppAdapter.getWallpaperBgTemp(this)
                var currentZipper = Glszl_AppAdapter.getSelectedZiperNumber(this)
                var currentChain = Glszl_AppAdapter.getSelectedChainNumber(this)
                var currentFont = Glszl_AppAdapter.getSelectedFontNumber(this)
                var currentWallpaper = Glszl_AppAdapter.getSelectedWallpaperNumber(this)
                var currentWallpaperBg = Glszl_AppAdapter.getSelectedWallpaperBgNumber(this)


                // Validate and use defaults if needed
                if (currentZipper <= 0) currentZipper = 1
                if (currentChain <= 0) currentChain = 1
                if (currentFont <= 0) currentFont = 1
                if (currentWallpaper <= 0) currentWallpaper = 1
                if (currentWallpaperBg <= 0) currentWallpaperBg = 1

                if (!(tempZipperId == 0 || tempZipperId == -1)) {
                    Glszl_AppAdapter.SaveZipperTemp(this, tempZipperId)
                }
                // Save current zipper selection
                if (currentTempZipperId > 0) {
                    Glszl_AppAdapter.SaveZipperTemp(this, currentTempZipperId)
                }
                if (!(tempChainId == 0 || tempChainId == -1)) {
                    Glszl_AppAdapter.SaveChainTemp(this, tempChainId)
                }
                if (!(tempFontId == 0 || tempFontId == -1)) {
                    Glszl_AppAdapter.SaveFontTemp(this, tempFontId)
                }
                if (!(tempWallpaperId == 0 || tempWallpaperId == -1)) {
                    Glszl_AppAdapter.SaveWallpaperTemp(this, tempWallpaperId)
                }
                if (!(tempWallpaperBgId == 0 || tempWallpaperBgId == -1)) {
                    Glszl_AppAdapter.SaveWallpaperBgTemp(this, tempWallpaperBgId)
                }
            }
        } else {
            if (this.next) {
                Glszl_AppAdapter.getZipperTemp(this)
                val tempChainId = Glszl_AppAdapter.getChainTemp(this)
                val tempFontId = Glszl_AppAdapter.getFontTemp(this)
                val tempWallpaperId = Glszl_AppAdapter.getWallpaperTemp(this)
                val tempWallpaperBgId = Glszl_AppAdapter.getWallpaperBgTemp(this)

                var currentChain = Glszl_AppAdapter.getSelectedChainNumber(this)
                var currentZipper = Glszl_AppAdapter.getSelectedZiperNumber(this)
                var currentFont = Glszl_AppAdapter.getSelectedFontNumber(this)
                var currentWallpaper = Glszl_AppAdapter.getSelectedWallpaperNumber(this)
                var currentWallpaperBg = Glszl_AppAdapter.getSelectedWallpaperBgNumber(this)

                // Validate and use defaults if needed
                if (currentZipper <= 0) currentZipper = 1
                if (currentChain <= 0) currentChain = 1
                if (currentFont <= 0) currentFont = 1
                if (currentWallpaper <= 0) currentWallpaper = 1
                if (currentWallpaperBg <= 0) currentWallpaperBg = 1

                if (!(tempChainId == 0 || tempChainId == -1)) {
                    Glszl_AppAdapter.SaveChainTemp(this, tempChainId)
                }
                if (!(tempFontId == 0 || tempFontId == -1)) {
                    Glszl_AppAdapter.SaveFontTemp(this, tempFontId)
                }
                if (!(tempWallpaperId == 0 || tempWallpaperId == -1)) {
                    Glszl_AppAdapter.SaveWallpaperTemp(this, tempWallpaperId)
                }
                if (!(tempWallpaperBgId == 0 || tempWallpaperBgId == -1)) {
                    Glszl_AppAdapter.SaveWallpaperBgTemp(this,tempWallpaperBgId)
                }

                // Save current zipper selection
                if (currentTempZipperId > 0) {
                    Glszl_AppAdapter.SaveZipperTemp(this, currentTempZipperId)
                }
            } else {
                if (currentZipperId <= 0) currentZipperId = 1
                Glszl_AppAdapter.SaveZipperTemp(this, currentTempZipperId)
            }
        }
    }

    private fun doPreviewWhenHadStoragePermission() {
        // Sử dụng vị trí hiện tại để lấy zipper chính xác
        var currentZipper: ZipperImageEntity? = null
        if (!zipperList.isEmpty() && currentPosition < zipperList.size) {
            currentZipper = zipperList[currentPosition]
        }

        if (currentZipper != null) {
            handlePreviewAction(currentZipper)
        } else {
            // Fallback về logic cũ nếu không có data - sử dụng vị trí + 1
            if (this.checkPermissionOverlay(PREVIEW_TYPE)) {
                Glszl_AppAdapter.SaveZipper(this, currentPosition + 1)
                saveZipper("zipper_index", currentPosition + 1)
                Glszl_LockScreenService.IsPreview = true
                Glszl_LockScreenService.Start(this)
            }
        }
    }

    override fun onCreate(bundle: Bundle?) {
        super.onCreate(bundle)
        dataBinding = ActivityPendantStyleBinding.inflate(layoutInflater)
        setContentView(dataBinding.root)
        zipperViewModel = ViewModelProvider(this)[ZipperViewModel::class.java]

        setStatusBarColor(this)
        this.packagePrefs = getSharedPreferences(applicationContext.packageName, 0)
        currentZipperId = Glszl_AppAdapter.getSelectedZiperNumber(this)
        utils = FileNameUtils()
        initialize()
        setupObservers()
        loadZipperData()
        initializeButtons()
    }

    fun saveZipper(str: String?, num: Int) {
        this.packagePrefs!!.edit { putInt(str, num) }
    }

    fun loadZipper(str: String?): Int {
        return this.packagePrefs!!.getInt(str, 0)
    }

    @SuppressLint("ResourceType")
    fun checkPermissionOverlay(type: Int): Boolean {
        try {
            if (Settings.canDrawOverlays(this)) {
                return true
            }
            try {
                logEventTracking(Constants.EventKey.GO_TO_OVERLAY_PERMISSION, Bundle())
            } catch (e: Exception) {
                e("LogEventTracking error: " + e.message)
            }
            this.type = type
            // Navigate to overlay permission screen instead of showing dialog
            startForResult(
                this, true
            )
            // Don't finish() here - wait for result
            return false
        } catch (_: Exception) {
            return true
        }
    }

    /**
     * Xử lý action preview
     */
    private fun handlePreviewAction(zipper: ZipperImageEntity) {
        // Log để debug
        println(
            "Preview action - Zipper ID: " + zipper.id +
                    ", Position: " + currentPosition +
                    ", Content: " + zipper.content
        )

        if (!zipper.content.isEmpty()) {
            // Kiểm tra xem ảnh đã có trong store chưa theo ID
            if (PrivateImageStore.isFileDownloadedById(
                    this,
                    zipper.id,
                    DownloadManager.FOLDER_ZIPPERS,
                    zipper.content
                )
            ) {
                // Ảnh đã có, thực hiện logic preview
                println("File already exists, executing preview logic")
                executePreviewLogic(zipper)
            } else {
                // Ảnh chưa có, download trước
                println("File not found, starting download")
                downloadAndPreview(zipper)
            }
        } else {
            // Không có URL, thực hiện logic preview trực tiếp
            println("No URL, executing preview logic directly")
            executePreviewLogic(zipper)
        }
    }

    /**
     * Xử lý action set
     */
    private fun handleSetAction(zipper: ZipperImageEntity) {
        // Log để debug
        println(
            "Set action - Zipper ID: " + zipper.id +
                    ", Position: " + currentPosition +
                    ", Content: " + zipper.content
        )

        if (!zipper.content.isEmpty()) {
            // Kiểm tra xem ảnh đã có trong store chưa theo ID
//            if (downloadRepository.isFileDownloadedById(this, zipper.getId(), DownloadManager.FOLDER_ZIPPERS, zipper.getContent())) {
            if (PrivateImageStore.isFileDownloadedById(
                    this,
                    zipper.id,
                    DownloadManager.FOLDER_ZIPPERS,
                    zipper.content
                )
            ) {
                // Ảnh đã có, thực hiện logic set
                println("File already exists, executing set logic")
                executeSetLogic(zipper)
            } else {
                // Ảnh chưa có, download trước
                println("File not found, starting download")
                downloadAndSet(zipper)
            }
        } else {
            // Không có URL, thực hiện logic set trực tiếp
            println("No URL, executing set logic directly")
            executeSetLogic(zipper)
        }
    }

    /**
     * Download ảnh và thực hiện preview
     */
    private fun downloadAndPreview(zipper: ZipperImageEntity) {
        try {
            // Sử dụng callback pattern thay vì Flow theo ID
            downloadImageWithCallbackById(zipper, object : DownloadInternalCallback {
                override fun onProgress(progress: Int) {
                }

                override fun onSuccess(filePath: String?) {
                    runOnUiThread { executePreviewLogic(zipper) }
                }

                override fun onFailed(error: String?) {
                    // Download thất bại, thực hiện preview trực tiếp
                    runOnUiThread { executePreviewLogic(zipper) }
                }
            })
        } catch (_: Exception) {
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
        } catch (_: Exception) {
            // Nếu có lỗi, thực hiện set trực tiếp
            runOnUiThread { executeSetLogic(zipper) }
        }
    }

    // Download ảnh với callback pattern theo ID
    private fun downloadImageWithCallbackById(
        zipper: ZipperImageEntity,
        callback: DownloadInternalCallback
    ) {
        try {
//            id, type, url
            val fileName = utils!!.generateFileNameById(
                zipper.id,
                FileNameUtils.FOLDER_ZIPPERS,
                zipper.content
            )
            PrivateImageStore.downloadToInternalFilesAsyncGuardedBySets(
                this,
                zipper.content, fileName, callback
            )
            // Sử dụng DownloadManager trực tiếp với callback pattern theo ID
//            DownloadManager downloadManager = new DownloadManager();
//            downloadManager.downloadImageWithCallbackById(this, zipper.getId(), zipper.getContent(), DownloadManager.FOLDER_ZIPPERS, callback);
        } catch (e: Exception) {
            callback.onFailed(e.message)
        }
    }

    // Thực hiện logic preview
    private fun executePreviewLogic(zipper: ZipperImageEntity) {
        // Hiển thị progressbar
        showLoadingProgress()

        Handler(Looper.getMainLooper()).postDelayed({ // Khởi động preview
            currentTempZipperId = zipper.id
            if (checkPermissionOverlay(PREVIEW_TYPE)) {
                Glszl_LockScreenService.IsPreview = true
                prepareDataBeforePreview(true)
                Glszl_UserDataAdapter.setIsPreview(true)
                Glszl_LockScreenService.Start(this)
            }

            // Ẩn progressbar sau khi hoàn thành
            hideLoadingProgress()
        }, 1000) // Delay 1000ms = 1 giây
    }

    // Thực hiện logic set
    private fun executeSetLogic(zipper: ZipperImageEntity) {
        // Hiển thị progressbar
        showLoadingProgress()

        // Delay 1 giây trước khi thực hiện logic
        Handler(Looper.getMainLooper()).postDelayed({

            if (this.next) {
                currentTempZipperId = zipper.id
                Glszl_AppAdapter.SaveZipperTemp(this, zipper.id)
                doSetWhenHadStoragePermission()
            } else {
                var currentZipper: ZipperImageEntity? = null
                if (!zipperList.isEmpty() && currentPosition < zipperList.size) {
                    currentZipper = zipperList[currentPosition]
                }
                if (currentZipper != null) {
                    try {
                        logEventTracking(
                            Constants.EventKey.SET_ZIP + "PENDANT_" + currentZipper.id
                        )
                    } catch (e: Exception) {
                        e("LogEventTracking error: " + e.message)
                    }
                }
                saveZipper("zipper_index", zipper.id)
                Glszl_AppAdapter.SaveZipper(this, zipper.id)
                // Hiển thị dialog thông báo set thành công
                showSetSuccessDialogAndFinish()
            }

            // Ẩn progressbar sau khi hoàn thành
            hideLoadingProgress()
        }, 1000) // Delay 1000ms = 1 giây
    }

    // Hiển thị progressbar loading
    private fun showLoadingProgress() {
        runOnUiThread { dataBinding.loadingProgressBar.visibility = View.VISIBLE }
    }

    // Ẩn progressbar loading
    private fun hideLoadingProgress() {
        runOnUiThread { dataBinding.loadingProgressBar.visibility = View.GONE }
    }

    override fun onResume() {
        super.onResume()
    }

    // Xử lý kết quả yêu cầu quyền
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
                    getString(R.string.storage_permission_granted_zipper),
                    Toast.LENGTH_SHORT
                ).show()
                if (type == PREVIEW_TYPE) {
                    // Nếu là preview, thực hiện logic preview
                    doPreviewWhenHadStoragePermission()
                } else if (type == SET_TYPE) {
                    // Nếu là set, thực hiện logic set
                    doSetWhenHadStoragePermission()
                }
            } else {
                // Quyền bị từ chối, hiển thị hướng dẫn vào cài đặt
                showPermissionDeniedDialog()
            }
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
                        logEventTracking(Constants.EventKey.DENY_OVERLAY_PERMISSION, Bundle())
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
                        logEventTracking(Constants.EventKey.GRANT_OVERLAY_PERMISSION, Bundle())
                    } catch (e: Exception) {
                        e("LogEventTracking error: " + e.message)
                    }
                    if (type == PREVIEW_TYPE) {
                        // Nếu là preview, thực hiện logic preview
                        doPreviewWhenHadStoragePermission()
                    } else if (type == SET_TYPE) {
                        // Nếu là set, thực hiện logic set
                        doSetWhenHadStoragePermission()
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
        tvDes.text = getString(R.string.set_chain_success)
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

    public override fun onDestroy() {
        super.onDestroy()
        System.gc()
    }

    private fun handleWhenLoadInterDone() {
        // Sử dụng vị trí hiện tại để lấy zipper chính xác
        var currentZipper: ZipperImageEntity? = null
        if (!zipperList.isEmpty() && currentPosition < zipperList.size) {
            currentZipper = zipperList[currentPosition]
        }

        if (currentZipper != null) {
            handleSetAction(currentZipper)
        } else {
            // Fallback về logic cũ nếu không có data - sử dụng vị trí + 1
            if (checkPermissionOverlay(SET_TYPE)) {
                Glszl_AppAdapter.SaveZipper(this, currentPosition + 1)
                saveZipper("zipper_index", currentPosition + 1)
                Glszl_LockScreenService.IsPreview = true
                Glszl_LockScreenService.Start(this)
            }

            // Fallback về logic cũ nếu không có data - sử dụng vị trí + 1
            if (next) {
                Glszl_AppAdapter.SaveZipper(this, currentPosition + 1)
                saveZipper("zipper_index", currentPosition + 1)
                val intent = Intent(this, ActivateZipLockActivity::class.java)
                intent.putExtra("next", true)
                startActivity(intent)
                overridePendingTransition(R.anim.slide_in_right, R.anim.slide_out_left)
            } else {
                Glszl_AppAdapter.SaveZipper(this, currentPosition + 1)
                saveZipper("zipper_index", currentPosition + 1)
            }
            finish()
        }
    }

    companion object {
        var color: Int = "#FFFFFFFF".toColorInt()
        private const val SET_TYPE = 1
        private const val PREVIEW_TYPE = 0
        private const val STORAGE_PERMISSION_REQUEST_CODE = 100
    }
}
