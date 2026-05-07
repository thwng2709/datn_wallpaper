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
import android.widget.Toast
import com.itsthwng.twallpaper.R
import com.itsthwng.twallpaper.databinding.DialogRateUsBinding
import com.itsthwng.twallpaper.ui.base.BaseDialogFragment
import com.itsthwng.twallpaper.utils.AppConfig
import com.itsthwng.twallpaper.utils.Constants
import dagger.hilt.android.AndroidEntryPoint
import java.util.Locale

@AndroidEntryPoint
class RateUsDialogFragment : BaseDialogFragment() {
    private lateinit var dataBinding: DialogRateUsBinding
    var clickConfirmYes: ((rate : Int) -> Unit)? = null
    var clickConfirmNo: (() -> Unit)? = null
    var clickConfirmCancel: (() -> Unit)? = null

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?
    ): View {
        dataBinding = DialogRateUsBinding.inflate(inflater, container, false)
        return dataBinding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        dataBinding.btnRateUs.setOnClickListener {
            when (dataBinding.rating.rating) {
                5f -> {
                    clickConfirmYes?.invoke(5)
                    AppConfig.logEventTracking(Constants.BUNDLE_ANALYTICS_RATING_5_HOME)
                    context?.let { it1 -> AppConfig.openApp(it1) }
                    dismiss()
                }

                1f -> {
                    clickConfirmYes?.invoke(1)
                    AppConfig.logEventTracking(Constants.BUNDLE_ANALYTICS_RATING_1_HOME)
                    dismiss()
                }

                2f -> {
                    clickConfirmYes?.invoke(2)
                    AppConfig.logEventTracking(Constants.BUNDLE_ANALYTICS_RATING_2_HOME)
                    dismiss()
                }

                3f -> {
                    clickConfirmYes?.invoke(3)
                    AppConfig.logEventTracking(Constants.BUNDLE_ANALYTICS_RATING_3_HOME)
                    dismiss()
                }

                4f -> {
                    clickConfirmYes?.invoke(4)
                    AppConfig.logEventTracking(Constants.BUNDLE_ANALYTICS_RATING_4_HOME)
                    context?.let { it1 -> AppConfig.openApp(it1) }
                    dismiss()
                }

                else -> {
                    Toast.makeText(context, resources.getString(R.string.warning_rating), Toast.LENGTH_SHORT).show()
                }
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
        } catch (_ : Exception) {
        }
    }

    override fun onCancel(dialog: DialogInterface) {
        super.onCancel(dialog)
        clickConfirmCancel?.invoke()
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
            dialog?.window?.setFlags( WindowManager.LayoutParams.FLAG_LAYOUT_NO_LIMITS, WindowManager.LayoutParams.FLAG_LAYOUT_NO_LIMITS )
        } else {
            @Suppress("DEPRECATION")
            window?.decorView?.systemUiVisibility =
                View.SYSTEM_UI_FLAG_IMMERSIVE_STICKY or
                        View.SYSTEM_UI_FLAG_FULLSCREEN or
                        View.SYSTEM_UI_FLAG_HIDE_NAVIGATION or
                        View.SYSTEM_UI_FLAG_LAYOUT_FULLSCREEN or
                        View.SYSTEM_UI_FLAG_LAYOUT_HIDE_NAVIGATION or
                        View.SYSTEM_UI_FLAG_LAYOUT_STABLE
            dialog?.window?.setFlags( WindowManager.LayoutParams.FLAG_LAYOUT_NO_LIMITS, WindowManager.LayoutParams.FLAG_LAYOUT_NO_LIMITS )
        }
    }


    companion object {
        const val TAG = "RateUsDialogFragment"
        fun show(fm: androidx.fragment.app.FragmentManager) {
            RateUsDialogFragment().show(fm, TAG)
        }
    }
}