package com.itsthwng.twallpaper.notification

import android.annotation.SuppressLint
import java.text.SimpleDateFormat
import java.util.*

object TimeUtils {
    fun formatTime(milliSeconds: Long, format: String, locale: Locale? = Locale.getDefault()): String? {
        val formatter = SimpleDateFormat(format, locale)
        val calendar = Calendar.getInstance()
        calendar.timeInMillis = milliSeconds
        return formatter.format(calendar.time)
    }

    fun formatTime(date: Date, format: String, locale: Locale? = Locale.getDefault()): String? {
        val formatter = SimpleDateFormat(format, locale)
        val calendar = Calendar.getInstance()
        calendar.time = date
        return formatter.format(calendar.time)
    }

}

@SuppressLint("SimpleDateFormat")
fun Date.format(format: String): String {
    val formater = SimpleDateFormat(format)
    return formater.format(this)
}