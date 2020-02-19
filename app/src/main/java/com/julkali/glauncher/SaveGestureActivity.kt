package com.julkali.glauncher

import android.content.ComponentName
import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.view.View
import android.widget.ArrayAdapter
import android.widget.Spinner
import android.widget.Toast
import androidx.fragment.app.FragmentActivity
import com.julkali.glauncher.fragments.GesturePanelFragment
import com.julkali.glauncher.io.database.AppLaunchEntry
import com.julkali.glauncher.io.database.GestureDBHandler
import com.julkali.glauncher.processing.GestureNormalizer
import com.julkali.glauncher.processing.data.Gesture

class SaveGestureActivity : FragmentActivity(),
    GesturePanelFragment.GesturePanelFragmentListener{

    private val TAG = "SaveGestureActivity"
    private lateinit var dbHandler: GestureDBHandler
    private lateinit var appFinder: LaunchableAppFinder

    private var selectedApp: AppInformationListEntry? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_save_gesture)
        dbHandler = GestureDBHandler(applicationContext)
        appFinder = LaunchableAppFinder(applicationContext)
        loadPackageNames()
    }

    fun onRecordButtonClicked(view: View) {
        val packageNames = findViewById<Spinner>(R.id.packageNames)
        selectedApp = packageNames.selectedItem as AppInformationListEntry
    }

    private fun loadPackageNames() {
        val packageNamesSpinner = findViewById<Spinner>(R.id.packageNames)
        val packages = appFinder.find()
        val appInfos = packages
            .sortedBy {
                it.name
            }
            .map {
                AppInformationListEntry(it.name, it.packageName, it.intentAction)
            }
        val adapter = ArrayAdapter(this, android.R.layout.simple_spinner_dropdown_item, appInfos)
        packageNamesSpinner.adapter = adapter
    }

    private fun saveGesture(gesture: Gesture) {
        val selected = selectedApp
        if (selected == null) {
            Toast.makeText(applicationContext, "You have to first choose an app and click on Register!", Toast.LENGTH_SHORT).show()
            return
        }
        // todo: check that gesture doesn't exist already
        val entry =
            dbHandler.saveAppLaunchEntry(selected.appName, selected.packageName, selected.intentAction, gesture)
        onGestureSaved(entry)
    }

    private fun onGestureSaved(entry: AppLaunchEntry) {
        Toast.makeText(
            applicationContext,
            "Gesture for ${entry.packageName} saved with id ${entry.id}.",
            Toast.LENGTH_SHORT
        ).show()
        finish()
    }

    override fun onGestureDrawn(gesture: Gesture) {
        val normalizer = GestureNormalizer()
        val normalized = normalizer.normalize(gesture)
        saveGesture(normalized)
    }
}
