package com.itsthwng.twallpaper.notification

import android.content.Context
import androidx.work.WorkManager
import com.google.gson.Gson
import com.google.gson.JsonSyntaxException
import com.google.gson.reflect.TypeToken
import com.itsthwng.twallpaper.notification.model.NotifyData
import com.itsthwng.twallpaper.utils.Logger
import java.io.IOException
import java.time.LocalDateTime


object NotifyHelper {

    fun getAllowNotifyHour(time: LocalDateTime): LocalDateTime {
        return if (time.hour !in 8..22) {
            time.withHour(8)
        } else {
            time
        }
    }

    fun clearAllNotifyWorker(context: Context) {
        WorkManager.getInstance(context).cancelAllWork()
    }

    fun readNotifyFromFile(context: Context, fileResId: Int): List<NotifyData>? {
        return try {
            val jsonString =
                context.resources.openRawResource(fileResId).bufferedReader().use { it.readText() }
            val listNotify = Gson().fromJson<List<NotifyData>>(
                jsonString, (object : TypeToken<List<NotifyData>>() {}).type
            )
            Logger.d("readNotifyFromFile $listNotify")
            listNotify
        } catch (jsonSyntaxException: JsonSyntaxException) {
            null
        } catch (ioException: IOException) {
            null
        }
    }
}