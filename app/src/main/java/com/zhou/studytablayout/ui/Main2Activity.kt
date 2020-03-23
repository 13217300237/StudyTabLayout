package com.zhou.studytablayout.ui

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.view.Gravity
import android.widget.LinearLayout
import android.widget.TextView
import androidx.core.view.setMargins
import com.zhou.studytablayout.R
import com.zhou.studytablayout.util.dpToPx
import kotlinx.android.synthetic.main.activity_main2.*

class Main2Activity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main2)
        init()
    }

    private fun init() {
        for (i in 0..10) {
            val tv = layoutInflater.inflate(
                R.layout.my_tablayout_textview,
                myTabLayout,
                false
            ) as TextView
            tv.text = "标题$i"
            val param = LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.WRAP_CONTENT,
                LinearLayout.LayoutParams.WRAP_CONTENT
            )
            param.gravity = Gravity.CENTER_VERTICAL
            param.setMargins(dpToPx(this, 10f))
            myTabLayout.addTab(tv as TextView?, i, param)
        }

    }
}

