package com.julkali.glauncher

import android.content.Intent
import android.os.Bundle
import android.widget.Toast
import androidx.fragment.app.FragmentActivity
import com.julkali.glauncher.fragments.GestureDrawerFragment
import com.julkali.glauncher.io.database.GestureDBHandler
import com.julkali.glauncher.processing.ClosestGestureFinder
import com.julkali.glauncher.processing.GestureNormalizer
import com.julkali.glauncher.processing.data.Gesture

class HomeActivity : FragmentActivity(),
    GestureDrawerFragment.GesturePanelFragmentListener {

    private val TAG = "Main"

    private lateinit var dbHandler: GestureDBHandler
    private lateinit var appLauncher: AppLauncher
    private lateinit var gestureFinder: ClosestGestureFinder

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_home)
        dbHandler = GestureDBHandler(applicationContext)
        appLauncher = AppLauncher(this)
        gestureFinder = ClosestGestureFinder(dbHandler)
    }

    private fun launchGestureManager() {
        val intent = Intent(this, GestureManagerActivity::class.java)
        startActivity(intent)
    }

    override fun onGestureDrawn(gesture: Gesture) {
        val normalizer = GestureNormalizer()
        val normalized = normalizer.normalize(gesture)
        if (gestureFinder.isLaunchGestureManagerGesture(gesture)) {
            launchGestureManager()
            return
        }
        val closest = gestureFinder.closestGesture(normalized)
        if (closest == null) {
            Toast.makeText(applicationContext, "Not found", Toast.LENGTH_SHORT).show()
            return
        }
        appLauncher.launch(closest)
    }
}
