package com.julkali.glauncher.processing

import com.julkali.glauncher.processing.data.Coordinate
import com.julkali.glauncher.processing.data.Gesture
import kotlin.math.max
import kotlin.math.min

class GestureNormalizer {

    private val IS_POINT_THRESHOLD = 10

    fun normalize(gesture: Gesture): Gesture {
        var totalMinX = Double.POSITIVE_INFINITY
        var totalMinY = Double.POSITIVE_INFINITY
        var allPointersPoints = true
        val pointerCoordsPointsConsolidated = gesture.pointers
            .map {
                var minX = Double.POSITIVE_INFINITY
                var minY = Double.POSITIVE_INFINITY
                var maxX = 0.0
                var maxY = 0.0
                it.coords
                    .forEach { (x, y) ->
                        if (maxX < x) maxX = x
                        if (minX > x) minX = x
                        if (maxY < y) maxY = y
                        if (minY > y) minY = y
                    }
                if (totalMinX > minX) totalMinX = minX
                if (totalMinY > minY) totalMinY = minY
                val widthX = maxX - minX
                val widthY = maxY - minY
                val maxWidth = max(widthX, widthY)
                if (maxWidth <= IS_POINT_THRESHOLD) {
                    val coord = calculateCenter(minX, minY, widthX, widthY)
                    return@map it.id to listOf(coord)
                }
                allPointersPoints = false
                it.id to it.coords
            }
            .toMap()
        if (allPointersPoints) {
            val pointers = gesture.pointers.map {
                val coords = listOf(Coordinate(0.0, 0.0))
                it.id to coords
            }.toMap()
            return Gesture.fromPointerMap(pointers)
        }
        var relativeMax = 0.0
        val relativeCoords = pointerCoordsPointsConsolidated
            .mapValues { (_, coords) ->
                coords.map {
                    val relativeX = it.x - totalMinX
                    val relativeY = it.y - totalMinY
                    if (relativeX > relativeMax) relativeMax = relativeX
                    if (relativeY > relativeMax) relativeMax = relativeY
                    Coordinate(
                        relativeX,
                        relativeY
                    )
                }
            }
        val normalized = relativeCoords
            .mapValues { (_, coords) ->
                coords.map {
                    Coordinate(it.x / relativeMax, it.y / relativeMax)
                }
            }
        return Gesture.fromPointerMap(normalized)
    }

    private fun calculateCenter(
        minX: Double,
        minY: Double,
        widthX: Double,
        widthY: Double
    ): Coordinate {
        val halfWidthX = widthX / 2
        val halfWidthY = widthY / 2
        val x = minX + halfWidthX
        val y = minY + halfWidthY
        return Coordinate(x, y)
    }
}