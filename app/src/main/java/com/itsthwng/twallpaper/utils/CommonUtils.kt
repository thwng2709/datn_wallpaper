package com.itsthwng.twallpaper.utils

import android.animation.Animator
import android.animation.AnimatorListenerAdapter
import android.animation.AnimatorSet
import android.animation.ObjectAnimator
import android.app.Activity
import android.content.ActivityNotFoundException
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.content.res.Resources
import android.database.Cursor
import android.net.Uri
import android.provider.MediaStore
import android.util.DisplayMetrics
import android.view.View
import android.view.animation.AccelerateDecelerateInterpolator
import androidx.core.content.ContextCompat
import java.util.Calendar
import java.util.Locale
import java.util.Random

object CommonUtil {
    const val GOOGLE_PLAY_URL_APP = "https://play.google.com/store/apps/details?id="
    const val GOOGLE_PLAY_URL_DEVELOPER = "https://play.google.com/store/apps/developer?id="

    fun hasPermissions(vararg permissions: String?, activity: Activity): Boolean {
        for (permission in permissions) {
            if (permission?.let { activity.checkSelfPermission(it) } != PackageManager.PERMISSION_GRANTED) {
                return false
            }
        }
        return true
    }

    fun moreApp(
        activity: Activity,
        publisherName: String,
    ) {
        activity.startActivity(
            Intent(
                Intent.ACTION_VIEW,
                Uri.parse(GOOGLE_PLAY_URL_DEVELOPER + publisherName),
            ),
        )
    }

    fun likeApp(
        activity: Activity,
        packageName: String,
    ) {
        activity.startActivity(
            Intent(
                Intent.ACTION_VIEW,
                Uri.parse(GOOGLE_PLAY_URL_APP + packageName),
            ),
        )
    }

    fun shareMail(
        activity: Activity,
        url: String,
        description: String,
    ) {
        val intent =
            Intent(Intent.ACTION_SEND).apply {
                type = "text/plain"
                putExtra(Intent.EXTRA_EMAIL, arrayOf(Constants.EMAIL))
                data  = Uri.parse(Constants.EMAIL)
                putExtra(Intent.EXTRA_TEXT, url)
                putExtra(Intent.EXTRA_SUBJECT, description)
                type = "message/rfc882"
            }
        activity.startActivity(intent)
    }

    fun share(
        activity: Activity,
        url: String,
    ) {
        val intent =
            Intent(Intent.ACTION_SEND).apply {
                putExtra(Intent.EXTRA_SUBJECT, "Sharing...")
                putExtra(Intent.EXTRA_TEXT, url)
                type = "text/plain"
            }
        activity.startActivity(Intent.createChooser(intent, "Sharing..."))
    }

    fun changeLanguage(
        context: Context,
        languageCode: String,
    ) {
        val res: Resources = context.resources
        val dm: DisplayMetrics = res.displayMetrics
        val conf = res.configuration
        conf.setLocale(Locale(languageCode.lowercase(Locale.getDefault()))) // API 17+ only
        res.updateConfiguration(conf, dm)
    }

    fun openMarket(context: Context, packageName: String? = null) {
        val appPackageName = if (packageName.isNullOrEmpty()) context.packageName else packageName
        try {
            context.startActivity(
                Intent(
                    Intent.ACTION_VIEW,
                    Uri.parse("market://details?id=$appPackageName"),
                ),
            )
        } catch (anfe: ActivityNotFoundException) {
            context.startActivity(
                Intent(
                    Intent.ACTION_VIEW,
                    Uri.parse("https://play.google.com/store/apps/details?id=$appPackageName"),
                ),
            )
        }
    }

    fun sendMail(
        activity: Activity,
        subject: String,
        title: String,
    ) {
        val email =
            Intent(Intent.ACTION_SEND).apply {
                putExtra(Intent.EXTRA_EMAIL, arrayOf(Constants.EMAIL))
                putExtra(Intent.EXTRA_SUBJECT, subject)
                putExtra(Intent.EXTRA_TEXT, "")
                type = "message/rfc822"
            }
        activity.startActivity(Intent.createChooser(email, title))
    }

    private var lastClickTime: Long = 0
    private const val DOUBLE_CLICK_TIME_DELTA: Long = 600

    fun isDoubleClick(): Boolean {
        val clickTime = System.currentTimeMillis()
        if (clickTime - lastClickTime < DOUBLE_CLICK_TIME_DELTA) {
            lastClickTime = clickTime
            return true
        }
        lastClickTime = clickTime
        return false
    }

    fun getRealPathFromURI(
        context: Context,
        contentUri: Uri,
    ): String? {
        var cursor: Cursor? = null
        return try {
            val proj = arrayOf(MediaStore.Images.Media.DATA)
            cursor = context.contentResolver.query(contentUri, proj, null, null, null)
            val column_index = cursor?.getColumnIndexOrThrow(MediaStore.Images.Media.DATA) ?: -1
            cursor?.moveToFirst()
            cursor?.getString(column_index)
        } finally {
            cursor?.close()
        }
    }

    fun changeMillisTo24Hour(millis: Long): Long {
        val calendar = Calendar.getInstance()
        calendar.timeInMillis = millis

        // Move to the next day at 00:00
        calendar.set(Calendar.HOUR_OF_DAY, 23)
        calendar.set(Calendar.MINUTE, 59)
        calendar.set(Calendar.SECOND, 59)
        calendar.set(Calendar.MILLISECOND, 999) // Để đảm bảo đúng cuối ngày

        return calendar.timeInMillis
    }

    fun toStartOfDay(timestamp: Long): Long {
        val cal = Calendar.getInstance().apply {
            timeInMillis = timestamp
            set(Calendar.HOUR_OF_DAY, 0)
            set(Calendar.MINUTE, 0)
            set(Calendar.SECOND, 0)
            set(Calendar.MILLISECOND, 0)
        }
        return cal.timeInMillis
    }

    fun View.startPulseAnimation() {
        val scaleUpX = ObjectAnimator.ofFloat(this, View.SCALE_X, 1f, 1.1f)
        val scaleUpY = ObjectAnimator.ofFloat(this, View.SCALE_Y, 1f, 1.1f)
        val fadeOut = ObjectAnimator.ofFloat(this, View.ALPHA, 1f, 0.5f)

        val scaleDownX = ObjectAnimator.ofFloat(this, View.SCALE_X, 1.1f, 1f)
        val scaleDownY = ObjectAnimator.ofFloat(this, View.SCALE_Y, 1.1f, 1f)
        val fadeIn = ObjectAnimator.ofFloat(this, View.ALPHA, 0.5f, 1f)

        val scaleUp = AnimatorSet().apply {
            playTogether(scaleUpX, scaleUpY, fadeOut)
            duration = 1200
        }

        val scaleDown = AnimatorSet().apply {
            playTogether(scaleDownX, scaleDownY, fadeIn)
            duration = 1200
        }

        val pulse = AnimatorSet().apply {
            playSequentially(scaleUp, scaleDown)
            interpolator = AccelerateDecelerateInterpolator()
        }

        pulse.addListener(object : AnimatorListenerAdapter() {
            override fun onAnimationEnd(animation: Animator) {
                this@startPulseAnimation.startPulseAnimation()
            }
        })

        pulse.start()
    }

    fun getColor(context: Context, i: Int): Int {
        return ContextCompat.getColor(context, i)
    }

    fun getDimensionInPx(context: Context, i: Int): Float {
        return context.getResources().getDimension(i)
    }

    fun shuffle(iArr: IntArray): IntArray {
        val length = iArr.size
        val random = Random()
        random.nextInt()
        for (i in 0..<length) {
            swap(iArr, i, random.nextInt(length - i) + i)
        }
        return iArr
    }

    private fun swap(iArr: IntArray, i: Int, i2: Int) {
        val i3 = iArr[i]
        iArr[i] = iArr[i2]
        iArr[i2] = i3
    }

}
