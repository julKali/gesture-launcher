package com.julkali.glauncher.processing

import android.util.Log
import com.julkali.glauncher.io.database.AppLaunchEntry
import com.julkali.glauncher.io.database.GestureDBHandler
import com.julkali.glauncher.processing.compress.GestureCompressor
import com.julkali.glauncher.processing.data.Coordinate
import com.julkali.glauncher.processing.data.Gesture
import com.julkali.glauncher.processing.data.Pointer
import com.julkali.glauncher.processing.score.GestureScoreCalculator

class ClosestGestureFinder(
    private val dbHandler: GestureDBHandler
) {

    private val TAG = "ClosestGestureFinder"
    private val MIN_SCORE_THRESHOLD = 0.7
    private val COMPRESSED_SIZE = 100

    private val gestureScoreCalculator =
        GestureScoreCalculator()
    private val compressor = GestureCompressor(
        COMPRESSED_SIZE
    )

    fun closestGesture(gesture: Gesture): AppLaunchEntry? {
        val gestures = dbHandler.readSavedGestures()
        val scores = mutableMapOf<AppLaunchEntry, Double>()
        val gestureCompressed = compressor.compress(gesture)
        for (toCompareDoc in gestures) {
            val toCompare = toCompareDoc.gesture
            val toCompareCompressed = compressor.compress(toCompare)
            val score = gestureScoreCalculator.calculate(gestureCompressed, toCompareCompressed)
            scores[toCompareDoc] = score
            Log.d(TAG, score.toString())
        }
        val max = scores.maxBy { it.value }
        Log.d(TAG, max?.value.toString())
        if (max != null && MIN_SCORE_THRESHOLD <= max.value) return max.key
        return null
    }

    fun isLaunchGestureManagerGesture(gesture: Gesture): Boolean {
        val toCompare = Gesture(
            listOf(
                Pointer(0, listOf(Coordinate(0.0, 1.0))),
                Pointer(1, listOf(Coordinate(0.0, 1.0))),
                Pointer(2, listOf(Coordinate(0.0, 1.0), Coordinate(0.0, 0.0)))
            )
        )
        val gestureCompressed = compressor.compress(gesture)
        val toCompareCompressed = compressor.compress(toCompare)
        val score = gestureScoreCalculator.calculate(gestureCompressed, toCompareCompressed)
        return score >= MIN_SCORE_THRESHOLD

    }
}