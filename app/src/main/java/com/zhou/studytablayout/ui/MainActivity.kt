package com.zhou.studytablayout.ui

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentManager
import androidx.fragment.app.FragmentPagerAdapter
import com.zhou.studytablayout.R
import com.zhou.studytablayout.ui.custom.GreenTabLayout
import kotlinx.android.synthetic.main.activity_main.*
import java.lang.StringBuilder

class MainActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        val adapter = MyPagerAdapter(supportFragmentManager)
        hankViewpager.adapter = adapter
        hankViewpager.offscreenPageLimit = 3
        hankViewpager.setPageTransformer(true, MyPageTransformer(this, adapter.count))
        hankTabLayout.setupWithViewPager(hankViewpager)
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
            val stringBuilder = StringBuilder("栏目")
            for (i in 0..position) {
                stringBuilder.append("1")
            }
            return stringBuilder.toString()
        }

    }
}
