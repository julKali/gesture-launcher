package com.julkali.glauncher.processing.compress

import kotlin.math.pow
import kotlin.math.sqrt

data class Vector(val x: Double, val y: Double) {

    val length by lazy {
        sqrt(x.pow(2) + y.pow(2))
    }

    val normalized by lazy {
        Vector(x / length, y / length)
    }
}