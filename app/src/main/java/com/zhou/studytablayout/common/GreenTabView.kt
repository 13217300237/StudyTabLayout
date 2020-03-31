package com.zhou.studytablayout.common

import android.animation.ValueAnimator
import android.content.Context
import android.util.Log
import android.util.TypedValue
import android.view.Gravity
import android.view.ViewGroup
import android.widget.LinearLayout
import android.widget.TextView
import androidx.interpolator.view.animation.LinearOutSlowInInterpolator
import kotlin.math.roundToInt

/**
 * 最里层TabView
 */
class GreenTabView(ctx: Context, private var parent: SlidingIndicatorLayout) : LinearLayout(ctx) {
    lateinit var titleTextView: GreenTextView
    private var selectedStatue: Boolean = false

    fun setTextView(textView: GreenTextView) {
        removeAllViews()

        titleTextView = textView
        parent.parent.tabViewAttrs.run {
            titleTextView.setBackgroundColor(tabViewBackgroundColor)

            titleTextView.setTextSize(TypedValue.COMPLEX_UNIT_PX, tabViewTextSizeSelected)
            titleTextView.typeface = tabViewTextTypeface
            titleTextView.setTextColor(tabViewTextColor)
            titleTextView.gravity = Gravity.CENTER

            titleTextView.setPadding(
                tabViewTextPaddingLeft.roundToInt(),
                tabViewTextPaddingTop.roundToInt(),
                tabViewTextPaddingRight.roundToInt(),
                tabViewTextPaddingBottom.roundToInt()
            )

        }

        val param =
            LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.MATCH_PARENT)
        addView(titleTextView, param)

        setOnClickListener {
            val index = parent.indexOfChild(this)
            parent.updateIndicatorPositionByAnimator(index)
            parent.parent.mViewPager.currentItem = index// 拿到viewPager，然后强制滑动到指定的page
        }
    }

    fun setSelectedStatus(selected: Boolean) {
        selectedStatue = selected

        parent.parent.tabViewAttrs.run {
            if (selected) {
                titleTextView.setTextColor(tabViewTextColorSelected)
                titleTextView.setTextSize(TypedValue.COMPLEX_UNIT_PX, tabViewTextSizeSelected)
                titleTextView.addShader(parent.parent.mOldCurrentPosition - parent.parent.mCurrentPosition)
            } else {
                Log.d("setSelectedStatus", "removeShader")
                titleTextView.setTextColor(tabViewTextColor)
                titleTextView.setTextSize(TypedValue.COMPLEX_UNIT_PX, tabViewTextSize)
                titleTextView.removeShader(parent.parent.mOldCurrentPosition - parent.parent.mCurrentPosition)
            }
        }
    }

    fun setSelectedStatusByAnimator(selected: Boolean) {
        selectedStatue = selected

        parent.parent.tabViewAttrs.run {
            if (selected) {
                titleTextView.setTextColor(tabViewTextColorSelected)
                setTextSizeByAnimator(titleTextView, tabViewTextSizeSelected)
            } else {
                titleTextView.setTextColor(tabViewTextColor)
                setTextSizeByAnimator(titleTextView, tabViewTextSize)
            }
        }
    }

    private var textSizeAnimator: ValueAnimator? = null

    private fun setTextSizeByAnimator(textView: TextView, targetTextSizePx: Float) {
        if (textSizeAnimator != null && textSizeAnimator?.isRunning!!) textSizeAnimator?.cancel() // 不允许动画重复执行
        textSizeAnimator = ValueAnimator.ofFloat(textView.textSize, targetTextSizePx)
        textSizeAnimator?.duration = 200
        textSizeAnimator?.interpolator = LinearOutSlowInInterpolator()
        textSizeAnimator?.addUpdateListener {
            textView.setTextSize(TypedValue.COMPLEX_UNIT_PX, it.animatedValue as Float)
        }
        textSizeAnimator?.start()
    }


    fun notifySetting(positionOffset: Float, currentPosition: Int, direction: Int) {
        titleTextView.onSetting(
            positionOffset,
            parent.indexOfChild(this) == currentPosition,
            direction
        )
    }

    fun updateTextViewShader(positionOffset: Float, currentPosition: Int) {
        titleTextView.handlerPositionOffset(
            positionOffset,
            parent.indexOfChild(this) == currentPosition
        )
    }
}