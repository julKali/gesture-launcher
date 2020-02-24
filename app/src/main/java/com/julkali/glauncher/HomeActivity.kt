package com.julkali.glauncher

import android.content.ComponentName
import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.view.View
import android.widget.Toast
import androidx.fragment.app.FragmentActivity
import com.julkali.glauncher.fragments.GestureDrawerFragment
import com.julkali.glauncher.io.database.AppLaunchEntry
import com.julkali.glauncher.io.database.GestureDBHandler
import com.julkali.glauncher.processing.GestureNormalizer
import com.julkali.glauncher.processing.compress.GestureCompressor
import com.julkali.glauncher.processing.data.Coordinate
import com.julkali.glauncher.processing.data.Gesture
import com.julkali.glauncher.processing.data.Pointer
import com.julkali.glauncher.processing.score.GestureScoreCalculator

class HomeActivity : FragmentActivity(),
    GestureDrawerFragment.GesturePanelFragmentListener {

    private val TAG = "Main"
    private val COMPRESSED_SIZE = 100
    private val MIN_SCORE_THRESHOLD = 0.8

    private val gestureScoreCalculator =
        GestureScoreCalculator()
    private val compressor = GestureCompressor(
        COMPRESSED_SIZE
    )
    private lateinit var dbHandler: GestureDBHandler
    private lateinit var appLauncher: AppLauncher

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_home)
        dbHandler = GestureDBHandler(applicationContext)
        appLauncher = AppLauncher(this)
    }

    private fun launchGestureManager() {
        val intent = Intent(this, GestureManagerActivity::class.java)
        startActivity(intent)
    }

    private fun closestGesture(gesture: Gesture): AppLaunchEntry? {

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

    private fun Gesture.isLaunchGestureManagerGesture(): Boolean {
        val toCompare = Gesture(
            listOf(
                Pointer(0, listOf(Coordinate(0.0, 1.0))),
                Pointer(1, listOf(Coordinate(0.0, 1.0))),
                Pointer(2, listOf(Coordinate(0.0, 1.0), Coordinate(0.0, 0.0)))
            )
        )
        val gestureCompressed = compressor.compress(this)
        val toCompareCompressed = compressor.compress(toCompare)
        val score = gestureScoreCalculator.calculate(gestureCompressed, toCompareCompressed)
        return score >= MIN_SCORE_THRESHOLD

    }

    override fun onGestureDrawn(gesture: Gesture) {
        val normalizer = GestureNormalizer()
        val normalized = normalizer.normalize(gesture)
        if (normalized.isLaunchGestureManagerGesture()) {
            launchGestureManager()
            return
        }
        val closest = closestGesture(normalized)
        if (closest == null) {
            Toast.makeText(applicationContext, "Not found", Toast.LENGTH_SHORT).show()
            return
        }
        appLauncher.launch(closest)
    }
}
