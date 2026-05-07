package com.itsthwng.twallpaper.utils

import android.content.Context
import android.graphics.Canvas
import android.graphics.Color
import android.graphics.Paint
import android.view.View

class RectangleView(
    context: Context,
    private val left: Float,
    private val top: Float,
    private val right: Float,
    private val bottom: Float
) : View(context) {

    private val paint = Paint().apply {
        color = Color.RED // Màu của hình chữ nhật
        style = Paint.Style.STROKE // Chỉ vẽ viền, bạn có thể đổi thành FILL để tô kín hình
        strokeWidth = 5f // Độ dày của viền hình chữ nhật
    }

    override fun onDraw(canvas: Canvas) {
        super.onDraw(canvas)
        // Vẽ hình chữ nhật với tọa độ đã cho
        canvas.drawRect(left, top, right, bottom, paint)
    }
}