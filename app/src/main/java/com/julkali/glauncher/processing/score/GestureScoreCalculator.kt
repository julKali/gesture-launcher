package com.julkali.glauncher.processing.score

import android.util.Log
import com.julkali.glauncher.processing.data.Coordinate
import com.julkali.glauncher.processing.data.Gesture
import com.julkali.glauncher.processing.data.Pointer
import java.util.*
import kotlin.math.sqrt
import kotlin.math.pow

class GestureScoreCalculator {

    data class CellValue(val value: Double, val stepCount: Int = 1)

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
        if (subject.isPoint() && toCompare.isPoint()) {
            return 1.0 - (subjCoords.single() dist otherCoords.single())
        }
        if (subject.isPoint() != toCompare.isPoint()) {
            return -1.0
        }
        val n = subjCoords.size
        val m = otherCoords.size
        val costs = ArrayDeque<CellValue>(m)
        costs.addLast(CellValue(subjCoords[0] dist otherCoords[0]))
        for (j in 1 until m) {
            costs.addLast(CellValue(subjCoords[0] dist otherCoords[j]) + costs.last)
        }
        for (i in 1 until n) {
            costs.addLast(CellValue((subjCoords[i] dist otherCoords[0])) + costs.first)
            for (j in 1 until m) {
                val dist = subjCoords[i] dist otherCoords[j]
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

    private infix fun Coordinate.dist(other: Coordinate)
            = sqrt((other.x - x).pow(2) + (other.y - y).pow(2))

    private operator fun CellValue.plus(other: CellValue)
            = CellValue(value + other.value, stepCount + other.stepCount)
}