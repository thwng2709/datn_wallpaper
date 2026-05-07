package com.itsthwng.twallpaper.ui.component.splash.dialog

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
import com.google.firebase.Firebase
import com.itsthwng.twallpaper.databinding.BottomSheetConfirmApplyLanguageBinding
import com.itsthwng.twallpaper.ui.base.BaseBottomSheetDialogFragment
import dagger.hilt.android.AndroidEntryPoint
import com.itsthwng.twallpaper.utils.AppConfig
import com.itsthwng.twallpaper.utils.Logger
import java.util.Locale

@AndroidEntryPoint
class ConfirmApplyLanguageBottomSheet(
    private val title: String = "",
    private val content: String = "",
    private val contentYes: String = "",
    private val contentNo: String = ""
) : BaseBottomSheetDialogFragment() {
    private lateinit var dataBinding: BottomSheetConfirmApplyLanguageBinding
    var clickConfirmYes: (() -> Unit)? = null
    var clickConfirmNo: (() -> Unit)? = null
    var onCancel: (() -> Unit) ?= null

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
        dataBinding = BottomSheetConfirmApplyLanguageBinding.inflate(inflater, container, false)
        return dataBinding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        isCancelable = false

        if(title.isNotEmpty()){
            dataBinding.tvTitle.text = title
        }
        if(content.isNotEmpty()){
            dataBinding.lytContent.text = content
        }
        if(contentYes.isNotEmpty()){
            dataBinding.btnConfirmYes.text = contentYes
        }
        if(contentNo.isNotEmpty()){
            dataBinding.btnCancel.text = contentNo
        }
        dataBinding.btnConfirmYes.setOnClickListener {
            if (!AppConfig.isDoubleClick()) {
                clickConfirmYes?.invoke()
                dismiss()
            }
        }
        dataBinding.btnCancel.setOnClickListener {
            if (!AppConfig.isDoubleClick()) {
                clickConfirmNo?.invoke()
                dismiss()
            }
        }
    }

    fun updateLanguage(context : Context?, language: String) {
        try {
            if (language.isNotBlank()) {
                val resources: Resources? = context?.resources
                val locale = Locale(language)
                Locale.setDefault(locale)
                val config = Configuration()
                config.locale = locale
                resources?.updateConfiguration(config, resources.displayMetrics)
            }
        } catch (e : Exception) {
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
        dialog?.window?.setBackgroundDrawableResource(android.R.color.transparent)
    }

    override fun onCancel(dialog: DialogInterface) {
        super.onCancel(dialog)
    }

    companion object {
        const val TAG = "ConfirmApplyLanguageBottomSheet"
    }
}