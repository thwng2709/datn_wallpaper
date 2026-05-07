package com.itsthwng.twallpaper.ui.component.preview

import android.app.Dialog
import android.app.WallpaperManager
import android.content.BroadcastReceiver
import android.content.ComponentName
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.graphics.Bitmap.CompressFormat
import android.graphics.Color
import android.graphics.drawable.Drawable
import android.os.Bundle
import android.os.Environment
import android.os.Handler
import android.os.Looper
import android.text.format.DateFormat
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import androidx.activity.result.contract.ActivityResultContracts
import androidx.core.graphics.drawable.toDrawable
import androidx.databinding.DataBindingUtil
import androidx.lifecycle.lifecycleScope
import androidx.media3.common.MediaItem
import androidx.media3.common.PlaybackException
import androidx.media3.common.Player
import androidx.media3.exoplayer.ExoPlayer
import com.bumptech.glide.Glide
import com.bumptech.glide.Priority
import com.bumptech.glide.load.DataSource
import com.bumptech.glide.load.engine.GlideException
import com.bumptech.glide.request.RequestListener
import com.bumptech.glide.request.RequestOptions
import com.downloader.Error
import com.downloader.OnDownloadListener
import com.downloader.PRDownloader
import com.google.gson.Gson
import com.itsthwng.twallpaper.data.model.SettingData
import com.itsthwng.twallpaper.extension.setSafeOnClickListener
import com.itsthwng.twallpaper.local.LocalData
import com.itsthwng.twallpaper.local.LocalStorage
import com.itsthwng.twallpaper.service.SettingWallpaperService
import com.itsthwng.twallpaper.ui.base.BaseActivityBinding
import com.itsthwng.twallpaper.ui.component.preview.viewModel.PreviewViewModel
import com.itsthwng.twallpaper.utils.AppConfig
import com.itsthwng.twallpaper.utils.Constants
import com.itsthwng.twallpaper.utils.Logger
import com.itsthwng.twallpaper.utils.UrlHelper
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.io.ByteArrayInputStream
import java.io.ByteArrayOutputStream
import java.io.File
import java.util.Date
import javax.inject.Inject
import androidx.core.net.toUri
import com.itsthwng.twallpaper.R
import com.itsthwng.twallpaper.databinding.ActivityPreviewBinding
import com.itsthwng.twallpaper.databinding.ItemSetWallpaperBinding

@AndroidEntryPoint
class PreviewActivity : BaseActivityBinding<ActivityPreviewBinding, PreviewViewModel>() {
    @Inject
    lateinit var localStorage: LocalStorage
    private var calledApply: Boolean = false
    lateinit var selectedWall: SettingData.WallpapersItem


    var player: ExoPlayer? = null
    private var coinLeft: Int? = 0
    lateinit var dialogSelectr: Dialog

    // Track pending live wallpaper to verify after user returns from system settings
    private var pendingLiveWallpaper: SettingData.WallpapersItem? = null

    private val liveWallpaperLauncher = registerForActivityResult(
        ActivityResultContracts.StartActivityForResult()
    ) { result ->
        Logger.d(PREVIEW_TAG, "Live wallpaper result: ${result.resultCode}")
        calledApply = false

        if (result.resultCode == RESULT_OK) {
            // User đã set live wallpaper thành công
            Logger.d(PREVIEW_TAG, "User confirmed setting live wallpaper")
            // Áp dụng video pending → video chính
            applyPendingVideo()
            if (pendingLiveWallpaper != null) {
                saveLiveWallpaperToHistory()
                showToast(R.string.wallpaper_added_successfully)
            }
        } else {
            // User đã cancel hoặc back - xóa video pending, giữ nguyên video cũ
            Logger.d(PREVIEW_TAG, "User cancelled live wallpaper setting")
            deletePendingVideo()
            pendingLiveWallpaper = null
        }
    }

    private val wallpaperChangedReceiver = object : BroadcastReceiver() {
        override fun onReceive(context: Context?, intent: Intent?) {
            try {
                if (intent?.action == Intent.ACTION_WALLPAPER_CHANGED) {
                    Logger.d(PREVIEW_TAG, "Wallpaper changed detected via ACTION_WALLPAPER_CHANGED")
                    // Chỉ xử lý nếu không phải đang pending (liveWallpaperLauncher đã xử lý)
                    if (!calledApply && pendingLiveWallpaper == null) {
                        checkWallpaperInfoAndSave()
                    }
                }
            } catch (e: Exception) {
                Logger.e(PREVIEW_TAG, "Error in wallpaper changed receiver: ${e.message}")
                e.printStackTrace()
            }
        }
    }

    override fun attachBaseContext(newBase: Context?) {
        if (newBase != null) {
            val localStorage = LocalData(newBase, "sharedPreferences")
            super.attachBaseContext(
                AppConfig.updateResources(newBase, localStorage.langCode)
            )
        } else {
            super.attachBaseContext(null)
        }
    }

    override fun getContentViewId(): Int {
        return R.layout.activity_preview
    }

    override fun initializeViews() {

    }

    override fun registerListeners() {

    }

    override fun initializeData() {

    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        initView()
        initListeners()
        logTracking()

        // Register broadcast receiver for live wallpaper set events
        if (selectedWall.wallpaperType == 1) {
            try {
                val filter = IntentFilter(Intent.ACTION_WALLPAPER_CHANGED)
                registerReceiver(wallpaperChangedReceiver, filter) // Broadcast runtime không cần flag
                Logger.d(PREVIEW_TAG, "Broadcast receiver registered for live wallpaper events")
            } catch (e: Exception) {
                Logger.e(PREVIEW_TAG, "Error registering broadcast receiver: ${e.message}")
                e.printStackTrace()
            }
        }
    }

    private fun showWhereSelectPopup() {
        dialogSelectr = Dialog(this)
        dialogSelectr.window!!.setBackgroundDrawable(Color.TRANSPARENT.toDrawable())
        var binding: ItemSetWallpaperBinding
        val view = LayoutInflater.from(this)
            .inflate(R.layout.item_set_wallpaper, null, false)
        binding = DataBindingUtil.bind(view)!!


        binding.btnHome.setOnClickListener {
            applyWallPaper(HOME)
            dialogSelectr.dismiss()
        }
        binding.btnBoth.setOnClickListener {
            applyWallPaper(BOTH)

            dialogSelectr.dismiss()

        }

        binding.btnLock.setOnClickListener {
            applyWallPaper(LOCK)

            dialogSelectr.dismiss()

        }
        binding.btnCancel.setOnClickListener {
            dialogSelectr.dismiss()
        }
        dialogSelectr.setContentView(view)
        dialogSelectr.setCancelable(true)
        dialogSelectr.show()
    }


    private fun initListeners() {
        dataBinding.btnApply.setSafeOnClickListener {
            try {
                val isFirstTimeSetWallpaper = localStorage.isFirstTimeSetWallpaper
                if (isFirstTimeSetWallpaper) {
                    AppConfig.logEventTracking(Constants.EventKey.SET_WALLPAPER_1ST)
                    localStorage.isFirstTimeSetWallpaper = false
                } else {
                    AppConfig.logEventTracking(Constants.EventKey.SET_WALLPAPER_2ND)
                }
            } catch (e: Exception) {
                Logger.e("LogEventTracking error: ${e.message}")
            }
            try {
                AppConfig.logEventTracking(Constants.EventKey.SELECT_SET_WALLPAPER)
                AppConfig.logEventTracking(Constants.EventKey.SET_WALLPAPER + "${selectedWall.id}")
            } catch (e: Exception) {
                Logger.e("LogEventTracking error: ${e.message}")
            }
            processApply()
        }
        dataBinding.loutLoader.setOnClickListener {

        }

        dataBinding.loutLoaderDownload.setOnClickListener {

        }

        dataBinding.btnDownload.setSafeOnClickListener {
            try {
                val isFirstTimeDownloadWallpaper = localStorage.isFirstTimeDownloadWallpaper
                if (isFirstTimeDownloadWallpaper) {
                    AppConfig.logEventTracking(Constants.EventKey.DOWNLOAD_WALLPAPER_1ST)
                    localStorage.isFirstTimeDownloadWallpaper = false
                } else {
                    AppConfig.logEventTracking(Constants.EventKey.DOWNLOAD_WALLPAPER_2ND)
                }
            } catch (e: Exception) {
                Logger.e("LogEventTracking error: ${e.message}")
            }
            try {
                AppConfig.logEventTracking(Constants.EventKey.DOWNLOAD_WALLPAPER + "${selectedWall.id}")
            } catch (e: Exception) {
                Logger.e("LogEventTracking error: ${e.message}")
            }
            startDownload()
        }

        dataBinding.cardClose.setOnClickListener {
            finishWithResult()
        }
    }

    override fun onBackPressed() {
        finishWithResult()
        super.onBackPressed()
    }

    private fun finishWithResult() {
        val data = Intent().apply {
            putExtra("id", selectedWall.id)
            putExtra("accessType", selectedWall.accessType)
            putExtra("isDownloaded", selectedWall.isDownloaded)
            putExtra("coinLeft", coinLeft)
        }
        setResult(RESULT_OK, data)
        finish()
    }


    private fun processApply() {
        if (selectedWall.wallpaperType == 0) {
            showWhereSelectPopup()
        } else {
            applyWallPaper(BOTH)
        }
    }

    private fun applyWallPaper(selectedType: Int, onSuccess: () -> Unit = {}) {
        dataBinding.loutLoader.visibility = View.VISIBLE
        if (selectedWall.wallpaperType == 0) {
            // Image wallpaper
            lifecycleScope.launch(Dispatchers.IO) {
                try {
                    val wallpaperManager = WallpaperManager.getInstance(applicationContext)
                    if (!wallpaperManager.isWallpaperSupported || !wallpaperManager.isSetWallpaperAllowed) {
                        withContext(Dispatchers.Main) {
                            dataBinding.loutLoader.visibility = View.GONE
                            showToast(R.string.setting_wallpaper_is_not_allowed)
                        }
                        return@launch
                    }

                    // Load image
                    @Suppress("DEPRECATION")
                    val bitmap = mLoad(UrlHelper.getFullUrl(selectedWall.content))
                    if (bitmap == null) {
                        withContext(Dispatchers.Main) {
                            dataBinding.loutLoader.visibility = View.GONE
                            showToast(R.string.something_went_wrong)
                        }
                        return@launch
                    }

                    // Apply wallpaper based on selection
                    when (selectedType) {
                        BOTH -> {
                            wallpaperManager.setBitmap(bitmap)
                        }

                        HOME -> {
                            val bos = ByteArrayOutputStream()
                            bitmap.compress(CompressFormat.PNG, 0, bos)
                            val bs = ByteArrayInputStream(bos.toByteArray())
                            wallpaperManager.setStream(
                                bs,
                                null,
                                true,
                                WallpaperManager.FLAG_SYSTEM
                            )
                        }

                        LOCK -> {
                            val bos = ByteArrayOutputStream()
                            bitmap.compress(CompressFormat.PNG, 0, bos)
                            val bs = ByteArrayInputStream(bos.toByteArray())
                            wallpaperManager.setStream(
                                bs,
                                null,
                                true,
                                WallpaperManager.FLAG_LOCK
                            )
                        }
                    }
                    viewModel.updateSelectedWallpaper(selectedWall, selectedType)
                    withContext(Dispatchers.Main) {
                        dataBinding.loutLoader.visibility = View.GONE
                        showToast(R.string.wallpaper_added_successfully)
                    }

                } catch (e: Exception) {
                    e.printStackTrace()
                    withContext(Dispatchers.Main) {
                        dataBinding.loutLoader.visibility = View.GONE
                        showToast(R.string.something_went_wrong)
                    }
                }
            }
        } else {
            // Video wallpaper
            downloadFileForSet(UrlHelper.getFullUrl(selectedWall.content), object : OnComplete {
                override fun onComplete() {
                    // Store pending wallpaper info - will verify and save to history via ACTION_WALLPAPER_CHANGED
                    pendingLiveWallpaper = selectedWall
                    dataBinding.loutLoader.visibility = View.GONE
                    // Don't finish activity yet - wait for user to return from settings
                }

                override fun onEroor() {
                    dataBinding.loutLoader.visibility = View.GONE
                    showToast(R.string.something_went_wrong)
                }
            }, onIntent = onSuccess)
        }
    }

    interface OnComplete {
        fun onComplete()
        fun onEroor()
    }

    fun getPathForDownload(): File {
        val state = Environment.getExternalStorageState()
        val filesDir: File? = if (Environment.MEDIA_MOUNTED == state) {
            // We can read and write the media
            getExternalFilesDir(null)
        } else {
            // Load another directory, probably local memory
            filesDir
        }
        return filesDir!!
    }

    fun downloadFileForSet(fileURL: String, onComplete: OnComplete, onIntent: () -> Unit = {}) {
        // Download vào file pending, KHÔNG ghi đè video.mp4 đang dùng
        val pendingFileName = "video_pending.mp4"
        val path: String? = getPathForDownload().path

        if (path != null) {
            val pendingFile = File("$path/$pendingFileName")

            // Xóa file pending cũ nếu có
            if (pendingFile.exists()) {
                pendingFile.delete()
            }
            PRDownloader.download(fileURL, path, pendingFileName).build()
                .setOnStartOrResumeListener { }
                .setOnCancelListener { }
                .setOnProgressListener { }
                .start(object : OnDownloadListener {
                    override fun onDownloadComplete() {
                        // Kiểm tra xem device có hỗ trợ live wallpaper không
                        val wallpaperManager = WallpaperManager.getInstance(this@PreviewActivity)
                        if (!wallpaperManager.isWallpaperSupported || !wallpaperManager.isSetWallpaperAllowed) {
                            Log.w(PREVIEW_TAG, "Device does not support wallpapers or setting is not allowed")
                            showToast(R.string.setting_wallpaper_is_not_allowed)
                            onComplete.onComplete()
                            onIntent.invoke()
                            return
                        }

                        val intent = Intent(
                            WallpaperManager.ACTION_CHANGE_LIVE_WALLPAPER
                        )
                        intent.putExtra(
                            WallpaperManager.EXTRA_LIVE_WALLPAPER_COMPONENT,
                            ComponentName(this@PreviewActivity, SettingWallpaperService::class.java)
                        )
                        calledApply = true

                        // Kiểm tra xem intent có thể resolve được không
                        if (intent.resolveActivity(packageManager) != null) {
                            try {
                                liveWallpaperLauncher.launch(intent)
                                onIntent.invoke()
                            } catch (e: Exception) {
                                Log.e(PREVIEW_TAG, "Error starting live wallpaper activity: ${e.message}")
                                // Xóa pending video nếu có lỗi
                                deletePendingVideo()
                                pendingLiveWallpaper = null
                                showToast(R.string.setting_wallpaper_is_not_allowed)
                                onIntent.invoke()
                            }
                        } else {
                            Log.w(PREVIEW_TAG, "No activity found to handle live wallpaper intent")
                            // Xóa pending video nếu không thể mở màn hình settings
                            deletePendingVideo()
                            pendingLiveWallpaper = null
                            showToast(R.string.setting_wallpaper_is_not_allowed)
                            onIntent.invoke()
                        }
                        onComplete.onComplete()
                    }

                    override fun onError(error: Error?) {
                        onIntent.invoke()
                        onComplete.onEroor()
                        Log.i(PREVIEW_TAG, "onError: " + error?.responseCode)
                    }
                })
        }
    }

    private fun startDownload(onDone: () -> Unit = {}) {
        // Show loader immediately when download button is clicked
        dataBinding.loutLoaderDownload.visibility = View.VISIBLE

        // Use the new permission-aware download method
        downloadWallWithPermissionCheck(selectedWall, object : OnDownload {
            override fun onComplete() {
                dataBinding.loutLoaderDownload.visibility = View.GONE
                val downloadTime = System.currentTimeMillis()
                viewModel.updateDownloadedWallpaperStatus(selectedWall.id!!, downloadTime)
                selectedWall.isDownloaded = downloadTime
                runOnUiThread {
                    showToast(R.string.wallpaper_downloaded_successfully)
                }
                onDone()
            }

            override fun onError() {
                // Hide loader if permission is denied or any error occurs
                dataBinding.loutLoaderDownload.visibility = View.GONE
                runOnUiThread {
                    showToast(R.string.error_saving_image)
                }
                onDone()
            }
        })
    }


    private fun initView() {
        val s = intent.getStringExtra(Constants.wallpaper)
        if (s != null) {
            if (!(::selectedWall.isInitialized)) {
                selectedWall = Gson().fromJson(s, SettingData.WallpapersItem::class.java)
            }

            when (selectedWall.accessType) {
                Constants.IMAGE_PREMIUM_TYPE -> {
                    dataBinding.iconVipLayout.visibility = View.VISIBLE
                    dataBinding.btnIconLock.visibility = View.GONE
                    dataBinding.btnIconPremium.visibility = View.VISIBLE
                }
                Constants.IMAGE_LOCK_TYPE -> {
                    dataBinding.iconVipLayout.visibility = View.VISIBLE
                    dataBinding.btnIconLock.visibility = View.VISIBLE
                    dataBinding.btnIconPremium.visibility = View.GONE
                }
                else -> {
                    dataBinding.iconVipLayout.visibility = View.GONE
                }
            }
            if (selectedWall.wallpaperType == 0) {

                dataBinding.exoPlayerView.visibility = View.GONE
                dataBinding.img.visibility = View.VISIBLE

                setBlur(dataBinding.blurView1, dataBinding.rootLout)
                setBlur(dataBinding.blurView2, dataBinding.rootLout)
                setBlur(dataBinding.blurView3, dataBinding.rootLout)
                setBlur(dataBinding.blurViewIconLock, dataBinding.rootLout)
                setBlur(dataBinding.blurViewIconPremium, dataBinding.rootLout)
                Glide.with(this).load(
                    UrlHelper.getFullUrl(selectedWall.content)
                ).apply(
                    RequestOptions().error(
                        R.color.transparent
                    ).priority(Priority.HIGH)
                )
                    .listener(object : RequestListener<Drawable> {
                        override fun onLoadFailed(
                            e: GlideException?,
                            model: Any?,
                            target: com.bumptech.glide.request.target.Target<Drawable?>?,
                            isFirstResource: Boolean
                        ): Boolean {
                            return false
                        }

                        override fun onResourceReady(
                            resource: Drawable?,
                            model: Any?,
                            target: com.bumptech.glide.request.target.Target<Drawable?>?,
                            dataSource: DataSource?,
                            isFirstResource: Boolean
                        ): Boolean {
                            return false
                        }
                    })
                    .into(dataBinding.img)
            } else {
                dataBinding.loutLoader.visibility = View.VISIBLE
                dataBinding.btnDownload.setCardBackgroundColor(this.getColorStateList(R.color.color_theme_blue_50))
                dataBinding.btnApply.setCardBackgroundColor(this.getColorStateList(R.color.color_theme_blue_50))
                dataBinding.cardClose.setCardBackgroundColor(this.getColorStateList(R.color.color_theme_blue_50))
                initPlayer(UrlHelper.getFullUrl(selectedWall.content))
            }

            val d = Date()
            val date: CharSequence = DateFormat.format("EEEE, dd MMMM", d.time)
            dataBinding.dayDate.text = date
            val time = DateFormat.format("hh:mm", d.time)
            dataBinding.time.text = time

        }

    }

    private fun initPlayer(s: String) {
        try {
            dataBinding.exoPlayerView.visibility = View.VISIBLE
            dataBinding.img.visibility = View.GONE

            // Release existing player to prevent memory leaks
            dataBinding.exoPlayerView.player?.release()
            player = ExoPlayer.Builder(this)
                .build()

            dataBinding.exoPlayerView.player = player

            val mediaItem = MediaItem.fromUri(s.toUri())

            player?.playWhenReady = true
            player?.repeatMode = Player.REPEAT_MODE_ALL
            player?.setMediaItem(mediaItem)
            player?.prepare()
            player?.volume = 0f
            player?.play()
            player?.addListener(object : Player.Listener {

                override fun onPlaybackStateChanged(playbackState: Int) {
                    super.onPlaybackStateChanged(playbackState)
                    if (playbackState == Player.STATE_READY) {
                        runOnUiThread {
                            dataBinding.loutLoader.visibility = View.GONE
                        }
                    }
                }

                override fun onPlayerError(error: PlaybackException) {
                    Logger.e("PreviewActivity", "Player error: ${error.message}")
                    super.onPlayerError(error)
                }
            })
        } catch (e: Exception) {
            Logger.e("PreviewActivity", "Error initializing player: ${e.message}")
            e.printStackTrace()
            dataBinding.loutLoader.visibility = View.GONE
        }
    }

    override fun onResume() {
        super.onResume()

        // Verify if live wallpaper was successfully set
        if (pendingLiveWallpaper != null) {
            verifyAndSaveLiveWallpaper()
        }
    }

    private fun verifyAndSaveLiveWallpaper() {
        try {
            val wallpaperManager = WallpaperManager.getInstance(applicationContext)
            val wallpaperInfo = wallpaperManager.wallpaperInfo

            // Check if our live wallpaper service is currently active
            if (wallpaperInfo != null &&
                wallpaperInfo.serviceName == SettingWallpaperService::class.java.name
            ) {

                // Live wallpaper was successfully set - save to history if not already saved by broadcast
                if (pendingLiveWallpaper != null) {
                    Logger.d(
                        "PreviewActivity",
                        "Fallback verification: Service is running but broadcast not received, saving now"
                    )
                    saveLiveWallpaperToHistory()
                } else {
                    Logger.d(
                        "PreviewActivity",
                        "Live wallpaper already saved by broadcast receiver"
                    )
                }
            } else {
                // User cancelled or chose different wallpaper - don't save to history
                Logger.d("PreviewActivity", "Live wallpaper not set - user may have cancelled")

                // Clear pending state since wallpaper was not set
                pendingLiveWallpaper = null
            }

        } catch (e: Exception) {
            Logger.e("PreviewActivity", "Error verifying live wallpaper: ${e.message}")
            e.printStackTrace()
        }
    }

    private fun saveLiveWallpaperToHistory() {
        pendingLiveWallpaper?.let { wallpaper ->
            // Không nhận được type từ android -> auto HOME
            viewModel.updateSelectedWallpaper(wallpaper, HOME)

            Logger.d("PreviewActivity", "Live wallpaper saved to history: ${wallpaper.id}")

            // Clear pending state after successful save
            pendingLiveWallpaper = null

            // Close activity after successful save
            Handler(Looper.getMainLooper()).postDelayed({
                finishWithResult()
            }, 500)
        }
    }

    private fun checkWallpaperInfoAndSave() {
        try {
            val wallpaperManager = WallpaperManager.getInstance(applicationContext)
            val wallpaperInfo = wallpaperManager.wallpaperInfo

            if (wallpaperInfo == null) {
                Logger.d(PREVIEW_TAG, "WallpaperInfo is null - không phải live wallpaper hoặc chưa set")
                return
            }

            // Kiểm tra xem có phải là live wallpaper của app không
            val expectedComponent = ComponentName(this, SettingWallpaperService::class.java)
            val currentPackageName = packageName
            val isOurWallpaper = wallpaperInfo.component == expectedComponent ||
                    (wallpaperInfo.serviceName == SettingWallpaperService::class.java.name &&
                            wallpaperInfo.packageName == currentPackageName)

            if (isOurWallpaper) {
                Logger.d(PREVIEW_TAG, "✓ XÁC NHẬN: Live wallpaper của app đã được set thành công!")

                // Live wallpaper was successfully set - save to history if not already saved
                if (pendingLiveWallpaper != null) {
                    Logger.d(PREVIEW_TAG, "Saving live wallpaper to history")
                    saveLiveWallpaperToHistory()
                    showToast(R.string.wallpaper_added_successfully)
                }
            } else {
                // Clear pending state since wallpaper was not set
                pendingLiveWallpaper = null
            }

        } catch (e: Exception) {
            Logger.e(PREVIEW_TAG, "Error checking wallpaper info: ${e.message}")
            e.printStackTrace()
        }
    }


    private fun applyPendingVideo() {
        try {
            val path = getPathForDownload().path ?: return
            val mainFile = File("$path/video.mp4")
            val pendingFile = File("$path/video_pending.mp4")

            if (pendingFile.exists()) {
                // Xóa video cũ
                if (mainFile.exists()) {
                    mainFile.delete()
                }
                // Rename pending → main
                pendingFile.renameTo(mainFile)
                Logger.d(PREVIEW_TAG, "Applied pending video as main video")
            }
        } catch (e: Exception) {
            Logger.e(PREVIEW_TAG, "Failed to apply pending video: ${e.message}")
        }
    }

    private fun deletePendingVideo() {
        try {
            val path = getPathForDownload().path ?: return
            val pendingFile = File("$path/video_pending.mp4")
            if (pendingFile.exists()) {
                pendingFile.delete()
                Logger.d(PREVIEW_TAG, "Deleted pending video")
            }
        } catch (e: Exception) {
            Logger.e(PREVIEW_TAG, "Failed to delete pending video: ${e.message}")
        }
    }

    private fun logTracking() {
        if (localStorage.isFirstOpenPreviewScreen) {
            localStorage.isFirstOpenPreviewScreen = false
            AppConfig.logEventTracking(Constants.EventKey.PREVIEW_OPEN_1ST)
        } else {
            AppConfig.logEventTracking(Constants.EventKey.PREVIEW_OPEN_2ND)
        }
    }

    override fun onDestroy() {
        try {
            // Release ExoPlayer to prevent memory leaks
            player?.let {
                it.release()
                player = null
            }
        } catch (e: Exception) {
            Logger.e("PreviewActivity", "Error releasing player: ${e.message}")
        }

        // Dismiss dialog if still showing to prevent memory leaks
        try {
            if (::dialogSelectr.isInitialized && dialogSelectr.isShowing) {
                dialogSelectr.dismiss()
            }
        } catch (e: Exception) {
            Logger.e("PreviewActivity", "Error dismissing dialog: ${e.message}")
        }

        // Unregister broadcast receiver to prevent memory leaks
        try {
            unregisterReceiver(wallpaperChangedReceiver)
            Logger.d("PreviewActivity", "Broadcast receiver unregistered")
        } catch (e: Exception) {
            Logger.e("PreviewActivity", "Error unregistering receiver: ${e.message}")
        }

        super.onDestroy()
    }

    companion object {
        const val HOME = 1
        const val LOCK = 2
        const val BOTH = 3
        private const val PREVIEW_TAG = "PreviewActivity"
    }
}