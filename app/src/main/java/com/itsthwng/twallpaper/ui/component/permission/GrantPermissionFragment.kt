package com.itsthwng.twallpaper.ui.component.permission

import android.animation.AnimatorSet
import android.animation.ObjectAnimator
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.net.Uri
import android.os.Build
import android.os.Environment
import android.os.PowerManager
import android.provider.Settings
import android.provider.Settings.ACTION_APPLICATION_DETAILS_SETTINGS
import android.view.View
import android.view.animation.DecelerateInterpolator
import android.widget.CompoundButton
import androidx.activity.result.ActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.annotation.RequiresApi
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import androidx.fragment.app.DialogFragment
import com.itsthwng.twallpaper.ui.base.BaseFragmentBinding
import com.itsthwng.twallpaper.ui.component.setting.view.SettingActivity
import com.itsthwng.twallpaper.utils.AppConfig.logEventTracking
import com.itsthwng.twallpaper.utils.CommonUtil
import com.itsthwng.twallpaper.utils.Constants
import com.itsthwng.twallpaper.utils.Constants.PERMISSIONS
import dagger.hilt.android.AndroidEntryPoint
import androidx.core.net.toUri
import com.itsthwng.twallpaper.BuildConfig
import com.itsthwng.twallpaper.R
import com.itsthwng.twallpaper.databinding.FragmentGrantPermissionBinding

@AndroidEntryPoint
class GrantPermissionFragment : BaseFragmentBinding<FragmentGrantPermissionBinding>() {
    private var requestSaveAsBottomSheet : RequestSaveAsPermissionBottomSheet? = null

    private val activityResultLauncher = registerForActivityResult(
        ActivityResultContracts.RequestMultiplePermissions()
    ) { permissions ->
        val deniedPermissions = permissions.filterValues { !it }

        if (deniedPermissions.isNotEmpty()) {
            dataBinding.swSaveAsPermission.isChecked = false
            dataBinding.swSaveAsPermission.visibility = View.VISIBLE
            dataBinding.tvSaveAsPermissionAllowed.visibility = View.GONE
        } else {
            dataBinding.swSaveAsPermission.isChecked = true
            dataBinding.swSaveAsPermission.visibility = View.GONE
            dataBinding.tvSaveAsPermissionAllowed.visibility = View.VISIBLE
        }
    }
    private val requestPermissionLauncher =
        registerForActivityResult(ActivityResultContracts.RequestPermission()) { result: Boolean ->
            if (result) {
                dataBinding.swNotification.isChecked = true
                dataBinding.swNotification.visibility = View.GONE
                dataBinding.tvNotificationAllowed.visibility = View.VISIBLE
            }
        }

    private val startForResult = registerForActivityResult(
        ActivityResultContracts.StartActivityForResult()
    ) { _: ActivityResult ->
        showBatteryState()
    }

    private var grantedCallback: (() -> Unit)? = null
    @RequiresApi(Build.VERSION_CODES.R)
    val storagePermissionResultLauncher = registerForActivityResult(
        ActivityResultContracts.StartActivityForResult()
    ) {
        showManageFileState()
    }

    private val requestManageFilePermissionLauncher = registerForActivityResult(
        ActivityResultContracts.RequestMultiplePermissions()
    ) { _ ->
        showManageFileState()
    }

    private val overlayPermissionLauncher = registerForActivityResult(
        ActivityResultContracts.StartActivityForResult()
    ) { _ ->
        // Update the switch state without triggering the listener
        updateOverlayPermissionState()
    }

    private var isRequestNotificationPermission: Boolean = false
    private var overlayPermissionListener: CompoundButton.OnCheckedChangeListener? = null


    override fun getContentViewId() = R.layout.fragment_grant_permission

    override fun initializeViews() {
        // Setup status bar
        setupStatusBar()
        checkPermissionAndBindView()
        logTracking()
        setupAnimations()

        // Hide/show permissions based on Android version
        setupPermissionVisibilityBasedOnAndroidVersion()
    }

    private fun setupAnimations() {
        // Set initial states for views that exist
        dataBinding.illustrationContainer.apply {
            alpha = 0f
            scaleX = 0.9f
            scaleY = 0.9f
        }

        dataBinding.contentContainer.apply {
            alpha = 0f
            translationY = 30f
        }

        // Animate header if exists
        dataBinding.headerContainer.apply {
            alpha = 0f
            translationY = -20f
            animate()
                .alpha(1f)
                .translationY(0f)
                .setDuration(500)
                .setInterpolator(DecelerateInterpolator())
                .start()
        }

        // Animate illustration if exists
        dataBinding.illustrationContainer.let { container ->
            val illustrationAnimator = AnimatorSet().apply {
                playTogether(
                    ObjectAnimator.ofFloat(container, View.ALPHA, 0f, 1f),
                    ObjectAnimator.ofFloat(container, View.SCALE_X, 0.9f, 1f),
                    ObjectAnimator.ofFloat(container, View.SCALE_Y, 0.9f, 1f)
                )
                duration = 600
                startDelay = 200
                interpolator = DecelerateInterpolator()
            }
            illustrationAnimator.start()
        }

        // Animate content if exists
        dataBinding.contentContainer.let { content ->
            val contentAnimator = AnimatorSet().apply {
                playTogether(
                    ObjectAnimator.ofFloat(content, View.ALPHA, 0f, 1f),
                    ObjectAnimator.ofFloat(content, View.TRANSLATION_Y, 30f, 0f)
                )
                duration = 500
                startDelay = 400
                interpolator = DecelerateInterpolator()
            }
            contentAnimator.start()
        }

        // Animate permission items with stagger if container exists
        dataBinding.permissionListContainer.apply {
            for (i in 0 until childCount) {
                getChildAt(i)?.apply {
                    alpha = 0f
                    translationX = -30f
                    animate()
                        .alpha(1f)
                        .translationX(0f)
                        .setDuration(400)
                        .setStartDelay(500L + (i * 100L))
                        .setInterpolator(DecelerateInterpolator())
                        .start()
                }
            }
        }

        // Add floating animation to illustration if it exists
        startFloatingAnimation()
    }

    private fun startFloatingAnimation() {
        dataBinding.imgIllustration.let { img ->
            val floatUp = ObjectAnimator.ofFloat(
                img,
                View.TRANSLATION_Y,
                0f,
                -15f
            ).apply {
                duration = 2000
                interpolator = DecelerateInterpolator()
            }

            val floatDown = ObjectAnimator.ofFloat(
                img,
                View.TRANSLATION_Y,
                -15f,
                0f
            ).apply {
                duration = 2000
                interpolator = DecelerateInterpolator()
            }

            val animatorSet = AnimatorSet().apply {
                playSequentially(floatUp, floatDown)
                addListener(object : android.animation.Animator.AnimatorListener {
                    override fun onAnimationEnd(animation: android.animation.Animator) {
                        start() // Repeat animation
                    }
                    override fun onAnimationStart(animation: android.animation.Animator) {}
                    override fun onAnimationCancel(animation: android.animation.Animator) {}
                    override fun onAnimationRepeat(animation: android.animation.Animator) {}
                })
            }

            animatorSet.start()
        }
    }

    private fun logTracking(){
        if(localStorage.isFirstOpenGrantPermissionScreen){
            localStorage.isFirstOpenGrantPermissionScreen = false
            logEventTracking(Constants.EventKey.GRANTPER_OPEN_1ST)
        } else {
            logEventTracking(Constants.EventKey.GRANTPER_OPEN_2ND)
        }
    }

    private fun showBatteryState() {
        dataBinding.apply {
            context?.let {
                if (isIgnoringBatteryOptimizations(it)) {
                    dataBinding.swBattery.isChecked = true
                    dataBinding.swBattery.visibility = View.GONE
                    dataBinding.tvBatteryAllowed.visibility = View.VISIBLE
                } else {
                    dataBinding.swBattery.isChecked = false
                    dataBinding.swBattery.visibility = View.VISIBLE
                    dataBinding.tvBatteryAllowed.visibility = View.GONE
                }
            }
        }
    }

    private fun isIgnoringBatteryOptimizations(context: Context): Boolean {
        val powerManager = context.getSystemService(Context.POWER_SERVICE) as PowerManager
        return powerManager.isIgnoringBatteryOptimizations(context.packageName)
    }

    private fun checkPermissionAndBindView() {
        if (Build.VERSION.SDK_INT < 33) {
            // Nếu Android SDK dưới 33, không cần quyền POST_NOTIFICATIONS
            dataBinding.swNotification.isChecked = true
            dataBinding.swNotification.visibility = View.GONE
            dataBinding.tvNotificationAllowed.visibility = View.VISIBLE
        } else if (ContextCompat.checkSelfPermission(
                requireContext(),
                "android.permission.POST_NOTIFICATIONS"
            ) == PackageManager.PERMISSION_GRANTED
        ) {
            // Nếu quyền đã được cấp, chạy dịch vụ
            dataBinding.swNotification.isChecked = true
            dataBinding.swNotification.visibility = View.GONE
            dataBinding.tvNotificationAllowed.visibility = View.VISIBLE
            isRequestNotificationPermission = true
        } else {
            dataBinding.swNotification.isChecked = false
            dataBinding.swNotification.visibility = View.VISIBLE
            dataBinding.tvNotificationAllowed.visibility = View.GONE
            isRequestNotificationPermission = true
        }

        // Handle Write Storage permission only for Android <= 10
        if (Build.VERSION.SDK_INT <= Build.VERSION_CODES.Q) {
            if(hasSaveAsPermissions()){
                dataBinding.swSaveAsPermission.isChecked = true
                dataBinding.swSaveAsPermission.visibility = View.GONE
                dataBinding.tvSaveAsPermissionAllowed.visibility = View.VISIBLE
            } else {
                dataBinding.swSaveAsPermission.isChecked = false
                dataBinding.swSaveAsPermission.visibility = View.VISIBLE
                dataBinding.tvSaveAsPermissionAllowed.visibility = View.GONE
            }
        }

        showManageFileState()
        showBatteryState()
        updateOverlayPermissionState()
    }

    override fun onBackPressed(): Boolean {
        if((activity as SettingActivity).isShowGrantPermission){
            handleWhenLoadInterDone()
        } else {
            findNavControllerSafety()?.navigateUp()
        }
        return true
    }

    override fun registerListeners() {
        dataBinding.btnBack.setOnClickListener {
            if((activity as SettingActivity).isShowGrantPermission){
                handleWhenLoadInterDone()
            } else {
                findNavControllerSafety()?.navigateUp()
            }
        }

        dataBinding.swSaveAsPermission.setOnCheckedChangeListener { buttonView, isChecked ->
            if(isChecked){
                requestSaveAsPermission {
                    if(hasSaveAsPermissions()){
                        dataBinding.swSaveAsPermission.isChecked = true
                        dataBinding.swSaveAsPermission.visibility = View.GONE
                        dataBinding.tvSaveAsPermissionAllowed.visibility = View.VISIBLE
                    } else {
                        dataBinding.swSaveAsPermission.isChecked = false
                        dataBinding.swSaveAsPermission.visibility = View.VISIBLE
                        dataBinding.tvSaveAsPermissionAllowed.visibility = View.GONE
                    }
                }
            }
        }

        dataBinding.swNotification.setOnCheckedChangeListener { buttonView, isChecked ->
            if (isChecked) {
                if (ContextCompat.checkSelfPermission(
                        requireContext(),
                        "android.permission.POST_NOTIFICATIONS"
                    ) != 0
                ) {
                    if (ActivityCompat.shouldShowRequestPermissionRationale(
                            requireActivity(),
                            "android.permission.POST_NOTIFICATIONS"
                        )
                    ) {
                        requestPermissionLauncher.launch("android.permission.POST_NOTIFICATIONS")
                        localStorage.isFirstNotificationPermissionRequire = false
                    } else {
                        if(localStorage.isFirstNotificationPermissionRequire){
                            requestPermissionLauncher.launch("android.permission.POST_NOTIFICATIONS")
                        } else {
                            isRequestNotificationPermission = false
                            val intent = if(Build.VERSION.SDK_INT >= 33){
                                Intent(Settings.ACTION_APP_NOTIFICATION_SETTINGS)
                                    .addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
                                    .putExtra(Settings.EXTRA_APP_PACKAGE, requireContext().packageName)
                            } else {
                                Intent(
                                    ACTION_APPLICATION_DETAILS_SETTINGS
                                )
                                    .putExtra("app_package", requireContext().packageName)
                                    .putExtra("app_uid", requireContext().applicationInfo.uid)
                            }
                            startActivity(intent)
                        }
                    }
                }
            } else {
                if(Build.VERSION.SDK_INT >= 33) {

                } else {
                    dataBinding.swNotification.isChecked = true
                }
            }
        }
        dataBinding.swManageFile.setOnCheckedChangeListener { buttonView, isChecked ->
//            logEventTracking(Constants.PERMISSION_BATTERY_ALLOW)
            if (isChecked) {
                if (Build.VERSION.SDK_INT > Build.VERSION_CODES.Q) {
                    // Android > 10: Request Manage Files permission
                    requestPermission { showManageFileState() }
                } else {
                    // Android <= 10: Request Write Storage permission
                    requestSaveAsPermission {
                        showManageFileState()
                    }
                }
            }
        }
        dataBinding.swBattery.setOnCheckedChangeListener { buttonView, isChecked ->
//            AppConfig.logEventTracking(Constants.PERMISSION_BATTERY_ALLOW)
            if(isChecked){
                val intentSettings = Intent(Settings.ACTION_REQUEST_IGNORE_BATTERY_OPTIMIZATIONS)
                with(intentSettings) {
                    context?.let {
                        data = Uri.fromParts("package", it.packageName, null)
                        addCategory(Intent.CATEGORY_DEFAULT)
                    }
                }
                startForResult.launch(intentSettings)
            }
        }

        // Create listener and store reference
        overlayPermissionListener = CompoundButton.OnCheckedChangeListener { _, isChecked ->
            // Check current permission state
            val currentPermissionState = hasOverlayPermission()
            
            when {
                // User wants to enable but permission not granted
                isChecked && !currentPermissionState -> {
                    requestOverlayPermission()
                }
                // User wants to disable but permission is granted
                !isChecked && currentPermissionState -> {
                    // Navigate to settings for user to revoke
                    requestOverlayPermission()
                }
                // If state doesn't match permission (e.g., isChecked=true but no permission)
                // This shouldn't happen in normal flow, but let's handle it
                isChecked != currentPermissionState -> {
                    // Update switch to match actual permission state
                    dataBinding.swOverlay.setOnCheckedChangeListener(null)
                    dataBinding.swOverlay.isChecked = currentPermissionState
                    // Re-attach this same listener
                    dataBinding.swOverlay.setOnCheckedChangeListener(overlayPermissionListener)
                }
                else -> {
                    // State already matches, no action needed
                }
            }
        }
        
        dataBinding.swOverlay.setOnCheckedChangeListener(overlayPermissionListener)
    }

    override fun initializeData() {

    }

    override fun onResume() {
        super.onResume()
        val hasNotificationPermission =
            ContextCompat.checkSelfPermission(
                requireContext(),
                "android.permission.POST_NOTIFICATIONS"
            ) == PackageManager.PERMISSION_GRANTED
                    || Build.VERSION.SDK_INT < 33
        dataBinding.swNotification.isChecked = hasNotificationPermission
        if (hasNotificationPermission) {
            dataBinding.swNotification.visibility = View.GONE
            dataBinding.tvNotificationAllowed.visibility = View.VISIBLE
        } else {
            dataBinding.swNotification.visibility = View.VISIBLE
            dataBinding.tvNotificationAllowed.visibility = View.GONE
        }

        isRequestNotificationPermission = true

        // Handle Write Storage permission only for Android <= 10
        if (Build.VERSION.SDK_INT <= Build.VERSION_CODES.Q) {
            if(hasSaveAsPermissions()){
                dataBinding.swSaveAsPermission.isChecked = true
                dataBinding.swSaveAsPermission.visibility = View.GONE
                dataBinding.tvSaveAsPermissionAllowed.visibility = View.VISIBLE
            } else {
                dataBinding.swSaveAsPermission.isChecked = false
                dataBinding.swSaveAsPermission.visibility = View.VISIBLE
                dataBinding.tvSaveAsPermissionAllowed.visibility = View.GONE
            }
        }

        showManageFileState()
        showBatteryState()
        updateOverlayPermissionState()
    }

    private fun requestPermission(grantedCallback: () -> Unit) {
        this.grantedCallback = grantedCallback
        when {
            Build.VERSION.SDK_INT >= Build.VERSION_CODES.R && !Environment.isExternalStorageManager() -> {
                val intent = Intent(
                    Settings.ACTION_MANAGE_APP_ALL_FILES_ACCESS_PERMISSION,
                    ("package:" + BuildConfig.APPLICATION_ID).toUri()
                )
                storagePermissionResultLauncher.launch(intent)
            }

            Build.VERSION.SDK_INT < Build.VERSION_CODES.R -> {
//                val intent = Intent()
//                intent.action = Settings.ACTION_APPLICATION_DETAILS_SETTINGS
//                val uri = Uri.fromParts("package", activity?.packageName, null)
//                intent.data = uri
//                activity?.startActivity(intent)
                requestManageFilePermissionLauncher.launch(Constants.PERMISSIONS)
            }

            else -> {
                this.grantedCallback?.invoke()
            }
        }
    }

    private fun hasSaveAsPermissions(): Boolean {
        return if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            PERMISSIONS.all { permission ->
                ContextCompat.checkSelfPermission(requireContext(), permission) == PackageManager.PERMISSION_GRANTED
            }
        } else if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R) {
            PERMISSIONS.all { permission ->
                ContextCompat.checkSelfPermission(requireContext(), permission) == PackageManager.PERMISSION_GRANTED
            }
        } else {
            CommonUtil.hasPermissions(
                permissions = PERMISSIONS,
                activity = requireActivity()
            )
        }
    }

    private fun showManageFileState() {
        dataBinding.apply {
            if (Build.VERSION.SDK_INT > Build.VERSION_CODES.Q) {
                if(Build.VERSION.SDK_INT >= Build.VERSION_CODES.R){
                    cardManageFiles.visibility = View.GONE
                    return@apply
                }
                // Android > 10: Handle Manage Files permission
                if (hasManageFilePermission()) {
                    swManageFile.isChecked = true
                    swManageFile.visibility = View.GONE
                    tvManageFileAllowed.visibility = View.VISIBLE
                } else {
                    swManageFile.isChecked = false
                    swManageFile.visibility = View.VISIBLE
                    tvManageFileAllowed.visibility = View.GONE
                }
            } else {
                // Android <= 10: Handle Write Storage permission in Manage Files UI
                if (hasSaveAsPermissions()) {
                    swManageFile.isChecked = true
                    swManageFile.visibility = View.GONE
                    tvManageFileAllowed.visibility = View.VISIBLE
                } else {
                    swManageFile.isChecked = false
                    swManageFile.visibility = View.VISIBLE
                    tvManageFileAllowed.visibility = View.GONE
                }
            }
        }
    }
    private fun hasManageFilePermission(): Boolean {
        return if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R) {
            Environment.isExternalStorageManager()
        } else {
            CommonUtil.hasPermissions(
                permissions = PERMISSIONS,
                activity = requireActivity()
            )
        }
    }

    private fun hasOverlayPermission(): Boolean {
        return Settings.canDrawOverlays(requireContext())
    }

    private fun requestOverlayPermission() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            val intent = Intent(
                Settings.ACTION_MANAGE_OVERLAY_PERMISSION,
                Uri.parse("package:${requireContext().packageName}")
            )
            overlayPermissionLauncher.launch(intent)
        }
    }

    private fun updateOverlayPermissionState() {
        dataBinding.apply {
            // Remove the listener temporarily to avoid triggering it
            swOverlay.setOnCheckedChangeListener(null)
            
            if (hasOverlayPermission()) {
                swOverlay.isChecked = true
                swOverlay.visibility = View.GONE
                tvOverlayAllowed.visibility = View.VISIBLE
            } else {
                swOverlay.isChecked = false
                swOverlay.visibility = View.VISIBLE
                tvOverlayAllowed.visibility = View.GONE
            }
            
            // Re-attach the stored listener
            swOverlay.setOnCheckedChangeListener(overlayPermissionListener)
        }
    }

    private fun initRequestSaveAsPermissionBottomSheet(){
        requestSaveAsBottomSheet = RequestSaveAsPermissionBottomSheet()
        requestSaveAsBottomSheet?.updateLanguage(context, localStorage.langCode)
        requestSaveAsBottomSheet?.setStyle(DialogFragment.STYLE_NORMAL, R.style.AppBottomSheetDialogTheme)

        requestSaveAsBottomSheet?.clickConfirmYes = {
            val intent = Intent(
                ACTION_APPLICATION_DETAILS_SETTINGS,
                Uri.fromParts("package", requireActivity().packageName, null)
            )
            startActivity(intent)
        }
        requestSaveAsBottomSheet?.clickConfirmNo = {
            showToast(getString(R.string.permission_denied))
            dataBinding.swSaveAsPermission.isChecked = false
            dataBinding.swSaveAsPermission.visibility = View.VISIBLE
            dataBinding.tvSaveAsPermissionAllowed.visibility = View.GONE
        }

        if (activity?.isFinishing == false) {
            activity?.supportFragmentManager?.let {
                requestSaveAsBottomSheet?.show(
                    it, RequestSaveAsPermissionBottomSheet.TAG
                )
            }
        }
    }

    private fun requestSaveAsPermission(grantedCallback: () -> Unit) {
        this.grantedCallback = grantedCallback
        val permissionsToRequest = PERMISSIONS.filter { permission ->
            ContextCompat.checkSelfPermission(
                requireContext(),
                permission
            ) != PackageManager.PERMISSION_GRANTED
        }
        if (permissionsToRequest.isNotEmpty()) {
            // Check if any permission(s) need the rationale
            val shouldShowRationale = permissionsToRequest.any { permission ->
                ActivityCompat.shouldShowRequestPermissionRationale(requireActivity(), permission)
            }

            if (shouldShowRationale) {
                // shouldShowRationale is true means user has denied permission(s) before
                // Show a dialog to explain why user needs to grant the permission
                activityResultLauncher.launch(permissionsToRequest.toTypedArray())
                localStorage.isFirstRecoverySLSaveAs = false
            } else {
                if (localStorage.isFirstRecoverySLSaveAs) {
                    // First time use this Save as button, request permission
                    activityResultLauncher.launch(permissionsToRequest.toTypedArray())
                } else {
                    // After the first time user save as button, if shouldShowRationale is false (user denied permission request the second time)
                    // show a toast message and navigate to the app settings for manual permission grant

                    initRequestSaveAsPermissionBottomSheet()
                }
            }
        } else {
            // If all permissions are granted
            grantedCallback.invoke()
        }
    }

    private fun setupStatusBar() {
        activity?.let { act ->
            // Make status bar visible and enable edge-to-edge
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R) {
                act.window.setDecorFitsSystemWindows(false)
            } else {
                @Suppress("DEPRECATION")
                act.window.decorView.systemUiVisibility = View.SYSTEM_UI_FLAG_LAYOUT_STABLE or
                        View.SYSTEM_UI_FLAG_LAYOUT_FULLSCREEN
            }

            // Clear fullscreen flag to show status bar
            act.window.clearFlags(android.view.WindowManager.LayoutParams.FLAG_FULLSCREEN)

            // Set status bar color transparent
            act.window.statusBarColor = android.graphics.Color.TRANSPARENT

            // Set light status bar for dark background (white icons)
            @Suppress("DEPRECATION")
            val flags = act.window.decorView.systemUiVisibility
            act.window.decorView.systemUiVisibility = flags and View.SYSTEM_UI_FLAG_LIGHT_STATUS_BAR.inv()
        }
    }

    private fun setupPermissionVisibilityBasedOnAndroidVersion() {
        if (Build.VERSION.SDK_INT > Build.VERSION_CODES.Q) {
            // Android > 10 (API 30+): Show Manage Files, hide Write Storage
//            dataBinding.cardManageFiles?.visibility = View.VISIBLE
            dataBinding.cardWriteStorage.visibility = View.GONE
        } else {
            // Android <= 10 (API 29-): Hide Manage Files, show Write Storage
            dataBinding.cardManageFiles.visibility = View.GONE
            dataBinding.cardWriteStorage.visibility = View.VISIBLE
        }
    }

    private fun handleWhenLoadInterDone() {
        activity?.finish()
    }
    //End Intertitial Back

}
