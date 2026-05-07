package com.itsthwng.twallpaper.extension

import android.os.SystemClock
import android.view.View

class SafeClickListener(
	private var defaultInterval: Int = 1500,
	private val onSafeCLick: (View) -> Unit
) : View.OnClickListener {
	private var lastTimeClicked: Long = 0
	override fun onClick(v: View) {
		if (SystemClock.elapsedRealtime() - lastTimeClicked < defaultInterval) {
			return
		}
		lastTimeClicked = SystemClock.elapsedRealtime()
		onSafeCLick(v)
	}
}

fun View.setSafeOnClickListener(defaultInterval: Int = 1500, onSafeClick: (View) -> Unit) {
	val safeClickListener = SafeClickListener(defaultInterval) {
		onSafeClick(it)
	}
	setOnClickListener(safeClickListener)
}