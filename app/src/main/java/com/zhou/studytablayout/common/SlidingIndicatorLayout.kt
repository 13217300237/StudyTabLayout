package com.zhou.studytablayout.common

import android.animation.ValueAnimator
import android.content.Context
import android.graphics.Canvas
import android.graphics.Rect
import android.graphics.drawable.Drawable
import android.graphics.drawable.GradientDrawable
import android.view.Gravity
import android.view.ViewGroup.LayoutParams.MATCH_PARENT
import android.view.ViewGroup.LayoutParams.WRAP_CONTENT
import android.widget.LinearLayout
import androidx.core.graphics.drawable.DrawableCompat
import androidx.interpolator.view.animation.FastOutSlowInInterpolator
import com.zhou.studytablayout.util.dpToPx
import kotlin.math.roundToInt

/**
 * 中间层 可滚动的 线性布局
 */
class SlidingIndicatorLayout(ctx: Context, var parent: GreenTabLayout) : LinearLayout(ctx) {

    private var indicatorLeft = 0
    private var indicatorRight = 0
    private var positionOffset = 0f
    private var inited: Boolean = false
    private var scrollAnimator: ValueAnimator = ValueAnimator.ofFloat(0f, 1f)
    private val tabViewBounds = Rect()
    private val parentBounds = Rect()

    init {
        init()
    }

    private fun init() {
        setWillNotDraw(false) // 如果不这么做，它自身的draw方法就不会调用
        gravity = Gravity.CENTER_VERTICAL
    }

    override fun onLayout(changed: Boolean, l: Int, t: Int, r: Int, b: Int) {
        super.onLayout(changed, l, t, r, b)
        if (!inited)
            parent.scrollTabLayout(0, 0f)//
    }

    /**
     * 作为一个viewGroup，有可能它不会执行自身的draw方法，这里有一个值去控制  setWillNotDraw
     */
    override fun draw(canvas: Canvas?) {
        val top: Int
        val bottom: Int
        val margin: Int = parent.indicatorAttrs.indicatorMargin.roundToInt()
        val indicatorHeight: Int = parent.indicatorAttrs.indicatorHeight.roundToInt()

        // 处理属性 indicatorAttrs.locationGravity --> indicator的Gravity
        when (parent.indicatorAttrs.indicatorLocationGravity) {
            GreenTabLayout.IndicatorAttrs.LocationGravity.BOTTOM -> {
                top = height - indicatorHeight - margin
                bottom = height - margin
            }
            GreenTabLayout.IndicatorAttrs.LocationGravity.TOP -> {
                top = 0 + margin
                bottom = indicatorHeight + margin
            }
        }


        val selectedIndicator: Drawable
        if (null != parent.indicatorAttrs.indicatorDrawable) {// 如果drawable是空
            selectedIndicator = parent.indicatorAttrs.indicatorDrawable!!
        } else { // 那就涂颜色
            selectedIndicator = GradientDrawable()
            DrawableCompat.setTint(
                selectedIndicator,
                parent.indicatorAttrs.indicatorColor
            )// 规定它的颜色
        }

        val tabViewWidth = indicatorRight - indicatorLeft
        var indicatorWidth = 0f

        // 处理属性 widthMode
        when (parent.indicatorAttrs.indicatorWidthMode) {
            GreenTabLayout.IndicatorAttrs.WidthMode.RELATIVE_TAB_VIEW -> {
                indicatorWidth =
                    ((indicatorRight - indicatorLeft) * parent.indicatorAttrs.indicatorWidthPercentages)
            }
            GreenTabLayout.IndicatorAttrs.WidthMode.EXACT -> {
                indicatorWidth = parent.indicatorAttrs.indicatorExactWidth
            }
        }

        val dif = tabViewWidth - indicatorWidth
        var centerX = 0
        // 处理属性 alignMode
        when (parent.indicatorAttrs.indicatorAlignMode) {
            GreenTabLayout.IndicatorAttrs.AlignMode.LEFT -> {
                centerX = (((indicatorLeft + indicatorRight - dif) / 2).toInt())
            }
            GreenTabLayout.IndicatorAttrs.AlignMode.CENTER -> {
                centerX =
                    ((indicatorLeft + indicatorRight) / 2) // 这个就是中心位置
            }
            GreenTabLayout.IndicatorAttrs.AlignMode.RIGHT -> {
                centerX = (((indicatorLeft + indicatorRight + dif) / 2).toInt())
            }
        }

        // 是否开启 indicator的弹性拉伸效果
        // 计算临界值
        val baseMultiple = parent.indicatorAttrs.indicatorElasticBaseMultiple // 基础倍数,决定拉伸的最大程度
//        val basePositionOffsetCriticalValue = 0.5f // positionOffset的中值
        val indicatorCriticalValue = 1 + baseMultiple
        // indicatorCriticalValue的计算方法很有参考价值，所以详细记录下来
        // positionOffset 是 从 0 慢慢变成1的，分为两段，一段从0->0.5 ,一段从0.5->1
        // 我要求，前半段的ratio最终值，要和后半段的初始值相等，这样才能无缝衔接
        //  前半段的ratio最终值 = 1（原始倍率）+ 0.5 * baseMultiple（拉伸倍数，数值越大，拉伸越明显）
        //  后半段的ratio值 = indicatorCriticalValue（临界值） - 0.5f * baseMultiple
        // 两者必须相等，所以算出 indicatorCriticalValue（临界值） = 1（原始倍率）+0.5 * baseMultiple + 0.5 * baseMultiple
        // 最终， indicatorCriticalValue（临界值） = 1+ baseMultiple
        val ratio =
            if (parent.indicatorAttrs.indicatorElastic) {
                when {
                    positionOffset >= 0 && positionOffset < 0.5 -> {
                        1 + positionOffset * baseMultiple // 拉伸长度
                    }
                    else -> {// 如果到了下半段，当offset越过中值之后ratio的值
                        indicatorCriticalValue - positionOffset * baseMultiple
                    }
                }
            } else 1f
        // 可以开始绘制
        selectedIndicator.run {
            setBounds(
                ((centerX - indicatorWidth * ratio / 2).toInt()),
                top,
                ((centerX + indicatorWidth * ratio / 2).toInt()),
                bottom
            )// 规定它的边界
            draw(canvas!!)// 然后绘制到画布上
        }

        initIndicator()// 刚开始的时候，indicatorLeft和indicatorRight都是0，所以需要通过触发一次tabView的click事件来绘制
        super.draw(canvas)
    }

    private fun initIndicator() {
        if (childCount > 0) {
            if (!inited) {
                inited = true
                val tabView0 = getChildAt(0) as GreenTabView
                tabView0.performClick() // 难道这里在岗添加进去，测量尚未完成？那怎么办,那只能在onDraw里面去执行了
            }
        }
    }

    fun updateIndicatorPosition(targetLeft: Int, targetRight: Int, positionOffset_: Float) {
        indicatorLeft = targetLeft
        indicatorRight = targetRight
        positionOffset = positionOffset_
        postInvalidate()
    }


    fun updateIndicatorPositionByAnimator(position: Int) {
        val view = getChildAt(position)
        if (view != null) {
            val tabView = view as GreenTabView
            updateIndicatorPositionByAnimator(tabView)
        }
    }

    /**
     * 用动画平滑更新indicator的位置
     * @param tabView 当前这个子view
     */
    private fun updateIndicatorPositionByAnimator(tabView: GreenTabView) {
        parent.run {
            // 处理最外层布局( HankTabLayout )的滑动
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

        resetTabViewsStatue(tabView)
    }

    private fun resetTabViewsStatue(tabView: GreenTabView) {
        for (i in 0 until childCount) {// 把其他的 TabView 都设置成未选中状态
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
        if (scrollAnimator.isRunning) scrollAnimator.cancel()
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
     * 添加TabView，内部创建 TextView
     */
    fun addTabView(text: String) {
        val textView = GreenTextView(context)
        addTabView(text, textView)
    }

    /**
     *
     * 添加TabView，使用参数传入的TextView
     * @param textView
     */
    fun addTabView(text: String, textView: GreenTextView) {
        val tabView = GreenTabView(context, this)
        val margin = dpToPx(context, 10f)
        tabView.setPadding(0, margin, 0, margin)
        val param =
            LayoutParams(WRAP_CONTENT, MATCH_PARENT)
        param.setMargins(margin, 0, margin, 0)
        textView.text = text
        tabView.setTextView(textView)
        addView(tabView, param)
    }

    fun resetTabViewsStatueByAnimator(tabView: GreenTabView) {
        for (i in 0 until childCount) {// 把其他的 TabView 都设置成未选中状态
            val current = getChildAt(i) as GreenTabView
            if (current.hashCode() == tabView.hashCode()) {// 如果是当前被点击的这个，那么就不需要管
                current.setSelectedStatusByAnimator(true) // 选中状态
            } else {// 如果不是
                current.setSelectedStatusByAnimator(false)// 非选中状态
            }
        }
    }
}