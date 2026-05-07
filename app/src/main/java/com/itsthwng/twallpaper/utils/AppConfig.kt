package com.itsthwng.twallpaper.utils

import android.app.Activity
import android.content.Context
import android.content.Intent
import android.content.res.Configuration
import android.content.res.Resources
import android.net.ConnectivityManager
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.os.LocaleList
import android.os.PowerManager
import android.os.VibrationEffect
import android.os.Vibrator
import android.util.DisplayMetrics
import android.util.TypedValue
import android.widget.Toast
import androidx.core.app.ShareCompat
import com.google.firebase.Firebase
import com.itsthwng.twallpaper.App
import com.itsthwng.twallpaper.BuildConfig
import com.itsthwng.twallpaper.data.CommonInfo
import com.itsthwng.twallpaper.local.LocalData
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Locale
import java.util.concurrent.TimeUnit
import androidx.core.net.toUri

object AppConfig {
    lateinit var connectivityManager: ConnectivityManager
    var displayMetrics: DisplayMetrics? = null
    private lateinit var configuration: Configuration
    private const val ONE_HOUR = 60 * 60 * 1000
    private var checkedConfig = ""

    fun setCheckedConfig(config: String) {
        val listVersion = config.replace(",,", ",").split(",")
        val versions = listVersion.joinToString(","){ it.trim() }
        checkedConfig = ",$versions,"
    }

    fun setup(context: Context) {
        connectivityManager =
            context.getSystemService(Context.CONNECTIVITY_SERVICE) as ConnectivityManager
        displayMetrics = getScreen(context)
        configuration = context.resources.configuration
    }

    val widthScreen: Int
        get() {
            if (!::configuration.isInitialized) {
                throw IllegalStateException("Configuration has not been initialized")
            }
            return dpToPx(configuration.screenWidthDp.toFloat())
        }

    val heightScreen: Int
        get() {
            if (!::configuration.isInitialized) {
                throw IllegalStateException("Configuration has not been initialized")
            }
            return dpToPx(configuration.screenHeightDp.toFloat())
        }

    fun getFirebaseRemoteKey(): String {
        return "configs_" + App.instance.packageName.replace(".", "_")
    }

    fun isAppReview() : Boolean {
        return CommonInfo.versionAppReview == BuildConfig.VERSION_CODE
    }
    private fun getScreen(context: Context): DisplayMetrics {
        // val windowManager = context.getSystemService(Context.WINDOW_SERVICE) as WindowManager
        // val dm = DisplayMetrics()
        // windowManager.defaultDisplay.getRealMetrics(dm)
        // return dm
        return context.resources.displayMetrics
    }

    fun dpToPx(dp: Float): Int {
        try {
            displayMetrics.let {
                return TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, dp, it).toInt()
            }
        } catch (ex : Exception) {
            return dp.toInt()
        }
    }

    fun dp2Px(dp: Float): Float =
        TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, dp, displayMetrics)

    fun spToPx(sp: Float): Float =
        TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_SP, sp, displayMetrics)

    fun Context.dp2px(dpValue: Float): Int {
        val scale = resources.displayMetrics.density
        return (dpValue * scale + 0.5f).toInt()
    }

    fun formatDate(milliSeconds: Long): String? {
        // Create a DateFormatter object for displaying date in specified format.
        val formatter = SimpleDateFormat("dd/MM/yyyy HH:mm:ss")

        // Create a calendar object that will convert the date and time value in milliseconds to date.
        val calendar: Calendar = Calendar.getInstance()
        calendar.timeInMillis = milliSeconds
        return formatter.format(calendar.time)
    }
    fun formatDate1(milliSeconds: Long): String? {
        // Create a DateFormatter object for displaying date in specified format.
        val formatter = SimpleDateFormat("dd_MM_yyyy_HH_mm_ss")

        // Create a calendar object that will convert the date and time value in milliseconds to date.
        val calendar: Calendar = Calendar.getInstance()
        calendar.timeInMillis = milliSeconds
        return formatter.format(calendar.time)
    }

    fun isToday(lastDate: Long): Boolean {
        val now = Calendar.getInstance()
        //now.add(Calendar.DATE,-1);
        val checkDate = Calendar.getInstance()
        checkDate.timeInMillis = lastDate
        Logger.d("isToDay >>> Now: ${now[Calendar.DATE]}/${now[Calendar.MONTH]}/${now[Calendar.YEAR]} checkDate: ${checkDate[Calendar.DATE]}/${checkDate[Calendar.MONTH]}/${checkDate[Calendar.YEAR]}")
        return now[Calendar.YEAR] == checkDate[Calendar.YEAR] && now[Calendar.MONTH] == checkDate[Calendar.MONTH] && now[Calendar.DATE] == checkDate[Calendar.DATE]
    }

    fun isOverOneHour(lastDate: Long) : Boolean {
        if ((System.currentTimeMillis() - lastDate) > ONE_HOUR) {
            Logger.d("CheckRewarded Over 1 hour")
            return true
        }
        return false
    }

    var vibrator: Vibrator? = null
    fun vibrate(context: Context) {
        CoroutineScope(Dispatchers.IO).launch {
            if (vibrator == null) {
                vibrator = context.getSystemService(Context.VIBRATOR_SERVICE) as Vibrator
            }
            if (vibrator == null) return@launch
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                withContext(Dispatchers.Main) {
                    vibrator?.vibrate(VibrationEffect.createOneShot(50, VibrationEffect.DEFAULT_AMPLITUDE))
                }
            } else {
                withContext(Dispatchers.Main) {
                    //deprecated in API 26
                    vibrator?.vibrate(50)
                }
            }
        }
    }

    var lastClickTime: Long = 0
    const val DOUBLE_CLICK_TIME_DELTA: Long = 600
    fun isDoubleClick(): Boolean {
        val clickTime = System.currentTimeMillis()
        if (clickTime - lastClickTime < DOUBLE_CLICK_TIME_DELTA) {
            return true
        }
        lastClickTime = clickTime
        return false
    }

    fun logEventTracking(nameEvent : String, bundle: Bundle? = Bundle()) {
        try {
            bundle?.putString(Constants.KEY_ANALYTICS_TRACKING, Constants.VALUE_ANALYTICS_TRACKING)
            bundle?.clear()
        } catch (e : Exception) {
            Logger.e(e.message)
        }
    }

    fun updateLanguage(language: String, resources : Resources) {
        if (language.isNotBlank()) {
            val locale: Locale
            if (language.contains("-")) {
                val splitLanguage = language.split("-")
                locale = Locale(splitLanguage[0], splitLanguage[1])
            } else {
                locale = Locale(language)
            }
            Locale.setDefault(locale)
            val config = Configuration(resources.configuration)
            config.setLocale(locale)
            resources.updateConfiguration(config, resources.displayMetrics)
        }
    }

    fun updateResources(
        context: Context?, language: String,
    ): Context? {
        val contextFormatted: Context?
        val locale: Locale
        if (language.contains("-")) {
            val splitLanguage = language.split("-")
            locale = Locale(splitLanguage[0], splitLanguage[1])
        } else {
            locale = Locale(language)
        }
        Locale.setDefault(locale)
        val res = context?.resources
        val config = Configuration(res?.configuration)
        config.setLocale(locale)
        contextFormatted = context?.createConfigurationContext(config)
        return contextFormatted
    }

    fun updateResourcesLocale(context: Context, languageCode: String): Context? {
        val locale = Locale(languageCode)
        Locale.setDefault(locale)

        val resources = context.resources
        val configuration = resources.configuration

        // Set layout direction based on language (for RTL support)
        configuration.setLayoutDirection(locale)

        // For Android N and above
        val localeList = LocaleList(locale)
        LocaleList.setDefault(localeList)
        configuration.setLocales(localeList)
        return context.createConfigurationContext(configuration)
    }

    fun openApp(context : Context) {
        try {
            val intent = Intent(
                Intent.ACTION_VIEW,
                (Constants.GOOGLE_PLAY_URL_APP + BuildConfig.APPLICATION_ID).toUri()
            )
            intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
            context.startActivity(intent)
        } catch (e: Exception) {
            e.printStackTrace()
            Logger.e(e.message)
        }
    }

    fun sendMail(context: Context, subject: String?, title: String?) {
        val email = Intent(Intent.ACTION_SEND)
        email.putExtra(Intent.EXTRA_EMAIL, arrayOf<String>(Constants.EMAIL))
        email.putExtra(Intent.EXTRA_SUBJECT, subject)
        email.putExtra(Intent.EXTRA_TEXT, "")
        email.type = "message/rfc822"
        context.startActivity(Intent.createChooser(email, title))
    }

    fun shareApp(activity : Activity) {
        ShareCompat.IntentBuilder.from(activity)
            .setType("text/plain")
            .setText("http://play.google.com/store/apps/details?id=" + activity.packageName)
            .startChooser()
    }

    fun isIgnoringBatteryOptimizations(context: Context): Boolean {
        val pwrm = context.applicationContext.getSystemService(Context.POWER_SERVICE) as PowerManager
        val name = context.applicationContext.packageName
        return pwrm.isIgnoringBatteryOptimizations(name)
    }

    fun getDaysBetween(startMillis: Long, endMillis: Long): Long {
        val diffInMillis = endMillis - startMillis
        return TimeUnit.MILLISECONDS.toDays(diffInMillis)
    }

    fun getLangCode(context: Context): String {
        try {
            val sharedPreferences = context.getSharedPreferences("sharedPreferences", Context.MODE_PRIVATE)
            return sharedPreferences.getString(Constants.PreferencesKey.LANG_CODE, null) ?: ""
        } catch (e : Exception){
            Logger.e("Error getting language: ${e.message}")
            return ""
        }
    }

    fun shouldShowToday(context: Context): Boolean {
        val localStorage = LocalData(context, "sharedPreferences")
        if(!CommonInfo.show_on_boarding_new_day || localStorage.lastOpenDate.isEmpty()) return false
        val today = java.time.LocalDate.now()
        val saved = localStorage.lastOpenDate
        return saved.let { java.time.LocalDate.parse(it) != today }
    }
}