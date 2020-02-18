package com.julkali.glauncher.io.database

import android.content.Context
import com.couchbase.lite.*
import com.julkali.glauncher.processing.data.Gesture

class GestureDBReader(private val context: Context) {

    private val dbConfig = DatabaseConfiguration(context)

    fun readSavedGestures(): List<Gesture> {
        val db = Database("gestures", dbConfig)
        val query = QueryBuilder
            .select(SelectResult.all())
            .from(DataSource.database(db))
        val result = query.execute()
        return result.allResults().map {
            it.getValue("gesture") as Gesture
        }
    }
}