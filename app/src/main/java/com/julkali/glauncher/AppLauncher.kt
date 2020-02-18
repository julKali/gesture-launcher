package com.julkali.glauncher

import android.content.Context

class AppLauncher(private val context: Context) {

    private val packageManager = context.packageManager

    fun launch(packageName: String) {
        val intent = packageManager.getLaunchIntentForPackage(packageName)
            ?: throw Exception("no launchable intent for package $packageName found")
        context.startActivity(intent)
    }
}
