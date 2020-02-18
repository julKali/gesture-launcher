package com.julkali.glauncher.io.database

import android.content.Context
import com.couchbase.lite.*
import com.julkali.glauncher.processing.data.Gesture

class GestureDBSaver(context: Context) {

    private val dbConfig = DatabaseConfiguration(context)

    fun saveGesture(gesture: Gesture) {
        val db = Database("gestures", dbConfig)
        val doc = MutableDocument()
            .setValue("gesture", convertToSafeType(gesture))
        db.save(doc)
    }

    fun convertToSafeType(gesture: Gesture): Map<String, List<Map<String, Double>>> {
        return gesture.pointers.map { (pIdx, coords) ->
            Pair(pIdx.toString(), coords.map {
                mapOf("x" to it.x, "y" to it.y)
            })
        }.toMap()
    }
}