package com.julkali.glauncher

import android.graphics.Canvas
import android.graphics.ColorFilter
import android.graphics.Paint
import android.graphics.PixelFormat
import android.graphics.drawable.Drawable
import com.julkali.glauncher.processing.data.Coordinate
import com.julkali.glauncher.processing.data.Gesture

class GestureCanvas(private val gesture: Gesture) : Drawable() {

    private val redPaint = Paint().apply {
        setARGB(255, 255, 0, 0)
        strokeWidth = 40.0F
        strokeCap = Paint.Cap.ROUND
    }

    override fun draw(canvas: Canvas) {
        val width = bounds.width()
        val height = bounds.height()
        canvas.zoomCenter(0.9F)
        gesture.pointers.forEach { (_, coords) ->
            val points = coords.flatMap { it.toFloatXYArray(width, height) }.toFloatArray()
            canvas.drawPoints(points, redPaint)
        }
    }

    private fun Canvas.zoomCenter(factor: Float) {
        scale(factor, factor)
        val inverted = 1 - factor
        val translateX = width * inverted * 0.5
        val translateY = height * inverted * 0.5
        translate(translateX.toFloat(), translateY.toFloat())
    }

    override fun setAlpha(alpha: Int) {

    }

    override fun getOpacity(): Int {
        return PixelFormat.OPAQUE
    }

    override fun setColorFilter(colorFilter: ColorFilter?) {

    }

    private fun Coordinate.toFloatXYArray(width: Int, height: Int): List<Float> {
        val newX = (x * width).toFloat()
        val newY = (y * height).toFloat()
        return listOf(newX, newY)
    }
}