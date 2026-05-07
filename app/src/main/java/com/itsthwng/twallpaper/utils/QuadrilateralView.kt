package com.itsthwng.twallpaper.utils

import android.content.Context
import android.graphics.Canvas
import android.graphics.Color
import android.graphics.LinearGradient
import android.graphics.Paint
import android.graphics.Path
import android.graphics.Shader
import android.view.View
import kotlin.math.atan2
import kotlin.math.cos
import kotlin.math.hypot
import kotlin.math.sin

class QuadrilateralView(
    context: Context,
    private var x1: Float,
    private var y1: Float,
    private var x2: Float,
    private var y2: Float,
    private var width: Int
) : View(context) {

    private val paint = Paint().apply {
        color = Color.BLUE
        strokeWidth = width.toFloat()
        alpha = 50
    }

    private val arrowPaint = Paint().apply {
        color = Color.BLACK
        style = Paint.Style.FILL_AND_STROKE
        strokeWidth = 5f
        isAntiAlias = true
    }

    init {
        updateShader()
    }

    private fun updateShader() {
        val lineLength = hypot((x2 - x1).toDouble(), (y2 - y1).toDouble()).toFloat()
        val ratio = 100f / lineLength
        paint.shader = LinearGradient(
            x1, y1, x2, y2,
            intArrayOf(Color.TRANSPARENT, Color.parseColor("#bbff5a"), Color.parseColor("#bbff5a"), Color.TRANSPARENT),
            floatArrayOf(0f, ratio, 1f - ratio, 1f),
            Shader.TileMode.CLAMP
        )
    }

    private fun drawArrowAtDistance(canvas: Canvas, startX: Float, startY: Float, endX: Float, endY: Float, distance: Float) {
        val arrowSize = 20f
        val deltaX = endX - startX
        val deltaY = endY - startY
        val lineLength = hypot(deltaX.toDouble(), deltaY.toDouble()).toFloat()

        val ratio = distance / lineLength
        val arrowX = startX + deltaX * ratio
        val arrowY = startY + deltaY * ratio

        val angle = atan2(deltaY.toDouble(), deltaX.toDouble())

        val arrowPath = Path().apply {
            moveTo(arrowX, arrowY)
            lineTo(
                (arrowX - arrowSize * cos(angle - Math.PI / 6)).toFloat(),
                (arrowY - arrowSize * sin(angle - Math.PI / 6)).toFloat()
            )
            lineTo(
                (arrowX - arrowSize * cos(angle + Math.PI / 6)).toFloat(),
                (arrowY - arrowSize * sin(angle + Math.PI / 6)).toFloat()
            )
            close()
        }

        canvas.drawPath(arrowPath, arrowPaint)
    }

    private fun calculateDistanceToEnd(): Float {
        val lineLength = Math.hypot((x2 - x1).toDouble(), (y2 - y1).toDouble()).toFloat()
        return lineLength - 50f
    }

    override fun onDraw(canvas: Canvas) {
        super.onDraw(canvas)
        canvas.drawLine(x1, y1, x2, y2, paint)
        drawArrowAtDistance(canvas, x1, y1, x2, y2, 70f)
        drawArrowAtDistance(canvas, x1, y1, x2, y2, calculateDistanceToEnd())
    }

    fun updateLine(x1: Float, y1: Float, x2: Float, y2: Float) {
        this.x1 = x1
        this.y1 = y1
        this.x2 = x2
        this.y2 = y2

        updateShader()

        invalidate()
//        println("invalidate $x1 $y1 $x2 $y2")
    }
}
