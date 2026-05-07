package com.itsthwng.twallpaper.ui.component.setting.view

import android.content.Context
import android.content.res.Configuration
import android.content.res.Resources
import android.view.ViewTreeObserver
import androidx.appcompat.app.AppCompatDelegate
import androidx.core.os.LocaleListCompat
import androidx.fragment.app.DialogFragment
import androidx.work.ExistingWorkPolicy
import androidx.work.OneTimeWorkRequestBuilder
import androidx.work.OutOfQuotaPolicy
import androidx.work.WorkManager
import androidx.work.workDataOf
import com.itsthwng.twallpaper.ui.component.setting.viewmodel.SettingViewModel
import com.google.firebase.Firebase
import com.itsthwng.twallpaper.R
import com.itsthwng.twallpaper.databinding.FragmentSettingLanguageBinding
import com.itsthwng.twallpaper.ui.base.BaseViewModelFragmentBinding
import com.itsthwng.twallpaper.ui.component.splash.dialog.ConfirmApplyLanguageBottomSheet
import com.itsthwng.twallpaper.ui.component.splash.view.AskLanguageFragment.LangCode
import com.itsthwng.twallpaper.ui.component.splash.view.AskLanguageFragment2
import com.itsthwng.twallpaper.utils.AppConfig
import com.itsthwng.twallpaper.utils.AppConfig.logEventTracking
import com.itsthwng.twallpaper.utils.Constants
import com.itsthwng.twallpaper.utils.Logger
import com.itsthwng.twallpaper.workManager.LocaleSyncWorker
import dagger.hilt.android.AndroidEntryPoint
import java.util.Locale

@AndroidEntryPoint
class SettingLanguageFragment :
    BaseViewModelFragmentBinding<FragmentSettingLanguageBinding, SettingViewModel>() {

    private var isChangeLanguage = true
    private var confirmApplyLanguageBottomSheet: ConfirmApplyLanguageBottomSheet? = null
    private var lastCheckedId: Int? = null
    private var scrollPosition: Int? = 0

    override fun getContentViewId() = R.layout.fragment_setting_language

    override fun onResume() {
        super.onResume()
        if (localStorage.justRecreate) {
            showToast(R.string.language_change_successful)
            localStorage.justRecreate = false
        }
    }

    override fun onPause() {
        super.onPause()
    }

    private fun getLocalizedContext(context: Context, locale: Locale): Context {
        val configuration = Configuration(context.resources.configuration)
        configuration.setLocale(locale)
        return context.createConfigurationContext(configuration)
    }

    override fun initializeViews() {
//        if (Build.VERSION.SDK_INT >= 31) {
//            dataBinding.root.layoutDirection = resources.configuration.layoutDirection
//        }
        val localeContext = Locale(localStorage.langCode)
        val localizedContext = getLocalizedContext(requireContext(), localeContext)
        dataBinding.heading.text = localizedContext.getString(R.string.languages)
        if (localStorage.languageScrollPosition != 0) {
            scrollPosition = localStorage.languageScrollPosition
        }

        if (localStorage.isFirstOpenSettingLanguage) {
            logEventTracking(Constants.EventKey.LANGSETTING_OPEN_1ST)
            localStorage.isFirstOpenSettingLanguage = false
        } else {
            logEventTracking(Constants.EventKey.LANGSETTING_OPEN_2ND)
        }
        if (localStorage.justRecreate) {
            dataBinding.svLanguage.viewTreeObserver.addOnGlobalLayoutListener(object :
                ViewTreeObserver.OnGlobalLayoutListener {
                override fun onGlobalLayout() {
                    // Cuộn đến vị trí mong muốn
                    scrollPosition?.let { dataBinding.svLanguage.smoothScrollTo(0, it) }

                    // Hoặc cuộn mượt
                    // scrollView.smoothScrollTo(0, scrollPosition)

                    // Xóa listener để không cuộn lại lần nữa
                    dataBinding.svLanguage.viewTreeObserver.removeOnGlobalLayoutListener(this)
                }
            })
        }
//        updateLanguageLocale()
        bindViewItemSelected()
    }

    override fun registerListeners() {
        var rbId = lastCheckedId
        dataBinding.radioGroup.setOnCheckedChangeListener { group, checkedId ->
            rbId = checkedId
        }

        dataBinding.ivChoose.setOnClickListener {
            if (confirmApplyLanguageBottomSheet != null && confirmApplyLanguageBottomSheet!!.isVisible) {
                confirmApplyLanguageBottomSheet?.dismiss()
            }
            else when (rbId) {
                R.id.rbt_hindi -> {
                    updateLanguage(AskLanguageFragment2.LangCode.HINDI.value)
                }
                R.id.rbt_german -> {
                    updateLanguage(AskLanguageFragment2.LangCode.GERMAN.value)
                }
                R.id.rbt_spanish -> {
                    updateLanguage(AskLanguageFragment2.LangCode.SPANISH.value)
                }
                R.id.rbt_portuguase -> {
                    updateLanguage(AskLanguageFragment2.LangCode.PORTUGUESE.value)
                }
                R.id.rbt_english -> {
                    updateLanguage(AskLanguageFragment2.LangCode.ENGLISH.value)
                }
                R.id.rbt_vietnam -> {
                    updateLanguage(AskLanguageFragment2.LangCode.VIETNAM.value)
                }
                R.id.rbt_turkey -> {
                    updateLanguage(AskLanguageFragment2.LangCode.TURKEY.value)
                }
                R.id.rbt_russian -> {
                    updateLanguage(AskLanguageFragment2.LangCode.RUSSIAN.value)
                }
                R.id.rbt_ukraian -> {
                    updateLanguage(AskLanguageFragment2.LangCode.UKRAIAN.value)
                }
                R.id.rbt_japan -> {
                    updateLanguage(AskLanguageFragment2.LangCode.JAPAN.value)
                }
                R.id.rbt_korean -> {
                    updateLanguage(AskLanguageFragment2.LangCode.KOREAN.value)
                }
                R.id.rbt_china -> {
                    updateLanguage(AskLanguageFragment2.LangCode.CHINA.value)
                }
                R.id.rbt_abric -> {
                    updateLanguage(AskLanguageFragment2.LangCode.ABRIC.value)
                }
                else -> Unit
            }
        }

        dataBinding.ivBack.setOnClickListener {
            findNavControllerSafety()?.popBackStack()
        }
    }

    override fun onBackPressed(): Boolean {
        return super.onBackPressed()
    }

    override fun initializeData() {
    }
    fun enqueueLocaleSync(context: Context, locale: String) {
        val req = OneTimeWorkRequestBuilder<LocaleSyncWorker>()
            .setInputData(workDataOf("locale" to locale))
            .setExpedited(OutOfQuotaPolicy.RUN_AS_NON_EXPEDITED_WORK_REQUEST) // nhanh cho lần đầu
            .addTag("LOCALE_SYNC")
            .build()

        WorkManager.getInstance(context).enqueueUniqueWork(
            "LOCALE_SYNC",
            ExistingWorkPolicy.REPLACE,
            req
        )
    }

    private fun updateLanguage(languageCode: String) {
        AppConfig.updateResources(requireContext(), localStorage.langCode)
        bindViewItemSelectedForTempLangCode(languageCode)
        confirmApplyLanguageBottomSheet = ConfirmApplyLanguageBottomSheet()
        confirmApplyLanguageBottomSheet?.setStyle(
            DialogFragment.STYLE_NORMAL,
            R.style.AppBottomSheetDialogTheme
        )
        confirmApplyLanguageBottomSheet?.clickConfirmYes = {
            if (languageCode.isNotBlank()) {
                val needRecreate =
                    if ((languageCode == "ar" && localStorage.langCode != "ar") || (languageCode != "ar" && localStorage.langCode == "ar")) true else false
                localStorage.langCode = languageCode
                context?.let {
                    enqueueLocaleSync(it, localStorage.langCode)
                }
                localStorage.languageScrollPosition = dataBinding.svLanguage.scrollY
                if (needRecreate) {
                    println("needRecreate w ${localStorage.langCode}")
                    updateLanguageLocale(needRecreate)
                } else {
                    updateLanguageLocale(false)
                    try {
                        bindViewItemSelected()
                        val current = Locale(localStorage.langCode)
                        val localizedContext = getLocalizedContext(requireContext(), current)
                        dataBinding.heading.text = localizedContext.getString(R.string.languages)
                        showToast(R.string.language_change_successful)
                    } catch (e: Exception) {
                        Logger.e(e.message)
                    }
                }
            }
        }
        confirmApplyLanguageBottomSheet?.clickConfirmNo = {
            isChangeLanguage = false
            bindViewItemSelected()
        }
        confirmApplyLanguageBottomSheet?.onCancel = {
            isChangeLanguage = false
        }
        if (activity?.isFinishing == false) {
            activity?.supportFragmentManager?.let {
                confirmApplyLanguageBottomSheet?.show(
                    it,
                    ConfirmApplyLanguageBottomSheet.TAG
                )
            }
        }
    }

    protected fun updateLanguageLocale(needRecreate: Boolean = false) {
        if (localStorage.langCode.isNotBlank()) {
            localStorage.justRecreate = needRecreate
            val resources: Resources? = context?.resources
            val tag = localStorage.langCode
            val locale = Locale.forLanguageTag(tag)
            Locale.setDefault(locale)
            val config = Configuration()
            config.locale = locale
            resources?.updateConfiguration(config, resources?.displayMetrics)
            val locales = LocaleListCompat.forLanguageTags(tag) // ví dụ "hi", "de", "en-US"
            AppCompatDelegate.setApplicationLocales(locales)
            if (needRecreate) {
                try {
                    requireActivity().recreate()
                } catch (e: Exception) {
                    Logger.e(e.message)
                }
            }
        }
    }

    private fun bindViewItemSelectedForTempLangCode(langCode: String) {
        if (langCode.isEmpty()) return
        when (langCode) {
            LangCode.NONE.value -> Unit

            LangCode.ENGLISH.value -> {
                lastCheckedId = R.id.rbt_english
            }

            LangCode.JAPAN.value -> {
                lastCheckedId = R.id.rbt_japan
            }

            LangCode.KOREAN.value -> {
                lastCheckedId = R.id.rbt_korean
            }

            LangCode.HINDI.value -> {
                lastCheckedId = R.id.rbt_hindi
            }

            LangCode.CHINA.value -> {
                lastCheckedId = R.id.rbt_china
            }

            LangCode.VIETNAM.value -> {
                lastCheckedId = R.id.rbt_vietnam
            }

            LangCode.PORTUGUESE.value -> {
                lastCheckedId = R.id.rbt_portuguase
            }

            LangCode.SPANISH.value -> {
                lastCheckedId = R.id.rbt_spanish
            }

            LangCode.GERMAN.value -> {
                lastCheckedId = R.id.rbt_german
            }

            LangCode.RUSSIAN.value -> {
                lastCheckedId = R.id.rbt_russian
            }

            LangCode.UKRAIAN.value -> {
                lastCheckedId = R.id.rbt_ukraian
            }

            LangCode.ABRIC.value -> {
                lastCheckedId = R.id.rbt_abric
            }

            LangCode.TURKEY.value -> {
                lastCheckedId = R.id.rbt_turkey
            }
        }
    }

    private fun bindViewItemSelected() {
        if (localStorage.langCode.isEmpty()) return
        when (localStorage.langCode) {
            LangCode.NONE.value -> Unit

            LangCode.ENGLISH.value -> {
                dataBinding.radioGroup.check(R.id.rbt_english)
                lastCheckedId = R.id.rbt_english
            }

            LangCode.JAPAN.value -> {
                dataBinding.radioGroup.check(R.id.rbt_japan)
                lastCheckedId = R.id.rbt_japan
            }

            LangCode.KOREAN.value -> {
                dataBinding.radioGroup.check(R.id.rbt_korean)
                lastCheckedId = R.id.rbt_korean
            }

            LangCode.HINDI.value -> {
                dataBinding.radioGroup.check(R.id.rbt_hindi)
                lastCheckedId = R.id.rbt_hindi
            }

            LangCode.CHINA.value -> {
                dataBinding.radioGroup.check(R.id.rbt_china)
                lastCheckedId = R.id.rbt_china
            }

            LangCode.VIETNAM.value -> {
                dataBinding.radioGroup.check(R.id.rbt_vietnam)
                lastCheckedId = R.id.rbt_vietnam
            }

            LangCode.PORTUGUESE.value -> {
                dataBinding.radioGroup.check(R.id.rbt_portuguase)
                lastCheckedId = R.id.rbt_portuguase
            }

            LangCode.SPANISH.value -> {
                dataBinding.radioGroup.check(R.id.rbt_spanish)
                lastCheckedId = R.id.rbt_spanish
            }

            LangCode.GERMAN.value -> {
                dataBinding.radioGroup.check(R.id.rbt_german)
                lastCheckedId = R.id.rbt_german
            }

            LangCode.RUSSIAN.value -> {
                dataBinding.radioGroup.check(R.id.rbt_russian)
                lastCheckedId = R.id.rbt_russian
            }

            LangCode.UKRAIAN.value -> {
                dataBinding.radioGroup.check(R.id.rbt_ukraian)
                lastCheckedId = R.id.rbt_ukraian
            }

            LangCode.ABRIC.value -> {
                dataBinding.radioGroup.check(R.id.rbt_abric)
                lastCheckedId = R.id.rbt_abric
            }

            LangCode.TURKEY.value -> {
                dataBinding.radioGroup.check(R.id.rbt_turkey)
                lastCheckedId = R.id.rbt_turkey
            }
        }
    }

//    private fun updateRadioButtonUI(checkedId: Int) {
//        lastCheckedId?.let {
//            val lastItem = getItemFromId(it)
//            lastItem?.setCardBackgroundColor(ContextCompat.getColor(requireContext(), R.color.white))
//            val lastRadioButton = view?.findViewById<RadioButton>(it)
//            lastRadioButton?.buttonTintList = ContextCompat.getColorStateList(requireContext(), R.color.color_darker_gray)
//        }
//
//        val currentItem = getItemFromId(checkedId)
//        currentItem?.setCardBackgroundColor(ContextCompat.getColor(requireContext(), R.color.color_language_selected))
//        val currentRadioButton = view?.findViewById<RadioButton>(checkedId)
//        currentRadioButton?.buttonTintList = ContextCompat.getColorStateList(requireContext(), R.color.color_language_radio_button)
//
//        //dataBinding.radioGroup.check(checkedId)
//
//        lastCheckedId = checkedId
//        updateTextColor(checkedId)
//    }
//
//    private fun getItemFromId(id: Int): CardView? {
//        return when (id) {
//            R.id.rbt_english -> dataBinding.itemEnglish
//            R.id.rbt_japan -> dataBinding.itemJapanese
//            R.id.rbt_korean -> dataBinding.itemKorean
//            R.id.rbt_hindi -> dataBinding.itemHindi
//            R.id.rbt_china -> dataBinding.itemChinese
//            R.id.rbt_vietnam -> dataBinding.itemVietnamese
//            R.id.rbt_portuguase -> dataBinding.itemPortuguese
//            R.id.rbt_spanish -> dataBinding.itemSpanish
//            R.id.rbt_german -> dataBinding.itemGerman
//            R.id.rbt_russian -> dataBinding.itemRussian
//            R.id.rbt_ukraian -> dataBinding.itemUkrainian
//            R.id.rbt_abric -> dataBinding.itemArabic
//            R.id.rbt_turkey -> dataBinding.itemTurkish
//            else -> null
//        }
//    }

    override fun onDestroy() {
        super.onDestroy()
        try {
            if (confirmApplyLanguageBottomSheet != null && confirmApplyLanguageBottomSheet?.isVisible == true) {
                confirmApplyLanguageBottomSheet?.dismiss()
            }
        } catch (e: Exception) {
            Logger.e(e.message)
        }
    }

    companion object {

    }
}