package com.julkali.glauncher.fragments

import android.content.Context
import android.os.Bundle
import android.util.Log
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.MotionEvent
import android.view.View
import android.view.ViewGroup
import com.julkali.glauncher.*

import com.julkali.glauncher.io.database.GestureDBSaver
import com.julkali.glauncher.processing.data.Gesture
import com.julkali.glauncher.processing.GestureBuilder
import com.julkali.glauncher.processing.GestureNormalizer

// TODO: Rename parameter arguments, choose names that match
// the fragment initialization parameters, e.g. ARG_ITEM_NUMBER
private const val ARG_PARAM1 = "param1"
private const val ARG_PARAM2 = "param2"

/**
 * A simple [Fragment] subclass.
 * Activities that contain this fragment must implement the
 * [SaveNewGestureFragment.OnFragmentInteractionListener] interface
 * to handle interaction events.
 * Use the [SaveNewGestureFragment.newInstance] factory method to
 * create an instance of this fragment.
 */
class SaveNewGestureFragment : Fragment() {
    // TODO: Rename and change types of parameters
    private val TAG = "SaveNewGestureFragment"
    private var param1: String? = null
    private var param2: String? = null
    private var listener: OnFragmentInteractionListener? = null
    private val gestureBuilder =
        GestureBuilder()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        arguments?.let {
            param1 = it.getString(ARG_PARAM1)
            param2 = it.getString(ARG_PARAM2)
        }
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        // Inflate the layout for this fragment
        val view = inflater.inflate(R.layout.fragment_save_new_gesture, container, false)
        view.setOnTouchListener(fun(v: View, ev: MotionEvent): Boolean {
            gestureBuilder.processMotionEvent(ev)
            if (gestureBuilder.getIsDone()) {
                saveGesture(gestureBuilder.getGesture())
                gestureBuilder.clear()
            }
            return true
        })
        return view
    }

    private fun saveGesture(gesture: Gesture) {
        val gestureNormalizer =
            GestureNormalizer()
        val saver =
            GestureDBSaver(activity!!.applicationContext)
        val normalized = gestureNormalizer.normalize(gesture)
        for ((pId, coords) in normalized.pointers) {
            Log.d(TAG, "POINTER $pId's trace:\n${coords.joinToString("\n")}")
        }
        saver.saveGesture(normalized)
        listener?.onGestureSaved()
    }

    override fun onAttach(context: Context) {
        super.onAttach(context)
        if (context is OnFragmentInteractionListener) {
            listener = context
        } else {
            throw RuntimeException(context.toString() + " must implement OnFragmentInteractionListener")
        }
    }

    override fun onDetach() {
        super.onDetach()
        listener = null
    }

    /**
     * This interface must be implemented by activities that contain this
     * fragment to allow an interaction in this fragment to be communicated
     * to the activity and potentially other fragments contained in that
     * activity.
     *
     *
     * See the Android Training lesson [Communicating with Other Fragments]
     * (http://developer.android.com/training/basics/fragments/communicating.html)
     * for more information.
     */
    interface OnFragmentInteractionListener {

        fun onGestureSaved()
    }

    companion object {
        /**
         * Use this factory method to create a new instance of
         * this fragment using the provided parameters.
         *
         * @param param1 Parameter 1.
         * @param param2 Parameter 2.
         * @return A new instance of fragment SaveNewGestureFragment.
         */
        // TODO: Rename and change types and number of parameters
        @JvmStatic
        fun newInstance(param1: String, param2: String) =
            SaveNewGestureFragment().apply {
                arguments = Bundle().apply {
                    putString(ARG_PARAM1, param1)
                    putString(ARG_PARAM2, param2)
                }
            }
    }
}
