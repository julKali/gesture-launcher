package com.julkali.glauncher.processing.data

import java.io.Serializable

data class Coordinate(val x: Double, val y: Double) : Serializable {
    override fun toString(): String {
        return "($x,$y)"
    }
}