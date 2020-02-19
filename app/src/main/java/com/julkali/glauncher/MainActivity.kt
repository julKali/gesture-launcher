package com.julkali.glauncher

import android.content.ComponentName
import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.view.View
import android.widget.Toast
import androidx.fragment.app.FragmentActivity
import com.julkali.glauncher.fragments.GesturePanelFragment
import com.julkali.glauncher.io.database.AppLaunchEntry
import com.julkali.glauncher.io.database.GestureDBHandler
import com.julkali.glauncher.processing.GestureNormalizer
import com.julkali.glauncher.processing.compress.GestureCompressor
import com.julkali.glauncher.processing.data.Gesture
import com.julkali.glauncher.processing.score.GestureScoreCalculator

class MainActivity : FragmentActivity(),
    GesturePanelFragment.GesturePanelFragmentListener {

    private val TAG = "Main"
    private val COMPRESSED_SIZE = 100
    private val MIN_SCORE_THRESHOLD = 0.6

    private val gestureScoreCalculator =
        GestureScoreCalculator()
    private lateinit var dbHandler: GestureDBHandler
    private lateinit var appFinder: LaunchableAppFinder
    private lateinit var appLauncher: AppLauncher

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        dbHandler = GestureDBHandler(applicationContext)
        appFinder = LaunchableAppFinder(applicationContext)
        appLauncher = AppLauncher(applicationContext)
        setGesturePanelFragment()
    }

    fun onNewButtonClicked(view: View) {
        val intent = Intent(this, SaveGestureActivity::class.java)
        startActivity(intent)
    }

    private fun closestGesture(gesture: Gesture): AppLaunchEntry? {
        val compressor =
            GestureCompressor(
                COMPRESSED_SIZE
            )
        val gestures = dbHandler.readSavedGestures()
        val scores = mutableMapOf<AppLaunchEntry, Double>()
        for (toCompareDoc in gestures) {
            val toCompare = toCompareDoc.gesture
            val gestureCompressed = compressor.compress(gesture)
            val toCompareCompressed = compressor.compress(toCompare)
            val score = gestureScoreCalculator.calculate(gestureCompressed, toCompareCompressed)
            scores[toCompareDoc] = score
            Log.d(TAG, score.toString())
        }
        val max = scores.maxBy { it.value }
        Log.d(TAG, max?.value.toString())
        if (max != null && MIN_SCORE_THRESHOLD <= max.value) return max.key
        return null
    }

    private fun setGesturePanelFragment() {
        val fragmentManager = supportFragmentManager
        val transaction = fragmentManager.beginTransaction()
        val fragment = GesturePanelFragment.newInstance()
        transaction.replace(R.id.gesture_fragment_container, fragment)
        transaction.commit()
    }

    override fun onGestureDrawn(gesture: Gesture) {
        val normalizer = GestureNormalizer()
        val normalized = normalizer.normalize(gesture)
        val closest = closestGesture(normalized)
        if (closest == null) {
            Toast.makeText(applicationContext, "Not found", Toast.LENGTH_SHORT).show()
            return
        }
        val intent = Intent().apply {
            component = ComponentName(closest.packageName, closest.intentAction)
        }
        startActivity(intent)
    }
}
