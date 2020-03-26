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
import androidx.interpolator.view.animation.FastOutSlowInInterpolator
import androidx.viewpager.widget.ViewPager
import androidx.viewpager.widget.ViewPager.SCROLL_STATE_DRAGGING
import com.zhou.studytablayout.R
import com.zhou.studytablayout.util.dpToPx
import com.zhou.studytablayout.util.getFontTypeFace

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
        var textSize: Float = 0f // 字体大小，单位sp
        var textColor: Int = 0 // 字体颜色
        var background: Int = 0 // 背景色
        var textAppearance: Int = 0 // 字体整体风格
        var textTypeface: Typeface? = null // 字体 指定ttf文件   /// TODO ?!!?!?!? 这个无效，原因不明，不过优先级放低，后面看
        var textColorSelected: Int = 0 // 选中之后的字体颜色
        var textGravity: Int = Gravity.CENTER
        var textPaddingLeft: Float = 0f
        var textPaddingRight: Float = 0f
        var textPaddingTop: Float = 0f
        var textPaddingBottom: Float = 0f
    }

    class IndicatorAttrs {
        var indicatorColor: Int = 0
        /**
         * 支持 Gravity.BOTTOM 和 Gravity.TOP
         * 设置其他属性，则默认为Gravity.BOTTOM
         */
        var locationGravity: Int = 0

        /**
         * 长度模式，TabView长度的倍数，或者 定死长度，还是取 TextView长度的倍数
         */
        var widthMode: WidthMode? = null

        /**
         * indicator的长度是TabView百分比长度，
         * 如果值是1，那就是等长
         */
        var widthPercentages: Float = 0.5f

        /**
         * 精确长度，只有在width模式为EXACT的时候有效, 单位dp
         */
        var exactWidth: Int = 0

        enum class WidthMode {
            RELATIVE_TAB_VIEW,// 取相对于TabView的长度的百分比(没有哪个傻缺会超过1，对吧?我就不做限制了)
            EXACT// 指定长度精确值
        }

        var height: Int = 0 // 高度，单位dp

        /**
         * 对齐模式
         */
        var alignMode: AlignMode? = null //

        enum class AlignMode {
            LEFT, // 靠左
            CENTER,// 居中
            RIGHT // 靠右
        }

        var margin: Int = 0 // 根据 locationGravity 决定，如果是放在底部，就是与底部的距离
    }

    private lateinit var indicatorLayout: SlidingIndicatorLayout
    private var mCurrentPosition = 0
    private var scrollState = 0
    lateinit var mViewPager: ViewPager

    var tabViewAttrs: TabViewAttrs = TabViewAttrs()
    var indicatorAttrs: IndicatorAttrs = IndicatorAttrs()

    private fun initIndicatorAttrs() {
        indicatorAttrs.run {
            indicatorColor = R.color.c1
            locationGravity = Gravity.BOTTOM
            margin = dpToPx(context, 5f)
            height = dpToPx(context, 4f)
            widthPercentages = 0.8f
            widthMode = IndicatorAttrs.WidthMode.EXACT // 默认就是与 tabView等长
            exactWidth = dpToPx(context, 20f)// 如果设定是精确模式，那么
            alignMode = IndicatorAttrs.AlignMode.CENTER //  默认居中
        }
    }

    private fun initTabViewAttrs() {
        tabViewAttrs.run {
            textSize = 12f
            textColor = R.color.cf
            textColorSelected = R.color.c1
            background = R.color.c4
            textAppearance = 0
            textTypeface = getFontTypeFace(context)
            textPaddingLeft = 0f
            textPaddingRight = 0f
            textPaddingTop = 5f
            textPaddingBottom = 5f
        }
    }

    private fun init() {
        isHorizontalScrollBarEnabled = false  // 禁用滚动横条
        overScrollMode = View.OVER_SCROLL_NEVER // 禁用按下的水波效果

        indicatorLayout = SlidingIndicatorLayout(context, this)
        val layoutParams = LinearLayout.LayoutParams(WRAP_CONTENT, MATCH_PARENT)
        addView(indicatorLayout, layoutParams)

        initTabViewAttrs()
        initIndicatorAttrs()
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

    private var indicatorLeft = 0
    private var indicatorRight = 0
    var parent: GreenTabLayout
    private var inited: Boolean = false
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
        var top: Int
        var bottom: Int
        var margin: Int = parent.indicatorAttrs.margin
        var indicatorHeight: Int = parent.indicatorAttrs.height

        // 处理属性 indicatorAttrs.locationGravity --> indicator的Gravity
        when (parent.indicatorAttrs.locationGravity) {
            Gravity.BOTTOM -> {
                top = height - indicatorHeight - margin
                bottom = height - margin
            }
            Gravity.TOP -> {
                top = 0 + margin
                bottom = indicatorHeight + margin
            }
            else -> {
                throw RuntimeException("Indicator LocationGravity设置错误，仅支持 Gravity.BOTTOM和Gravity.TOP")
            }
        }

        var selectedIndicator: Drawable = GradientDrawable()//  用一个drawable
        val tabViewWidth = indicatorRight - indicatorLeft
        var indicatorWidth = 0

        // 处理属性 widthMode
        when (parent.indicatorAttrs.widthMode) {
            GreenTabLayout.IndicatorAttrs.WidthMode.RELATIVE_TAB_VIEW -> {
                indicatorWidth =
                    ((indicatorRight - indicatorLeft) * parent.indicatorAttrs.widthPercentages).toInt()
            }
            GreenTabLayout.IndicatorAttrs.WidthMode.EXACT -> {
                indicatorWidth = parent.indicatorAttrs.exactWidth
            }
        }

        val dif = tabViewWidth - indicatorWidth
        var centerX = 0
        // 处理属性 alignMode
        when (parent.indicatorAttrs.alignMode) {
            GreenTabLayout.IndicatorAttrs.AlignMode.LEFT -> {
                centerX = ((indicatorLeft + indicatorRight - dif) / 2)
            }
            GreenTabLayout.IndicatorAttrs.AlignMode.CENTER -> {
                centerX =
                    ((indicatorLeft + indicatorRight) / 2) // 这个就是中心位置
            }
            GreenTabLayout.IndicatorAttrs.AlignMode.RIGHT -> {
                centerX = ((indicatorLeft + indicatorRight + dif) / 2)
            }
        }

        // 可以开始绘制
        selectedIndicator.run {
            setBounds(
                (centerX - indicatorWidth / 2),
                top,
                (centerX + indicatorWidth / 2),
                bottom
            )// 规定它的边界

            DrawableCompat.setTint(
                this,
                resources.getColor(parent.indicatorAttrs.indicatorColor)
            )// 规定它的颜色
            draw(canvas!!)// 然后绘制到画布上
        }

        initIndicator()// 刚开始的时候，indicatorLeft和indicatorRight都是0，所以需要通过触发一次tabView的click事件来绘制
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
        parent.parent.tabViewAttrs.run {
            titleTextView.setBackgroundColor(resources.getColor(background))
            titleTextView.setTextSize(TypedValue.COMPLEX_UNIT_SP, textSize)
            titleTextView.setTypeface(textTypeface, Typeface.NORMAL)
            titleTextView.setTextColor(resources.getColor(textColor))
            titleTextView.setTextAppearance(context, textAppearance)
            titleTextView.gravity = textGravity

            titleTextView.setPadding(
                dpToPx(context, textPaddingLeft),
                dpToPx(context, textPaddingTop),
                dpToPx(context, textPaddingRight),
                dpToPx(context, textPaddingBottom)
            )

        }

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

        parent.parent.tabViewAttrs.run {
            if (selected) {
                titleTextView.setTextColor(resources.getColor(textColorSelected))
            } else {
                titleTextView.setTextColor(resources.getColor(textColor))
            }
        }
    }

}

