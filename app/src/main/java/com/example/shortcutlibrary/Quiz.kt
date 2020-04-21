package com.example.shortcutlibrary

import android.os.Bundle
import android.util.Log
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.LinearLayout
import android.widget.TextView

class Quiz : Fragment(), View.OnClickListener {
    var thisView : View? = null

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        thisView = inflater.inflate(R.layout.fragment_quiz, container, false)
        // Inflate the layout for this fragment
        return thisView
    }

    override fun onActivityCreated(savedInstanceState: Bundle?) {
        super.onActivityCreated(savedInstanceState)

        Log.d("체크", thisView?.toString())
        thisView?.findViewById<LinearLayout>(R.id.quiz_main_wrap)!!.visibility = View.INVISIBLE

        thisView?.findViewById<TextView>(R.id.quiz_enterance_button)!!.setOnClickListener(this)
        thisView?.findViewById<TextView>(R.id.quiz_end_button)!!.setOnClickListener(this)
        thisView?.findViewById<TextView>(R.id.quiz_option1)!!.setOnClickListener(this)
        thisView?.findViewById<TextView>(R.id.quiz_option2)!!.setOnClickListener(this)
        thisView?.findViewById<TextView>(R.id.quiz_option3)!!.setOnClickListener(this)
        thisView?.findViewById<TextView>(R.id.quiz_option4)!!.setOnClickListener(this)
        thisView?.findViewById<TextView>(R.id.quiz_option5)!!.setOnClickListener(this)
    }

    override fun onClick(v: View) {
        when (v.id) {
            R.id.quiz_enterance_button -> {
                v.findViewById<LinearLayout>(R.id.quiz_enterance_wrap).visibility = View.INVISIBLE
                v.findViewById<LinearLayout>(R.id.quiz_main_wrap).visibility = View.VISIBLE
            }
            R.id.quiz_end_button -> {
                v.findViewById<LinearLayout>(R.id.quiz_enterance_wrap).visibility = View.VISIBLE
                v.findViewById<LinearLayout>(R.id.quiz_main_wrap).visibility = View.INVISIBLE
            }
        }
    }
}
