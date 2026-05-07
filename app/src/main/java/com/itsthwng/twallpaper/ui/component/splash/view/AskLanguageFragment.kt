package com.itsthwng.twallpaper.ui.component.splash.view

import android.os.Bundle
import android.view.View
import android.widget.RadioGroup
import com.google.firebase.Firebase
import com.itsthwng.twallpaper.App
import com.itsthwng.twallpaper.R
import com.itsthwng.twallpaper.databinding.FragmentAskLanguageBinding
import com.itsthwng.twallpaper.ui.base.BaseFragmentBinding
import com.itsthwng.twallpaper.ui.component.bottomSheet.WarningBottomSheet
import com.itsthwng.twallpaper.utils.AppConfig
import com.itsthwng.twallpaper.utils.Constants
import com.itsthwng.twallpaper.utils.Logger
import dagger.hilt.android.AndroidEntryPoint
import java.util.Locale

@AndroidEntryPoint
class AskLanguageFragment : BaseFragmentBinding<FragmentAskLanguageBinding>() {

    private var langCode = App.instance.localStorage.langCode
    private var isChooseLanguage: Boolean? = false
    private var warningBottomSheet: WarningBottomSheet? = null
    private var isBackPressed = false

    override fun getContentViewId() = R.layout.fragment_ask_language

    override fun initializeViews() {
        dataBinding.txtNext.visibility = View.INVISIBLE
        if (langCode.isEmpty()) {
            val currentLang = Locale.getDefault().language            // vd: "en"
            val currentTag  = Locale.getDefault().toLanguageTag()     // vd: "en-US"
            langCode = LangCode.ENGLISH.value

            // Ưu tiên khớp theo languageTag (en-US, pt-BR...), nếu không có thì fallback theo language
            val all = enumValues<LangCode>().map { it.value.lowercase() }
            langCode = when {
                all.contains(currentTag.lowercase()) -> currentTag
                all.contains(currentLang.lowercase()) -> currentLang
                else -> LangCode.ENGLISH.value
            }
            updateLanguage()
        }

        if (localStorage.isFirstOpenLanguage) {
            localStorage.isFirstOpenLanguage = false
            AppConfig.logEventTracking(Constants.EventKey.ASKLANG1_OPEN_1ST)
        } else {
            AppConfig.logEventTracking(Constants.EventKey.ASKLANG1_OPEN_2ND)
        }
        resetViewItem()
    }

    override fun registerListeners() {
        dataBinding.radioGroup.setOnCheckedChangeListener { _: RadioGroup, checkedId: Int ->
            localStorage.isFirstSession++
            when (checkedId) {
                R.id.rbt_english -> {
                    try {
                        val isFirstSLEnglish = localStorage.isFirstSelectLanguageEnglish
                        if(isFirstSLEnglish){
                            AppConfig.logEventTracking(Constants.EventKey.ASKLANG1_SELECT_ENGLISH_1ST)
                            localStorage.isFirstSelectLanguageEnglish = false
                        } else {
                            AppConfig.logEventTracking(Constants.EventKey.ASKLANG1_SELECT_ENGLISH_2ND)
                        }
                    } catch (e : Exception){
                        Logger.e("LogEventTracking error: ${e.message}")
                    }
                    isChooseLanguage = true
                    langCode = LangCode.ENGLISH.value
                    goToLanguage2()
                }

                R.id.rbt_japan -> {
                    try {
                        val isFirstSLJapan = localStorage.isFirstSelectLanguageJapan
                        if(isFirstSLJapan){
                            AppConfig.logEventTracking(Constants.EventKey.ASKLANG1_SELECT_JAPAN_1ST)
                            localStorage.isFirstSelectLanguageJapan = false
                        } else {
                            AppConfig.logEventTracking(Constants.EventKey.ASKLANG1_SELECT_JAPAN_2ND)
                        }
                    } catch (e : Exception){
                        Logger.e("LogEventTracking error: ${e.message}")
                    }
                    isChooseLanguage = true
                    langCode = LangCode.JAPAN.value
                    goToLanguage2()
                }

                R.id.rbt_korean -> {
                    try {
                        val isFirstSLKorean = localStorage.isFirstSelectLanguageKorea
                        if(isFirstSLKorean){
                            AppConfig.logEventTracking(Constants.EventKey.ASKLANG1_SELECT_KOREAN_1ST)
                            localStorage.isFirstSelectLanguageKorea = false
                        } else {
                            AppConfig.logEventTracking(Constants.EventKey.ASKLANG1_SELECT_KOREAN_2ND)
                        }
                    } catch (e : Exception){
                        Logger.e("LogEventTracking error: ${e.message}")
                    }
                    isChooseLanguage = true
                    langCode = LangCode.KOREAN.value
                    goToLanguage2()
                }

                R.id.rbt_hindi -> {
                    try {
                        val isFirstSLHindi = localStorage.isFirstSelectLanguageHindi
                        if(isFirstSLHindi){
                            AppConfig.logEventTracking(Constants.EventKey.ASKLANG1_SELECT_HINDI_1ST)
                            localStorage.isFirstSelectLanguageHindi = false
                        } else {
                            AppConfig.logEventTracking(Constants.EventKey.ASKLANG1_SELECT_HINDI_2ND)
                        }
                    } catch (e : Exception){
                        Logger.e("LogEventTracking error: ${e.message}")
                    }
                    isChooseLanguage = true
                    langCode = LangCode.HINDI.value
                    goToLanguage2()
                }

                R.id.rbt_china -> {
                    try {
                        val isFirstSLChina = localStorage.isFirstSelectLanguageChina
                        if(isFirstSLChina){
                            AppConfig.logEventTracking(Constants.EventKey.ASKLANG1_SELECT_CHINA_1ST)
                            localStorage.isFirstSelectLanguageChina = false
                        } else {
                            AppConfig.logEventTracking(Constants.EventKey.ASKLANG1_SELECT_CHINA_2ND)
                        }
                    } catch (e : Exception){
                        Logger.e("LogEventTracking error: ${e.message}")
                    }
                    isChooseLanguage = true
                    langCode = LangCode.CHINA.value
                    goToLanguage2()
                }

                R.id.rbt_vietnam -> {
                    try {
                        val isFirstSLVietnam = localStorage.isFirstSelectLanguageVietnam
                        if(isFirstSLVietnam){
                            AppConfig.logEventTracking(Constants.EventKey.ASKLANG1_SELECT_VIETNAM_1ST)
                            localStorage.isFirstSelectLanguageVietnam = false
                        } else {
                            AppConfig.logEventTracking(Constants.EventKey.ASKLANG1_SELECT_VIETNAM_2ND)
                        }
                    } catch (e : Exception){
                        Logger.e("LogEventTracking error: ${e.message}")
                    }
                    isChooseLanguage = true
                    langCode = LangCode.VIETNAM.value
                    goToLanguage2()
                }

                R.id.rbt_portuguase -> {
                    try {
                        val isFirstSLPortuguese = localStorage.isFirstSelectLanguagePortuguese
                        if(isFirstSLPortuguese){
                            AppConfig.logEventTracking(Constants.EventKey.ASKLANG1_SELECT_PORTUGUESE_1ST)
                            localStorage.isFirstSelectLanguagePortuguese = false
                        } else {
                            AppConfig.logEventTracking(Constants.EventKey.ASKLANG1_SELECT_PORTUGUESE_2ND)
                        }
                    } catch (e : Exception){
                        Logger.e("LogEventTracking error: ${e.message}")
                    }
                    isChooseLanguage = true
                    langCode = LangCode.PORTUGUESE.value
                    goToLanguage2()
                }

                R.id.rbt_spanish -> {
                    try {
                        val isFirstSLSpanish = localStorage.isFirstSelectLanguageSpanish
                        if(isFirstSLSpanish){
                            AppConfig.logEventTracking(Constants.EventKey.ASKLANG1_SELECT_SPANISH_1ST)
                            localStorage.isFirstSelectLanguageSpanish = false
                        } else {
                            AppConfig.logEventTracking(Constants.EventKey.ASKLANG1_SELECT_SPANISH_2ND)
                        }
                    } catch (e : Exception){
                        Logger.e("LogEventTracking error: ${e.message}")
                    }
                    isChooseLanguage = true
                    langCode = LangCode.SPANISH.value
                    goToLanguage2()
                }

                R.id.rbt_german -> {
                    try {
                        val isFirstSLGerman = localStorage.isFirstSelectLanguageGerman
                        if(isFirstSLGerman){
                            AppConfig.logEventTracking(Constants.EventKey.ASKLANG1_SELECT_GERMAN_1ST)
                            localStorage.isFirstSelectLanguageGerman = false
                        } else {
                            AppConfig.logEventTracking(Constants.EventKey.ASKLANG1_SELECT_GERMAN_2ND)
                        }
                    } catch (e : Exception){
                        Logger.e("LogEventTracking error: ${e.message}")
                    }
                    isChooseLanguage = true
                    langCode = LangCode.GERMAN.value
                    goToLanguage2()
                }

                R.id.rbt_russian -> {
                    try {
                        val isFirstSLRussian = localStorage.isFirstSelectLanguageRussian
                        if(isFirstSLRussian){
                            AppConfig.logEventTracking(Constants.EventKey.ASKLANG1_SELECT_RUSSIAN_1ST)
                            localStorage.isFirstSelectLanguageRussian = false
                        } else {
                            AppConfig.logEventTracking(Constants.EventKey.ASKLANG1_SELECT_RUSSIAN_2ND)
                        }
                    } catch (e : Exception){
                        Logger.e("LogEventTracking error: ${e.message}")
                    }
                    isChooseLanguage = true
                    langCode = LangCode.RUSSIAN.value
                    goToLanguage2()
                }

                R.id.rbt_ukraian -> {
                    try {
                        val isFirstSLUkrainian = localStorage.isFirstSelectLanguageUkrainian
                        if(isFirstSLUkrainian){
                            AppConfig.logEventTracking(Constants.EventKey.ASKLANG1_SELECT_UKRAIAN_1ST)
                            localStorage.isFirstSelectLanguageUkrainian = false
                        } else {
                            AppConfig.logEventTracking(Constants.EventKey.ASKLANG1_SELECT_UKRAIAN_2ND)
                        }
                    } catch (e : Exception){
                        Logger.e("LogEventTracking error: ${e.message}")
                    }
                    isChooseLanguage = true
                    langCode = LangCode.UKRAIAN.value
                    goToLanguage2()
                }

                R.id.rbt_abric -> {
                    try {
                        val isFirstSLAbric = localStorage.isFirstSelectLanguageArabic
                        if(isFirstSLAbric){
                            AppConfig.logEventTracking(Constants.EventKey.ASKLANG1_SELECT_ABRIC_1ST)
                            localStorage.isFirstSelectLanguageArabic = false
                        } else {
                            AppConfig.logEventTracking(Constants.EventKey.ASKLANG1_SELECT_ABRIC_2ND)
                        }
                    } catch (e : Exception){
                        Logger.e("LogEventTracking error: ${e.message}")
                    }
                    isChooseLanguage = true
                    langCode = LangCode.ABRIC.value
                    goToLanguage2()
                }

                R.id.rbt_turkey -> {
                    try {
                        val isFirstSLTurkey = localStorage.isFirstSelectLanguageTurkey
                        if(isFirstSLTurkey){
                            AppConfig.logEventTracking(Constants.EventKey.ASKLANG1_SELECT_TURKEY_1ST)
                            localStorage.isFirstSelectLanguageTurkey = false
                        } else {
                            AppConfig.logEventTracking(Constants.EventKey.ASKLANG1_SELECT_TURKEY_2ND)
                        }
                    } catch (e : Exception){
                        Logger.e("LogEventTracking error: ${e.message}")
                    }
                    isChooseLanguage = true
                    langCode = LangCode.TURKEY.value
                    goToLanguage2()
                }

                else -> Unit
            }
        }

        dataBinding.txtNext.setOnClickListener {
            if (isChooseLanguage == true) {
                when (langCode) {
                    LangCode.ENGLISH.value -> {
                        AppConfig.logEventTracking(Constants.BUNDLE_ANALYTICS_LANGUAGE_ENLISH)
                    }

                    LangCode.JAPAN.value -> {
                        AppConfig.logEventTracking(Constants.BUNDLE_ANALYTICS_LANGUAGE_JAPAN)
                    }

                    LangCode.KOREAN.value -> {
                        AppConfig.logEventTracking(Constants.BUNDLE_ANALYTICS_LANGUAGE_KOREA)
                    }

                    LangCode.HINDI.value -> {
                        AppConfig.logEventTracking(Constants.BUNDLE_ANALYTICS_LANGUAGE_HINDI)
                    }

                    LangCode.CHINA.value -> {
                        AppConfig.logEventTracking(Constants.BUNDLE_ANALYTICS_LANGUAGE_CHINA)
                    }

                    LangCode.VIETNAM.value -> {
                        AppConfig.logEventTracking(Constants.BUNDLE_ANALYTICS_LANGUAGE_VIETNAM)
                    }

                    LangCode.PORTUGUESE.value -> {
                        AppConfig.logEventTracking(Constants.BUNDLE_ANALYTICS_LANGUAGE_PORTUGUESE)
                    }

                    LangCode.SPANISH.value -> {
                        AppConfig.logEventTracking(Constants.BUNDLE_ANALYTICS_LANGUAGE_SPANISH)
                    }

                    LangCode.GERMAN.value -> {
                        AppConfig.logEventTracking(Constants.BUNDLE_ANALYTICS_LANGUAGE_GERMAN)
                    }

                    LangCode.RUSSIAN.value -> {
                        AppConfig.logEventTracking(Constants.BUNDLE_ANALYTICS_LANGUAGE_RUSSIAN)
                    }

                    LangCode.UKRAIAN.value -> {
                        AppConfig.logEventTracking(Constants.BUNDLE_ANALYTICS_LANGUAGE_UKRAIAN)
                    }

                    LangCode.ABRIC.value -> {
                        AppConfig.logEventTracking(Constants.BUNDLE_ANALYTICS_LANGUAGE_ABRIC)
                    }

                    LangCode.TURKEY.value -> {
                        AppConfig.logEventTracking(Constants.BUNDLE_ANALYTICS_LANGUAGE_TURKEY)
                    }
                }
            } else {
                initWarningBottomSheet()
            }
        }
    }

    private fun goToLanguage2() {
        localStorage.languageScrollPosition = dataBinding.scvLanguage.scrollY
        val bundle = Bundle().apply {
            putString(KEY_LANGUAGE, langCode)
            putInt(SCROLL_POSITION, dataBinding.scvLanguage.scrollY)
        }
        try {
            AppConfig.logEventTracking(Constants.EventKey.GO_TO_LANGUAGE_2)
        } catch (e : Exception){
            Logger.e("LogEventTracking error: ${e.message}")
        }
        try {
            findNavControllerSafety()?.navigate(R.id.action_askLanguageFragment_to_askLanguageFragment2, bundle)
        } catch (e: Throwable) {
            Logger.e(e.message)
        }
    }


    override fun initializeData() {

    }

    override fun onResume() {
        super.onResume()
        // Vô hiệu hóa AppResumeAdHelper để tránh hiển thị quảng cáo khi nhấn Back

        if (localStorage.isFirstOpenLanguage) {
            AppConfig.logEventTracking(Constants.BUNDLE_ANALYTICS_GO_TO_LANGUAGE_FIRST)
            localStorage.isFirstOpenLanguage = false
        } else {
            AppConfig.logEventTracking(Constants.BUNDLE_ANALYTICS_GO_TO_LANGUAGE_AGAIN)
        }
    }

    private fun resetViewItem() {
        dataBinding.itemEnglish.setBackgroundResource(R.drawable.bg_item_language)
        dataBinding.itemJapan.setBackgroundResource(R.drawable.bg_item_language)
        dataBinding.itemKorean.setBackgroundResource(R.drawable.bg_item_language)
        dataBinding.itemHindi.setBackgroundResource(R.drawable.bg_item_language)
        dataBinding.itemChina.setBackgroundResource(R.drawable.bg_item_language)
        dataBinding.itemVietNam.setBackgroundResource(R.drawable.bg_item_language)
        dataBinding.itemPortuguase.setBackgroundResource(R.drawable.bg_item_language)
        dataBinding.itemSpanish.setBackgroundResource(R.drawable.bg_item_language)
        dataBinding.itemGerman.setBackgroundResource(R.drawable.bg_item_language)
        dataBinding.itemRussian.setBackgroundResource(R.drawable.bg_item_language)
        dataBinding.itemUkraian.setBackgroundResource(R.drawable.bg_item_language)
        dataBinding.itemAbric.setBackgroundResource(R.drawable.bg_item_language)
        dataBinding.itemTurkey.setBackgroundResource(R.drawable.bg_item_language)
    }

    private fun bindViewItemSelected() {
        if (langCode.isEmpty()) return
        when (langCode) {
            LangCode.NONE.value -> Unit

            LangCode.ENGLISH.value -> {
            }

            LangCode.JAPAN.value -> {
            }

            LangCode.KOREAN.value -> {
            }

            LangCode.HINDI.value -> {
            }

            LangCode.CHINA.value -> {
            }

            LangCode.VIETNAM.value -> {
            }

            LangCode.PORTUGUESE.value -> {
            }

            LangCode.SPANISH.value -> {
            }

            LangCode.GERMAN.value -> {
            }

            LangCode.RUSSIAN.value -> {
            }

            LangCode.UKRAIAN.value -> {
            }

            LangCode.ABRIC.value -> {
            }

            LangCode.TURKEY.value -> {
            }
        }
    }

    private fun initWarningBottomSheet() {
        warningBottomSheet = WarningBottomSheet()
        if (activity?.isFinishing == false) {
            activity?.supportFragmentManager?.let {
                warningBottomSheet?.show(
                    it, WarningBottomSheet.TAG
                )
            }
        }
    }


    enum class LangCode(val value: String) {
        NONE(""), ENGLISH("en"), JAPAN("ja"), KOREAN("ko"), HINDI("hi"), CHINA("zh"), VIETNAM("vi"), SPANISH(
            "es"
        ),
        PORTUGUESE("pt"), GERMAN("de"), RUSSIAN("ru"), UKRAIAN("uk"), ABRIC("ar"), TURKEY("tr")
    }

    companion object {
        const val KEY_LANGUAGE = "KEY_LANGUAGE"
        const val SCROLL_POSITION = "SCROLL_POSITION"
        const val TAG = "AskLanguageFragment"
    }

}