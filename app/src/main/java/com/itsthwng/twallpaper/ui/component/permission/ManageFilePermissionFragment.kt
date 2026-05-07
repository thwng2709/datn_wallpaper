package com.itsthwng.twallpaper.ui.component.permission

import android.animation.AnimatorSet
import android.animation.ObjectAnimator
import android.app.ActivityOptions
import android.content.Context
import android.content.Intent
import android.net.Uri
import android.os.Build
import android.os.Environment
import android.os.PowerManager
import android.provider.Settings
import android.view.View
import android.view.WindowManager
import android.view.animation.DecelerateInterpolator
import androidx.activity.result.contract.ActivityResultContracts
import androidx.annotation.RequiresApi
import com.google.firebase.Firebase
import com.itsthwng.twallpaper.BuildConfig
import com.itsthwng.twallpaper.R
import com.itsthwng.twallpaper.databinding.FragmentManageFilePermissionBinding
import com.itsthwng.twallpaper.ui.base.BaseFragmentBinding
import com.itsthwng.twallpaper.ui.component.MainActivity
import com.itsthwng.twallpaper.ui.component.splash.view.IntroFragment.Companion.TAG
import com.itsthwng.twallpaper.utils.AppConfig
import com.itsthwng.twallpaper.utils.CommonUtil
import com.itsthwng.twallpaper.utils.Constants
import com.itsthwng.twallpaper.utils.Constants.PERMISSIONS
import com.itsthwng.twallpaper.utils.Logger
import dagger.hilt.android.AndroidEntryPoint
import androidx.core.net.toUri

@AndroidEntryPoint
class ManageFilePermissionFragment : BaseFragmentBinding<FragmentManageFilePermissionBinding>() {
    private var isFromHome: Int = 0

    private var grantedCallback: (() -> Unit)? = null
    @RequiresApi(Build.VERSION_CODES.R)
    val storagePermissionResultLauncher = registerForActivityResult(
        ActivityResultContracts.StartActivityForResult()
    ) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R && Environment.isExternalStorageManager()) {
            grantedCallback?.invoke()
        }
    }

    private val requestPermissionLauncher = registerForActivityResult(
        ActivityResultContracts.RequestMultiplePermissions()
    ) { _ ->
        if(CommonUtil.hasPermissions(
                permissions = PERMISSIONS,
                activity = requireActivity()
            )){
            grantedCallback?.invoke()
        } else {
            showToast(R.string.permission_request)
        }
    }

    override fun getContentViewId() = R.layout.fragment_manage_file_permission

    override fun onResume() {
        super.onResume()
        // App.appResumeAdHelper?.setRequestAppResumeValid(true)
        // App.appResumeAdHelper?.setEnableAppResumeOnScreen()
    }

    override fun initializeViews() {
        // Setup status bar
        setupStatusBar()
        
        arguments?.let {
            isFromHome = it.getInt("ISFROMHOME") ?: 0
        }
        if(localStorage.isFirstOpenManageFilePermission){
            localStorage.isFirstOpenManageFilePermission = false
            AppConfig.logEventTracking(Constants.EventKey.MANAGEFILE_OPEN_1ST)
        } else {
            AppConfig.logEventTracking(Constants.EventKey.MANAGEFILE_OPEN_2ND)
        }
        
        // Add entrance animations
        setupAnimations()
    }
    
    private fun setupAnimations() {
        // Set initial states
        dataBinding.illustrationContainer.apply {
            alpha = 0f
            scaleX = 0.8f
            scaleY = 0.8f
        }
        
        // Create animations
        val illustrationAnimator = AnimatorSet().apply {
            playTogether(
                ObjectAnimator.ofFloat(dataBinding.illustrationContainer, View.ALPHA, 0f, 1f),
                ObjectAnimator.ofFloat(dataBinding.illustrationContainer, View.SCALE_X, 0.8f, 1f),
                ObjectAnimator.ofFloat(dataBinding.illustrationContainer, View.SCALE_Y, 0.8f, 1f)
            )
            duration = 600
            interpolator = DecelerateInterpolator()
        }
        
        // Animate text views
        dataBinding.txtTitle.apply {
            alpha = 0f
            translationY = 30f
            animate()
                .alpha(1f)
                .translationY(0f)
                .setDuration(500)
                .setStartDelay(200)
                .setInterpolator(DecelerateInterpolator())
                .start()
        }
        
        dataBinding.txtMsg.apply {
            alpha = 0f
            translationY = 30f
            animate()
                .alpha(1f)
                .translationY(0f)
                .setDuration(500)
                .setStartDelay(300)
                .setInterpolator(DecelerateInterpolator())
                .start()
        }
        
        // Animate buttons
        dataBinding.btnNext.apply {
            alpha = 0f
            translationY = 50f
            animate()
                .alpha(1f)
                .translationY(0f)
                .setDuration(500)
                .setStartDelay(400)
                .setInterpolator(DecelerateInterpolator())
                .start()
        }
        
        dataBinding.btnNotNow.apply {
            alpha = 0f
            translationY = 50f
            animate()
                .alpha(1f)
                .translationY(0f)
                .setDuration(500)
                .setStartDelay(500)
                .setInterpolator(DecelerateInterpolator())
                .start()
        }
        
        // Start illustration animation
        illustrationAnimator.start()
        
        // Add floating animation to illustration
        startFloatingAnimation()
    }
    
    private fun startFloatingAnimation() {
        val floatUp = ObjectAnimator.ofFloat(
            dataBinding.imgIntro,
            View.TRANSLATION_Y,
            0f,
            -20f
        ).apply {
            duration = 2000
            interpolator = DecelerateInterpolator()
        }
        
        val floatDown = ObjectAnimator.ofFloat(
            dataBinding.imgIntro,
            View.TRANSLATION_Y,
            -20f,
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

    override fun registerListeners() {
        dataBinding.btnNext.setOnClickListener {
            requestPermission(grantedCallback = {
                if(isIgnoringBatteryOptimizations(requireContext())){
                    goToMainActivity()
                } else {
                    if(findNavControllerSafety()?.currentDestination?.id == R.id.manageFilePermissionFragment) {
                        findNavControllerSafety()?.navigate(R.id.action_manageFilePermissionFragment_to_batteryPermissionFragment)
                    }
                }
            })
        }
        dataBinding.btnNotNow.setOnClickListener {
            if(isIgnoringBatteryOptimizations(requireContext())){
                goToMainActivity()
            } else {
                if(findNavControllerSafety()?.currentDestination?.id == R.id.manageFilePermissionFragment) {
                    findNavControllerSafety()?.navigate(R.id.action_manageFilePermissionFragment_to_batteryPermissionFragment)
                }
            }
        }
    }

    private fun goToMainActivity(){
        try {
            if (activity != null) {
                val intent = Intent(requireActivity(), MainActivity::class.java)
                intent.putExtra("source_screen", TAG)
                var options : ActivityOptions? = null
                context?.let {
                    options = ActivityOptions.makeCustomAnimation(it, R.anim.slide_in_right, R.anim.slide_out_left)
                }
                if (options != null) {
                    startActivity(intent, options?.toBundle())
                } else {
                    startActivity(intent)
                }
                requireActivity().finish()
            }
        } catch (e: Exception) {
            Logger.e(e.message)
        }
    }

    override fun initializeData() {}

    private fun isIgnoringBatteryOptimizations(context: Context): Boolean {
        val powerManager = context.getSystemService(Context.POWER_SERVICE) as PowerManager
        return powerManager.isIgnoringBatteryOptimizations(context.packageName)
    }

    private fun requestPermission(grantedCallback: () -> Unit) {
        this.grantedCallback = grantedCallback
        when {
            Build.VERSION.SDK_INT >= Build.VERSION_CODES.R && !Environment.isExternalStorageManager() -> {
                val intent = Intent(
                    Settings.ACTION_MANAGE_APP_ALL_FILES_ACCESS_PERMISSION,
                    ("package:" + BuildConfig.APPLICATION_ID).toUri()
                )
                if (intent.resolveActivity(requireActivity().packageManager) != null) {
                    storagePermissionResultLauncher.launch(intent)
                } else {
                    val settingsIntent = Intent(Settings.ACTION_APPLICATION_DETAILS_SETTINGS)
                    settingsIntent.data = ("package:" + BuildConfig.APPLICATION_ID).toUri()
                    storagePermissionResultLauncher.launch(settingsIntent)
                }
            }

            Build.VERSION.SDK_INT < Build.VERSION_CODES.R -> {
                requestPermissionLauncher.launch(Constants.PERMISSIONS)
            }

            else -> {
                this.grantedCallback?.invoke()
            }
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
            act.window.clearFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN)
            
            // Set status bar color transparent
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
                act.window.statusBarColor = android.graphics.Color.TRANSPARENT
            }
            
            // Set light status bar for dark background (white icons)
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                @Suppress("DEPRECATION")
                val flags = act.window.decorView.systemUiVisibility
                act.window.decorView.systemUiVisibility = flags and View.SYSTEM_UI_FLAG_LIGHT_STATUS_BAR.inv()
            }
        }
    }
    
    override fun onDestroyView() {
        // Clean up animations when view is destroyed
        dataBinding.imgIntro.clearAnimation()
        super.onDestroyView()
    }
}