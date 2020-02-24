package com.julkali.glauncher

import android.content.Intent
import android.os.Bundle
import android.view.View
import android.widget.*
import androidx.fragment.app.FragmentActivity
import com.julkali.glauncher.fragments.GestureViewerFragment
import com.julkali.glauncher.io.database.GestureDBHandler
import kotlinx.android.synthetic.main.activity_gesture_manager.*

class GestureManagerActivity : FragmentActivity() {

    private lateinit var dbHandler: GestureDBHandler

    private var displayedId: String? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_gesture_manager)
        dbHandler = GestureDBHandler(this)
        loadSavedApps()
    }

    fun onNewButtonClicked(view: View) {
        val intent = Intent(this, SaveGestureActivity::class.java)
        startActivity(intent)
    }

    private fun loadSavedApps() {
        val savedAppEntries = dbHandler.readSavedGestures()
        if (savedAppEntries.isEmpty()) {
            displayNoGesturesMessage()
            return
        }
        hideNoGestureMessage()
        val appEntriesSpinner = findViewById<Spinner>(R.id.savedAppEntries)
        val entries = savedAppEntries
            .map {
                object {
                    val id = it.id
                    val appName = it.appName
                    val gesture = it.gesture

                    override fun toString(): String {
                        return appName
                    }
                }
            }
        val adapter = ArrayAdapter(this, android.R.layout.simple_spinner_dropdown_item, entries)
        appEntriesSpinner.onItemSelectedListener = object : AdapterView.OnItemSelectedListener {
            override fun onNothingSelected(parent: AdapterView<*>?) {
                Toast.makeText(applicationContext, "Please select something!", Toast.LENGTH_SHORT).show()
            }

            override fun onItemSelected(
                parent: AdapterView<*>?,
                view: View?,
                position: Int,
                id: Long
            ) {
                val selected = entries[position]
                val fragment: GestureViewerFragment =
                    this@GestureManagerActivity.gestureViewerFragment as GestureViewerFragment
                fragment.display(
                    selected.appName,
                    selected.gesture
                    )
                displayedId = selected.id
            }

        }
        appEntriesSpinner.adapter = adapter
    }

    override fun onResume() {
        super.onResume()
        loadSavedApps()
    }

    private fun displayNoGesturesMessage() {
        val appEntriesSpinner = findViewById<Spinner>(R.id.savedAppEntries)
        val deleteButton = findViewById<Button>(R.id.deleteLaunchEntryButton)
        appEntriesSpinner.visibility = View.GONE
        deleteButton.visibility = View.GONE
        val fragmentManager = supportFragmentManager
        fragmentManager.beginTransaction().hide(gestureViewerFragment).commit()
        val txtNoGestures = findViewById<TextView>(R.id.txtNoGestures)
        txtNoGestures?.visibility = View.VISIBLE
    }

    private fun hideNoGestureMessage() {
        val appEntriesSpinner = findViewById<Spinner>(R.id.savedAppEntries)
        val deleteButton = findViewById<Button>(R.id.deleteLaunchEntryButton)
        appEntriesSpinner.visibility = View.VISIBLE
        deleteButton.visibility = View.VISIBLE
        val fragmentManager = supportFragmentManager
        fragmentManager.beginTransaction().show(gestureViewerFragment).commit()
        val txtNoGestures = findViewById<TextView>(R.id.txtNoGestures)
        txtNoGestures?.visibility = View.GONE
    }

    fun deleteAppLaunchEntry(view: View) {
        val lock = displayedId ?: return
        dbHandler.deleteAppLaunchEntryById(lock)
        loadSavedApps()
    }
}
