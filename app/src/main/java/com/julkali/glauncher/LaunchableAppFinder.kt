package com.julkali.glauncher

import android.content.Context
import android.content.Intent

class LaunchableAppFinder(context: Context) {

    private val packageManager = context.packageManager

    fun find(): List<LaunchableApp> {
        val launchableIntent = createLaunchableIntent()
        val launchables = packageManager.queryIntentActivities(launchableIntent, 0)
        return launchables.map {
            val activityInfo = it.activityInfo
            val appName = activityInfo.loadLabel(packageManager).toString()
            LaunchableApp(appName, activityInfo.packageName, activityInfo.name)
        }
    }

    private fun createLaunchableIntent(): Intent {
        val mainIntent = Intent(Intent.ACTION_MAIN)
        mainIntent.addCategory(Intent.CATEGORY_LAUNCHER)
        return mainIntent
    }
}