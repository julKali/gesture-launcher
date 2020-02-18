package com.julkali.glauncher.processing.data

data class Coordinate(val x: Double, val y: Double) {
    override fun toString(): String {
        return "($x,$y)"
    }
}