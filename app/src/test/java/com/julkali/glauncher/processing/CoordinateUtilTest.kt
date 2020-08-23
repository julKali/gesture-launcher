package com.julkali.glauncher.processing

import com.julkali.glauncher.processing.data.Coordinate
import org.junit.Test
import org.junit.Assert.*

/**
 * Example local unit test, which will execute on the development machine (host).
 *
 * See [testing documentation](http://d.android.com/tools/testing).
 */
class CoordinateUtilTest {

    @Test
    fun dist() {
        val res = Coordinate(2.0, 4.0) dist Coordinate(2.0, 2.0)
        assertEquals(2.0, res, 0.0)
    }

    @Test
    fun center() {
        val res = Coordinate(0.0, 0.0) center Coordinate(2.0, 5.0)
        assertEquals(1.0, res.x, 0.0)
        assertEquals(2.5, res.y, 0.0)
    }
}
