package com.julkali.glauncher.processing

import android.util.Log
import com.julkali.glauncher.io.database.AppLaunchEntry
import com.julkali.glauncher.io.database.GestureDBHandler
import com.julkali.glauncher.processing.compress.CoordinateListCoarsener
import com.julkali.glauncher.processing.data.Coordinate
import com.julkali.glauncher.processing.data.Gesture
import com.julkali.glauncher.processing.data.Pointer
import com.julkali.glauncher.processing.score.GestureScoreCalculator
import kotlin.math.ceil
import kotlin.math.min

class ClosestGestureFinder(
    private val dbHandler: GestureDBHandler
) {

    data class CandidateDistance(
        val cand: AppLaunchEntry,
        val coords: List<List<List<Coordinate>>>,
        val dist: Double
        )

    private val TAG = "ClosestGestureFinder"
    private val MIN_SCORE_THRESHOLD = 0.6
    private val COARSEN_MIN_COORD_COUNT = 15

    private val gestureScoreCalculator =
        GestureScoreCalculator()
    private val coordListCoarsener = CoordinateListCoarsener(COARSEN_MIN_COORD_COUNT)

    fun closestGesture(gesture: Gesture): AppLaunchEntry? {
        val gestures = dbHandler.readSavedGestures()
        var candidates = gestures.mapNotNull {
            val pointers = it.gesture.pointers
            if (pointers.size != gesture.pointers.size) null
            else Pair(it, pointers.map { p -> coordListCoarsener.coarsen(p.coords) } )
        }
        val query = gesture.pointers.map { coordListCoarsener.coarsen(it.coords) }
        val pointerCount = gesture.pointers.size
        /*
            TODO: find out when to best stop:
                at last query coarse level?
                or at last candidate coarse level
                    (i.e. candidates.map{it.second.map {it.size}.max()!!}.max()!!)
        */
        val maxCoarseLevel = query.map {it.size}.max()!!
        var best: AppLaunchEntry? = null
        for (coarseLevel in 0 until maxCoarseLevel) {
            val candidateDists = mutableListOf<CandidateDistance>()
            for ((launchEntry, coords) in candidates) {
                var dist = 0.0
                for (pIdx in 0 until pointerCount) {
                    val queryPointer = query[pIdx]
                    val queryCoarseLevel = min(coarseLevel, queryPointer.size - 1)
                    val queryPointerCoarse = queryPointer[queryCoarseLevel]
                    val candPointer = coords[pIdx]
                    val candCoarseLevel = min(coarseLevel, candPointer.size - 1)
                    val candPointerCoarse = candPointer[candCoarseLevel]
                    dist +=
                        gestureScoreCalculator.calculateCoordListScore(queryPointerCoarse, candPointerCoarse)
                }
                candidateDists.add(CandidateDistance(launchEntry, coords, dist / pointerCount))
            }
            val takeCount = ceil(candidateDists.size.toDouble() / 2).toInt()
            val betterHalf = candidateDists.sortedByDescending { it.dist }.take(takeCount)
            if (coarseLevel == maxCoarseLevel - 1) {
                val first = betterHalf.first()
                best = if (first.dist >= MIN_SCORE_THRESHOLD) first.cand else null
            }
            candidates = betterHalf.map { Pair(it.cand, it.coords) }
        }
        return best
    }

    fun isLaunchGestureManagerGesture(gesture: Gesture): Boolean {
        val toCompare = Gesture(
            listOf(
                Pointer(0, listOf(Coordinate(0.0, 1.0))),
                Pointer(1, listOf(Coordinate(0.0, 1.0))),
                Pointer(2, listOf(Coordinate(0.0, 1.0), Coordinate(0.0, 0.0)))
            )
        )
        val score = gestureScoreCalculator.calculate(gesture, toCompare)
        return score >= MIN_SCORE_THRESHOLD

    }
}