package com.julkali.glauncher.io.database

import android.content.Context
import com.couchbase.lite.*
import com.julkali.glauncher.processing.data.Coordinate
import com.julkali.glauncher.processing.data.Gesture
import com.julkali.glauncher.processing.data.Pointer

class GestureDBHandler(context: Context) {

    val dbConfig = DatabaseConfiguration(context)
    val db = Database("gestures", dbConfig)


    fun readSavedGestures(): List<AppLaunchEntry> {
        val query = QueryBuilder
            .select(SelectResult.all())
            .from(DataSource.database(db))
        val results = query.execute().allResults()
        return results.map {
            val gestureDoc = it.getDictionary("gestures")
            val id = gestureDoc.getString("id")
            val packageName = gestureDoc.getString("packageName")
            val intentAction = gestureDoc.getString("intentAction")
            val gesture = Gesture.fromDictionary(gestureDoc.getDictionary("gesture"))
            AppLaunchEntry(id, packageName, intentAction, gesture)
        }
    }

    fun saveAppLaunchEntry(packageName: String, intentAction: String, gesture: Gesture): AppLaunchEntry {
        val doc = MutableDocument()
            .setString("packageName", packageName)
            .setString("intentAction", intentAction)
            .setValue("gesture", convertToSafeType(gesture))
        doc.setString("id", doc.id)
        db.save(doc)
        val id = doc.id!!
        return AppLaunchEntry(id, packageName, intentAction, gesture)
    }

    private fun convertToSafeType(gesture: Gesture): Map<String, List<Map<String, Double>>> {
        return gesture.pointers.map { (pIdx, coords) ->
            Pair(
                pIdx.toString(),
                coords.map {
                    mapOf("x" to it.x, "y" to it.y)
                }
            )
        }.toMap()
    }

    private fun Gesture.Companion.fromDictionary(dictionary: Dictionary): Gesture {
        val pointers = dictionary.keys.map { pointerId ->
            val coordinateData = dictionary.getArray(pointerId) ?: throw Exception("bad data in db")
            val coords = coordinateData.map {
                if (it !is Dictionary) throw Exception("bad data in db")
                Coordinate(it.getDouble("x"), it.getDouble("y"))
            }
            Pointer(pointerId.toInt(), coords)
        }
        return Gesture(pointers)
    }
}