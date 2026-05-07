package com.itsthwng.twallpaper.ui.component.permission

import android.animation.ObjectAnimator
import android.animation.ValueAnimator
import android.app.ActivityOptions
import android.content.Intent
import android.net.Uri
import android.os.Build
import android.os.Handler
import android.os.Looper
import android.provider.Settings
import android.view.View
import android.view.WindowManager
import android.view.animation.AccelerateDecelerateInterpolator
import android.view.animation.DecelerateInterpolator
import android.view.animation.OvershootInterpolator
import androidx.activity.result.contract.ActivityResultContracts
import com.itsthwng.twallpaper.R
import com.itsthwng.twallpaper.databinding.FragmentOverlayPermissionBinding
import com.itsthwng.twallpaper.ui.base.BaseFragmentBinding
import com.itsthwng.twallpaper.ui.component.MainActivity
import com.itsthwng.twallpaper.utils.AppConfig
import com.itsthwng.twallpaper.utils.Constants
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class OverlayPermissionFragment : BaseFragmentBinding<FragmentOverlayPermissionBinding>() {

    private val overlayPermissionLauncher = registerForActivityResult(
        ActivityResultContracts.StartActivityForResult()
    ) { _ ->
        if (findNavControllerSafety()?.currentDestination?.id == R.id.overlayPermissionFragment) {
            if (hasOverlayPermission()) {
                // Set result for permission granted
                requireActivity().setResult(
                    android.app.Activity.RESULT_OK,
                    Intent().putExtra(OverlayPermissionActivity.EXTRA_PERMISSION_GRANTED, true)
                )
                goToMainActivity()
            } else {
                // Show toast when permission is denied
                showToast(R.string.overlay_permission_denied_message)
            }
        }
    }

    override fun getContentViewId() = R.layout.fragment_overlay_permission

    override fun initializeViews() {
        // Setup status bar
        setupStatusBar()
        
        // Log event for tracking
        if (localStorage.isFirstOpenOverlayPermission) {
            localStorage.isFirstOpenOverlayPermission = false
            AppConfig.logEventTracking(Constants.EventKey.OVERLAY_PERMISSION_OPEN_1ST)
        } else {
            AppConfig.logEventTracking(Constants.EventKey.OVERLAY_PERMISSION_OPEN_2ND)
        }
        
        // Start animations
        startAnimations()
        
        // Handle back press
        requireActivity().onBackPressedDispatcher.addCallback(
            viewLifecycleOwner,
            object : androidx.activity.OnBackPressedCallback(true) {
                override fun handleOnBackPressed() {
                    // Set result for permission denied when pressing back
                    requireActivity().setResult(
                        android.app.Activity.RESULT_CANCELED,
                        Intent().putExtra(OverlayPermissionActivity.EXTRA_PERMISSION_GRANTED, false)
                    )
                    requireActivity().finish()
                }
            }
        )
    }
    
    private fun startAnimations() {
        // Hide all views initially
        dataBinding.illustrationContainer.alpha = 0f
        dataBinding.txtTitle.alpha = 0f
        dataBinding.txtMsg.alpha = 0f
        dataBinding.btnNext.alpha = 0f
        dataBinding.btnNotNow.alpha = 0f
        
        // Animate illustration container with image
        animateIllustrationContainer()
        
        // Animate text content
        Handler(Looper.getMainLooper()).postDelayed({
            animateTextContent()
        }, 400)
        
        // Animate buttons
        Handler(Looper.getMainLooper()).postDelayed({
            animateButtons()
        }, 700)
        
        // Start continuous animations
        startContinuousAnimations()
    }
    
    private fun animateIllustrationContainer() {
        dataBinding.illustrationContainer.apply {
            translationY = -50f
            animate()
                .alpha(1f)
                .translationY(0f)
                .setDuration(800)
                .setInterpolator(OvershootInterpolator(0.8f))
                .start()
        }
        
        // Animate the image inside
        dataBinding.imgIntro.apply {
            scaleX = 0.8f
            scaleY = 0.8f
            animate()
                .scaleX(1f)
                .scaleY(1f)
                .setDuration(1000)
                .setInterpolator(OvershootInterpolator(1.2f))
                .start()
        }
    }
    
    private fun animateTextContent() {
        // Animate title
        dataBinding.txtTitle.apply {
            translationY = 30f
            animate()
                .alpha(1f)
                .translationY(0f)
                .setDuration(500)
                .setInterpolator(DecelerateInterpolator())
                .start()
        }
        
        // Animate description with delay
        dataBinding.txtMsg.apply {
            translationY = 30f
            animate()
                .alpha(1f)
                .translationY(0f)
                .setDuration(500)
                .setStartDelay(150)
                .setInterpolator(DecelerateInterpolator())
                .start()
        }
    }
    
    private fun animateButtons() {
        // Animate primary button
        dataBinding.btnNext.apply {
            translationY = 50f
            animate()
                .alpha(1f)
                .translationY(0f)
                .setDuration(500)
                .setInterpolator(DecelerateInterpolator())
                .start()
        }
        
        // Animate secondary button with delay
        dataBinding.btnNotNow.apply {
            translationY = 50f
            animate()
                .alpha(1f)
                .translationY(0f)
                .setDuration(500)
                .setStartDelay(150)
                .setInterpolator(DecelerateInterpolator())
                .start()
        }
    }
    
    private fun startContinuousAnimations() {
        // Floating animation for illustration
        animateFloatingIllustration()
    }
    
    private fun animateFloatingIllustration() {
        val animator = ObjectAnimator.ofFloat(
            dataBinding.imgIntro, 
            "translationY", 
            0f, -15f, 0f
        ).apply {
            duration = 3000
            interpolator = AccelerateDecelerateInterpolator()
            repeatCount = ValueAnimator.INFINITE
        }
        animator.start()
    }

    override fun registerListeners() {
        dataBinding.btnNext.setOnClickListener {
            // Add scale animation feedback
            it.animate()
                .scaleX(0.95f)
                .scaleY(0.95f)
                .setDuration(100)
                .withEndAction {
                    it.animate()
                        .scaleX(1f)
                        .scaleY(1f)
                        .setDuration(100)
                        .start()
                    requestOverlayPermission()
                }
                .start()
        }
        
        dataBinding.btnNotNow.setOnClickListener {
            if (!AppConfig.isDoubleClick()) {
                // Add scale animation feedback
                it.animate()
                    .scaleX(0.95f)
                    .scaleY(0.95f)
                    .setDuration(100)
                    .withEndAction {
                        it.animate()
                            .scaleX(1f)
                            .scaleY(1f)
                            .setDuration(100)
                            .start()
                        
                        if (findNavControllerSafety()?.currentDestination?.id == R.id.overlayPermissionFragment) {
                            // Set result for permission denied when clicking "Not Now"
                            requireActivity().setResult(
                                android.app.Activity.RESULT_CANCELED,
                                Intent().putExtra(OverlayPermissionActivity.EXTRA_PERMISSION_GRANTED, false)
                            )
                            goToMainActivity()
                        }
                    }
                    .start()
            }
        }
    }

    override fun initializeData() {}

    private fun hasOverlayPermission(): Boolean {
        return if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            Settings.canDrawOverlays(requireContext())
        } else {
            true
        }
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

    private fun goToMainActivity() {
        // Check if we came from zipper lock activity
        val fromZipperLock = requireActivity().intent.getBooleanExtra("from_zipper_lock", false)
        
        if (fromZipperLock) {
            // Simply finish to return to zipper lock activity
            requireActivity().finish()
        } else {
            // Go to main activity
            val intent = Intent(requireActivity(), MainActivity::class.java)
            val options = ActivityOptions.makeCustomAnimation(
                requireActivity(),
                R.anim.slide_in_right,
                R.anim.slide_out_left
            )
            intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TASK or Intent.FLAG_ACTIVITY_NEW_TASK)
            startActivity(intent, options.toBundle())
            requireActivity().finish()
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
        // Clean up animations
        dataBinding.illustrationContainer.clearAnimation()
        dataBinding.imgIntro.clearAnimation()
        super.onDestroyView()
    }
}