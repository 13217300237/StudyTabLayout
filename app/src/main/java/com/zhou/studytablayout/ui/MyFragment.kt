package com.zhou.studytablayout.ui

import android.os.Build
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.annotation.RequiresApi
import androidx.cardview.widget.CardView
import androidx.fragment.app.Fragment
import com.zhou.studytablayout.R

class MyFragment(name: Int) : Fragment() {

    private val mPosition = name

    @RequiresApi(Build.VERSION_CODES.M)
    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val root = inflater.inflate(R.layout.fragment_1, container, false)
        init(root)
        return root
    }

    @RequiresApi(Build.VERSION_CODES.M)
    private fun init(root: View?) {
        val tv = root?.findViewById<TextView>(R.id.tv)
        val cardMain = root?.findViewById<CardView>(R.id.card_main)
        tv?.text = "$mPosition"
        var color = 0
        when (mPosition % 5) {
            0 -> color = R.color.c1
            1 -> color = R.color.c2
            2 -> color = R.color.c3
            3 -> color = R.color.c4
            4 -> color = R.color.c5
        }
        cardMain?.setCardBackgroundColor(resources.getColor(color))
    }
}