package com.julkali.glauncher.processing.score

import android.util.Log
import com.julkali.glauncher.processing.data.Gesture
import com.julkali.glauncher.processing.data.Pointer
import kotlin.math.abs

class GestureScoreCalculator {

    private val TAG = "GestureScoreCalculator"

    fun calculate(subject: Gesture, toCompare: Gesture): Double {
        if (subject.pointers.size != toCompare.pointers.size) {
            return -1.0 // todo: create score for each compbination of pointer lists for bigger one
        }
        // todo: have to compare every combination of pointers, because they might not have same indices
        val pointerScores = subject.pointers
            .zip(toCompare.pointers)
            .map { (subj, other) ->
                calculatePointerScore(subj, other)
            }
        return pointerScores.average()
    }

    private fun calculatePointerScore(subject: Pointer, toCompare: Pointer): Double {
        val subjCoords = subject.coords
        val otherCoords = toCompare.coords
        val size = subjCoords.size
        if (size != otherCoords.size) {
            Log.e(TAG, "subjCoords.size = ${subjCoords.size}, otherCoords.size = ${otherCoords.size}")
            throw Exception("Coords are not of same length")
        }
        val total = subjCoords
            .zip(otherCoords)
            .map { (subj, other) ->
                abs(other.x - subj.x) + abs(other.y - subj.y)
            }
            .sum()
        val averageDifference = total / size
        return 1.0 - averageDifference
    }
}