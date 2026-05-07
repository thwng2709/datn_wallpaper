package com.itsthwng.twallpaper.notification.localNotification

import android.app.AlarmManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.util.Log
import com.itsthwng.twallpaper.R
import java.util.Calendar

object MyAlarmManager {

    fun scheduleRepeatingAlarmAt10AM(context: Context, dayOfWeek: Int, alarmId: Int) {
        val times = context.resources.getStringArray(R.array.notification_times)
        val randomTime = times.random()
        val parts = randomTime.split(":")
        val hour = parts[0].toInt()
        val minute = parts[1].toInt()
        val alarmTime = Calendar.getInstance().apply {
            set(Calendar.DAY_OF_WEEK, dayOfWeek)
            set(Calendar.HOUR_OF_DAY, hour)
            set(Calendar.MINUTE, minute)
            set(Calendar.SECOND, 0)
            set(Calendar.MILLISECOND, 0)

            // Nếu đã quá 10h hôm nay thì đặt cho tuần sau
            if (timeInMillis < System.currentTimeMillis()) {
                add(Calendar.WEEK_OF_YEAR, 1)
            }
        }

        val intent = Intent(context, AlarmReceiver::class.java).apply {
            action = "com.test.intent.action.ALARM"
            putExtra("alarmId", alarmId)
            putExtra("dayOfWeek", dayOfWeek)
        }

        val pendingIntent = PendingIntent.getBroadcast(
            context,
            alarmId,
            intent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )

        val alarmManager = context.getSystemService(Context.ALARM_SERVICE) as AlarmManager
        alarmManager.setRepeating(
            AlarmManager.RTC_WAKEUP,
            alarmTime.timeInMillis,
            AlarmManager.INTERVAL_DAY * 7,
            pendingIntent
        )

        Log.d("SCHEDULED_ALARM", "Repeating alarm set for day $dayOfWeek (ID=$alarmId) at ${alarmTime.time}")
    }

    fun scheduleAlarmsEveryDayAt10AM(context: Context) {
        for (day in Calendar.SUNDAY..Calendar.SATURDAY) {
            val alarmId = 1000 + day
            scheduleRepeatingAlarmAt10AM(context, day, alarmId)
        }
    }
}
