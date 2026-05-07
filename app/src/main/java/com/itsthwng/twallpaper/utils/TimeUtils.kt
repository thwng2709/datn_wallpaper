package com.itsthwng.twallpaper.utils

import android.annotation.SuppressLint
import android.util.Log
import com.itsthwng.twallpaper.utils.Constants.DEFAULT_HOUR_INTERVAL
import com.itsthwng.twallpaper.utils.Constants.DEFAULT_MILLISECOND_INTERVAL
import com.itsthwng.twallpaper.utils.Constants.DEFAULT_MINUTE_INTERVAL
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Date
import java.util.Locale

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

    fun calendarToString(
        calendar: Calendar,
        format: String,
        locale: Locale? = Locale.getDefault()
    ): String? {
        val formatter = SimpleDateFormat(format, locale)
        return formatter.format(calendar.time)
    }

    fun stringToCalendar(
        dateString: String,
        format: String,
        locale: Locale? = Locale.getDefault()
    ): Calendar? {
        return try {
            val dateFormat = SimpleDateFormat(format, locale)
            val date = dateFormat.parse(dateString)
            val calendar = Calendar.getInstance()
            if (date != null) {
                calendar.time = date
                calendar
            } else {
                null
            }

        } catch (e: Exception) {
            null
        }
    }

    fun compareCalendar(calendar1: Calendar, calendar2: Calendar): Boolean {
        return calendar1.time.before(calendar2.time)
    }

    fun compareDate(d1: Date, d2: Date): Boolean {
        return d1.before(d2)
    }

    fun formatLongToTime(milliSeconds: Long): String {
        val totalSecond = milliSeconds / DEFAULT_MILLISECOND_INTERVAL
        if (totalSecond < 0) return ""
        val hour = totalSecond / DEFAULT_HOUR_INTERVAL
        val minute = (totalSecond - hour * DEFAULT_HOUR_INTERVAL) / DEFAULT_MINUTE_INTERVAL
        Log.d("TimeUtils", "formatLongToTime: ${if (hour > 0) "$hour:$minute" else "$minute"} == $hour === $minute === $totalSecond")
        return if (hour > 0) "$hour:$minute" else "$minute"
    }
    fun dateToString(date: Date, format: String = Constants.FORMAT_TIME): String {
        val dateFormat = SimpleDateFormat(format, Locale.getDefault())
        return dateFormat.format(date)
    }

    fun convertStringToDate(saveDay: String, format: String = Constants.FORMAT_TIME): Date? {
        if (saveDay.isEmpty()) return null
        val dateFormat = SimpleDateFormat(format, Locale.getDefault())
        val date = dateFormat.parse(saveDay)
        return date
    }

    fun resetTimeToZero(date: Date): String {
        val resetTimeString = dateToString(date, Constants.FORMAT_DATE) + Constants.ZERO_TIME
        return resetTimeString
    }
}

@SuppressLint("SimpleDateFormat")
fun Date.format(format: String): String {
    val formater = SimpleDateFormat(format)
    return formater.format(this)
}