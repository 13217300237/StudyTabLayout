package com.zhou.studytablayout.ui.custom

import android.content.Context
import android.graphics.Canvas
import android.graphics.drawable.Drawable
import android.graphics.drawable.GradientDrawable
import android.util.AttributeSet
import android.util.Log
import android.util.TypedValue
import android.view.Gravity
import android.view.View
import android.view.ViewGroup.LayoutParams.MATCH_PARENT
import android.view.ViewGroup.LayoutParams.WRAP_CONTENT
import android.widget.HorizontalScrollView
import android.widget.LinearLayout
import android.widget.TextView
import androidx.core.graphics.drawable.DrawableCompat
import androidx.core.view.setMargins
import androidx.core.view.setPadding
import com.zhou.studytablayout.R
import com.zhou.studytablayout.util.dpToPx

/**
 * 最外层
 */
class HankTabLayout : HorizontalScrollView {
    constructor(ctx: Context) : super(ctx) {
        init()
    }

    constructor(ctx: Context, attributes: AttributeSet) : super(ctx, attributes) {
        init()
    }

    constructor(context: Context, attrs: AttributeSet?, defStyleAttr: Int) : super(context, attrs, defStyleAttr) {
        init()
    }


    private lateinit var indicatorLayout: IndicatorLayout

    private fun init() {
        indicatorLayout = IndicatorLayout(context)
        val layoutParams = LinearLayout.LayoutParams(WRAP_CONTENT, MATCH_PARENT)
        addView(indicatorLayout, layoutParams)

        overScrollMode = View.OVER_SCROLL_NEVER
        isHorizontalScrollBarEnabled = false
    }

    fun addTabView(text: String) {
        indicatorLayout.addTabView(text)
    }

}

/**
 * 中间层 可滚动的
 */
class IndicatorLayout : LinearLayout {
    constructor(ctx: Context) : super(ctx) {
        init()
    }

    private fun init() {
        setWillNotDraw(false) // 如果不这么做，它自身的draw方法就不会调用
    }


    /**
     * 作为一个viewGroup，有可能它不会执行自身的draw方法，这里有一个值去控制，好像是 setWillNotDraw
     */
    override fun draw(canvas: Canvas?) {
        val indicatorHeight = dpToPx(context, 4f)// 指示器高度
        // 现在貌似应该去画indicator了
        // 要绘制，首先要确定范围，左上右下
        var left = 0
        var right = dpToPx(context, 100f)
        var top = height - indicatorHeight
        var bottom = height

        Log.d("drawTag", "$left    $right   $top     $bottom")

        // 现在只考虑在底下的情况
        var selectedIndicator: Drawable = GradientDrawable()
        selectedIndicator.setBounds(left, top, right, bottom)
        DrawableCompat.setTint(selectedIndicator, resources.getColor(R.color.c2))
        selectedIndicator.draw(canvas!!)

        super.draw(canvas)
    }


    /**
     * 但是onDraw一定会执行
     */
    override fun onDraw(canvas: Canvas?) {
        super.onDraw(canvas)
    }

    // 对外提供方法，添加TabView
    fun addTabView(text: String) {
        val tabView = TabView(context)
        val param = LayoutParams(WRAP_CONTENT, WRAP_CONTENT)
        param.setMargins(dpToPx(context, 10f))

        val textView = TextView(context)
        textView.setBackgroundDrawable(resources.getDrawable(R.drawable.my_tablayout_textview_bg))
        textView.text = text
        textView.gravity = Gravity.CENTER
        textView.setPadding(dpToPx(context, 15f))
        textView.setTextSize(TypedValue.COMPLEX_UNIT_SP, 20f)
        textView.setTextColor(resources.getColor(R.color.c4))
        tabView.setTextView(textView)

        addView(tabView, param)
        postInvalidate()
    }
}

/**
 * 最里层TabView
 */
class TabView : LinearLayout {
    private lateinit var titleTextView: TextView
    private var selectedStatue: Boolean = false

    constructor(ctx: Context) : super(ctx) {
        init()
    }

    fun setTextView(textView: TextView) {
        titleTextView = textView
        removeAllViews()
        val param = LayoutParams(WRAP_CONTENT, MATCH_PARENT)
        addView(titleTextView, param)

        titleTextView.setOnClickListener {
            setSelectedStatus(!selectedStatue)
        }
    }

    private fun init() {

    }

    private fun setSelectedStatus(selected: Boolean) {
        selectedStatue = selected
        if (selected) {
            titleTextView.setTextColor(resources.getColor(R.color.c10))
        } else {
            titleTextView.setTextColor(resources.getColor(R.color.cf))
        }
    }


}

//现在，给每一个TabView提供一个选中和取消选中的方法
// 下一步，给IndicatorLayout提供一个方法，将indicator画在文字的正下方，等长