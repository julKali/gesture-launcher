package com.julkali.glauncher.fragments

import android.content.Context
import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import com.julkali.glauncher.processing.data.Gesture
import com.julkali.glauncher.processing.GestureBuilder
import com.julkali.glauncher.R

class GesturePanelFragment : Fragment() {
    private val TAG = "GesturePanelFragment"
    private var listener: GesturePanelFragmentListener? = null
    private val gestureBuilder =
        GestureBuilder()

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        // Inflate the layout for this fragment
        val view = inflater.inflate(R.layout.fragment_gesture_panel, container, false)
        view.setOnTouchListener { v, ev ->
            gestureBuilder.processMotionEvent(ev)
            if (gestureBuilder.getIsDone()) {
                listener?.onGestureDrawn(gestureBuilder.getGesture())
                gestureBuilder.clear()
            }
            true
        }
        return view
    }

    override fun onAttach(context: Context) {
        super.onAttach(context)
        if (context is GesturePanelFragmentListener) {
            listener = context
        } else {
            throw RuntimeException(context.toString() + " must implement GesturePanelFragmentListener")
        }
    }

    override fun onDetach() {
        super.onDetach()
        listener = null
    }

    interface GesturePanelFragmentListener {

        fun onGestureDrawn(gesture: Gesture)
    }

    companion object {
        @JvmStatic
        fun newInstance() =
            GesturePanelFragment()
    }
}
