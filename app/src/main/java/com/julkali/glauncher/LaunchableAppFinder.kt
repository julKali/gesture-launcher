package com.julkali.glauncher

import android.content.Context
import android.content.Intent

data class LaunchableApp(val name: String, val packageName: String, val intentAction: String)

class LaunchableAppFinder(context: Context) {

    private val packageManager = context.packageManager

    fun find(): List<LaunchableApp> {
        val launchableIntent = createLaunchableIntent()
        val launchables = packageManager.queryIntentActivities(launchableIntent, 0)
        return launchables.map {
            val appInfo = it.activityInfo.applicationInfo
            val appName = packageManager.getApplicationLabel(appInfo)?.toString()
                ?: throw Exception("app name not found!")
            LaunchableApp(appName, it.activityInfo.packageName, it.activityInfo.name)
        }
    }

    private fun createLaunchableIntent(): Intent {
        val mainIntent = Intent(Intent.ACTION_MAIN)
        mainIntent.addCategory(Intent.CATEGORY_LAUNCHER)
        return mainIntent
    }
}