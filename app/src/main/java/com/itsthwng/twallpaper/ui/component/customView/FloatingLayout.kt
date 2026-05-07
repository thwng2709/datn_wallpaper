package com.itsthwng.twallpaper.ui.component.customView

import android.annotation.SuppressLint
import android.content.Context
import android.util.AttributeSet
import android.view.MotionEvent
import android.view.View
import android.widget.LinearLayout

class FloatingLayout(context: Context, attrs: AttributeSet? = null) : LinearLayout(context, attrs) {
    private var initialX = 0f
    private var initialY = 0f
    private var dx = 0f
    private var dy = 0f

    var saveCurrentLeftPosition: ((x: Int, y: Int) -> Unit)? = null
    var saveCurrentRightPosition: ((x: Int, y: Int) -> Unit)? = null

    @SuppressLint("ClickableViewAccessibility")
    override fun onTouchEvent(event: MotionEvent): Boolean {
        when (event.action) {
            MotionEvent.ACTION_DOWN -> {
                initialX = event.rawX
                initialY = event.rawY
                return true
            }

            MotionEvent.ACTION_MOVE -> {
                dx = event.rawX - initialX
                dy = event.rawY - initialY
                updateLayoutPosition(x.toInt() + dx.toInt(), y.toInt() + dy.toInt())
                initialX = event.rawX
                initialY = event.rawY
                return true
            }

            MotionEvent.ACTION_UP -> {
                initialX = 0F
                initialY = 0F
                return true
            }
        }
        return false
    }

    private fun updateLayoutPosition(x: Int, y: Int) {
        // Ensure the layout stays within fragment boundaries
        val maxX = (parent as? View)?.width ?: 0
        val maxY = ((parent as? View)?.height ?: 0)
        val newX = x.coerceIn(0, maxX - width)
        val newY = y.coerceIn(0, maxY - height)
        layout(newX, newY, newX + width, newY + height)
        saveCurrentRightPosition?.invoke((maxX - newX - width), newY)
        saveCurrentLeftPosition?.invoke(newX, newY)
    }
}
