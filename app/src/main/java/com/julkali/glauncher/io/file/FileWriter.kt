package com.julkali.glauncher.io.file

import android.util.Log
import com.julkali.glauncher.processing.data.Gesture
import java.io.File

class FileWriter {

    fun write(gesture: Gesture, path: String) {
        val content = gesture.pointers.map { (id, coords) ->
            val coordString = coords.joinToString(separator = "\n") {
                "${it.x},${it.y}"
            }
            id to coordString
        }
        val timestamp = System.currentTimeMillis().toString();
        for ((pId, data) in content) {
            val file = File("${path}/${timestamp}.$pId.txt")
            if (file.exists()) {
                throw Exception("not gonna overwrite that.")
            }
            file.createNewFile()
            file.writeText(data)
            Log.d("FileWriter", "Wrote to ${file.absolutePath}")
        }
    }
}