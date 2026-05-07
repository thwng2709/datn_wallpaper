package com.itsthwng.twallpaper.notification.localNotification

import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.os.Build
import androidx.core.app.NotificationCompat
import com.itsthwng.twallpaper.R
import com.itsthwng.twallpaper.ui.component.MainActivity
import com.itsthwng.twallpaper.utils.Logger

class NotificationHelper(private val context: Context) {

    companion object {
        private const val NOTIFICATION_CHANNEL_ID = "10002"
    }


    fun createNotification() {

        val intent = Intent(context, MainActivity::class.java).apply {
            flags = Intent.FLAG_ACTIVITY_CLEAR_TOP or Intent.FLAG_ACTIVITY_SINGLE_TOP
        }

        val pendingIntent = PendingIntent.getActivity(
            context,
            0,
            intent,
            PendingIntent.FLAG_IMMUTABLE or PendingIntent.FLAG_UPDATE_CURRENT
        )

        val messages = context.resources.getStringArray(R.array.daily_notifications)
        val randomMessage = messages.random()

        val builder = NotificationCompat.Builder(context, NOTIFICATION_CHANNEL_ID)
            .setSmallIcon(R.drawable.ic_notification)
            .setContentTitle(context.getString(R.string.title_notification_24h))
            .setContentText(randomMessage)
            .setAutoCancel(true)
            .setContentIntent(pendingIntent)
            .setPriority(NotificationCompat.PRIORITY_HIGH)

        val notificationManager = context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager

        // Tạo kênh notification nếu Android >= O
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val channel = NotificationChannel(
                NOTIFICATION_CHANNEL_ID,
                "Photo Daily Notification Channel",
                NotificationManager.IMPORTANCE_HIGH
            )

            // Không cần gọi setSound hoặc enableVibration → để hệ thống tự quyết
            notificationManager.createNotificationChannel(channel)
        }

        Logger.d("NotificationHelper", "Creating notification with message: $randomMessage")
        val currentTime = System.currentTimeMillis().toInt()
        notificationManager.notify(currentTime, builder.build())
    }
}
