package com.itsthwng.twallpaper.ui.component.customView

import android.content.Context
import android.util.AttributeSet
import android.view.MotionEvent
import android.widget.FrameLayout
import kotlin.math.abs

class SwipeRefreshLayout @JvmOverloads constructor(
    context: Context,
    attrs: AttributeSet? = null,
    defStyleAttr: Int = 0
) : FrameLayout(context, attrs, defStyleAttr) {

    private var onRefreshListener: (() -> Unit)? = null
    private var touchSlop = 16f
    private var startY = 0f
    private var isDragging = false
    private var refreshThreshold = 140f
    var isRefreshing: Boolean = false
        set(value) {
            field = value
            if (!value) {
                animate().translationY(0f).setDuration(180).start()
            }
        }

    init {
        isClickable = true
        isFocusable = true
    }

    fun setColorSchemeResources(vararg colors: Int) {
        // No-op placeholder for compatibility.
    }

    fun setOnRefreshListener(listener: () -> Unit) {
        onRefreshListener = listener
    }

    override fun onInterceptTouchEvent(ev: MotionEvent): Boolean {
        if (isRefreshing) return true

        when (ev.actionMasked) {
            MotionEvent.ACTION_DOWN -> {
                startY = ev.y
                isDragging = false
            }

            MotionEvent.ACTION_MOVE -> {
                val dy = ev.y - startY
                if (dy > touchSlop && canChildScrollUp().not()) {
                    isDragging = true
                    return true
                }
            }
        }
        return super.onInterceptTouchEvent(ev)
    }

    override fun onTouchEvent(event: MotionEvent): Boolean {
        if (isRefreshing) return true

        when (event.actionMasked) {
            MotionEvent.ACTION_MOVE -> {
                val dy = (event.y - startY).coerceAtLeast(0f)
                if (dy > touchSlop && canChildScrollUp().not()) {
                    isDragging = true
                    translationY = dy / 2f
                    return true
                }
            }

            MotionEvent.ACTION_UP, MotionEvent.ACTION_CANCEL -> {
                if (isDragging) {
                    val dragDistance = abs(translationY)
                    if (dragDistance >= refreshThreshold) {
                        isRefreshing = true
                        onRefreshListener?.invoke()
                    } else {
                        animate().translationY(0f).setDuration(180).start()
                    }
                    isDragging = false
                    return true
                }
            }
        }

        return super.onTouchEvent(event)
    }

    private fun canChildScrollUp(): Boolean {
        val child = getChildAt(0) ?: return false
        return child.canScrollVertically(-1)
    }
}