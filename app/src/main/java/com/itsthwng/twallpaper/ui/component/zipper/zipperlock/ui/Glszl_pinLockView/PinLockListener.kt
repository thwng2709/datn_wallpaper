package com.itsthwng.twallpaper.ui.component.zipper.zipperlock.ui.Glszl_pinLockView

interface PinLockListener {
    fun onComplete(str: String?)

    fun onEmpty()

    fun onPinChange(i: Int, str: String?)
}
