package com.julkali.glauncher.io.file

import android.util.Log
import com.julkali.glauncher.processing.data.Gesture
import java.io.File

class FileWriter {

    fun write(gesture: Gesture, path: String, name: String? = null) {
        val content = gesture.pointers.joinToString(separator = "\n\n") { (id, coords) ->
            val coordString = coords.joinToString(separator = "\n") {
                "${it.x},${it.y}"
            }
            "#$id\n$coordString"
        }
        val filename = name ?: System.currentTimeMillis().toString();
        val file = File("${path}/$filename.txt")
        if (file.exists()) {
            throw Exception("not gonna overwrite that: ${file.name}")
        }
        file.createNewFile()
        file.writeText(content)
        Log.d("FileWriter", "Wrote to ${file.absolutePath}")
    }
}