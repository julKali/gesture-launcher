package com.julkali.glauncher.processing

import android.util.Log
import android.view.MotionEvent
import com.julkali.glauncher.processing.data.Coordinate
import com.julkali.glauncher.processing.data.Gesture

class GestureBuilder {

    private val TAG = "GestureBuilder"
    private val currentGesture = mutableMapOf<Int, MutableList<Coordinate>>()
    private var isDone = false

    fun processMotionEvent(ev: MotionEvent) {
        if (isDone) {
            throw Exception("Gesture already built!")
        }
        val pointerCount = ev.pointerCount
        when (ev.action) {
            MotionEvent.ACTION_DOWN -> {
                //Log.d(TAG, "NEW POINTER")
                for (p in 0 until pointerCount) {
                    val pId = ev.getPointerId(p)
                    addToGesture(pId, ev.getX(p).toDouble(), ev.getY(p).toDouble())
                }
            }
            MotionEvent.ACTION_UP -> {
                //Log.d(TAG, "POINTER RELEASED")
                for (p in 0 until pointerCount) {
                    val pId = ev.getPointerId(p)
                    addToGesture(pId, ev.getX(p).toDouble(), ev.getY(p).toDouble())
                }
                isDone = true
            }
            MotionEvent.ACTION_MOVE -> {
                //Log.d(TAG, "POINTER MOVED")
                val historySize = ev.historySize;
                for (h in 0 until historySize) {
                    for (p in 0 until pointerCount) {
                        val pId = ev.getPointerId(p)
                        addToGesture(
                            pId,
                            ev.getHistoricalX(p, h).toDouble(),
                            ev.getHistoricalY(p, h).toDouble()
                        )
                    }
                }
                for (p in 0 until pointerCount) {
                    val pId = ev.getPointerId(p)
                    addToGesture(pId, ev.getX(p).toDouble(), ev.getY(p).toDouble())
                }
            }
            MotionEvent.ACTION_CANCEL -> {
                Log.d(TAG, "GESTURE CANCELLED")
                currentGesture.clear()
            }
        }
    }

    private fun addToGesture(pointerId: Int, xCoord: Double, yCoord: Double) {
        val coord =
            Coordinate(xCoord, yCoord)
        val pointerCoords = currentGesture.getOrPut(pointerId) { mutableListOf() }
        val last = pointerCoords.lastOrNull()
        if (last != null && last == coord) return
        pointerCoords.add(coord)
    }

    fun getIsDone(): Boolean {
        return isDone
    }

    fun clear() {
        currentGesture.clear()
        isDone = false
    }

    fun getGesture(): Gesture {
        return Gesture.fromPointerMap(currentGesture)
    }
}