package com.itsthwng.twallpaper.utils

import android.Manifest
import android.os.Build
import com.itsthwng.twallpaper.BuildConfig

object Constants {
    val PERMISSIONS = if (Build.VERSION.SDK_INT <= Build.VERSION_CODES.P) {
        arrayOf(Manifest.permission.WRITE_EXTERNAL_STORAGE)
    } else {
        emptyArray()
    }

    // Wallpaper constants
    const val BASE = "https://taki-tech.com/"
    const val APIKEY = "123"
    const val BASE_URL = BASE + "image/upload/f_auto,q_auto/"
    const val BASE_VIDEO_URL = BASE + "video/upload/f_auto,q_auto/"
    const val TERMS_URL = BASE + "termsOfUse"
    const val PRIVACY_URL = BASE + "privacyPolicy"
    // Notification Configuration
    const val FCM_CHANNEL_ID = "fcm_default_channel"
    const val FCM_CHANNEL_NAME = "Firebase Notifications"
    const val FCM_TAG = "MyFirebaseMessaging"
    const val IMAGE_DOWNLOAD_TIMEOUT = 10000 // 10 seconds
    const val MAX_IMAGE_SIZE = 5 * 1024 * 1024 // 5MB

    // Notification Topics
    const val TOPIC_WALLPAPER_UPDATES = "wallpaper_updates"
    const val TOPIC_GENERAL_NOTIFICATIONS = "general_notifications"
    const val TOPIC_PROMOTIONAL_NOTIFICATIONS = "promotional_notifications"
    const val PAGINATION_COUNT = 20
    const val IMAGE_PREMIUM_TYPE = 0
    const val IMAGE_LOCK_TYPE = 1
    const val IMAGE_TYPE_FREE = 2

    // Broadcast Actions
    const val ACTION_LIVE_WALLPAPER_SET = "com.tkd.wallpaper.LIVE_WALLPAPER_SET"

    // KEY:
    const val categories = "categories"
    const val favourites = "favourites"
    const val downloaded = "downloaded"
    const val wallpaper = "wallpaper"
    const val position = "position"
    const val dataList = "dataList"
    const val is_notification = "is_notification"
    const val has_requested_notification_permission = "has_requested_notification_permission"
    const val current_version_data = "current_version_data"
    const val current_version_data_new = "current_version_data_new"
    const val current_locale = "current_locale"
    const val is_old = "is_old"
    const val data = "data"

    const val URL_POLICY = "https://sites.google.com/view/higherstudio-wallpaper"
    const val TIME_DELAY_SPLASH_MAX = 15000L
    const val GOOGLE_PLAY_URL_APP = "https://play.google.com/store/apps/details?id="
    const val EMAIL = "thwng2709@gmail.com"
    const val SUBJECT_EMAIL = "Feedback Wallpapers Theme - " + BuildConfig.VERSION_NAME

    const val DEFAULT_MILLISECOND_INTERVAL = 1000L
    const val DEFAULT_HOUR_INTERVAL = 3600L
    const val DEFAULT_MINUTE_INTERVAL = 60L

    const val KEY_ANALYTICS_TRACKING = "KEY_ANALYTICS_TRACKING"

    const val FORMAT_DATE = "yyyy-MM-dd"
    const val FORMAT_TIME = "yyyy-MM-dd HH:mm:ss"
    const val ZERO_TIME = " 00:00:00"


    const val VALUE_ANALYTICS_TRACKING = "tracking_flow"
    const val BUNDLE_ANALYTICS_GO_TO_LANGUAGE_FIRST = "GO_TO_LANGUAGE_FIRST"
    const val BUNDLE_ANALYTICS_GO_TO_LANGUAGE_AGAIN = "GO_TO_LANGUAGE_AGAIN"

    const val BUNDLE_ANALYTICS_LANGUAGE_ENLISH = "LANGUAGE_ENLISH"
    const val BUNDLE_ANALYTICS_LANGUAGE_VIETNAM = "LANGUAGE_VIETNAM"
    const val BUNDLE_ANALYTICS_LANGUAGE_HINDI = "LANGUAGE_HINDI"
    const val BUNDLE_ANALYTICS_LANGUAGE_JAPAN = "LANGUAGE_JAPAN"
    const val BUNDLE_ANALYTICS_LANGUAGE_KOREA = "LANGUAGE_KOREA"
    const val BUNDLE_ANALYTICS_LANGUAGE_CHINA = "LANGUAGE_CHINA"
    const val BUNDLE_ANALYTICS_LANGUAGE_PORTUGUESE = "LANGUAGE_PORTUGUESE"
    const val BUNDLE_ANALYTICS_LANGUAGE_SPANISH = "LANGUAGE_SPANISH"
    const val BUNDLE_ANALYTICS_LANGUAGE_GERMAN = "LANGUAGE_GERMAN"
    const val BUNDLE_ANALYTICS_LANGUAGE_RUSSIAN = "LANGUAGE_RUSSIAN"
    const val BUNDLE_ANALYTICS_LANGUAGE_UKRAIAN = "LANGUAGE_UKRAIAN"
    const val BUNDLE_ANALYTICS_LANGUAGE_ABRIC = "LANGUAGE_ABRIC"
    const val BUNDLE_ANALYTICS_LANGUAGE_TURKEY = "LANGUAGE_TURKEY"

    //SETTING
    const val BUNDLE_ANALYTICS_DIALOG_UPDATE_YES = "DIALOG_UPDATE_YES"
    const val BUNDLE_ANALYTICS_DIALOG_UPDATE_NO = "DIALOG_UPDATE_NO"
    const val BUNDLE_ANALYTICS_DIALOG_UPDATE_CANCEL = "DIALOG_UPDATE_CANCEL"

    const val BUNDLE_ANALYTICS_RATING_1_HOME = "RATING_1_HOME"
    const val BUNDLE_ANALYTICS_RATING_2_HOME = "RATING_2_HOME"
    const val BUNDLE_ANALYTICS_RATING_3_HOME = "RATING_3_HOME"
    const val BUNDLE_ANALYTICS_RATING_4_HOME = "RATING_4_HOME"
    const val BUNDLE_ANALYTICS_RATING_5_HOME = "RATING_5_HOME"

    const val NAVIGATE_FAVOURITE = 2
    const val NAVIGATE_ZIPPER_LOCKER = 4
    const val NAVIGATE_HISTORY = 3

    const val MAIN_REQUIRE_INTERNET_YES = "MAIN_REQUIRE_INTERNET_YES"
    const val MAIN_REQUIRE_INTERNET_EXIT = "MAIN_REQUIRE_INTERNET_EXIT"
    const val MAIN_REQUEST_INTERNET_CANCEL = "MAIN_REQUEST_INTERNET_CANCEL"

    const val HOME_REQUIRE_INTERNET_YES = "HOME_REQUIRE_INTERNET_YES"
    const val HOME_REQUIRE_INTERNET_EXIT = "HOME_REQUIRE_INTERNET_EXIT"
    const val HOME_REQUEST_INTERNET_CANCEL = "HOME_REQUEST_INTERNET_CANCEL"
    object PreferencesKey {

        const val IS_FIRST_OPEN = "is_first_open"
        const val IS_FIRST_INSTALL = "is_first_install"
        const val LANG_CODE = "lang_code"
        const val IS_FIRST_OPEN_INTRO_1 = "is_first_open_intro_1"
        const val IS_FIRST_OPEN_INTRO_2 = "is_first_open_intro_2"
        const val IS_FIRST_OPEN_INTRO_3 = "is_first_open_intro_3"
        const val IS_FIRST_OPEN_INTRO_4 = "is_first_open_intro_4"
        const val IS_FIRST_OPEN_LANGUAGE = "is_first_open_language"
        const val IS_FIRST_OPEN_LANGUAGE_2 = "is_first_open_language_2"
        const val IS_FIRST_OPEN_HOME = "is_first_open_home"
        const val IS_FIRST_OPEN_SETTING = "is_first_open_setting"
        const val IS_FIRST_OPEN_SETTING_LANGUAGE = "is_first_open_setting_language"
        const val IS_SHOW_RATING = "IS_SHOW_RATING"
        const val IS_CHANGE_LANGUAGE = "IS_CHANGE_LANGUAGE"
        const val FIRST_TIME_OPEN_APP = "FIRST_TIME_OPEN_APP"
        const val LAST_TIME_EXIT_APP = "LAST_TIME_EXIT_APP"


        const val NOTIFICATION_ENABLED = "NOTIFICATION_ENABLED"
        const val NOTIFICATION_DISABLED = "NOTIFICATION_DISABLED"

        const val USER_ID = "USER_ID"
    }

    const val ZIPPER_IMAGE = "zipper_image"
    const val ZIPPERS = "zippers"
    const val CHAINS = "chains"

    object EventKey {
        const val GO_TO_SPLASH = "GO_TO_SPLASH"
        const val GO_TO_LANGUAGE_1 = "GO_TO_LANGUAGE_1"
        const val GO_TO_LANGUAGE_2 = "GO_TO_LANGUAGE_2"
        const val GO_TO_INTRO = "GO_TO_INTRO"
        const val GO_TO_INTRO_1 = "GO_TO_INTRO_1"
        const val GO_TO_INTRO_2 = "GO_TO_INTRO_2"
        const val GO_TO_INTRO_3 = "GO_TO_INTRO_3"
        const val GO_TO_INTRO_4 = "GO_TO_INTRO_4"
        const val GO_TO_HOME = "GO_TO_HOME"
        const val GO_TO_CATEGORY = "GO_TO_CATEGORY"
        const val GO_TO_ZIPPER_LOCKER = "GO_TO_ZIPPER_LOCKER"
        const val GO_TO_FAVOURITE = "GO_TO_FAVOURITE"
        const val GO_TO_HISTORY = "GO_TO_HISTORY"
        const val GO_TO_CATEGORY_DETAIL = "GO_TO_CATEGORY_DETAIL"
        const val GO_TO_VIEW_WALLPAPER = "GO_TO_VIEW_WALLPAPER"
        const val GO_TO_PREVIEW_WALLPAPER = "GO_TO_PREVIEW_WALLPAPER"
        const val GO_TO_SEARCH = "GO_TO_SEARCH"
        const val GO_TO_DOWNLOAD = "GO_TO_DOWNLOAD"
        const val GO_TO_GRANT_PERMISSION = "GO_TO_GRANT_PERMISSION"
        const val GO_TO_SETTING = "GO_TO_SETTING"
        const val GO_TO_FEEDBACK = "GO_TO_FEEDBACK"
        const val GO_TO_LANGUAGE_SETTING = "GO_TO_LANGUAGE_SETTING"
        const val GO_TO_CUSTOMIZE = "GO_TO_CUSTOMIZE"
        const val GO_TO_ZIPPER_SETTING = "GO_TO_ZIPPER_SETTING"
        const val GO_TO_ZIPPER_FOREGROUND = "GO_TO_ZIPPER_FOREGROUND"
        const val GO_TO_ZIPPER_BACKGROUND = "GO_TO_ZIPPER_BACKGROUND"
        const val GO_TO_ZIPPER_FONT = "GO_TO_ZIPPER_FONT"
        const val GO_TO_ZIPPER_STYLE = "GO_TO_ZIPPER_STYLE"
        const val GO_TO_PENDANT_STYLE = "GO_TO_PENDANT_STYLE"
        const val GO_TO_OVERLAY_PERMISSION = "GO_TO_OVERLAY_PERMISSION"
        const val GO_TO_ACTIVATE_LOCK = "GO_TO_ACTIVATE_LOCK"
        const val GO_TO_CUSTOM_ZIPPER_FOREGROUND = "GO_TO_CUSTOM_ZIPPER_FOREGROUND"
        const val GO_TO_CUSTOM_ZIPPER_BACKGROUND = "GO_TO_CUSTOM_ZIPPER_BACKGROUND"
        const val GO_TO_CUSTOM_ZIPPER_FONT = "GO_TO_CUSTOM_ZIPPER_FONT"
        const val GO_TO_CUSTOM_ZIPPER_STYLE = "GO_TO_CUSTOM_ZIPPER_STYLE"
        const val GO_TO_CUSTOM_PENDANT_STYLE = "GO_TO_CUSTOM_PENDANT_STYLE"
        const val ASKLANG1_OPEN_1ST = "ASKLANG1_OPEN_1ST"
        const val ASKLANG1_OPEN_2ND = "ASKLANG1_OPEN_2ND"
        const val ASKLANG2_OPEN_1ST = "ASKLANG2_OPEN_1ST"
        const val ASKLANG2_OPEN_2ND = "ASKLANG2_OPEN_2ND"
        const val INTRO1_OPEN_1ST = "INTRO1_OPEN_1ST"
        const val INTRO1_OPEN_2ND = "INTRO1_OPEN_2ND"
        const val INTRO2_OPEN_1ST = "INTRO2_OPEN_1ST"
        const val INTRO2_OPEN_2ND = "INTRO2_OPEN_2ND"
        const val INTRO3_OPEN_1ST = "INTRO3_OPEN_1ST"
        const val INTRO3_OPEN_2ND = "INTRO3_OPEN_2ND"
        const val INTRO4_OPEN_1ST = "INTRO4_OPEN_1ST"
        const val INTRO4_OPEN_2ND = "INTRO4_OPEN_2ND"
        const val NOTI_PERMISSION_OPEN_1ST = "NOTI_PERMISSION_OPEN_1ST"
        const val NOTI_PERMISSION_OPEN_2ND = "NOTI_PERMISSION_OPEN_2ND"
        const val BATTERYPER_OPEN_1ST = "BATTERYPER_OPEN_1ST"
        const val BATTERYPER_OPEN_2ND = "BATTERYPER_OPEN_2ND"
        const val MANAGEFILE_OPEN_1ST = "MANAGEFILE_OPEN_1ST"
        const val MANAGEFILE_OPEN_2ND = "MANAGEFILE_OPEN_2ND"
        const val HOME_OPEN_1ST = "HOME_OPEN_1ST"
        const val HOME_OPEN_2ND = "HOME_OPEN_2ND"
        const val GRANTPER_OPEN_1ST = "GRANTPER_OPEN_1ST"
        const val GRANTPER_OPEN_2ND = "GRANTPER_OPEN_2ND"
        const val SETTING_OPEN_1ST = "SETTING_OPEN_1ST"
        const val SETTING_OPEN_2ND = "SETTING_OPEN_2ND"
        const val LANGSETTING_OPEN_1ST = "LANGSETTING_OPEN_1ST"
        const val LANGSETTING_OPEN_2ND = "LANGSETTING_OPEN_2ND"
        const val FEEDBACK_OPEN_1ST = "FEEDBACK_OPEN_1ST"
        const val FEEDBACK_OPEN_2ND = "FEEDBACK_OPEN_2ND"
        const val OVERLAY_PERMISSION_OPEN_1ST = "OVERLAY_PERMISSION_OPEN_1ST"
        const val OVERLAY_PERMISSION_OPEN_2ND = "OVERLAY_PERMISSION_OPEN_2ND"
        const val DOWNLOAD_OPEN_1ST = "DOWNLOAD_OPEN_1ST"
        const val DOWNLOAD_OPEN_2ND = "DOWNLOAD_OPEN_2ND"
        const val HISTORY_OPEN_1ST = "HISTORY_OPEN_1ST"
        const val HISTORY_OPEN_2ND = "HISTORY_OPEN_2ND"
        const val CATEGORY_OPEN_1ST = "CATEGORY_OPEN_1ST"
        const val CATEGORY_OPEN_2ND = "CATEGORY_OPEN_2ND"
        const val FAVOURITE_OPEN_1ST = "FAVOURITE_OPEN_1ST"
        const val FAVOURITE_OPEN_2ND = "FAVOURITE_OPEN_2ND"
        const val PREVIEW_OPEN_1ST = "PREVIEW_OPEN_1ST"
        const val PREVIEW_OPEN_2ND = "PREVIEW_OPEN_2ND"
        const val SEARCH_OPEN_1ST = "SEARCH_OPEN_1ST"
        const val SEARCH_OPEN_2ND = "SEARCH_OPEN_2ND"
        const val WALLPAPER_OPEN_1ST = "WALLPAPER_OPEN_1ST"
        const val WALLPAPER_OPEN_2ND = "WALLPAPER_OPEN_2ND"
        const val WALLPAPER_BY_CAT_OPEN_1ST = "WALLPAPER_BY_CAT_OPEN_1ST"
        const val WALLPAPER_BY_CAT_OPEN_2ND = "WALLPAPER_BY_CAT_OPEN_2ND"

        const val DENY_OVERLAY_PERMISSION = "DENY_OVERLAY_PERMISSION"
        const val GRANT_OVERLAY_PERMISSION = "GRANT_OVERLAY_PERMISSION"
        const val GRANT_NOTI_PERMISSION = "GRANT_NOTI_PERMISSION"
        const val DENY_NOTI_PERMISSION = "DENY_NOTI_PERMISSION"

        const val SELECT_WALLPAPER = "SELECT_WALLPAPER_"
        const val FAVOURITE_WALLPAPER = "FAVOURITE_WALLPAPER_"
        const val DOWNLOAD_WALLPAPER = "DOWNLOAD_WALLPAPER_"
        const val SET_WALLPAPER = "SET_WALLPAPER_"
        const val SELECT_CATEGORY = "SELECT_CATEGORY_"
        const val SELECT_ZIP = "SELECT_ZIP_"
        const val SET_ZIP = "SET_ZIP_"
        const val ENABLE_ZIPPER = "ENABLE_ZIPPER"
        const val SELECT_SET_WALLPAPER = "SELECT_SET_WALLPAPER"

        //Ask Language 1
        const val ASKLANG1_SELECT_ENGLISH_1ST = "ASKLANG1_SELECT_ENGLISH_1ST"
        const val ASKLANG1_SELECT_ENGLISH_2ND = "ASKLANG1_SELECT_ENGLISH_2ND"
        const val ASKLANG1_SELECT_VIETNAM_1ST = "ASKLANG1_SELECT_VIETNAM_1ST"
        const val ASKLANG1_SELECT_VIETNAM_2ND = "ASKLANG1_SELECT_VIETNAM_2ND"
        const val ASKLANG1_SELECT_HINDI_1ST = "ASKLANG1_SELECT_HINDI_1ST"
        const val ASKLANG1_SELECT_HINDI_2ND = "ASKLANG1_SELECT_HINDI_2ND"
        const val ASKLANG1_SELECT_JAPAN_1ST = "ASKLANG1_SELECT_JAPAN_1ST"
        const val ASKLANG1_SELECT_JAPAN_2ND = "ASKLANG1_SELECT_JAPAN_2ND"
        const val ASKLANG1_SELECT_KOREAN_1ST = "ASKLANG1_SELECT_KOREAN_1ST"
        const val ASKLANG1_SELECT_KOREAN_2ND = "ASKLANG1_SELECT_KOREAN_2ND"
        const val ASKLANG1_SELECT_CHINA_1ST = "ASKLANG1_SELECT_CHINA_1ST"
        const val ASKLANG1_SELECT_CHINA_2ND = "ASKLANG1_SELECT_CHINA_2ND"
        const val ASKLANG1_SELECT_PORTUGUESE_1ST = "ASKLANG1_SELECT_PORTUGUESE_1ST"
        const val ASKLANG1_SELECT_PORTUGUESE_2ND = "ASKLANG1_SELECT_PORTUGUESE_2ND"
        const val ASKLANG1_SELECT_SPANISH_1ST = "ASKLANG1_SELECT_SPANISH_1ST"
        const val ASKLANG1_SELECT_SPANISH_2ND = "ASKLANG1_SELECT_SPANISH_2ND"
        const val ASKLANG1_SELECT_GERMAN_1ST = "ASKLANG1_SELECT_GERMAN_1ST"
        const val ASKLANG1_SELECT_GERMAN_2ND = "ASKLANG1_SELECT_GERMAN_2ND"
        const val ASKLANG1_SELECT_RUSSIAN_1ST = "ASKLANG1_SELECT_RUSSIAN_1ST"
        const val ASKLANG1_SELECT_RUSSIAN_2ND = "ASKLANG1_SELECT_RUSSIAN_2ND"
        const val ASKLANG1_SELECT_UKRAIAN_1ST = "ASKLANG1_SELECT_UKRAIAN_1ST"
        const val ASKLANG1_SELECT_UKRAIAN_2ND = "ASKLANG1_SELECT_UKRAIAN_2ND"
        const val ASKLANG1_SELECT_ABRIC_1ST = "ASKLANG1_SELECT_ABRIC_1ST"
        const val ASKLANG1_SELECT_ABRIC_2ND = "ASKLANG1_SELECT_ABRIC_2ND"
        const val ASKLANG1_SELECT_TURKEY_1ST = "ASKLANG1_SELECT_TURKEY_1ST"
        const val ASKLANG1_SELECT_TURKEY_2ND = "ASKLANG1_SELECT_TURKEY_2ND"

        //Ask Language 2
        const val ASKLANG2_NEXT_1ST = "ASKLANG2_NEXT_1ST"
        const val ASKLANG2_NEXT_2ND = "ASKLANG2_NEXT_2ND"

        // Main
        const val MAIN_SL_PERMISSION_MENU_1ST = "MAIN_SL_PERMISSION_MENU_1ST"
        const val MAIN_SL_PERMISSION_MENU_2ND = "MAIN_SL_PERMISSION_MENU_2ND"
        const val MAIN_SL_DOWNLOAD_MENU_1ST = "MAIN_SL_DOWNLOAD_MENU_1ST"
        const val MAIN_SL_DOWNLOAD_MENU_2ND = "MAIN_SL_DOWNLOAD_MENU_2ND"
        const val MAIN_SL_SETTING_MENU_1ST = "MAIN_SL_SETTING_MENU_1ST"
        const val MAIN_SL_SETTING_MENU_2ND = "MAIN_SL_SETTING_MENU_2ND"
        const val MAIN_SL_MENU_1ST = "MAIN_SL_MENU_1ST"
        const val MAIN_SL_MENU_2ND = "MAIN_SL_MENU_2ND"
        const val MAIN_SL_ZIPPER_1ST = "MAIN_SL_ZIPPER_1ST"
        const val MAIN_SL_ZIPPER_2ND = "MAIN_SL_ZIPPER_2ND"
        const val MAIN_SL_FAVOURITE_1ST = "MAIN_SL_FAVOURITE_1ST"
        const val MAIN_SL_FAVOURITE_2ND = "MAIN_SL_FAVOURITE_2ND"
        const val MAIN_SL_HISTORY_1ST = "MAIN_SL_HISTORY_1ST"
        const val MAIN_SL_HISTORY_2ND = "MAIN_SL_HISTORY_2ND"
        const val MAIN_SL_CATEGORY_1ST = "MAIN_SL_CATEGORY_1ST"
        const val MAIN_SL_CATEGORY_2ND = "MAIN_SL_CATEGORY_2ND"
        const val MAIN_SL_HOME_1ST = "MAIN_SL_HOME_1ST"
        const val MAIN_SL_HOME_2ND = "MAIN_SL_HOME_2ND"
        const val MAIN_SL_SEARCH_1ST = "MAIN_SL_SEARCH_1ST"
        const val MAIN_SL_SEARCH_2ND = "MAIN_SL_SEARCH_2ND"

        //HISTORY
        const val HISTORY_SL_BROWSE_1ST = "HISTORY_SL_BROWSE_1ST"
        const val HISTORY_SL_BROWSE_2ND = "HISTORY_SL_BROWSE_2ND"
        const val HISTORY_SL_CLEAR_FILTER_1ST = "HISTORY_SL_CLEAR_FILTER_1ST"
        const val HISTORY_SL_CLEAR_FILTER_2ND = "HISTORY_SL_CLEAR_FILTER_2ND"
        const val HISTORY_SL_ALL_FILTER_1ST = "HISTORY_SL_ALL_FILTER_1ST"
        const val HISTORY_SL_ALL_FILTER_2ND = "HISTORY_SL_ALL_FILTER_2ND"
        const val HISTORY_SL_HOME_FILTER_1ST = "HISTORY_SL_HOME_FILTER_1ST"
        const val HISTORY_SL_HOME_FILTER_2ND = "HISTORY_SL_HOME_FILTER_2ND"
        const val HISTORY_SL_LOCK_FILTER_1ST = "HISTORY_SL_LOCK_FILTER_1ST"
        const val HISTORY_SL_LOCK_FILTER_2ND = "HISTORY_SL_LOCK_FILTER_2ND"

        //ALL
        const val SELECT_WALLPAPER_1ST = "SELECT_WALLPAPER_1ST"
        const val SELECT_WALLPAPER_2ND = "SELECT_WALLPAPER_2ND"
        const val FAVOURITE_WALLPAPER_1ST = "FAVOURITE_WALLPAPER_1ST"
        const val FAVOURITE_WALLPAPER_2ND = "FAVOURITE_WALLPAPER_2ND"
        const val DOWNLOAD_WALLPAPER_1ST = "DOWNLOAD_WALLPAPER_1ST"
        const val DOWNLOAD_WALLPAPER_2ND = "DOWNLOAD_WALLPAPER_2ND"
        const val SET_WALLPAPER_1ST = "SET_WALLPAPER_1ST"
        const val SET_WALLPAPER_2ND = "SET_WALLPAPER_2ND"
        const val SELECT_CATEGORY_1ST = "SELECT_CATEGORY_1ST"
        const val SELECT_CATEGORY_2ND = "SELECT_CATEGORY_2ND"
    }
}