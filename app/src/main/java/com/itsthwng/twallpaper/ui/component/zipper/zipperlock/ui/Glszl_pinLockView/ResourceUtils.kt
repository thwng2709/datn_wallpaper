package com.itsthwng.twallpaper.ui.component.zipper.zipperlock.ui.Glszl_pinLockView

import android.content.Context
import androidx.core.content.ContextCompat

class ResourceUtils private constructor() {
    init {
        throw AssertionError()
    }

    companion object {
        @JvmStatic
        fun getColor(context: Context, i: Int): Int {
            return ContextCompat.getColor(context, i)
        }

        @JvmStatic
        fun getDimensionInPx(context: Context, i: Int): Float {
            return context.resources.getDimension(i)
        }
    }
}
