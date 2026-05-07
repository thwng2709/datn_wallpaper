package com.itsthwng.twallpaper.ui.component.bottomSheet

import android.content.DialogInterface
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
import com.itsthwng.twallpaper.databinding.BottomSheetPermissionNotificationBinding
import com.itsthwng.twallpaper.ui.base.BaseBottomSheetDialogFragment
import com.itsthwng.twallpaper.utils.AppConfig
import com.itsthwng.twallpaper.utils.Logger
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class ConfirmPermissionNotificationBottomSheet : BaseBottomSheetDialogFragment() {
    private lateinit var dataBinding: BottomSheetPermissionNotificationBinding
    var clickConfirmYes: (() -> Unit)? = null
    var clickConfirmNo: (() -> Unit)? = null
    var clickConfirmCancel: (() -> Unit)? = null
    var isDontShowNotificationPermission = false

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
        dataBinding = BottomSheetPermissionNotificationBinding.inflate(inflater, container, false)
        return dataBinding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        dataBinding.btnConfirmYes.setOnClickListener {
            if (!AppConfig.isDoubleClick()) {
                clickConfirmYes?.invoke()
                dismiss()
            }
        }
        dataBinding.btnCancel.setOnClickListener {
            if (!AppConfig.isDoubleClick()) {
                if (dataBinding.cbDontShowAgain.isChecked) {
                    isDontShowNotificationPermission = true
                }
                clickConfirmNo?.invoke()
                dismiss()
            }
        }
    }

    override fun onCancel(dialog: DialogInterface) {
        super.onCancel(dialog)
        clickConfirmCancel?.invoke()
        dismiss()
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
    }

    companion object {
        const val TAG = "ConfirmPermissionBottomSheet"
    }
}