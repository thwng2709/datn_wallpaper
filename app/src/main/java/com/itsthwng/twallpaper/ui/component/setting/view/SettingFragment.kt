package com.itsthwng.twallpaper.ui.component.setting.view

import android.content.ClipData
import android.content.ClipboardManager
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.os.Build
import android.os.Handler
import android.os.Looper
import android.os.PowerManager
import android.provider.Settings
import android.view.View
import androidx.core.content.ContextCompat
import androidx.fragment.app.DialogFragment
import com.itsthwng.twallpaper.ui.component.setting.viewmodel.SettingViewModel
import com.google.firebase.Firebase
import com.itsthwng.twallpaper.ui.base.BaseViewModelFragmentBinding
import com.itsthwng.twallpaper.ui.component.bottomSheet.RateUsDialogFragment
import com.itsthwng.twallpaper.ui.component.splash.dialog.ConfirmApplyLanguageBottomSheet
import com.itsthwng.twallpaper.utils.AppConfig
import com.itsthwng.twallpaper.utils.CommonUtil
import com.itsthwng.twallpaper.utils.Constants
import com.itsthwng.twallpaper.utils.Constants.PERMISSIONS
import com.itsthwng.twallpaper.utils.Logger
import dagger.hilt.android.AndroidEntryPoint
import androidx.core.net.toUri
import com.itsthwng.twallpaper.BuildConfig
import com.itsthwng.twallpaper.R
import com.itsthwng.twallpaper.databinding.FragmentSettingBinding

@AndroidEntryPoint
class SettingFragment : BaseViewModelFragmentBinding<FragmentSettingBinding, SettingViewModel>() {
    private var ratingAppBottomSheet: RateUsDialogFragment? = null
    private var confirmApplyLanguageBottomSheet: ConfirmApplyLanguageBottomSheet? = null

    override fun getContentViewId() = R.layout.fragment_setting

    override fun onResume() {
        showHideRedDot(requireContext())
        super.onResume()
    }

    override fun onBackPressed(): Boolean {
        handleWhenLoadInterDone()
        return true
    }

    fun copyTextToClipboard(context: Context, text: String) {
        val clipboard = context.getSystemService(Context.CLIPBOARD_SERVICE) as ClipboardManager
        val clip = ClipData.newPlainText("Copied Text", text)
        clipboard.setPrimaryClip(clip)

        showToast(R.string.copy_user_id)
    }

    override fun initializeViews() {
        dataBinding.tvUserId.text = localStorage.userId
        dataBinding.llCopy.setOnClickListener {
            copyTextToClipboard(requireContext(), dataBinding.tvUserId.text.toString())
        }
        dataBinding.txtVersion.text = BuildConfig.VERSION_NAME

        if (localStorage.isFirstOpenSetting) {
            localStorage.isFirstOpenSetting = false
            AppConfig.logEventTracking(Constants.EventKey.SETTING_OPEN_1ST)
        } else {
            AppConfig.logEventTracking(Constants.EventKey.SETTING_OPEN_2ND)
        }
        dataBinding.langChosen.text = getLangName(localStorage.langCode)
    }

    private fun getLangName(langCode: String): String {
        return when (langCode) {
            // Exact language tags first
            "en-US" -> getString(R.string.text_lang_en_us)
            "en-CA" -> getString(R.string.text_lang_en_ca)
            "pt-BR" -> getString(R.string.text_lang_pt_br)

            // Base languages in your layout
            "hi" -> getString(R.string.text_lang_hi)
            "de" -> getString(R.string.text_lang_de)
            "es" -> getString(R.string.text_lang_es)
            "pt" -> getString(R.string.text_lang_pt)
            "en" -> getString(R.string.text_lang_en)
            "vi" -> getString(R.string.text_lang_vi)
            "tr" -> getString(R.string.text_lang_tr)
            "ru" -> getString(R.string.text_lang_ru)
            "uk" -> getString(R.string.text_lang_uk)
            "ja" -> getString(R.string.text_lang_ja)
            "ko" -> getString(R.string.text_lang_ko)
            "zh" -> getString(R.string.text_lang_zh)
            "ar" -> getString(R.string.text_lang_ar)
            "fr" -> getString(R.string.text_lang_fr) // không có trong list ngôn ngữ
            "id" -> getString(R.string.text_lang_id) // không có trong list ngôn ngữ
            "fil", "tl" -> getString(R.string.text_lang_fil) // không có trong list ngôn ngữ
            "bn" -> getString(R.string.text_lang_bn) // không có trong list ngôn ngữ
            "af" -> getString(R.string.text_lang_af) // không có trong list ngôn ngữ
            "nl" -> getString(R.string.text_lang_nl) // không có trong list ngôn ngữ
            "mr" -> getString(R.string.text_lang_mr) // không có trong list ngôn ngữ

            // Fallback: nếu là biến thể vùng khác (vd: en-GB, pt-PT...) thì map về base
            else -> {
                val base = langCode.substringBefore('-', langCode).lowercase()
                when (base) {
                    "hi" -> getString(R.string.text_lang_hi)
                    "en" -> getString(R.string.text_lang_en)
                    "pt" -> getString(R.string.text_lang_pt)
                    "zh" -> getString(R.string.text_lang_zh)
                    "es" -> getString(R.string.text_lang_es)
                    "de" -> getString(R.string.text_lang_de)
                    "ru" -> getString(R.string.text_lang_ru)
                    "fr" -> getString(R.string.text_lang_fr)
                    "id" -> getString(R.string.text_lang_id)
                    "fil", "tl" -> getString(R.string.text_lang_fil)
                    "bn" -> getString(R.string.text_lang_bn)
                    "af" -> getString(R.string.text_lang_af)
                    "nl" -> getString(R.string.text_lang_nl)
                    "mr" -> getString(R.string.text_lang_mr)
                    "vi" -> getString(R.string.text_lang_vi)
                    "uk" -> getString(R.string.text_lang_uk)
                    "ko" -> getString(R.string.text_lang_ko)
                    else -> getString(R.string.text_lang_en)
                }
            }
        }
    }

    override fun registerListeners() {
        dataBinding.btnBack.setOnClickListener {
            handleWhenLoadInterDone()
            disableEnableStatus1Sec(it)
        }

        dataBinding.rlfeedback.setOnClickListener {
            if (!AppConfig.isDoubleClick()) {
                try {
                    AppConfig.logEventTracking(Constants.EventKey.GO_TO_FEEDBACK)
                } catch (e : Exception){
                    Logger.e("LogEventTracking error: ${e.message}")
                }
                findNavControllerSafety()?.navigate(R.id.action_settingFragment2_to_feedbackFragment2)
            }
            disableEnableStatus1Sec(it)
        }
        dataBinding.rlrate.setOnClickListener {
            if (!AppConfig.isDoubleClick()) {
                initRatingBottomSheet()
            }
            disableEnableStatus1Sec(it)
        }

        dataBinding.rlShare.setOnClickListener {
            if (!AppConfig.isDoubleClick()) {
                activity?.let { it1 -> AppConfig.shareApp(it1) }
            }
            disableEnableStatus1Sec(it)
        }

        dataBinding.rlprivacypol.setOnClickListener {
            if (!AppConfig.isDoubleClick()) {
                openPrivacyPolicyLink()
            }
            disableEnableStatus1Sec(it)
        }
        dataBinding.rllang.setOnClickListener {
            try {
                try {
                    AppConfig.logEventTracking(Constants.EventKey.GO_TO_LANGUAGE_SETTING)
                } catch (e : Exception){
                    Logger.e("LogEventTracking error: ${e.message}")
                }
                findNavControllerSafety()?.navigate(R.id.action_settingFragment2_to_settingLanguageFragment2)
            } catch (e: Throwable) {
                Logger.e(e.message)
            }
            disableEnableStatus1Sec(it)
        }
        dataBinding.rlPermission.setOnClickListener {
            runCatching {
                try {
                    AppConfig.logEventTracking(Constants.EventKey.GO_TO_GRANT_PERMISSION)
                } catch (e : Exception){
                    Logger.e("LogEventTracking error: ${e.message}")
                }
                findNavControllerSafety()?.navigate(R.id.action_settingFragment2_to_grantPermissionFragment2)
            }
            disableEnableStatus1Sec(it)
        }
    }

    private fun initRatingBottomSheet() {
        ratingAppBottomSheet = RateUsDialogFragment()
        ratingAppBottomSheet?.updateLanguage(context, localStorage.langCode)
        ratingAppBottomSheet?.setStyle(
            DialogFragment.STYLE_NORMAL,
            R.style.AppBottomSheetDialogTheme
        )
        ratingAppBottomSheet?.clickConfirmYes = {
            when (it) {
                1, 2, 3 -> initConfirmSendFeedbackBottomSheet()
                4, 5 -> {
                    context?.let { AppConfig.openApp(it) }
                    ratingAppBottomSheet?.dismiss()
                }
                else -> ratingAppBottomSheet?.dismiss()
            }
        }
        ratingAppBottomSheet?.clickConfirmNo = {
            ratingAppBottomSheet?.dismiss()
        }
        ratingAppBottomSheet?.clickConfirmCancel = {
            ratingAppBottomSheet?.dismiss()
        }
        if (activity?.isFinishing == false) {
            activity?.supportFragmentManager?.let {
                ratingAppBottomSheet?.show(
                    it, RateUsDialogFragment.TAG
                )
            }
        }
    }

    override fun onPause() {
        super.onPause()
        if(ratingAppBottomSheet != null && ratingAppBottomSheet!!.isVisible){
            ratingAppBottomSheet?.dismiss()
        }
        if(confirmApplyLanguageBottomSheet != null && confirmApplyLanguageBottomSheet!!.isVisible){
            confirmApplyLanguageBottomSheet?.dismiss()
        }
    }

    private fun initConfirmSendFeedbackBottomSheet() {
        confirmApplyLanguageBottomSheet = ConfirmApplyLanguageBottomSheet(
            title = getString(R.string.feedback),
            content = getString(R.string.content_feedback),
            contentYes = getString(R.string.feedback),
            contentNo = getString(R.string.cancel)
        )
        confirmApplyLanguageBottomSheet?.updateLanguage(context, localStorage.langCode)
        confirmApplyLanguageBottomSheet?.setStyle(
            DialogFragment.STYLE_NORMAL,
            R.style.AppBottomSheetDialogTheme
        )
        confirmApplyLanguageBottomSheet?.clickConfirmYes = {
            try {
                localStorage.isShowRating = true
                activity?.let { it1 ->
                    AppConfig.sendMail(
                        requireActivity(), Constants.SUBJECT_EMAIL, resources.getString(
                            R.string.choose_email
                        )
                    )
                }
                confirmApplyLanguageBottomSheet?.dismiss()
            } catch (e: Exception) {
            }
        }
        confirmApplyLanguageBottomSheet?.clickConfirmNo = {
            confirmApplyLanguageBottomSheet?.dismiss()
        }
        confirmApplyLanguageBottomSheet?.onCancel = {
            confirmApplyLanguageBottomSheet?.dismiss()
            if (activity?.isFinishing == false) {
                activity?.supportFragmentManager?.let {
                    ratingAppBottomSheet?.show(
                        it, RateUsDialogFragment.TAG
                    )
                }
            }
        }
        if (activity?.isFinishing == false) {
            activity?.supportFragmentManager?.let {
                confirmApplyLanguageBottomSheet?.show(
                    it,
                    RateUsDialogFragment.TAG
                )
            }
        }
    }

    private fun rateUs() {
        val packageName: String = requireContext().packageName
        val intent = Intent("android.intent.action.VIEW")
        intent.data = "market://details?id=$packageName".toUri()
        try {
            startActivity(intent)
        } catch (unused: Exception) {
            val intent2 = Intent("android.intent.action.VIEW")
            intent2.data = "https://play.google.com/store/apps/details?id=$packageName".toUri()
            try {
                startActivity(intent2)
            } catch (unused2: Exception) {
                showToast(R.string.no_app_to_handle_action)
            }
        }
    }

    private fun openPrivacyPolicyLink() {
        val intentPrivacy = Intent(Intent.ACTION_VIEW, Constants.URL_POLICY.toUri())
        startActivity(intentPrivacy)
    }

    override fun initializeData() {

    }

    companion object {
        const val TAG = "SettingFragment"
        const val DEV_PAGE = "8302674046174023449"
    }

    private fun hasManageFilePermission(): Boolean {
        return if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R) {
//            Environment.isExternalStorageManager()
            true
        } else {
            CommonUtil.hasPermissions(
                permissions = PERMISSIONS,
                activity = requireActivity()
            )
        }
    }

    private fun hasSaveAsPermissions(): Boolean {
        return if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            PERMISSIONS.all { permission ->
                ContextCompat.checkSelfPermission(
                    requireContext(),
                    permission
                ) == PackageManager.PERMISSION_GRANTED
            }
        } else if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R) {
            PERMISSIONS.all { permission ->
                ContextCompat.checkSelfPermission(
                    requireContext(),
                    permission
                ) == PackageManager.PERMISSION_GRANTED
            }
        } else {
            CommonUtil.hasPermissions(
                permissions = PERMISSIONS,
                activity = requireActivity()
            )
        }
    }

    private fun hasNotificationPermissions(): Boolean {
        return (ContextCompat.checkSelfPermission(
            requireContext(), "android.permission.POST_NOTIFICATIONS"
        ) == PackageManager.PERMISSION_GRANTED) || Build.VERSION.SDK_INT < 33
    }


    private fun isIgnoringBatteryOptimizations(context: Context): Boolean {
        val powerManager = context.getSystemService(Context.POWER_SERVICE) as PowerManager
        return powerManager.isIgnoringBatteryOptimizations(context.packageName)
    }

    private fun hasDownloadPermission(): Boolean{
        return if (Build.VERSION.SDK_INT <= Build.VERSION_CODES.Q) {
            hasSaveAsPermissions()
        } else {
            hasManageFilePermission()
        }
    }

    private fun hasOverlayPermission(): Boolean {
        return Settings.canDrawOverlays(requireContext())
    }

    private fun isAllPermissionGrant(context: Context): Boolean {
        return hasOverlayPermission() && hasDownloadPermission() && hasNotificationPermissions() && isIgnoringBatteryOptimizations(context)
    }

    private fun showHideRedDot(context: Context) {
        dataBinding.notificationDot.visibility =
            if (isAllPermissionGrant(context)) View.GONE else View.VISIBLE
    }

    private fun disableEnableStatus1Sec(view: View) {
        view.isEnabled = false
        
        Handler(Looper.getMainLooper()).postDelayed({
            view.isEnabled = true
        }, 1_000L)
    }

    private fun handleWhenLoadInterDone() {
        activity?.finish()
    }
}
