package com.zhou.studytablayout.ui.custom

import android.animation.ValueAnimator
import android.content.Context
import android.graphics.Canvas
import android.graphics.Rect
import android.graphics.Typeface
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
import androidx.interpolator.view.animation.FastOutSlowInInterpolator
import androidx.viewpager.widget.ViewPager
import androidx.viewpager.widget.ViewPager.SCROLL_STATE_DRAGGING
import com.zhou.studytablayout.R
import com.zhou.studytablayout.util.dpToPx

/**
 * 绿色版
 *
 * @author Hank.Zhou
 *
 */
class GreenTabLayout : HorizontalScrollView, ViewPager.OnPageChangeListener {
    constructor(ctx: Context) : super(ctx) {
        init()
    }

    constructor(ctx: Context, attributes: AttributeSet) : super(ctx, attributes) {
        init()
    }

    constructor(context: Context, attrs: AttributeSet?, defStyleAttr: Int) : super(
        context,
        attrs,
        defStyleAttr
    ) {
        init()
    }

    // 自定义属性相关

    // TabView相关属性
    class TabViewAttrs {
        var textSize: Int = 0 // 字体大小，单位dp
        var textColor: Int = 0 // 字体颜色
        var background: Int = 0 // 背景色
        var textAppearance: Int = 0 // 字体整体风格
        var textTypeface: Typeface? = null // 字体 指定ttf文件
    }

    class IndicatorAttrs {
        var locationGravity: Int = Gravity.BOTTOM // 默认底部放置
        var widthMode: Int = 0 // 长度模式，TabView长度的倍数，或者 定死长度，还是取 TextView长度的倍数
        val height:Int = 0 // 高度，单位dp
        val alignMode :Int = 0 // 对齐模式，如果长度小于TabView，应该向靠左还是靠右
        val margin :Int = 0 // 根据 locationGravity 决定，如果是放在底部，就是与底部的距离
    }

    private lateinit var indicatorLayout: SlidingIndicatorLayout
    private var mCurrentPosition = 0
    private var scrollState = 0
    lateinit var mViewPager: ViewPager

    val selectedTextColor = R.color.c1
    val unselectedTextColor = R.color.cf

    private fun init() {
        indicatorLayout = SlidingIndicatorLayout(context, this)
        val layoutParams = LinearLayout.LayoutParams(WRAP_CONTENT, MATCH_PARENT)
        addView(indicatorLayout, layoutParams)
        overScrollMode = View.OVER_SCROLL_NEVER
        isHorizontalScrollBarEnabled = false
    }

    private fun addTabView(text: String) {
        indicatorLayout.addTabView(text)
    }

    fun setupWithViewPager(viewPager: ViewPager) {
        this.mViewPager = viewPager
        viewPager.addOnPageChangeListener(this)
        val adapter = viewPager.adapter ?: return
        val count = adapter!!.count // 栏目数量
        for (i in 0 until count) {
            val pageTitle = adapter.getPageTitle(i)
            addTabView(pageTitle.toString())
        }
    }

    private fun scrollTabLayout(position: Int, positionOffset: Float) {
        // 如果是向左, 就用当前的tabView滑动到左边一个tabView
        val currentTabView = indicatorLayout.getChildAt(position) as GreenTabView
        val currentLeft = currentTabView.left
        val currentRight = currentTabView.right

        val nextTabView = indicatorLayout.getChildAt(position + 1)
        if (nextTabView != null) {
            val nextLeft = nextTabView.left
            val nextRight = nextTabView.right

            val leftDiff = nextLeft - currentLeft
            val rightDiff = nextRight - currentRight

            indicatorLayout.updateIndicatorPosition(
                currentLeft + (leftDiff * positionOffset).toInt(),
                currentRight + (rightDiff * positionOffset).toInt()
            )
        }
    }

    override fun onPageScrollStateChanged(state: Int) {
        scrollState = state
        if (state == SCROLL_STATE_DRAGGING) {
            mCurrentPosition = mViewPager.currentItem
        }
    }

    override fun onPageScrolled(position: Int, positionOffset: Float, positionOffsetPixels: Int) {
        scrollTabLayout(position, positionOffset)
    }

    override fun onPageSelected(position: Int) {
        val tabView = indicatorLayout.getChildAt(position) as GreenTabView
        if (tabView != null) {
            indicatorLayout.updateIndicatorPositionByAnimator(tabView, tabView.left, tabView.right)
        }
    }
}

/**
 * 中间层 可滚动的 线性布局
 */
class SlidingIndicatorLayout : LinearLayout {

    var indicatorLeft = 0
    var indicatorRight = 0
    var parent: GreenTabLayout
    var inited: Boolean = false
    private var indicatorAnimator: ValueAnimator = ValueAnimator.ofFloat(0f, 1f)
    private var scrollAnimator: ValueAnimator = ValueAnimator.ofFloat(0f, 1f)
    private val tabViewBounds = Rect()
    private val parentBounds = Rect()

    constructor(ctx: Context, parent: GreenTabLayout) : super(ctx) {
        init()
        this.parent = parent
    }

    private fun init() {
        setWillNotDraw(false) // 如果不这么做，它自身的draw方法就不会调用
    }

    /**
     * 作为一个viewGroup，有可能它不会执行自身的draw方法，这里有一个值去控制  setWillNotDraw
     */
    override fun draw(canvas: Canvas?) {
        val indicatorHeight = dpToPx(context, 4f)// 指示器高度
        // 现在貌似应该去画indicator了
        // 要绘制，首先要确定范围，左上右下
        var top = height - indicatorHeight
        var bottom = height
        // 现在只考虑在底下的情况
        var selectedIndicator: Drawable = GradientDrawable()
        selectedIndicator.setBounds(indicatorLeft, top, indicatorRight, bottom)
        DrawableCompat.setTint(
            selectedIndicator,
            resources.getColor(parent.selectedTextColor)
        )
        selectedIndicator.draw(canvas!!)
        initIndicator()
        super.draw(canvas)
    }

    private fun initIndicator() {
        Log.d("addTabViewTag", "$childCount")
        if (childCount > 0) {
            if (!inited) {
                inited = true
                val tabView0 = getChildAt(0) as GreenTabView
                tabView0.performClick() // 难道这里在岗添加进去，测量尚未完成？那怎么办,那只能在onDraw里面去执行了
            }
        }
    }

    fun updateIndicatorPosition(targetLeft: Int, targetRight: Int) {
        indicatorLeft = targetLeft
        indicatorRight = targetRight
        postInvalidate()//
    }

    /**
     * 用动画平滑更新indicator的位置
     * @param tabView 当前这个子view
     * @param targetLeft 目标left
     * @param targetRight 目标right
     */
    fun updateIndicatorPositionByAnimator(
        tabView: GreenTabView,
        targetLeft: Int,
        targetRight: Int
    ) {
        val currentLeft = indicatorLeft
        val currentRight = indicatorRight
        val leftDiff = targetLeft - currentLeft
        val rightDiff = targetRight - currentRight

        if (indicatorAnimator != null)
            indicatorAnimator?.cancel()

        // 这里应该有一个动画
        indicatorAnimator.run {
            duration = 200
            interpolator = FastOutSlowInInterpolator()
            addUpdateListener {
                val progress = it.animatedValue as Float
                indicatorLeft = currentLeft + (leftDiff * progress).toInt()
                indicatorRight = currentRight + (rightDiff * progress).toInt()
                postInvalidate()//  刷新自身，调用draw
            }
            start()
        }

        // 处理最外层布局( HankTabLayout )的滑动
        parent.run {
            tabView.getHitRect(tabViewBounds)
            getHitRect(parentBounds)
            val scrolledX = scrollX // 已经滑动过的距离
            val tabViewRealLeft = tabViewBounds.left - scrolledX  // 真正的left, 要算上scrolledX
            val tabViewRealRight = tabViewBounds.right - scrolledX // 真正的right, 要算上scrolledX

            val tabViewCenterX = (tabViewRealLeft + tabViewRealRight) / 2
            val parentCenterX = (parentBounds.left + parentBounds.right) / 2
            val needToScrollX = -parentCenterX + tabViewCenterX //  差值就是需要滚动的距离

            startScrollAnimator(this, scrolledX, scrolledX + needToScrollX)
        }

        // 把其他的 TabView 都设置成未选中状态
        for (i in 0 until childCount) {
            val current = getChildAt(i) as GreenTabView
            if (current.hashCode() == tabView.hashCode()) {// 如果是当前被点击的这个，那么就不需要管
                current.setSelectedStatus(true) // 选中状态
            } else {// 如果不是
                current.setSelectedStatus(false)// 非选中状态
            }
        }
    }

    /**
     * 用动画效果平滑滚动过去
     */
    private fun startScrollAnimator(tabLayout: GreenTabLayout, from: Int, to: Int) {
        if (scrollAnimator != null && scrollAnimator.isRunning) scrollAnimator.cancel()
        scrollAnimator.duration = 200
        scrollAnimator.interpolator = FastOutSlowInInterpolator()
        scrollAnimator.addUpdateListener {
            val progress = it.animatedValue as Float
            val diff = to - from
            val currentDif = (diff * progress).toInt()
            tabLayout.scrollTo(from + currentDif, 0)
        }
        scrollAnimator.start()
    }

    /**
     * 添加TabView
     */
    fun addTabView(text: String) {
        val tabView = GreenTabView(context, this)
        val param = LayoutParams(WRAP_CONTENT, WRAP_CONTENT)
        param.setMargins(dpToPx(context, 10f))

        val textView = TextView(context)
        textView.text = text
        textView.gravity = Gravity.CENTER
        textView.setPadding(dpToPx(context, 15f))
        textView.setTextSize(TypedValue.COMPLEX_UNIT_SP, 20f)
        textView.setTextColor(resources.getColor(parent.unselectedTextColor))
        tabView.setTextView(textView)

        addView(tabView, param)
        postInvalidate()

    }

}

/**
 * 最里层TabView
 */
class GreenTabView : LinearLayout {
    private lateinit var titleTextView: TextView
    private var selectedStatue: Boolean = false
    private var parent: SlidingIndicatorLayout

    constructor(ctx: Context, parent: SlidingIndicatorLayout) : super(ctx) {
        this.parent = parent
    }

    fun setTextView(textView: TextView) {
        removeAllViews()

        titleTextView = textView
        titleTextView.setTextSize(TypedValue.COMPLEX_UNIT_SP, 10f)
        val param = LayoutParams(WRAP_CONTENT, MATCH_PARENT)
        addView(titleTextView, param)

        setOnClickListener {
            parent.updateIndicatorPositionByAnimator(this, left, right)
            parent.parent.mViewPager.currentItem =
                parent.indexOfChild(this)// 拿到viewPager，然后强制滑动到指定的page
        }
    }

    fun setSelectedStatus(selected: Boolean) {
        selectedStatue = selected
        if (selected) {
            titleTextView.setTextColor(resources.getColor(parent.parent.selectedTextColor))
        } else {
            titleTextView.setTextColor(resources.getColor(parent.parent.unselectedTextColor))
        }
    }

}

