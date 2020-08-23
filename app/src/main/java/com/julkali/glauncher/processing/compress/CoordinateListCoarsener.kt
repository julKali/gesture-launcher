package com.julkali.glauncher.processing.compress

import com.julkali.glauncher.processing.center
import com.julkali.glauncher.processing.data.Coordinate

class CoordinateListCoarsener(val minCoordCount: Int) {

    tailrec fun coarsen(coords: List<Coordinate>, rest: List<List<Coordinate>> = listOf(coords)): List<List<Coordinate>> {
        val n = coords.size
        if (n < minCoordCount) return rest
        val averages = coords.windowed(2, step = 2, partialWindows = true) {
                if (it.size == 2) it[0] center it[1]
                else it[0]
            }
        return coarsen(averages, listOf(averages) + rest)
    }
}