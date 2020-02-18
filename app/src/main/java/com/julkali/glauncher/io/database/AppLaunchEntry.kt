package com.julkali.glauncher.io.database

import com.julkali.glauncher.processing.data.Gesture

data class AppLaunchEntry(
    val id: String,
    val packageName: String,
    val intentAction: String,
    val gesture: Gesture
)