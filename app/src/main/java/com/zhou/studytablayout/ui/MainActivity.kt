package com.zhou.studytablayout.ui

import android.content.Context
import android.graphics.Canvas
import android.graphics.Paint
import android.graphics.Rect
import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentManager
import androidx.fragment.app.FragmentPagerAdapter
import com.zhou.studytablayout.R
import com.zhou.studytablayout.common.SlidingIndicatorLayout
import com.zhou.studytablayout.ui.view.GradientTextView
import com.zhou.studytablayout.util.dpToPx
import kotlinx.android.synthetic.main.activity_main.*

class MainActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        val adapter = MyPagerAdapter(supportFragmentManager)
        hankViewpager.adapter = adapter
        hankViewpager.offscreenPageLimit = 3
        hankViewpager.setPageTransformer(true, MyPageTransformer(this, adapter.count))
        hankTabLayout.setupWithViewPager(hankViewpager, GradientTextView(this))
        hankTabLayout.setIndicatorDrawHandler(CustomDrawHandlerImpl(this))

        hankTabLayout2.setupWithViewPager(hankViewpager)
    }

    class CustomDrawHandlerImpl : SlidingIndicatorLayout.CustomDrawHandler {
        val context: Context

        constructor(context_: Context) {
            context = context_
        }

        override fun draw(indicatorLayout: SlidingIndicatorLayout, canvas: Canvas?) {
            val paint = Paint()
            paint.color = context.resources.getColor(R.color.c1)
            val fraction =
                (indicatorLayout.parent.mCurrentPosition.toFloat() + 1) / indicatorLayout.childCount.toFloat()// 分数
            val left = indicatorLayout.parent.scrollX
            val right =
                (indicatorLayout.parent.scrollX + indicatorLayout.parent.measuredWidth * fraction).toInt()
            val rect = Rect(left, 0, right, dpToPx(context, 10f))
            canvas?.drawRect(rect, paint)
        }
    }

    class MyPagerAdapter(manager: FragmentManager) :
        FragmentPagerAdapter(manager, BEHAVIOR_RESUME_ONLY_CURRENT_FRAGMENT) {

        override fun getItem(position: Int): Fragment {
            return MyFragment(position)
        }

        override fun getCount(): Int {
            return 10
        }

        override fun getPageTitle(position: Int): CharSequence? {
            val stringBuilder = StringBuilder("栏目$position")
//            for (i in 0..position) {
//                stringBuilder.append("$position")
//            }
            return stringBuilder.toString()
        }

    }
}
