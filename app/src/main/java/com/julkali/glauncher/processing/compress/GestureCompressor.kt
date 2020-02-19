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
                val vectors = it.toPositionedVectors()
                val totalLength = vectors.map { it.vector.length }.sum()
                val pieceLength = totalLength / compressedSize
                val coords = (0 until compressedSize).map { i ->
                    val offset = pieceLength * i
                    findCoordinateAtOffset(vectors, offset)
                }
                Pointer(it.id, coords)
            }
        return Gesture(pointers)
    }

    private fun findCoordinateAtOffset(vectors: List<PositionedVector>, offset: Double): Coordinate {
        var remaining = offset
        for (vector in vectors) {
            val length = vector.vector.length
            if (length < remaining) {
                remaining -= length
                continue
            }
            return vector.start.addVectorWithLength(vector.vector, remaining)
        }
        Log.e(TAG, vectors.joinToString { "${it.start} - ${it.vector}" })
        throw Exception("Can't find coordinate for $offset, remaining: $remaining")
    }

    private fun Pointer.toPositionedVectors(): List<PositionedVector> {
        return coords.windowed(2)
            .map {
                (last, curr) ->
                val x = curr.x - last.x
                val y = curr.y - last.y
                val vec = Vector(x, y)
                PositionedVector(
                    last,
                    vec
                )
            }
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