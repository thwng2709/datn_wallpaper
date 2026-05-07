package com.itsthwng.twallpaper.ui.component.zipper.zipperlock.ui

import android.Manifest
import android.annotation.SuppressLint
import android.content.Context
import android.content.Intent
import android.content.SharedPreferences
import android.content.pm.PackageManager
import android.graphics.Color
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.os.Environment
import android.os.Handler
import android.os.Looper
import android.provider.Settings
import android.view.MotionEvent
import android.view.View
import android.view.Window
import android.widget.AdapterView
import android.widget.GridView
import android.widget.ImageView
import android.widget.ListAdapter
import android.widget.ProgressBar
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.widget.AppCompatButton
import androidx.cardview.widget.CardView
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import androidx.core.content.edit
import androidx.core.graphics.drawable.toDrawable
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProvider
import com.itsthwng.twallpaper.data.entity.ZipperImageEntity
import com.itsthwng.twallpaper.local.LocalData
import com.itsthwng.twallpaper.local.LocalStorage
import com.itsthwng.twallpaper.repository.DownloadRepository
import com.itsthwng.twallpaper.ui.component.zipper.lib.Glszl_GameAdapters.ZipperCompatActivity
import com.itsthwng.twallpaper.ui.component.permission.OverlayPermissionActivity
import com.itsthwng.twallpaper.ui.component.permission.OverlayPermissionActivity.Companion.startForResult
import com.itsthwng.twallpaper.ui.component.zipper.ChainDownloadCallback
import com.itsthwng.twallpaper.ui.component.zipper.FileNameUtils
import com.itsthwng.twallpaper.ui.component.zipper.PrivateImageStore
import com.itsthwng.twallpaper.ui.component.zipper.ZipperViewModel
import com.itsthwng.twallpaper.ui.component.zipper.zipperlock.utils.StatusBarUtils.setStatusBarColor
import com.itsthwng.twallpaper.utils.AppConfig.logEventTracking
import com.itsthwng.twallpaper.utils.AppConfig.updateResources
import com.itsthwng.twallpaper.utils.Constants
import com.itsthwng.twallpaper.utils.Logger.e
import com.itsthwng.twallpaper.utils.download.DownloadCallback
import com.itsthwng.twallpaper.utils.download.DownloadManager
import dagger.hilt.android.AndroidEntryPoint
import java.io.File
import java.util.Objects
import java.util.concurrent.Executors
import javax.inject.Inject
import androidx.core.graphics.toColorInt
import com.itsthwng.twallpaper.R
import com.itsthwng.twallpaper.ui.component.zipper.zipperlock.ui.adapter.ChainStyleGridAdapter

// Zipper Style
@AndroidEntryPoint
class ChainStyleChooserActivity : ZipperCompatActivity() {
    private var currentChainId = -1
    private var zipperViewModel: ZipperViewModel? = null
    private val zipperList: MutableList<ZipperImageEntity> = mutableListOf()
    var adapter: ChainStyleGridAdapter? = null
    var backBtn: ImageView? = null
    var chains: List<String> = listOf(
        "chainleft",
        "chainleft1",
        "chainleft2",
        "chainleft3",
        "chainleft4",
        "chainleft5",
        "chainleft6",
        "chainleft7",
        "chainleft8",
        "chainleft9",
        "chainleft10",
        "chainleft11",
        "chainleft12",
        "chainleft13",
        "chainleft14",
        "chainleft15",
        "chainleft16",
        "chainleft17",
        "chainleft18",
        "chainleft19"
    )
    private var currentPosition = 0
    private var currentTempChainId = 0
    private var currentTempChainType = 0
    var gridView: GridView? = null
    var mContext: Context? = null
    private var next = false
    var packagePrefs: SharedPreferences? = null
    var prefsEditor: SharedPreferences.Editor? = null
    var previewBtn: ImageView? = null
    var setBtn: ImageView? = null
    var setCard: CardView? = null
    var setCount: Int = 0
    var title: TextView? = null
    private var utils: FileNameUtils? = null

    // Download related
    @JvmField
    @Inject
    var downloadRepository: DownloadRepository? = null
    private var loadingProgressBar: ProgressBar? = null


    override fun attachBaseContext(newBase: Context?) {
        if (newBase != null) {
            val localStorage: LocalStorage = LocalData(newBase, "sharedPreferences")
            super.attachBaseContext(updateResources(newBase, localStorage.langCode))
        } else {
            super.attachBaseContext(null)
        }
    }

    private fun initialize() {
        this.gridView = findViewById<View?>(R.id.grid_view) as GridView
        this.backBtn = findViewById<View?>(R.id.back_btn) as ImageView
        this.previewBtn = findViewById<View?>(R.id.preview_btn) as ImageView
        this.setBtn = findViewById<View?>(R.id.set_btn) as ImageView
        this.title = findViewById<View?>(R.id.title_text) as TextView
        this.setCard = findViewById<View?>(R.id.cardView) as CardView
        this.loadingProgressBar = findViewById<View?>(R.id.loading_progress_bar) as ProgressBar?
        if (this.loadingProgressBar != null) {
            this.loadingProgressBar!!.visibility = View.GONE // Ẩn mặc định
        }

        // Đọc chain_index từ SharedPreferences (giờ sẽ là ID thay vì vị trí)
        loadChain("chain_index")

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

    private fun setupViewModel() {
        zipperViewModel = ViewModelProvider(this)[ZipperViewModel::class.java]
    }

    private fun setupObservers() {
        zipperViewModel!!.zippersByType.observe(this) { zippers ->
            if (zippers != null && !zippers.isEmpty()) {
                zipperList.clear()
                zipperList.addAll(zippers)
                // Debug log
                println("Data loaded: " + zipperList.size + " items")

                // Sau khi load data, tìm vị trí của chain đã lưu
                findAndSetCurrentPosition()

                setChains()
            } else {
                // Debug log khi không có data
                println("No data received from ViewModel")
                // Fallback về data cũ nếu cần
                setChainsWithFallback()
            }
        }

        zipperViewModel!!.error.observe(this, Observer { error: String? ->
            if (!error.isNullOrEmpty()) {
                println("Error loading data: $error")
                zipperViewModel!!.clearError()
                // Fallback về data cũ khi có lỗi
                setChainsWithFallback()
            }
        })
    }

    /**
     * Tìm vị trí của chain đã lưu trong list hiện tại
     */
    private fun findAndSetCurrentPosition() {
        if (zipperList.isEmpty()) {
            this.currentPosition = 0
            return
        }

        // Đọc chain_index từ SharedPreferences (giờ sẽ là ID)
        val savedChainId = loadChain("chain_index")
        println("Looking for chain with ID: $savedChainId")

        if (savedChainId > 0) {
            // Tìm vị trí của chain có ID này trong list
            for (i in zipperList.indices) {
                val chain = zipperList[i]
                if (chain.id == savedChainId) {
                    this.currentPosition = i
                    println("Found chain at position: $i with ID: $savedChainId")
                    return
                }
            }
        }

        // Nếu không tìm thấy hoặc savedChainId = 0, set về item đầu tiên
        this.currentPosition = 0
        println("Chain not found, setting to first item (position 0)")
    }

    private fun setChainsWithFallback() {
        println("Using fallback data with " + this.chains.size + " items")

        // Với fallback data, vẫn sử dụng logic mới
        findAndSetCurrentPositionForFallback()

        val chainStyleGridViewAdapter = ChainStyleGridAdapter(
            this,
            chains = this.chains,
            selectedPosition = this.currentPosition
        )
        this.adapter = chainStyleGridViewAdapter
        this.gridView!!.adapter = chainStyleGridViewAdapter as ListAdapter
        this.gridView!!.onItemClickListener =
            AdapterView.OnItemClickListener { adapterView, view, i, j ->
                this@ChainStyleChooserActivity.currentPosition = i
                this@ChainStyleChooserActivity.adapter!!.update(i)

                //                // Lưu chain_index theo vị trí + 1 (cho fallback data)
                //                SaveChain("chain_index", Integer.valueOf(i + 1));
                //                Glszl_AppAdapter.SaveChain(Glszl_ChainStyleChooser.this, Integer.valueOf(i + 1));
                println("Fallback item clicked - Position: " + i + ", Saving as: " + (i + 1))
            }
    }

    /**
     * Tìm vị trí cho fallback data (khi không có zipperList)
     */
    private fun findAndSetCurrentPositionForFallback() {
        val savedChainId = loadChain("chain_index")

        if (savedChainId > 0 && savedChainId <= this.chains.size) {
            // Với fallback data, chain_index vẫn là vị trí (1-based)
            this.currentPosition = savedChainId - 1 // Chuyển về 0-based
            println("Fallback: Using saved position: " + this.currentPosition)
        } else {
            this.currentPosition = 0
            println("Fallback: Setting to first item (position 0)")
        }
    }

    private fun loadZipperData() {
        println("Loading chain data with Constants.ZIPPERS...")
        zipperViewModel!!.loadZippersByType(Constants.CHAINS)
    }

    @SuppressLint("ClickableViewAccessibility")
    fun buttonEffect(imageView: ImageView) {
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

    @SuppressLint("ClickableViewAccessibility")
    fun buttonEffect2(imageView: ImageView) {
        imageView.setOnTouchListener(object : View.OnTouchListener {
            override fun onTouch(view: View?, motionEvent: MotionEvent): Boolean {
                val action = motionEvent.action
                if (action == 0) {
//                    Glszl_ChainStyleChooser.this.setCard.setCardBackgroundColor(Glszl_ChainStyleChooser.this.getResources().getColor(R.color.greenDark));
                    this@ChainStyleChooserActivity.setCard!!.invalidate()
                    return false
                } else if (action != 1) {
                    return false
                } else {
//                    Glszl_ChainStyleChooser.this.setCard.setCardBackgroundColor(Glszl_ChainStyleChooser.this.getResources().getColor(R.color.green));
                    this@ChainStyleChooserActivity.setCard!!.invalidate()
                    return false
                }
            }
        })
    }

    private fun initialzeButtons() {
        buttonEffect(this.previewBtn!!)
        buttonEffect2(this.setBtn!!)

        this.backBtn!!.setOnClickListener { finish() }
        this.previewBtn!!.setOnClickListener {
            // Kiểm tra quyền trước khi cho phép preview
            // Save selection first - sử dụng ID thay vì vị trí
            var currentChain: ZipperImageEntity? = null
            if (!zipperList.isEmpty() && currentPosition < zipperList.size) {
                currentChain = zipperList[currentPosition]
            }

            //                if (currentChain != null) {
            //                    SaveChain("chain_index", Integer.valueOf(currentChain.getId()));
            //                    Glszl_AppAdapter.SaveChain(Glszl_ChainStyleChooser.this, Integer.valueOf(currentChain.getId()));
            //                } else {
            //                    // Fallback nếu không có data
            //                    SaveChain("chain_index", Integer.valueOf(currentPosition + 1));
            //                    Glszl_AppAdapter.SaveChain(Glszl_ChainStyleChooser.this, currentPosition + 1);
            //                }
            if (currentChain != null) {
                handlePreviewAction(currentChain)
            } else {
                // Fallback về logic cũ nếu không có data
                if (this@ChainStyleChooserActivity.checkPermissionOverlay()) {
                    // Fallback logic - sử dụng vị trí + 1
                    saveChain("chain_index", currentPosition + 1)
                    Glszl_AppAdapter.SaveChain(
                        this@ChainStyleChooserActivity,
                        currentPosition + 1
                    )
                    Glszl_LockScreenService.IsPreview = true
                    Glszl_LockScreenService.Start(this@ChainStyleChooserActivity)
                }
            }
            //                if (checkReadStoragePermission()) {
            //                } else {
            //                    requestStoragePermission();
            //                }
        }
        this.setBtn!!.setOnClickListener {
            var currentChain: ZipperImageEntity? = null
            if (!zipperList.isEmpty() && currentPosition < zipperList.size) {
                currentChain = zipperList[currentPosition]
            }
            if (currentChain != null) {
                try {
                    logEventTracking(
                        Constants.EventKey.SELECT_ZIP + "CHAIN_" + currentChain.id,
                        Bundle()
                    )
                } catch (e: Exception) {
                    e("LogEventTracking error: " + e.message)
                }
            }
            if (this@ChainStyleChooserActivity.next) {
                handleWhenLoadInterDone()
            } else {
                handleWhenLoadInterDone()
            }
            setBtn!!.isEnabled = false
        }
        this.title!!.text = getString(R.string.set_zipper_style)
    }

    private fun setChains() {
        if (zipperList.isEmpty()) {
            println("ZipperList is empty, using fallback")
            setChainsWithFallback()
            return
        }

        println("Setting chains with " + zipperList.size + " items")

        val chainStyleGridViewAdapter = ChainStyleGridAdapter(
            this,
            zipperList = zipperList,
            selectedPosition = this.currentPosition,
            isUsingZipperList = true
        )
        this.adapter = chainStyleGridViewAdapter
        this.gridView!!.adapter = chainStyleGridViewAdapter as ListAdapter
        this.gridView!!.onItemClickListener =
            AdapterView.OnItemClickListener { adapterView, view, i, j ->
                this@ChainStyleChooserActivity.currentPosition = i
                this@ChainStyleChooserActivity.adapter!!.update(i)

                // Lưu chain_index theo ID thay vì vị trí
                if (!zipperList.isEmpty() && i < zipperList.size) {
                    // Đang thực hiện lưu Chain ở code này -> Chuyển thành lưu temp hoặc bỏ đi

                    val selectedChain = zipperList[i]
                    //                    SaveChain("chain_index", Integer.valueOf(selectedChain.getId()));
                    //                    Glszl_AppAdapter.SaveChain(Glszl_ChainStyleChooser.this, Integer.valueOf(selectedChain.getId()));
                    println("Zipper item clicked - Position: " + i + ", Chain ID: " + selectedChain.id)
                }
            }
        if (currentChainId >= 1) {
            gridView!!.post { gridView!!.setSelection(currentPosition) }
        }
    }

    override fun onCreate(bundle: Bundle?) {
        super.onCreate(bundle)
        setContentView(R.layout.activity_chain_style_chooser)

        setStatusBarColor(this)
        val applicationContext = getApplicationContext()
        this.mContext = applicationContext
        this.packagePrefs =
            applicationContext.getSharedPreferences(applicationContext.packageName, 0)
        currentChainId = Glszl_AppAdapter.getSelectedChainNumber(this.mContext)
        initialize()
        setupViewModel()
        setupObservers()
        loadZipperData()
        utils = FileNameUtils()
        //        if(getLocalStorage().getCountSetZipperStyle() >= (CommonInfo.INSTANCE.getCount_set_zipper_style() - 1)){
//            initIntertitial();
//        }
        // setChains() sẽ được gọi từ observer
        initialzeButtons()
    }

    public override fun onStart() {
        super.onStart()
        setBtn!!.isEnabled = true
    }

    fun saveChain(str: String?, num: Int) {
        this.packagePrefs!!.edit(commit = true) {
            prefsEditor = this
            putInt(str, num)
        }
    }

    fun loadChain(str: String?): Int {
        return this.packagePrefs!!.getInt(str, 0)
    }

    /**
     * Xử lý action preview
     */
    private fun handlePreviewAction(chain: ZipperImageEntity) {
        // Log để debug
        println("=== CHAIN PREVIEW ACTION DEBUG ===")
        println("Chain ID: " + chain.id)
        println("Position: $currentPosition")
        println("ContentLeft URL: " + chain.contentLeft)
        println("ContentRight URL: " + chain.contentRight)

        // ✅ Sử dụng ContentLeft cho chain style
        var downloadUrl: String? = null
        if (!chain.contentLeft.isEmpty()) {
            downloadUrl = chain.contentLeft
            println("Using ContentLeft for download: $downloadUrl")
        }

        if (!downloadUrl.isNullOrEmpty()) {
            // Kiểm tra xem cả 2 ảnh đã có trong store chưa theo ID
            val isLeftDownloaded = PrivateImageStore.isChainFileDownloadedById(
                this,
                chain.id,
                DownloadManager.FOLDER_CHAINS,
                chain.contentLeft,
                true
            )
            val isRightDownloaded = PrivateImageStore.isChainFileDownloadedById(
                this,
                chain.id,
                DownloadManager.FOLDER_CHAINS,
                chain.contentRight,
                false
            )
            println("Is left file downloaded: $isLeftDownloaded")
            println("Is right file downloaded: $isRightDownloaded")

            if (isLeftDownloaded && isRightDownloaded) {
                // Cả 2 ảnh đã có, thực hiện logic preview
                println("Both files already exist, executing preview logic")
                executePreviewLogic(chain)
            } else {
                // Một hoặc cả 2 ảnh chưa có, download cả 2
                println("One or both files not found, starting download for both")
                downloadChainImagesAndPreview(chain)
            }
        } else {
            // Không có URL, thực hiện logic preview trực tiếp
            println("No URL, executing preview logic directly")
            executePreviewLogic(chain)
        }
    }

    /**
     * Xử lý action set
     */
    private fun handleSetAction(chain: ZipperImageEntity) {
        // ✅ Sử dụng ContentLeft cho chain style
        var downloadUrl: String? = null
        var rightDownloadUrl: String? = null
        if (!chain.contentLeft.isEmpty()) {
            downloadUrl = chain.contentLeft
            rightDownloadUrl = chain.contentRight
        }


        if (rightDownloadUrl != null && !downloadUrl.isNullOrEmpty()) {
            // Kiểm tra xem cả 2 ảnh đã có trong store chưa theo ID
            val isLeftDownloaded = PrivateImageStore.isChainFileDownloadedById(
                this,
                chain.id,
                DownloadManager.FOLDER_CHAINS,
                chain.contentLeft,
                true
            )
            val isRightDownloaded = PrivateImageStore.isChainFileDownloadedById(
                this,
                chain.id,
                DownloadManager.FOLDER_CHAINS,
                chain.contentRight,
                false
            )
            println("Is left file downloaded: $isLeftDownloaded")
            println("Is right file downloaded: $isRightDownloaded")

            if (isLeftDownloaded && isRightDownloaded) {
                // Cả 2 ảnh đã có, thực hiện logic set
                executeSetLogic(chain)
            } else {
                // Một hoặc cả 2 ảnh chưa có, download cả 2
                downloadChainImagesAndSet(chain)
            }
        } else {
            // Không có URL, thực hiện logic set trực tiếp
            executeSetLogic(chain)
        }
    }

    /**
     * Download cả 2 ảnh chain (left và right) và thực hiện preview
     */
    private fun downloadChainImagesAndPreview(chain: ZipperImageEntity) {
        println("=== STARTING CHAIN IMAGES DOWNLOAD AND PREVIEW ===")
        println("Chain ID: " + chain.id)
        println("ContentLeft URL: " + chain.contentLeft)
        println("ContentRight URL: " + chain.contentRight)

        try {
            // Sử dụng hàm download mới cho cả 2 ảnh
            PrivateImageStore.downloadChainImagesInternalAsyncGuardedBySets(
                this,
                chain.id,
                chain.contentLeft,
                chain.contentRight,
                DownloadManager.FOLDER_CHAINS,
                object : ChainDownloadCallback {
                    override fun onProgress(progress: Int) {
                        println("Chain download progress: $progress%")
                    }

                    override fun onSuccess(leftPath: String?, rightPath: String?) {
                        println("Chain download SUCCESS!")
                        println("Left file: $leftPath")
                        println("Right file: $rightPath")
                        runOnUiThread { executePreviewLogic(chain) }
                    }

                    override fun onPartialSuccess(
                        leftPath: String?,
                        rightPath: String?,
                        errors: MutableList<String?>?
                    ) {
                        println("Chain download PARTIAL SUCCESS!")
                        println("Left file: $leftPath")
                        println("Right file: $rightPath")
                        println("Errors: $errors")
                        // Vẫn thực hiện preview nếu có ít nhất 1 file thành công
                        runOnUiThread { executePreviewLogic(chain) }
                    }

                    override fun onFailed(error: String?) {
                        println("Chain download FAILED! Error: $error")
                        // Download thất bại, thực hiện preview trực tiếp
                        runOnUiThread { executePreviewLogic(chain) }
                    }
                }
            )
        } catch (e: Exception) {
            // Nếu có lỗi, thực hiện preview trực tiếp
            println("Chain download EXCEPTION! Error: " + e.message)
            e.printStackTrace()
            runOnUiThread { executePreviewLogic(chain) }
        }
    }

    /**
     * Download ảnh và thực hiện set
     */
    private fun downloadAndSet(chain: ZipperImageEntity, downloadUrl: String) {
        println("=== STARTING DOWNLOAD AND SET ===")
        println("Chain ID: " + chain.id)
        println("Chain URL: $downloadUrl")

        try {
            // Sử dụng callback pattern thay vì Flow theo ID
            downloadImageWithCallbackById(chain, downloadUrl, object : DownloadCallback {
                override fun onProgress(progress: Int) {
                    // Có thể hiển thị progress nếu cần
                    println("Download progress: $progress%")
                }

                override fun onSuccess(filePath: String) {
                    // Download thành công, thực hiện set
                    println("Download SUCCESS! File path: $filePath")
                    runOnUiThread { executeSetLogic(chain) }
                }

                override fun onFailed(error: String) {
                    // Download thất bại, thực hiện set trực tiếp
                    println("Download FAILED! Error: $error")
                    runOnUiThread { executeSetLogic(chain) }
                }
            })
        } catch (e: Exception) {
            // Nếu có lỗi, thực hiện set trực tiếp
            println("Download EXCEPTION! Error: " + e.message)
            e.printStackTrace()
            runOnUiThread { executeSetLogic(chain) }
        }
    }

    /**
     * Download cả 2 ảnh chain (left và right) và thực hiện set
     */
    private fun downloadChainImagesAndSet(chain: ZipperImageEntity) {
        println("=== STARTING CHAIN IMAGES DOWNLOAD AND SET ===")
        println("Chain ID: " + chain.id)
        println("ContentLeft URL: " + chain.contentLeft)
        println("ContentRight URL: " + chain.contentRight)

        try {
            // Sử dụng hàm download mới cho cả 2 ảnh
            PrivateImageStore.downloadChainImagesInternalAsyncGuardedBySets(
                this,
                chain.id,
                chain.contentLeft,
                chain.contentRight,
                DownloadManager.FOLDER_CHAINS,
                object : ChainDownloadCallback {
                    override fun onProgress(progress: Int) {
                    }

                    override fun onSuccess(leftPath: String?, rightPath: String?) {
                        println("Chain download SUCCESS!")
                        println("Left file: $leftPath")
                        println("Right file: $rightPath")
                        runOnUiThread { executeSetLogic(chain) }
                    }

                    override fun onPartialSuccess(
                        leftPath: String?,
                        rightPath: String?,
                        errors: MutableList<String?>?
                    ) {
                        println("Errors: $errors")
                        // Vẫn thực hiện set nếu có ít nhất 1 file thành công
                        runOnUiThread { executeSetLogic(chain) }
                    }

                    override fun onFailed(error: String?) {
                        println("Chain download FAILED! Error: $error")
                        // Download thất bại, thực hiện set trực tiếp
                        runOnUiThread { executeSetLogic(chain) }
                    }
                }
            )
        } catch (e: Exception) {
            // Nếu có lỗi, thực hiện set trực tiếp
            println("Chain download EXCEPTION! Error: " + e.message)
            runOnUiThread { executeSetLogic(chain) }
        }
    }

    /**
     * Download ảnh với callback pattern theo ID
     */
    private fun downloadImageWithCallbackById(
        chain: ZipperImageEntity,
        downloadUrl: String,
        callback: DownloadCallback
    ) {
        println("=== DOWNLOAD IMAGE WITH CALLBACK BY ID ===")
        println("Chain ID: " + chain.id)
        println("Chain URL: $downloadUrl")

        try {
            // Sử dụng DownloadManager trực tiếp với callback pattern theo ID
            val downloadManager = DownloadManager()
            println("DownloadManager instance created successfully")

            println("Calling downloadImageWithCallbackById...")
            downloadManager.downloadImageWithCallbackById(
                this,
                chain.id,
                downloadUrl,
                DownloadManager.FOLDER_CHAINS,
                callback
            )
            println("downloadImageWithCallbackById called successfully")
        } catch (e: Exception) {
            println("EXCEPTION in downloadImageWithCallbackById: " + e.message)
            e.printStackTrace()
            callback.onFailed(e.message!!)
        }
    }

    /**
     * Thực hiện logic preview
     */
    private fun executePreviewLogic(chain: ZipperImageEntity) {
        // Hiển thị progressbar
        showLoadingProgress()

        Handler(Looper.getMainLooper()).postDelayed({ //                // Lưu chain đã chọn
            //                SaveChain("chain_index", Integer.valueOf(chain.getId()));
            //                System.out.println("NINVB: " + Integer.valueOf(chain.getId()));
            //                Glszl_AppAdapter.SaveChain(Glszl_ChainStyleChooser.this, Integer.valueOf(chain.getId()));
            currentTempChainId = chain.id
            currentTempChainType = chain.chainType
            // Khởi động preview
            if (checkPermissionOverlay()) {
                Glszl_LockScreenService.IsPreview = true
                prepareDataBeforePreview()
                Glszl_UserDataAdapter.setIsPreview(true)
                //                    Glszl_LockScreenService.currentChainId = currentChainId;
                Glszl_LockScreenService.Start(this@ChainStyleChooserActivity)
            }

            // Ẩn progressbar sau khi hoàn thành
            hideLoadingProgress()
        }, 1000) // Delay 1000ms = 1 giây
    }

    /**
     * Thực hiện logic set
     */
    private fun executeSetLogic(chain: ZipperImageEntity) {
        // Hiển thị progressbar
        showLoadingProgress()

        // Delay 1 giây trước khi thực hiện logic
        Handler(Looper.getMainLooper()).postDelayed({ // Lưu chain đã chọn
            currentChainId = chain.id
            if (this@ChainStyleChooserActivity.next) {
                // Chuyển đến bước tiếp theo
                try {
                    logEventTracking(Constants.EventKey.GO_TO_PENDANT_STYLE, Bundle())
                } catch (e: Exception) {
                    e("LogEventTracking error: " + e.message)
                }
                Glszl_AppAdapter.SaveChainTemp(this@ChainStyleChooserActivity, chain.id)
                Glszl_AppAdapter.SaveChainTypeTemp(
                    this@ChainStyleChooserActivity,
                    chain.chainType
                )
                val intent = Intent(
                    this@ChainStyleChooserActivity,
                    PendantStyleChooserActivity::class.java
                )
                intent.putExtra("next", true)
                startActivity(intent)
                overridePendingTransition(R.anim.slide_in_right, R.anim.slide_out_left)
            } else {
                try {
                    logEventTracking(
                        Constants.EventKey.SET_ZIP + "CHAIN_" + chain.id,
                        Bundle()
                    )
                } catch (e: Exception) {
                    e("LogEventTracking error: " + e.message)
                }
                // Hoàn thành setup - hiển thị dialog thành công
                saveChain("chain_index", chain.id)
                Glszl_AppAdapter.SaveChain(this@ChainStyleChooserActivity, chain.id)
                Glszl_AppAdapter.SaveChainType(this@ChainStyleChooserActivity, chain.chainType)
                 this@ChainStyleChooserActivity.setCount++
                 if (this@ChainStyleChooserActivity.setCount > 0) {
                     Glszl_SharedPreferencisUtil(this@ChainStyleChooserActivity)
                 }
                 // Hiển thị dialog thông báo set thành công
                 showSetSuccessDialogAndFinish()
             }

            // Ẩn progressbar sau khi hoàn thành
            hideLoadingProgress()
        }, 1000) // Delay 1000ms = 1 giây
    }

    private fun prepareDataBeforePreview() {
        if (this.next) {
            val tempZipperId = Glszl_AppAdapter.getZipperTemp(this@ChainStyleChooserActivity)
            val tempFontId = Glszl_AppAdapter.getFontTemp(this@ChainStyleChooserActivity)
            val tempWallpaperId = Glszl_AppAdapter.getWallpaperTemp(this@ChainStyleChooserActivity)
            val tempWallpaperBgId =
                Glszl_AppAdapter.getWallpaperBgTemp(this@ChainStyleChooserActivity)

            var currentZipper =
                Glszl_AppAdapter.getSelectedZiperNumber(this@ChainStyleChooserActivity)
            var currentChain = Glszl_AppAdapter.getSelectedChainNumber(this@ChainStyleChooserActivity)
            var currentFont = Glszl_AppAdapter.getSelectedFontNumber(this@ChainStyleChooserActivity)
            var currentWallpaper =
                Glszl_AppAdapter.getSelectedWallpaperNumber(this@ChainStyleChooserActivity)
            var currentWallpaperBg =
                Glszl_AppAdapter.getSelectedWallpaperBgNumber(this@ChainStyleChooserActivity)

            // Validate and use defaults if needed
            if (currentZipper <= 0) currentZipper = 1
            if (currentChain <= 0) currentChain = 1
            if (currentFont <= 0) currentFont = 1
            if (currentWallpaper <= 0) currentWallpaper = 1
            if (currentWallpaperBg <= 0) currentWallpaperBg = 1

            if (!(tempZipperId == 0 || tempZipperId == -1)) {
                Glszl_AppAdapter.SaveZipperTemp(this@ChainStyleChooserActivity, tempZipperId)
            }
            if (!(tempFontId == 0 || tempFontId == -1)) {
                Glszl_AppAdapter.SaveFontTemp(this@ChainStyleChooserActivity, tempFontId)
            }
            if (!(tempWallpaperId == 0 || tempWallpaperId == -1)) {
                Glszl_AppAdapter.SaveWallpaperTemp(this@ChainStyleChooserActivity, tempWallpaperId)
            }
            if (!(tempWallpaperBgId == 0 || tempWallpaperBgId == -1)) {
                Glszl_AppAdapter.SaveWallpaperBgTemp(
                    this@ChainStyleChooserActivity,
                    tempWallpaperBgId
                )
            }

            // Save current chain selection
            if (currentTempChainId > 0) {
                Glszl_AppAdapter.SaveChainTemp(this@ChainStyleChooserActivity, currentTempChainId)
                Glszl_AppAdapter.SaveChainTypeTemp(
                    this@ChainStyleChooserActivity,
                    currentTempChainType
                )
            }
        } else {
            if (currentChainId <= 0) currentChainId = 1
            Glszl_AppAdapter.SaveChainTemp(this@ChainStyleChooserActivity, currentTempChainId)
            Glszl_AppAdapter.SaveChainTypeTemp(this@ChainStyleChooserActivity, currentTempChainType)
        }
    }

    /**
     * Hiển thị progressbar loading
     */
    private fun showLoadingProgress() {
        if (this.loadingProgressBar != null) {
            runOnUiThread { loadingProgressBar!!.visibility = View.VISIBLE }
        }
    }

    /**
     * Ẩn progressbar loading
     */
    private fun hideLoadingProgress() {
        if (this.loadingProgressBar != null) {
            runOnUiThread { loadingProgressBar!!.visibility = View.GONE }
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
        tvDes.text = getString(R.string.set_zipper_success)
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

    /**
     * Kiểm tra quyền truy cập storage
     */
    private fun checkStoragePermission(): Boolean {
        // Kiểm tra folder Downloads có tồn tại không
        val downloadDir =
            Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOWNLOADS)
        println("Downloads directory: " + downloadDir.absolutePath)
        println("Downloads directory exists: " + downloadDir.exists())
        println("Downloads directory canWrite: " + downloadDir.canWrite())

        // Kiểm tra folder chains
        val chainsDir = File(downloadDir, "chains")
        println("Chains directory: " + chainsDir.absolutePath)
        println("Chains directory exists: " + chainsDir.exists())

        if (!chainsDir.exists()) {
            val created = chainsDir.mkdirs()
            println("Creating chains directory: $created")
        }

        println("Chains directory after creation: " + chainsDir.exists())
        println("Chains directory canWrite: " + chainsDir.canWrite())

        // Chỉ yêu cầu quyền nếu thực sự cần thiết (Android 10+)
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
            // Android 10+ sử dụng scoped storage, không cần quyền WRITE_EXTERNAL_STORAGE
            println("Android 10+, using scoped storage - no additional permissions needed")
            return true
        } else if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            // Android 6-9 cần quyền WRITE_EXTERNAL_STORAGE
            val writePermission = checkSelfPermission(Manifest.permission.WRITE_EXTERNAL_STORAGE)
            val readPermission = checkSelfPermission(Manifest.permission.READ_EXTERNAL_STORAGE)

            println("Storage permissions - Write: $writePermission, Read: $readPermission")

            if (writePermission != PackageManager.PERMISSION_GRANTED ||
                readPermission != PackageManager.PERMISSION_GRANTED
            ) {
                println("Storage permissions not granted, requesting...")
                requestPermissions(
                    arrayOf(
                        Manifest.permission.WRITE_EXTERNAL_STORAGE,
                        Manifest.permission.READ_EXTERNAL_STORAGE
                    ),
                    1001
                )
                return false
            }
        }

        return true
    }

    /**
     * Kiểm tra quyền truy cập storage
     */
    private fun checkReadStoragePermission(): Boolean {
        return if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            // Android 13+ sử dụng READ_MEDIA_IMAGES
            ContextCompat.checkSelfPermission(
                this,
                Manifest.permission.READ_MEDIA_IMAGES
            ) == PackageManager.PERMISSION_GRANTED
        } else {
            // Android 12 trở xuống sử dụng READ_EXTERNAL_STORAGE
            ContextCompat.checkSelfPermission(
                this,
                Manifest.permission.READ_EXTERNAL_STORAGE
            ) == PackageManager.PERMISSION_GRANTED
        }
    }

    /**
     * Yêu cầu quyền truy cập storage
     */
    private fun requestStoragePermission() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            // Android 13+ sử dụng READ_MEDIA_IMAGES
            if (ContextCompat.checkSelfPermission(this, Manifest.permission.READ_MEDIA_IMAGES)
                != PackageManager.PERMISSION_GRANTED
            ) {
                ActivityCompat.requestPermissions(
                    this,
                    arrayOf(Manifest.permission.READ_MEDIA_IMAGES),
                    STORAGE_PERMISSION_REQUEST_CODE
                )
            }
        } else {
            // Android 12 trở xuống sử dụng READ_EXTERNAL_STORAGE
            if (ContextCompat.checkSelfPermission(this, Manifest.permission.READ_EXTERNAL_STORAGE)
                != PackageManager.PERMISSION_GRANTED
            ) {
                ActivityCompat.requestPermissions(
                    this,
                    arrayOf(Manifest.permission.READ_EXTERNAL_STORAGE),
                    STORAGE_PERMISSION_REQUEST_CODE
                )
            }
        }
    }


    override fun onRequestPermissionsResult(
        requestCode: Int,
        permissions: Array<String?>,
        grantResults: IntArray
    ) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)

        if (requestCode == 1001) {
            if (grantResults.isNotEmpty() && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                println("Storage permissions granted!")
                checkStoragePermission() // Kiểm tra lại
            }
        } else if (requestCode == STORAGE_PERMISSION_REQUEST_CODE) {
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

    override fun onResume() {
        super.onResume()
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

    @SuppressLint("ResourceType")
    fun checkPermissionOverlay(): Boolean {
        try {
            if (Settings.canDrawOverlays(this)) {
                return true
            }
            try {
                logEventTracking(Constants.EventKey.GO_TO_OVERLAY_PERMISSION, Bundle())
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
        if (checkPermissionOverlay()) {
            // Sử dụng vị trí hiện tại để lấy chain chính xác
            var currentChain: ZipperImageEntity? = null
            if (!zipperList.isEmpty() && currentPosition < zipperList.size) {
                currentChain = zipperList[currentPosition]
            }
            if (currentChain != null) {
                currentTempChainId = currentChain.id
                // Sử dụng ID thay vì vị trí
                Glszl_LockScreenService.IsPreview = true
                prepareDataBeforePreview()
                Glszl_LockScreenService.Start(this@ChainStyleChooserActivity)
            }
        }
    }

    private fun handleWhenLoadInterDone() {
        // Sử dụng vị trí hiện tại để lấy chain chính xác
        var currentChain: ZipperImageEntity? = null
        if (!zipperList.isEmpty() && currentPosition < zipperList.size) {
            currentChain = zipperList[currentPosition]
        }

        if (currentChain != null) {
            handleSetAction(currentChain)
        } else {
            // Fallback về logic cũ nếu không có data - sử dụng vị trí + 1
            if (next) {
                saveChain("chain_index", currentPosition + 1)
                Glszl_AppAdapter.SaveChain(
                    this,
                    currentPosition + 1
                )
                val intent =
                    Intent(this, PendantStyleChooserActivity::class.java)
                intent.putExtra("next", true)
                startActivity(intent)
                overridePendingTransition(
                    R.anim.slide_in_right,
                    R.anim.slide_out_left
                )
            } else {
                saveChain("chain_index", currentPosition + 1)
                Glszl_AppAdapter.SaveChain(
                    this,
                    currentPosition + 1
                )
                setCount++
                if (setCount > 0) {
                    Glszl_SharedPreferencisUtil(this)
                }
            }
            finish()
        }
    }

    companion object {
        var color: Int = "#FFFFFFFF".toColorInt()
        private const val STORAGE_PERMISSION_REQUEST_CODE = 100
    }
}
