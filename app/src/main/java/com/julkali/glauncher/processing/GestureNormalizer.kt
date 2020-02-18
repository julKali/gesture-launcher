package com.julkali.glauncher.processing

import com.julkali.glauncher.processing.data.Coordinate
import com.julkali.glauncher.processing.data.Gesture
import kotlin.math.max

class GestureNormalizer {

    fun normalize(gesture: Gesture): Gesture {
        val allCoords = gesture.pointers.map { it.coords }
        val (minX, minY) = getMinCoordValuesOfAll(allCoords)
        val relativeCoords = allCoords.map { list ->
            list.map {
                Coordinate(
                    it.x - minX,
                    it.y - minY
                )
            }
        }
        val (maxX, maxY) = getMaxCoordValuesOfAll(relativeCoords)
        val maxTotal = max(maxX, maxY)
        val normalized = mutableMapOf<Int, MutableList<Coordinate>>()
        for ((pId, coords) in gesture.pointers) {
            normalized[pId] = coords
                .map {
                    Coordinate(
                        (it.x - minX) / maxTotal,
                        (it.y - minY) / maxTotal
                    )
                }
                .toMutableList()
        }
        return Gesture.fromPointerMap(normalized)
    }

    private fun getMinCoordValuesOfAll(lists: List<List<Coordinate>>): Pair<Double, Double> {
        if (lists.isEmpty() || lists[0].isEmpty()) {
            throw Exception("No.")
        }
        var minX = Double.POSITIVE_INFINITY
        var minY = Double.POSITIVE_INFINITY
        for (list in lists) {
            for (el in list) {
                val currX = el.x
                val currY = el.y
                if (currX < minX) minX = currX
                if (currY < minY) minY = currY
            }
        }
        return Pair(minX, minY)
    }

    private fun getMaxCoordValuesOfAll(lists: List<List<Coordinate>>): Pair<Double, Double> {
        if (lists.isEmpty() || lists[0].isEmpty()) {
            throw Exception("No.")
        }
        var maxX = 0.0
        var maxY = 0.0
        for (list in lists) {
            for (el in list) {
                val currX = el.x
                val currY = el.y
                if (maxX < currX) maxX = currX
                if (maxY < currY) maxY = currY
            }
        }
        return Pair(maxX, maxY)
    }
}