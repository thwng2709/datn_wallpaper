package com.itsthwng.twallpaper.data

import android.util.Log
import com.google.firebase.remoteconfig.FirebaseRemoteConfig
import com.google.firebase.remoteconfig.remoteConfigSettings

object CommonInfo {
    val remoteConfig = FirebaseRemoteConfig.getInstance()
    val versionAppReview = 0
    val currentData_Vi: String
        get() = FirebaseRemoteConfig.getInstance().getString("current_data_vi")
    val currentData_En: String
        get() = FirebaseRemoteConfig.getInstance().getString("current_data_en")
    val currentData_Fr: String
        get() = FirebaseRemoteConfig.getInstance().getString("current_data_fr")
    val currentData_Es: String
        get() = FirebaseRemoteConfig.getInstance().getString("current_data_es")
    val currentData_De: String
        get() = FirebaseRemoteConfig.getInstance().getString("current_data_de")
    val currentData_Ar: String
        get() = FirebaseRemoteConfig.getInstance().getString("current_data_ar")
    val currentData_Ru: String
        get() = FirebaseRemoteConfig.getInstance().getString("current_data_ru")
    val currentData_Zh: String
        get() = FirebaseRemoteConfig.getInstance().getString("current_data_zh")
    val currentData_Ja: String
        get() = FirebaseRemoteConfig.getInstance().getString("current_data_ja")
    val currentData_Ko: String
        get() = FirebaseRemoteConfig.getInstance().getString("current_data_ko")
    val currentData_Pt: String
        get() = FirebaseRemoteConfig.getInstance().getString("current_data_pt")
    val currentData_Tr: String
        get() = FirebaseRemoteConfig.getInstance().getString("current_data_tr")
    val currentData_Uk: String
        get() = FirebaseRemoteConfig.getInstance().getString("current_data_uk")

    val currentVersionApp: Long
        get() = FirebaseRemoteConfig.getInstance().getLong("current_version_app")
    val currentVersionData: Long
        get() = FirebaseRemoteConfig.getInstance().getLong("current_version_data")
    //0: True, 1: False
    val isForceUpdate: Boolean
        get() = FirebaseRemoteConfig.getInstance().getBoolean("is_force_update")
    val show_on_boarding_new_day: Boolean
        get() = FirebaseRemoteConfig.getInstance().getBoolean("show_on_boarding_new_day")
    val show_on_boarding_kill_app: Boolean
        get() = FirebaseRemoteConfig.getInstance().getBoolean("show_on_boarding_kill_app")

    val categoryOrderJson: String
        get() = FirebaseRemoteConfig.getInstance().getString("category_order_json")
    val show_live_categories: Boolean
        get() = FirebaseRemoteConfig.getInstance().getBoolean("show_live_categories")
    val version_code_an_qc: String
        get() = ""
    val is_show_premium: Boolean
        get() = FirebaseRemoteConfig.getInstance().getBoolean("is_show_premium")

    val current_version_data_cloudflare_R2: Long
        get() = FirebaseRemoteConfig.getInstance().getLong("current_version_data_cloudflare_r2")

    val priceNormal: Long
        get() = FirebaseRemoteConfig.getInstance().getLong("price_normal")
    val priceMedium: Long
        get() = FirebaseRemoteConfig.getInstance().getLong("price_medium")
    val priceHigh: Long
        get() = FirebaseRemoteConfig.getInstance().getLong("price_high")
    fun initDefaults() {
        val configSettings = remoteConfigSettings {
            setMinimumFetchIntervalInSeconds(if (com.google.firebase.remoteconfig.BuildConfig.DEBUG) 0L else 0L)
        }

        remoteConfig.setConfigSettingsAsync(configSettings)
        remoteConfig.setDefaultsAsync(
            mapOf(
                "current_version_app" to 1L,
                "current_version_data" to 0L,
                "current_version_data_cloudflare_r2" to 0L,
                "is_force_update" to false,
                "duration_show_noti_daily" to 1L,
                "current_data_vi" to "",
                "current_data_en" to "",
                "current_data_fr" to "",
                "current_data_es" to "",
                "current_data_de" to "",
                "current_data_ar" to "",
                "current_data_ru" to "",
                "current_data_zh" to "",
                "current_data_ja" to "",
                "current_data_ko" to "",
                "current_data_pt" to "",
                "current_data_tr" to "",
                "current_data_uk" to "",
                "show_on_boarding_new_day" to false,
                "show_on_boarding_kill_app" to false,
                "category_order_json" to "",
                "show_live_categories" to true,
                "is_show_premium" to true,
                "price_normal" to 50L,
                "price_medium" to 100L,
                "price_high" to 200L,
                "restore_coins" to ""
            )
        )
    }

    fun fetchRemoteConfig(onComplete: (Boolean) -> Unit = {}) {
        remoteConfig.fetchAndActivate()
            .addOnCompleteListener { task ->
                if (task.isSuccessful) {
                    Log.d("RemoteConfig", "Fetch & Activate thành công")
                    onComplete(true)
                } else {
                    Log.e("RemoteConfig", "Fetch thất bại", task.exception)
                    onComplete(false)
                }
            }
    }
}