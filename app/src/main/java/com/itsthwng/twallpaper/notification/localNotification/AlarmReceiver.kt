package com.itsthwng.twallpaper.notification.localNotification

import android.app.ActivityManager
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import com.itsthwng.twallpaper.utils.Logger

class AlarmReceiver: BroadcastReceiver() {
    override fun onReceive(context: Context, intent: Intent?) {
        val action = intent!!.getAction()
        Logger.d("AlarmReceiver", "Alarm received: " + action)

        WakeLocker.acquire(context)

        if (Intent.ACTION_BOOT_COMPLETED.equals(action, ignoreCase = true)) {
            MyAlarmManager.scheduleAlarmsEveryDayAt10AM(context)
        } else if ("com.test.intent.action.ALARM".equals(action, ignoreCase = true)) {
            if(!isAppInForeground(context)){
                Logger.d("NotificationHelper", "receive alarm intent: " + action)
                val notificationHelper = NotificationHelper(context)
                notificationHelper.createNotification()
            } else {
                Logger.d("AlarmReceiver", "App is in foreground, not showing notification")
            }
        }

        WakeLocker.release()
    }

    fun isAppInForeground(context: Context): Boolean {
        val activityManager = context.getSystemService(Context.ACTIVITY_SERVICE) as ActivityManager
        val appProcesses = activityManager.runningAppProcesses ?: return false

        val packageName = context.packageName
        for (appProcess in appProcesses) {
            if (appProcess.importance == ActivityManager.RunningAppProcessInfo.IMPORTANCE_FOREGROUND &&
                appProcess.processName == packageName) {
                return true
            }
        }
        return false
    }
}