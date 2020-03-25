package com.zhou.studytablayout.ui.custom

import android.animation.ValueAnimator
import android.content.Context
import android.graphics.Canvas
import android.graphics.Rect
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
import java.lang.ref.WeakReference

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
        indicatorLayout = IndicatorLayout(context, this)
        val layoutParams = LinearLayout.LayoutParams(WRAP_CONTENT, MATCH_PARENT)
        addView(indicatorLayout, layoutParams)

        overScrollMode = View.OVER_SCROLL_NEVER
        isHorizontalScrollBarEnabled = false
    }

    private fun addTabView(text: String) {
        indicatorLayout.addTabView(text)
    }

    lateinit var mViewPager: ViewPager

    fun setupWithViewPager(viewPager: ViewPager) {
        this.mViewPager = viewPager

        // Add our custom OnPageChangeListener to the ViewPager
        pageChangeListener = MyTabPageChangeLis(this)
        viewPager.addOnPageChangeListener(pageChangeListener)

        // 标题栏
        val adapter = viewPager.adapter ?: return
        val count = adapter!!.count // 栏目数量
        for (i in 0 until count) {
            val pageTitle = adapter.getPageTitle(i)
            addTabView(pageTitle.toString())
        }

        // 下一步，viewPager发生滑动的时候，要求indicator也跟着变动位置

    }


    fun scrollTabLayout(position: Int, positionOffset: Float) {
        // 如果是向左, 就用当前的tabView滑动到左边一个tabView
        val currentTabView = indicatorLayout.getChildAt(position) as TabView
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
                    currentRight + (rightDiff * positionOffset).toInt())
        }
    }

    private lateinit var pageChangeListener: MyTabPageChangeLis

    class MyTabPageChangeLis : ViewPager.OnPageChangeListener {
        private var tabLayoutRef: WeakReference<HankTabLayout> // 防止内存泄漏，用弱引用
        private var previousScrollState = 0
        private var scrollState = 0

        constructor(tabLayout: HankTabLayout) {
            tabLayoutRef = WeakReference(tabLayout)
        }

        override fun onPageScrollStateChanged(state: Int) {
            previousScrollState = scrollState
            scrollState = state

            val tabLayout: HankTabLayout = tabLayoutRef.get() ?: return

            if (state == SCROLL_STATE_DRAGGING) {
                mCurrentPosition = tabLayout.mViewPager.currentItem
            }
        }

        private var mCurrentPosition = 0

        override fun onPageScrolled(position: Int, positionOffset: Float, positionOffsetPixels: Int) {
            val tabLayout: HankTabLayout = tabLayoutRef.get() ?: return
            tabLayout.scrollTabLayout(position,positionOffset)
        }

        /**
         * 这个方法有问题，明明还没滑动到2的位置，position已经显示为2了,
         * 他奶奶的，他带有预测性质，预感到将要滑动到2，就提前变成2了，应该是fling手势导致
         */
        override fun onPageSelected(position: Int) {
            val tabLayout: HankTabLayout = tabLayoutRef.get() ?: return
            Log.d("MyTabPageChangeLis", "onPageSelected--> position:$position")
            // 如果他被选中，那么，让更新indicator的位置
            val tabView = tabLayout.indicatorLayout.getChildAt(position) as TabView
            if (tabView != null) {
                tabLayout.indicatorLayout.updateIndicatorPositionByAnimator(tabView, tabView.left, tabView.right)
            }
        }

    }
}

/**
 * 中间层 可滚动的
 */
class IndicatorLayout : LinearLayout {
    constructor(ctx: Context, parent: HankTabLayout) : super(ctx) {
        init()
        this.parent = parent
    }

    private fun init() {
        setWillNotDraw(false) // 如果不这么做，它自身的draw方法就不会调用
    }


    var indicatorLeft = 0
    var indicatorRight = 0

    var parent: HankTabLayout

    /**
     * 作为一个viewGroup，有可能它不会执行自身的draw方法，这里有一个值去控制，好像是 setWillNotDraw
     */
    override fun draw(canvas: Canvas?) {
        val indicatorHeight = dpToPx(context, 4f)// 指示器高度
        // 现在貌似应该去画indicator了
        // 要绘制，首先要确定范围，左上右下
        var top = height - indicatorHeight
        var bottom = height

        Log.d("drawTag", "$indicatorLeft    $indicatorRight   $top     $bottom")

        // 现在只考虑在底下的情况
        var selectedIndicator: Drawable = GradientDrawable()
        selectedIndicator.setBounds(indicatorLeft, top, indicatorRight, bottom)
        DrawableCompat.setTint(selectedIndicator, resources.getColor(ColorManager.selectedTextColor))
        selectedIndicator.draw(canvas!!)

        initIndicator()

        super.draw(canvas)
    }

    private fun initIndicator() {
        Log.d("addTabViewTag", "$childCount")
        if (childCount > 0) {
            if (!inited) {
                inited = true
                val tabView0 = getChildAt(0) as TabView
                tabView0.performClick() // 难道这里在岗添加进去，测量尚未完成？那怎么办,那只能在onDraw里面去执行了
            }
        }
    }

    var inited: Boolean = false
    private var indicatorAnimator: ValueAnimator = ValueAnimator.ofFloat(0f, 1f)
    private var scrollAnimator: ValueAnimator = ValueAnimator.ofFloat(0f, 1f)
    private val tabViewBounds = Rect()
    private val parentBounds = Rect()

    fun updateIndicatorPosition(targetLeft: Int, targetRight: Int) {
        indicatorLeft = targetLeft
        indicatorRight = targetRight
        postInvalidate()//
    }

    /**
     * @param tabView 当前这个子view
     * @param targetLeft
     * @param targetRight
     */
    fun updateIndicatorPositionByAnimator(tabView: TabView, targetLeft: Int, targetRight: Int) {

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

            // 被选中的TabView，让他尽量处于正中间，由于 HorizontalScrollView 已经自动做了边界控制，所以scroll的时候不用担心越界问题
            // 先计算得出tabView的中心位置
            val tabViewCenterX = (tabViewRealLeft + tabViewRealRight) / 2
            // 然后计算出parentBound的中心
            val parentCenterX = (parentBounds.left + parentBounds.right) / 2
            val needToScrollX = -parentCenterX + tabViewCenterX //  差值就是需要滚动的距离

            startScrollAnimator(this, scrolledX, scrolledX + needToScrollX)
        }


        // 把其他的都设置成未选中状态
        for (i in 0 until childCount) {
            val current = getChildAt(i) as TabView
            if (current.hashCode() == tabView.hashCode()) {// 如果是当前被点击的这个，那么就不需要管
                current.setSelectedStatus(true) // 选中状态
            } else {// 如果不是
                current.setSelectedStatus(false)// 非选中状态
            }
        }
    }

    private fun startScrollAnimator(tabLayout: HankTabLayout, from: Int, to: Int) {
        // 应该用动画效果，慢慢scroll过去
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
     * 对外提供方法，添加TabView
     */
    fun addTabView(text: String) {
        val tabView = TabView(context, this)
        val param = LayoutParams(WRAP_CONTENT, WRAP_CONTENT)
        param.setMargins(dpToPx(context, 10f))

        val textView = TextView(context)
        textView.text = text
        textView.gravity = Gravity.CENTER
        textView.setPadding(dpToPx(context, 15f))
        textView.setTextSize(TypedValue.COMPLEX_UNIT_SP, 20f)
        textView.setTextColor(resources.getColor(ColorManager.unselectedTextColor))
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
    private var parent: IndicatorLayout

    constructor(ctx: Context, parent: IndicatorLayout) : super(ctx) {
        init()
        this.parent = parent
    }

    fun setTextView(textView: TextView) {
        titleTextView = textView
        titleTextView.setTextSize(TypedValue.COMPLEX_UNIT_SP, 10f)
        removeAllViews()
        val param = LayoutParams(WRAP_CONTENT, MATCH_PARENT)
        addView(titleTextView, param)

        setOnClickListener {
            parent.updateIndicatorPositionByAnimator(this, left, right)
            parent.parent.mViewPager.currentItem = parent.indexOfChild(this)// 拿到viewPager，然后强制滑动到指定的page
        }
    }

    private fun init() {

    }

    fun setSelectedStatus(selected: Boolean) {
        selectedStatue = selected
        if (selected) {
            titleTextView.setTextColor(resources.getColor(ColorManager.selectedTextColor))
        } else {
            titleTextView.setTextColor(resources.getColor(ColorManager.unselectedTextColor))
        }
    }

}

class ColorManager {
    companion object {
        const val selectedTextColor = R.color.c1
        const val unselectedTextColor = R.color.cf
    }
}
