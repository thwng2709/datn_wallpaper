package com.itsthwng.twallpaper.local

import android.content.Context
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken
import com.itsthwng.twallpaper.data.model.SettingData
import com.itsthwng.twallpaper.utils.Constants
import com.itsthwng.twallpaper.utils.Constants.PreferencesKey.LANG_CODE
import dagger.hilt.android.qualifiers.ApplicationContext
import javax.inject.Inject
import kotlin.reflect.KClass
import androidx.core.content.edit

class LocalData @Inject constructor(
    @ApplicationContext context: Context, @PreferenceInfo val fileName: String
) : LocalStorage {

    private val sharedPreferences = context.getSharedPreferences(fileName, Context.MODE_PRIVATE)

    override fun saveCategories(dataList: List<SettingData.CategoriesItem>?){
        categories = Gson().toJson(dataList)
    }

    override fun getListCategories(): List<SettingData.CategoriesItem> {
        val json = categories
        return if (json.isNotEmpty()) {
            Gson().fromJson(json, object : TypeToken<List<SettingData.CategoriesItem>>() {}.type)
        } else {
            emptyList()
        }
    }

    override fun putString(key: String, value: String?) {
        sharedPreferences.edit {
            putString(key, value)
        }
    }

    override fun getString(key: String): String? {
        return sharedPreferences.getString(key, null)
    }

    override fun remove(key: String) {
        sharedPreferences.edit { remove(key) }
    }

    override var authorization: String?
        get() = getString("authorization")
        set(value) {
            putString("authorization", value)
        }

    override fun <T : Any> putData(key: String, t: T?) {
        if (t != null) {
            val str = Gson().toJson(t)
            putString(key, str)
        } else putString(key, null)
    }

    override fun <T : Any> getData(key: String): T? {
        try {
            val string = getString(key) ?: return null
            return Gson().fromJson(string, object : TypeToken<T>() {}.type)
        } catch (_: Exception) {
        }
        return null
    }

    override fun <T : Any> getData(key: String, clazz: KClass<T>): T? {
        try {
            val string = getString(key) ?: return null
            return Gson().fromJson(string, clazz.java)
        } catch (_: Exception) {
        }
        return null
    }

    override var isFirstOpen: Boolean
        get() = getData(Constants.PreferencesKey.IS_FIRST_OPEN, Boolean::class) ?: true
        set(value) {
            putData(Constants.PreferencesKey.IS_FIRST_OPEN, value)
        }

    override var isFirstInstall: Boolean
        get() = getData(Constants.PreferencesKey.IS_FIRST_INSTALL, Boolean::class) ?: true
        set(value) {
            putData(Constants.PreferencesKey.IS_FIRST_INSTALL, value)
        }
    override var langCode: String
        get() = getString(LANG_CODE) ?: ""
        set(value) {
            putString(LANG_CODE, value)
        }
    override var isFirstOpenLanguage: Boolean
        get() = getData(Constants.PreferencesKey.IS_FIRST_OPEN_LANGUAGE, Boolean::class) ?: true
        set(value) {
            putData(Constants.PreferencesKey.IS_FIRST_OPEN_LANGUAGE, value)
        }
    override var isFirstOpenLanguage2: Boolean
        get() = getData(Constants.PreferencesKey.IS_FIRST_OPEN_LANGUAGE_2, Boolean::class) ?: true
        set(value) {
            putData(Constants.PreferencesKey.IS_FIRST_OPEN_LANGUAGE_2, value)
        }
    override var isFirstOpenHome: Boolean
        get() = getData(Constants.PreferencesKey.IS_FIRST_OPEN_HOME, Boolean::class) ?: true
        set(value) {
            putData(Constants.PreferencesKey.IS_FIRST_OPEN_HOME, value)
        }
    override var isFirstOpenSetting: Boolean
        get() = getData(Constants.PreferencesKey.IS_FIRST_OPEN_SETTING, Boolean::class) ?: true
        set(value) {
            putData(Constants.PreferencesKey.IS_FIRST_OPEN_SETTING, value)
        }
    override var isFirstOpenSettingLanguage: Boolean
        get() = getData(Constants.PreferencesKey.IS_FIRST_OPEN_SETTING_LANGUAGE, Boolean::class) ?: true
        set(value) {
            putData(Constants.PreferencesKey.IS_FIRST_OPEN_SETTING_LANGUAGE, value)
        }
    override var isShowRating: Boolean
        get() = getData(Constants.PreferencesKey.IS_SHOW_RATING, Boolean::class) ?: false
        set(value) {
            putData(Constants.PreferencesKey.IS_SHOW_RATING, value)
        }
    override var isChangeLanguage: Boolean
        get() = getData(Constants.PreferencesKey.IS_CHANGE_LANGUAGE, Boolean::class) ?: false
        set(value) {
            putData(Constants.PreferencesKey.IS_CHANGE_LANGUAGE, value)
        }
    override var firstTimeOpenApp: Long
        get() = getData(Constants.PreferencesKey.FIRST_TIME_OPEN_APP, Long::class) ?: 0
        set(value) {
            putData(Constants.PreferencesKey.FIRST_TIME_OPEN_APP, value)
        }
    override var lastTimeExitApp: Long
        get() = getData(Constants.PreferencesKey.LAST_TIME_EXIT_APP, Long::class) ?: 0
        set(value) {
            putData(Constants.PreferencesKey.LAST_TIME_EXIT_APP, value)
        }
    override var isFirstSession: Int
        get() = getData("IS_FIRST_SESSION", Int::class) ?: 0
        set(value) {
            putData("IS_FIRST_SESSION", value)
        }
    override var languageScrollPosition: Int
        get() = getData("LANGUAGE_SCROLL_POSITION", Int::class) ?: 0
        set(value) {
            putData("LANGUAGE_SCROLL_POSITION", value)
        }
    override var justRecreate: Boolean
        get() = getData("JUST_RECREATE", Boolean::class) ?: false
        set(value) {
            putData("JUST_RECREATE", value)
        }
    override var isFirstOpenGrantPermissionScreen: Boolean
        get() = getData("IS_FIRST_OPEN_GRANT_PERMISSION_SCREEN", Boolean::class) ?: true
        set(value) {
            putData("IS_FIRST_OPEN_GRANT_PERMISSION_SCREEN", value)
        }
    override var isFirstNotificationPermissionRequire: Boolean
        get() = getData("IS_FIRST_NOTIFICATION_PERMISSION_REQUIRE", Boolean::class) ?: true
        set(value) {
            putData("IS_FIRST_NOTIFICATION_PERMISSION_REQUIRE", value)
        }
    override var isFirstRecoverySLSaveAs: Boolean
        get() = getData("IS_FIRST_RECOVERY_SL_SAVE_AS", Boolean::class) ?: true
        set(value) {
            putData("IS_FIRST_RECOVERY_SL_SAVE_AS", value)
        }
    override var isFirstOpenNotiPermission: Boolean
        get() = getData("IS_FIRST_OPEN_NOTI_PERMISSION", Boolean::class) ?: true
        set(value) {
            putData("IS_FIRST_OPEN_NOTI_PERMISSION", value)
        }
    override var isFirstOpenManageFilePermission: Boolean
        get() = getData("IS_FIRST_OPEN_MANAGE_FILE_PERMISSION", Boolean::class) ?: true
        set(value) {
            putData("IS_FIRST_OPEN_MANAGE_FILE_PERMISSION", value)
        }
    override var isFirstOpenBatteryPermission: Boolean
        get() = getData("IS_FIRST_OPEN_BATTERY_PERMISSION", Boolean::class) ?: true
        set(value) {
            putData("IS_FIRST_OPEN_BATTERY_PERMISSION", value)
        }
    override var isFirstOpenFeedbackFragment: Boolean
        get() = getData("IS_FIRST_OPEN_FEEDBACK_FRAGMENT", Boolean::class) ?: true
        set(value) {
            putData("IS_FIRST_OPEN_FEEDBACK_FRAGMENT", value)
        }
    override var categories: String
        get() = getData(Constants.categories, String::class) ?: ""
        set(value) {
            putData(Constants.categories, value)
        }
    override var favourites: String
        get() = getData(Constants.favourites, String::class) ?: ""
        set(value) {
            putData(Constants.favourites, value)
        }
    override var isNotification: Boolean
        get() = getData(Constants.is_notification, Boolean::class) ?: true
        set(value) {
            putData(Constants.is_notification, value)
        }
    override var hasRequestedNotificationPermission: Boolean
        get() = getData(Constants.has_requested_notification_permission, Boolean::class) ?: false
        set(value) {
            putData(Constants.has_requested_notification_permission, value)
        }
    override var currentVersionData: Long
        get() = getData(Constants.current_version_data, Long::class) ?: 0L
        set(value) {
            putData(Constants.current_version_data, value)
        }
    override var currentVersionDataR2: Long
        get() = getData(Constants.current_version_data_new, Long::class) ?: 0L
        set(value) {
            putData(Constants.current_version_data_new, value)
        }
    override var currentLocale: String
        get() = getData(Constants.current_locale, String::class) ?: ""
        set(value) {
            putData(Constants.current_locale, value)
        }
    override var lastOpenDate: String
        get() = getData("last_open_date", String::class) ?: ""
        set(value) {
            putData("last_open_date", value)
        }
    override var callOnboardingFlow: Boolean
        get() = getData("call_onboarding_flow", Boolean::class) ?: false
        set(value) {
            putData("call_onboarding_flow", value)
        }
    override var isFirstOpenDownloadScreen: Boolean
        get() = getData("IS_FIRST_OPEN_DOWNLOAD_SCREEN", Boolean::class) ?: true
        set(value) {
            putData("IS_FIRST_OPEN_DOWNLOAD_SCREEN", value)
        }
    override var isFirstOpenHistoryScreen: Boolean
        get() = getData("IS_FIRST_OPEN_HISTORY_SCREEN", Boolean::class) ?: true
        set(value) {
            putData("IS_FIRST_OPEN_HISTORY_SCREEN", value)
        }
    override var isFirstOpenCategoryScreen: Boolean
        get() = getData("IS_FIRST_OPEN_CATEGORY_SCREEN", Boolean::class) ?: true
        set(value) {
            putData("IS_FIRST_OPEN_CATEGORY_SCREEN", value)
        }
    override var isFirstOpenFavouriteScreen: Boolean
        get() = getData("IS_FIRST_OPEN_FAVOURITE_SCREEN", Boolean::class) ?: true
        set(value) {
            putData("IS_FIRST_OPEN_FAVOURITE_SCREEN", value)
        }
    override var isFirstOpenOverlayPermission: Boolean
        get() = getData("IS_FIRST_OPEN_OVERLAY_PERMISSION", Boolean::class) ?: true
        set(value) {
            putData("IS_FIRST_OPEN_OVERLAY_PERMISSION", value)
        }
    override var isFirstOpenPreviewScreen: Boolean
        get() = getData("IS_FIRST_OPEN_PREVIEW_SCREEN", Boolean::class) ?: true
        set(value) {
            putData("IS_FIRST_OPEN_PREVIEW_SCREEN", value)
        }
    override var isFirstOpenSearchScreen: Boolean
        get() = getData("IS_FIRST_OPEN_SEARCH_SCREEN", Boolean::class) ?: true
        set(value) {
            putData("IS_FIRST_OPEN_SEARCH_SCREEN", value)
        }
    override var isFirstOpenViewWallpaper: Boolean
        get() = getData("IS_FIRST_OPEN_VIEW_WALLPAPER", Boolean::class) ?: true
        set(value) {
            putData("IS_FIRST_OPEN_VIEW_WALLPAPER", value)
        }
    override var isFirstOpenWallpaperByCat: Boolean
        get() = getData("IS_FIRST_OPEN_WALLPAPER_BY_CAT", Boolean::class) ?: true
        set(value) {
            putData("IS_FIRST_OPEN_WALLPAPER_BY_CAT", value)
        }
    override var categorySort: String
        get() = getData("CATEGORY_SORT", String::class) ?: ""
        set(value) {
            putData("CATEGORY_SORT", value)
        }

    override var isFirstSelectLanguageEnglish: Boolean
        get() = getData("IS_FIRST_SELECT_LANGUAGE_ENGLISH", Boolean::class) ?: true
        set(value) {
            putData("IS_FIRST_SELECT_LANGUAGE_ENGLISH", value)
        }

    override var isFirstSelectLanguageJapan: Boolean
        get() = getData("IS_FIRST_SELECT_LANGUAGE_JAPAN", Boolean::class) ?: true
        set(value) {
            putData("IS_FIRST_SELECT_LANGUAGE_JAPAN", value)
        }

    override var isFirstSelectLanguageKorea: Boolean
        get() = getData("IS_FIRST_SELECT_LANGUAGE_KOREA", Boolean::class) ?: true
        set(value) {
            putData("IS_FIRST_SELECT_LANGUAGE_KOREA", value)
        }

    override var isFirstSelectLanguageHindi: Boolean
        get() = getData("IS_FIRST_SELECT_LANGUAGE_HINDI", Boolean::class) ?: true
        set(value) {
            putData("IS_FIRST_SELECT_LANGUAGE_HINDI", value)
        }

    override var isFirstSelectLanguageChina: Boolean
        get() = getData("IS_FIRST_SELECT_LANGUAGE_CHINA", Boolean::class) ?: true
        set(value) {
            putData("IS_FIRST_SELECT_LANGUAGE_CHINA", value)
        }

    override var isFirstSelectLanguageVietnam: Boolean
        get() = getData("IS_FIRST_SELECT_LANGUAGE_VIETNAM", Boolean::class) ?: true
        set(value) {
            putData("IS_FIRST_SELECT_LANGUAGE_VIETNAM", value)
        }

    override var isFirstSelectLanguageSpanish: Boolean
        get() = getData("IS_FIRST_SELECT_LANGUAGE_SPANISH", Boolean::class) ?: true
        set(value) {
            putData("IS_FIRST_SELECT_LANGUAGE_SPANISH", value)
        }

    override var isFirstSelectLanguagePortuguese: Boolean
        get() = getData("IS_FIRST_SELECT_LANGUAGE_PORTUGUESE", Boolean::class) ?: true
        set(value) {
            putData("IS_FIRST_SELECT_LANGUAGE_PORTUGUESE", value)
        }

    override var isFirstSelectLanguageGerman: Boolean
        get() = getData("IS_FIRST_SELECT_LANGUAGE_GERMAN", Boolean::class) ?: true
        set(value) {
            putData("IS_FIRST_SELECT_LANGUAGE_GERMAN", value)
        }

    override var isFirstSelectLanguageRussian: Boolean
        get() = getData("IS_FIRST_SELECT_LANGUAGE_RUSSIAN", Boolean::class) ?: true
        set(value) {
            putData("IS_FIRST_SELECT_LANGUAGE_RUSSIAN", value)
        }

    override var isFirstSelectLanguageUkrainian: Boolean
        get() = getData("IS_FIRST_SELECT_LANGUAGE_UKRAINIAN", Boolean::class) ?: true
        set(value) {
            putData("IS_FIRST_SELECT_LANGUAGE_UKRAINIAN", value)
        }

    override var isFirstSelectLanguageArabic: Boolean
        get() = getData("IS_FIRST_SELECT_LANGUAGE_ARABIC", Boolean::class) ?: true
        set(value) {
            putData("IS_FIRST_SELECT_LANGUAGE_ARABIC", value)
        }

    override var isFirstSelectLanguageTurkey: Boolean
        get() = getData("IS_FIRST_SELECT_LANGUAGE_TURKEY", Boolean::class) ?: true
        set(value) {
            putData("IS_FIRST_SELECT_LANGUAGE_TURKEY", value)
        }
    override var isFirstTimeNextAskLanguage: Boolean
        get() = getData("IS_FIRST_TIME_NEXT_ASK_LANGUAGE", Boolean::class) ?: true
        set(value) {
            putData("IS_FIRST_TIME_NEXT_ASK_LANGUAGE", value)
        }
    override var isFirstTimeClickPermissionNavBar: Boolean
        get() = getData("IS_FIRST_TIME_CLICK_PERMISSION_NAV_BAR", Boolean::class) ?: true
        set(value) {
            putData("IS_FIRST_TIME_CLICK_PERMISSION_NAV_BAR", value)
        }
    override var isFirstTimeClickDownloadNavBar: Boolean
        get() = getData("IS_FIRST_TIME_CLICK_DOWNLOAD_NAV_BAR", Boolean::class) ?: true
        set(value) {
            putData("IS_FIRST_TIME_CLICK_DOWNLOAD_NAV_BAR", value)
        }
    override var isFirstTimeClickSettingNavBar: Boolean
        get() = getData("IS_FIRST_TIME_CLICK_SETTING_NAV_BAR", Boolean::class) ?: true
        set(value) {
            putData("IS_FIRST_TIME_CLICK_SETTING_NAV_BAR", value)
        }
    override var isFirstClickMenuMain: Boolean
        get() = getData("IS_FIRST_CLICK_MENU_MAIN", Boolean::class) ?: true
        set(value) {
            putData("IS_FIRST_CLICK_MENU_MAIN", value)
        }
    override var isFirstClickHomeMain: Boolean
        get() = getData("IS_FIRST_CLICK_HOME_MAIN", Boolean::class) ?: true
        set(value) {
            putData("IS_FIRST_CLICK_HOME_MAIN", value)
        }
    override var isFirstClickCategoryMain: Boolean
        get() = getData("IS_FIRST_CLICK_CATEGORY_MAIN", Boolean::class) ?: true
        set(value) {
            putData("IS_FIRST_CLICK_CATEGORY_MAIN", value)
        }
    override var isFirstClickFavouriteMain: Boolean
        get() = getData("IS_FIRST_CLICK_FAVOURITE_MAIN", Boolean::class) ?: true
        set(value) {
            putData("IS_FIRST_CLICK_FAVOURITE_MAIN", value)
        }
    override var isFirstClickHistoryMain: Boolean
        get() = getData("IS_FIRST_CLICK_HISTORY_MAIN", Boolean::class) ?: true
        set(value) {
            putData("IS_FIRST_CLICK_HISTORY_MAIN", value)
        }
    override var isFirstClickZipperMain: Boolean
        get() = getData("IS_FIRST_CLICK_ZIPPER_MAIN", Boolean::class) ?: true
        set(value) {
            putData("IS_FIRST_CLICK_ZIPPER_MAIN", value)
        }
    override var isFirstClickSearchMain: Boolean
        get() = getData("IS_FIRST_CLICK_SEARCH_MAIN", Boolean::class) ?: true
        set(value) {
            putData("IS_FIRST_CLICK_SEARCH_MAIN", value)
        }
    override var isFirstClickBrowseHistory: Boolean
        get() = getData("IS_FIRST_CLICK_BROWSE_HISTORY", Boolean::class) ?: true
        set(value) {
            putData("IS_FIRST_CLICK_BROWSE_HISTORY", value)
        }
    override var isFirstClickClearFilterHistory: Boolean
        get() = getData("IS_FIRST_CLICK_CLEAR_FILTER_HISTORY", Boolean::class) ?: true
        set(value) {
            putData("IS_FIRST_CLICK_CLEAR_FILTER_HISTORY", value)
        }
    override var isFirstClickAllFilterHistory: Boolean
        get() = getData("IS_FIRST_CLICK_ALL_FILTER_HISTORY", Boolean::class) ?: true
        set(value) {
            putData("IS_FIRST_CLICK_ALL_FILTER_HISTORY", value)
        }
    override var isFirstClickHomeFilterHistory: Boolean
        get() = getData("IS_FIRST_CLICK_HOME_FILTER_HISTORY", Boolean::class) ?: true
        set(value) {
            putData("IS_FIRST_CLICK_HOME_FILTER_HISTORY", value)
        }
    override var isFirstClickLockScreenFilterHistory: Boolean
        get() = getData("IS_FIRST_CLICK_LOCK_SCREEN_FILTER_HISTORY", Boolean::class) ?: true
        set(value) {
            putData("IS_FIRST_CLICK_LOCK_SCREEN_FILTER_HISTORY", value)
        }

    override var isFirstTimeSelectWallpaper: Boolean
        get() = getData("IS_FIRST_TIME_SELECT_WALLPAPER", Boolean::class) ?: true
        set(value) {
            putData("IS_FIRST_TIME_SELECT_WALLPAPER", value)
        }
    override var isFirstTimeFavouriteWallpaper: Boolean
        get() = getData("IS_FIRST_TIME_FAVOURITE_WALLPAPER", Boolean::class) ?: true
        set(value) {
            putData("IS_FIRST_TIME_FAVOURITE_WALLPAPER", value)
        }
    override var isFirstTimeDownloadWallpaper: Boolean
        get() = getData("IS_FIRST_TIME_DOWNLOAD_WALLPAPER", Boolean::class) ?: true
        set(value) {
            putData("IS_FIRST_TIME_DOWNLOAD_WALLPAPER", value)
        }
    override var isFirstTimeSetWallpaper: Boolean
        get() = getData("IS_FIRST_TIME_SET_WALLPAPER", Boolean::class) ?: true
        set(value) {
            putData("IS_FIRST_TIME_SET_WALLPAPER", value)
        }
    override var isFirstTimeSelectCategory: Boolean
        get() = getData("IS_FIRST_TIME_SELECT_CATEGORY", Boolean::class) ?: true
        set(value) {
            putData("IS_FIRST_TIME_SELECT_CATEGORY", value)
        }

    override var userId: String
        get() = getData(Constants.PreferencesKey.USER_ID, String::class) ?: ""
        set(value) {
            putData(Constants.PreferencesKey.USER_ID, value)
        }
}
