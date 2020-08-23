package com.julkali.glauncher.processing

import com.julkali.glauncher.processing.data.Coordinate
import kotlin.math.pow
import kotlin.math.sqrt

infix fun Coordinate.dist(other: Coordinate) = sqrt((other.x - x).pow(2.0) + (other.y - y).pow(2.0))

infix fun Coordinate.center(other: Coordinate)
        = Coordinate(x + (other.x - x)/2, y + (other.y - y)/2)