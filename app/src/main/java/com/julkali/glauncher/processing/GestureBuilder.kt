package com.julkali.glauncher.processing

import android.annotation.SuppressLint
import android.util.Log
import android.view.MotionEvent
import com.julkali.glauncher.processing.data.Coordinate
import com.julkali.glauncher.processing.data.Gesture
import io.reactivex.BackpressureStrategy
import io.reactivex.Flowable
import io.reactivex.subjects.PublishSubject
import java.util.*

class GestureBuilder {

    private val TAG = "GestureBuilder"
    private val WAIT_FOR_NEXT_DOWN_MILLISECONDS: Long = 250

    private val currentGesture = mutableMapOf<Int, MutableList<Coordinate>>()
    private val internalToExternalPointerIds = mutableMapOf<Int, Int>()
    private val timer = Timer()
    private var timerTask: TimerTask? = null
    private val gesturesBuilt = PublishSubject.create<Gesture>()

    @SuppressLint("CheckResult")
    fun processMotionEvents(motionEvents: Flowable<MotionEvent>) {
        motionEvents
            .forEach { ev ->
                val pointerCount = ev.pointerCount
                // todo: use ev.getActionMasked(), see https://developer.android.com/training/gestures/multi
                when (ev.action) {
                    MotionEvent.ACTION_DOWN -> {
                        timerTask?.cancel()
                        for (p in 0 until pointerCount) {
                            val pId = ev.getPointerId(p)
                            val externalId = getNextPointerId()
                            internalToExternalPointerIds[pId] = externalId
                            addToGesture(externalId, ev.getX(p).toDouble(), ev.getY(p).toDouble())
                        }
                    }
                    MotionEvent.ACTION_UP -> {
                        for (p in 0 until pointerCount) {
                            val pId = ev.getPointerId(p)
                            val externalId = internalToExternalPointerIds[pId]
                                ?: throw Exception("Pointer ID $pId not found")
                            addToGesture(externalId, ev.getX(p).toDouble(), ev.getY(p).toDouble())
                            internalToExternalPointerIds.remove(pId)
                        }
                        timerTask = object : TimerTask() {
                            override fun run() {
                                val gesture = Gesture.fromPointerMap(currentGesture)
                                gesturesBuilt.onNext(gesture)
                                clear()
                            }
                        }
                        timer.schedule(timerTask, WAIT_FOR_NEXT_DOWN_MILLISECONDS)
                    }
                    MotionEvent.ACTION_MOVE -> {
                        val historySize = ev.historySize;
                        for (h in 0 until historySize) {
                            for (p in 0 until pointerCount) {
                                val pId = ev.getPointerId(p)
                                val externalId = internalToExternalPointerIds[pId]
                                    ?: throw Exception("Pointer ID $pId not found")
                                addToGesture(
                                    externalId,
                                    ev.getHistoricalX(p, h).toDouble(),
                                    ev.getHistoricalY(p, h).toDouble()
                                )
                            }
                        }
                        for (p in 0 until pointerCount) {
                            val pId = ev.getPointerId(p)
                            val externalId = internalToExternalPointerIds[pId]
                                ?: throw Exception("Pointer ID $pId not found")
                            addToGesture(externalId, ev.getX(p).toDouble(), ev.getY(p).toDouble())
                        }
                    }
                    MotionEvent.ACTION_CANCEL -> {
                        Log.d(TAG, "GESTURE CANCELLED")
                        clear()
                    }
                }
            }
    }

    private fun getNextPointerId(): Int {
        return currentGesture.size
    }

    private fun addToGesture(pointerId: Int, xCoord: Double, yCoord: Double) {
        val coord =
            Coordinate(xCoord, yCoord)
        val pointerCoords = currentGesture.getOrPut(pointerId) { mutableListOf() }
        val last = pointerCoords.lastOrNull()
        if (last != null && last == coord) return
        pointerCoords.add(coord)
    }

    private fun clear() {
        timerTask?.cancel()
        timerTask = null
        currentGesture.clear()
        internalToExternalPointerIds.clear()
    }

    public fun getGesturesObserver(): Flowable<Gesture> {
        return gesturesBuilt.toFlowable(BackpressureStrategy.ERROR)
    }
}