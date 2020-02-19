package com.julkali.glauncher.fragments

import android.content.Context
import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.MotionEvent
import android.view.View
import android.view.ViewGroup
import com.julkali.glauncher.processing.data.Gesture
import com.julkali.glauncher.processing.GestureBuilder
import com.julkali.glauncher.R
import io.reactivex.BackpressureStrategy
import io.reactivex.android.schedulers.AndroidSchedulers
import io.reactivex.subjects.PublishSubject

class GesturePanelFragment : Fragment() {
    private val TAG = "GesturePanelFragment"
    private var listener: GesturePanelFragmentListener? = null
    private lateinit var gestureBuilder: GestureBuilder

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val view = inflater.inflate(R.layout.fragment_gesture_panel, container, false)
        val subject = PublishSubject.create<MotionEvent>()
        gestureBuilder = GestureBuilder()
        gestureBuilder.getGesturesObserver()
            .observeOn(AndroidSchedulers.mainThread())
            .forEach {
                listener?.onGestureDrawn(it)
            }
        gestureBuilder.processMotionEvents(subject.toFlowable(BackpressureStrategy.ERROR))
        view.setOnTouchListener { v, ev ->
            subject.onNext(ev)
            true
        }
        return view
    }

    override fun onAttach(context: Context) {
        super.onAttach(context)
        if (context is GesturePanelFragmentListener) {
            listener = context
        } else {
            throw RuntimeException("$context must implement GesturePanelFragmentListener")
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
