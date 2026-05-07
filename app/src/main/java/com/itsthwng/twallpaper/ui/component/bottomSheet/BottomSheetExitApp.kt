package com.itsthwng.twallpaper.ui.component.bottomSheet

import android.content.Context
import android.content.DialogInterface
import android.content.res.Configuration
import android.content.res.Resources
import android.os.Build
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.WindowInsets
import android.view.WindowInsetsController
import android.view.WindowManager
import androidx.fragment.app.FragmentManager
import androidx.fragment.app.FragmentTransaction
import com.itsthwng.twallpaper.R
import com.itsthwng.twallpaper.databinding.BottomSheetExitAppBinding
import com.itsthwng.twallpaper.ui.base.BaseBottomSheetDialogFragment
import com.itsthwng.twallpaper.utils.Logger
import dagger.hilt.android.AndroidEntryPoint
import java.util.Locale

@AndroidEntryPoint
class BottomSheetExitApp : BaseBottomSheetDialogFragment() {
    private lateinit var dataBinding: BottomSheetExitAppBinding
    var clickConfirmYes: (() -> Unit)? = null
    var clickConfirmNo: (() -> Unit)? = null
    var clickConfirmCancel: (() -> Unit)? = null

    override fun show(manager: FragmentManager, tag: String?) {
        try {
            val ft: FragmentTransaction = manager.beginTransaction()
            ft.add(this, tag)
            ft.commitAllowingStateLoss()
        } catch (e: IllegalStateException) {
            Logger.e(TAG, "Exception : $e")
        }
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?
    ): View {
        dataBinding = BottomSheetExitAppBinding.inflate(inflater, container, false)

        dataBinding.btnConfirmYes.setOnClickListener {
            clickConfirmYes?.invoke()
            dismiss()
        }

        dataBinding.btnCancel.setOnClickListener {
            clickConfirmNo?.invoke()
            dismiss()
        }

        return dataBinding.root
    }

    override fun onCancel(dialog: DialogInterface) {
        super.onCancel(dialog)
        clickConfirmCancel?.invoke()
    }

    fun updateLanguage(context: Context?, language: String) {
        try {
            if (language.isNotBlank()) {
                val resources: Resources? = context?.resources
                val locale = Locale(language)
                Locale.setDefault(locale)
                val config = Configuration()
                config.locale = locale
                resources?.updateConfiguration(config, resources?.displayMetrics)
            }
        } catch (e: Exception) {
            Logger.e(TAG, "Exception: ${e.message}")
        }
    }

    override fun onStart() {
        super.onStart()
        val window = dialog?.window
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R) {
            window?.setDecorFitsSystemWindows(false)

            window?.insetsController?.let {
                it.hide(WindowInsets.Type.statusBars() or WindowInsets.Type.navigationBars())
                it.systemBarsBehavior =
                    WindowInsetsController.BEHAVIOR_SHOW_TRANSIENT_BARS_BY_SWIPE
            }
            dialog?.window?.setFlags(
                WindowManager.LayoutParams.FLAG_LAYOUT_NO_LIMITS,
                WindowManager.LayoutParams.FLAG_LAYOUT_NO_LIMITS
            )
        } else {
            @Suppress("DEPRECATION")
            window?.decorView?.systemUiVisibility =
                View.SYSTEM_UI_FLAG_IMMERSIVE_STICKY or
                        View.SYSTEM_UI_FLAG_FULLSCREEN or
                        View.SYSTEM_UI_FLAG_HIDE_NAVIGATION or
                        View.SYSTEM_UI_FLAG_LAYOUT_FULLSCREEN or
                        View.SYSTEM_UI_FLAG_LAYOUT_HIDE_NAVIGATION or
                        View.SYSTEM_UI_FLAG_LAYOUT_STABLE
            dialog?.window?.setFlags(
                WindowManager.LayoutParams.FLAG_LAYOUT_NO_LIMITS,
                WindowManager.LayoutParams.FLAG_LAYOUT_NO_LIMITS
            )
        }
        dialog?.window?.setBackgroundDrawableResource(R.color.transparent)
    }


    companion object {
        const val TAG = "BottomSheetExitApp"
    }
}