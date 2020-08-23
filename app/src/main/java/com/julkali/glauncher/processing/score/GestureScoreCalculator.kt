package com.julkali.glauncher.processing.score

import android.util.Log
import com.julkali.glauncher.processing.data.Coordinate
import com.julkali.glauncher.processing.data.Gesture
import com.julkali.glauncher.processing.data.Pointer
import com.julkali.glauncher.processing.dist
import java.util.*
import kotlin.math.sqrt
import kotlin.math.pow

class GestureScoreCalculator {

    data class CellValue(val value: Double, val stepCount: Int = 1)

    private val TAG = "GestureScoreCalculator"

    fun calculate(subject: Gesture, toCompare: Gesture): Double {
        if (subject.pointers.size != toCompare.pointers.size) {
            return -1.0
        }
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
        return calculateCoordListScore(subjCoords, otherCoords)
    }

    fun calculateCoordListScore(p: List<Coordinate>, q: List<Coordinate>): Double {
        val n = p.size
        val m = q.size
        if (n == 1 && m == 1) {
            return 1.0 - (p.single() dist q.single())
        }
        if ((n == 1) != (m == 1)) {
            return -1.0
        }
        val costs = ArrayDeque<CellValue>(m)
        costs.addLast(CellValue(p[0] dist q[0]))
        for (j in 1 until m) {
            costs.addLast(CellValue(p[0] dist q[j]) + costs.last)
        }
        for (i in 1 until n) {
            costs.addLast(CellValue((p[i] dist q[0])) + costs.first)
            for (j in 1 until m) {
                val dist = p[i] dist q[j]
                val cellVal = CellValue(dist) + listOf(
                    costs.removeFirst(),
                    costs.first(),
                    costs.last()
                ).minBy { it.value }!!
                costs.addLast(cellVal)
            }
            costs.removeFirst()
        }
        val lastCell = costs.last
        return 1.0 - sqrt(lastCell.value / lastCell.stepCount)
    }

    private fun Pointer.isPoint(): Boolean {
        return coords.size == 1
    }

    private operator fun CellValue.plus(other: CellValue)
            = CellValue(value + other.value, stepCount + other.stepCount)
}