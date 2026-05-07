package com.itsthwng.twallpaper.local

import com.itsthwng.twallpaper.data.model.SettingData
import kotlin.reflect.KClass

interface LocalStorage {

    fun putString(key: String, value: String?)
    fun getString(key: String): String?
    fun remove(key: String)

    var authorization: String?

    fun saveCategories(dataList: List<SettingData.CategoriesItem>?)

    fun getListCategories(): List<SettingData.CategoriesItem>

    fun <T : Any> putData(key: String, t: T?)

    fun <T : Any> getData(key: String): T?

    fun <T : Any> getData(key: String, clazz: KClass<T>): T?

    var isFirstOpen: Boolean

    var isFirstInstall: Boolean

    var langCode: String

    var isFirstOpenLanguage: Boolean

    var isFirstOpenLanguage2: Boolean

    var isFirstOpenHome: Boolean

    var isFirstOpenSetting: Boolean

    var isFirstOpenSettingLanguage: Boolean

    var isShowRating: Boolean


    var isChangeLanguage: Boolean
    var firstTimeOpenApp: Long

    var lastTimeExitApp: Long

    var isFirstSession: Int
    var languageScrollPosition: Int
    var justRecreate: Boolean
    var isFirstOpenGrantPermissionScreen: Boolean
    var isFirstNotificationPermissionRequire: Boolean
    var isFirstRecoverySLSaveAs: Boolean
    var isFirstOpenNotiPermission: Boolean
    var isFirstOpenManageFilePermission: Boolean
    var isFirstOpenBatteryPermission: Boolean
    var isFirstOpenOverlayPermission: Boolean
    var isFirstOpenFeedbackFragment: Boolean
    var isFirstOpenDownloadScreen: Boolean
    var isFirstOpenHistoryScreen: Boolean
    var isFirstOpenCategoryScreen: Boolean
    var isFirstOpenFavouriteScreen: Boolean
    var isFirstOpenPreviewScreen: Boolean
    var isFirstOpenSearchScreen: Boolean
    var isFirstOpenViewWallpaper: Boolean
    var isFirstOpenWallpaperByCat: Boolean

    var categories: String
    var favourites: String
    var isNotification: Boolean
    var hasRequestedNotificationPermission: Boolean
    var currentVersionData: Long
    var currentVersionDataR2: Long
    var currentLocale: String
    var lastOpenDate: String
    var callOnboardingFlow: Boolean
    var categorySort: String

    // Event Tracking Button Click
    var isFirstSelectLanguageEnglish: Boolean
    var isFirstSelectLanguageJapan: Boolean
    var isFirstSelectLanguageKorea: Boolean
    var isFirstSelectLanguageHindi: Boolean
    var isFirstSelectLanguageChina: Boolean
    var isFirstSelectLanguageVietnam: Boolean
    var isFirstSelectLanguageSpanish: Boolean
    var isFirstSelectLanguagePortuguese: Boolean
    var isFirstSelectLanguageGerman: Boolean
    var isFirstSelectLanguageRussian: Boolean
    var isFirstSelectLanguageUkrainian: Boolean
    var isFirstSelectLanguageArabic: Boolean
    var isFirstSelectLanguageTurkey: Boolean

    var isFirstTimeNextAskLanguage: Boolean

    //Main
    var isFirstTimeClickPermissionNavBar: Boolean
    var isFirstTimeClickDownloadNavBar: Boolean
    var isFirstTimeClickSettingNavBar: Boolean
    var isFirstClickMenuMain: Boolean
    var isFirstClickHomeMain: Boolean
    var isFirstClickCategoryMain: Boolean
    var isFirstClickFavouriteMain: Boolean
    var isFirstClickHistoryMain: Boolean
    var isFirstClickZipperMain: Boolean
    var isFirstClickSearchMain: Boolean
    // History
    var isFirstClickBrowseHistory: Boolean
    var isFirstClickClearFilterHistory: Boolean
    var isFirstClickAllFilterHistory: Boolean
    var isFirstClickHomeFilterHistory: Boolean
    var isFirstClickLockScreenFilterHistory: Boolean

    //All
    var isFirstTimeSelectWallpaper: Boolean
    var isFirstTimeFavouriteWallpaper: Boolean
    var isFirstTimeDownloadWallpaper: Boolean
    var isFirstTimeSetWallpaper: Boolean
    var isFirstTimeSelectCategory: Boolean

    var userId: String
}