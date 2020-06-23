package com.julkali.glauncher.processing.compress

import android.util.Log
import com.julkali.glauncher.processing.data.Coordinate
import com.julkali.glauncher.processing.data.Gesture
import com.julkali.glauncher.processing.data.Pointer

class GestureCompressor(private val compressedSize: Int) {

    private val TAG = "GestureCompressor"

    fun compress(gesture: Gesture): Gesture {
        val pointers = gesture.pointers
            .map {
                val isPoint = it.coords.size == 1
                if (isPoint) return@map it
                val coords = calculateCompressedCoordinates(it)
                Pointer(it.id, coords)
            }
        return Gesture(pointers)
    }

    private fun calculateCompressedCoordinates(pointer: Pointer): List<Coordinate> {
        val (vectors, totalLength) = pointer.toPositionedVectors()
        val pieceLength = totalLength / compressedSize
        val compressedCoords = mutableListOf<Coordinate>()
        var currentVecIndex = 0
        var firstRemainingVecOffset = 0.0
        for (i in 0 until compressedSize) {
            var remaining = pieceLength
            var nextVec = vectors[currentVecIndex]
            var nextVecRemainingLength = nextVec.vector.length - firstRemainingVecOffset
            firstRemainingVecOffset += remaining
            while (nextVecRemainingLength < remaining && currentVecIndex != vectors.lastIndex) {
                remaining -= nextVecRemainingLength
                currentVecIndex++
                nextVec = vectors[currentVecIndex]
                nextVecRemainingLength = nextVec.vector.length
                firstRemainingVecOffset = remaining
            }
            val nextCompressedCoord = nextVec.start.addVectorWithLength(nextVec.vector, remaining)
            compressedCoords.add(nextCompressedCoord)
        }
        return compressedCoords
    }

    private fun Pointer.toPositionedVectors(): Pair<List<PositionedVector>, Double> {
        var length = 0.0
        val vecs = coords.windowed(2)
            .map {
                    (last, curr) ->
                val x = curr.x - last.x
                val y = curr.y - last.y
                val vec = Vector(x, y)
                length += vec.length
                PositionedVector(
                    last,
                    vec
                )
            }
        return Pair(vecs, length)
    }

    private fun Coordinate.addVectorWithLength(vector: Vector, length: Double): Coordinate {
        val rescaledVector = vector.rescaledTo(length)
        return this + rescaledVector
    }

    private fun Vector.rescaledTo(newLength: Double): Vector {
        return normalized * newLength
    }

    private operator fun Vector.times(scalar: Double): Vector {
        return Vector(
            x * scalar,
            y * scalar
        )
    }

    private operator fun Coordinate.plus(vector: Vector): Coordinate {
        return Coordinate(x + vector.x, y + vector.y)
    }
}