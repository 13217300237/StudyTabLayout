package com.zhou.studytablayout.common

import android.content.Context
import android.content.res.TypedArray
import android.graphics.Typeface
import android.graphics.drawable.Drawable
import android.util.AttributeSet
import android.util.Log
import android.util.TypedValue
import android.view.View
import android.view.ViewGroup.LayoutParams.MATCH_PARENT
import android.view.ViewGroup.LayoutParams.WRAP_CONTENT
import android.widget.HorizontalScrollView
import android.widget.LinearLayout
import androidx.core.view.get
import androidx.viewpager.widget.ViewPager
import com.zhou.studytablayout.R
import com.zhou.studytablayout.util.dpToPx
import com.zhou.studytablayout.util.getFontTypeFace
import com.zhou.studytablayout.util.sp2px

/**
 * 绿色版
 *
 * @author Hank.Zhou
 *
 */
class GreenTabLayout : HorizontalScrollView, ViewPager.OnPageChangeListener {
    constructor(ctx: Context) : super(ctx) {
        init(null)
    }

    constructor(ctx: Context, attributes: AttributeSet) : super(ctx, attributes) {
        init(attributes)
    }

    constructor(context: Context, attrs: AttributeSet?, defStyleAttr: Int) : super(
        context,
        attrs,
        defStyleAttr
    ) {
        init(attrs)
    }

    // 自定义属性相关
    // TabView相关属性
    class TabViewAttrs {
        var tabViewDynamicSizeWhenScrolling: Boolean = true // 是否支持 滚动ViewPager时，tabView的字体大小动态变化
        var tabViewTextSize: Float = 0f // 字体大小，单位sp
        var tabViewTextSizeSelected: Float = 0f // 选中的字体大小
        var tabViewTextColor: Int = 0 // 字体颜色
        var tabViewBackgroundColor: Int = 0 // 背景色
        var tabViewTextTypeface: Typeface? = null // 字体 指定ttf文件   /// TODO 这个无效，原因不明，不过优先级放低，后面看
        var tabViewTextColorSelected: Int = 0 // 选中之后的字体颜色
        var tabViewTextPaddingLeft: Float = 0f
        var tabViewTextPaddingRight: Float = 0f
        var tabViewTextPaddingTop: Float = 0f
        var tabViewTextPaddingBottom: Float = 0f
    }

    class IndicatorAttrs {
        var indicatorColor: Int = 0
        /**
         * 支持 Gravity.BOTTOM 和 Gravity.TOP
         * 设置其他属性，则默认为Gravity.BOTTOM
         */
        var indicatorLocationGravity: LocationGravity = LocationGravity.BOTTOM

        enum class LocationGravity(v: Int) {
            TOP(0),
            BOTTOM(1)
        }

        /**
         * 长度模式，TabView长度的倍数，或者 定死长度，还是取 TextView长度的倍数
         */
        var indicatorWidthMode: WidthMode? = null

        enum class WidthMode(v: Int) {
            RELATIVE_TAB_VIEW(0),// 取相对于TabView的长度的百分比(没有哪个傻缺会超过1，对吧?我就不做限制了)
            EXACT(1)// 指定长度精确值
        }

        /**
         * indicator的长度是TabView百分比长度，
         * 如果值是1，那就是等长indicatorExactWidth
         */
        var indicatorWidthPercentages: Float = 0.5f

        /**
         * 精确长度，只有在width模式为EXACT的时候有效, 单位dp
         */
        var indicatorExactWidth: Float = 0f

        var indicatorHeight: Float = 0f // 高度，单位dp

        /**
         * 对齐模式
         */
        var indicatorAlignMode: AlignMode? = AlignMode.CENTER //

        enum class AlignMode(v: Int) {
            LEFT(0), // 靠左
            CENTER(1),// 居中
            RIGHT(2) // 靠右
        }

        var indicatorMargin: Float = 0f // 根据 locationGravity 决定，如果是放在底部，就是与底部的距离

        var indicatorDrawable: Drawable? = null // 默认drawable

        /**
         *  indicator的弹性效果
         */
        var indicatorElastic: Boolean = false
        /**
         *  拉伸的基础倍数，倍数越大，拉伸效果越明显
         */
        var indicatorElasticBaseMultiple = 1f //
    }

    private lateinit var indicatorLayout: SlidingIndicatorLayout
    lateinit var mViewPager: ViewPager

    var tabViewAttrs: TabViewAttrs = TabViewAttrs()
    var indicatorAttrs: IndicatorAttrs = IndicatorAttrs()

    private var currentTabViewTextSizeRealtime = 0f
    private var nextTabViewTextSizeRealtime = 0f


    private fun init(attrs: AttributeSet?) {
        isHorizontalScrollBarEnabled = false  // 禁用滚动横条
        overScrollMode = View.OVER_SCROLL_NEVER // 禁用按下的水波效果

        indicatorLayout = SlidingIndicatorLayout(context, this)
        val layoutParams = LinearLayout.LayoutParams(WRAP_CONTENT, MATCH_PARENT)
        addView(indicatorLayout, layoutParams)

        dealAttributeSet(attrs)
    }

    private fun dealAttributeSet(attrs: AttributeSet?) {
        if (attrs == null) return
        var a: TypedArray? = null
        try {
            a = context.obtainStyledAttributes(attrs, R.styleable.GreenTabLayout)
            tabViewAttrs.run {
                tabViewTextTypeface = getFontTypeFace(context)

                tabViewTextSize =
                    a.getDimension(
                        R.styleable.GreenTabLayout_tabViewTextSize,
                        sp2px(context, 12f).toFloat()
                    )
                tabViewTextSizeSelected =
                    a.getDimension(
                        R.styleable.GreenTabLayout_tabViewTextSizeSelected,
                        sp2px(context, 15f).toFloat()
                    )
                tabViewTextColor = a.getColor(
                    R.styleable.GreenTabLayout_tabViewTextColor,
                    resources.getColor(R.color.c1)
                )
                tabViewTextColorSelected = a.getColor(
                    R.styleable.GreenTabLayout_tabViewTextColorSelected,
                    resources.getColor(R.color.c3)
                )

                tabViewBackgroundColor = a.getColor(
                    R.styleable.GreenTabLayout_tabViewBackgroundColor,
                    resources.getColor(R.color.c_11)
                )

                tabViewTextPaddingLeft =
                    a.getDimension(R.styleable.GreenTabLayout_tabViewTextPaddingLeft, 5f)
                tabViewTextPaddingRight =
                    a.getDimension(R.styleable.GreenTabLayout_tabViewTextPaddingRight, 5f)
                tabViewTextPaddingTop =
                    a.getDimension(R.styleable.GreenTabLayout_tabViewTextPaddingTop, 5f)
                tabViewTextPaddingBottom =
                    a.getDimension(R.styleable.GreenTabLayout_tabViewTextPaddingBottom, 5f)

                tabViewDynamicSizeWhenScrolling =
                    a.getBoolean(R.styleable.GreenTabLayout_tabViewDynamicSizeWhenScrolling, true)
            }

            indicatorAttrs.run {
                indicatorColor = a.getColor(
                    R.styleable.GreenTabLayout_indicatorColor,
                    resources.getColor(R.color.c3)
                )
                indicatorMargin = a.getDimension(R.styleable.GreenTabLayout_indicatorMargin, 0f)
                indicatorDrawable = a.getDrawable(R.styleable.GreenTabLayout_indicatorDrawable)
                indicatorHeight =
                    a.getDimension(
                        R.styleable.GreenTabLayout_indicatorHeight,
                        dpToPx(context, 4f).toFloat()
                    )
                indicatorWidthPercentages =
                    a.getFloat(R.styleable.GreenTabLayout_indicatorWidthPercentages, 1f)
                indicatorExactWidth =
                    a.getDimension(
                        R.styleable.GreenTabLayout_indicatorExactWidth,
                        dpToPx(context, 20f).toFloat()
                    )

                // 处理枚举 LocationGravity
                val locationGravity = a.getInteger(
                    R.styleable.GreenTabLayout_indicatorLocationGravity,
                    IndicatorAttrs.LocationGravity.BOTTOM.ordinal
                )
                indicatorLocationGravity =
                    when (locationGravity) {
                        IndicatorAttrs.LocationGravity.TOP.ordinal -> {
                            IndicatorAttrs.LocationGravity.TOP
                        }
                        else -> {
                            IndicatorAttrs.LocationGravity.BOTTOM
                        }
                    }

                // 处理枚举 widthModeW
                val widthMode = a.getInteger(
                    R.styleable.GreenTabLayout_indicatorWidthMode,
                    IndicatorAttrs.WidthMode.RELATIVE_TAB_VIEW.ordinal
                )
                indicatorWidthMode =
                    when (widthMode) {
                        IndicatorAttrs.WidthMode.RELATIVE_TAB_VIEW.ordinal -> {
                            IndicatorAttrs.WidthMode.RELATIVE_TAB_VIEW
                        }
                        else -> {
                            IndicatorAttrs.WidthMode.EXACT
                        }
                    }

                // 处理枚举 AlignMode
                val alignMode = a.getInteger(
                    R.styleable.GreenTabLayout_indicatorAlignMode,
                    IndicatorAttrs.AlignMode.CENTER.ordinal
                )
                indicatorAlignMode = when (alignMode) {
                    IndicatorAttrs.AlignMode.LEFT.ordinal -> {
                        IndicatorAttrs.AlignMode.LEFT
                    }
                    IndicatorAttrs.AlignMode.CENTER.ordinal -> {
                        IndicatorAttrs.AlignMode.CENTER
                    }
                    else -> {
                        IndicatorAttrs.AlignMode.RIGHT
                    }
                }

                indicatorElastic = a.getBoolean(R.styleable.GreenTabLayout_indicatorElastic, true)
                indicatorElasticBaseMultiple =
                    a.getFloat(R.styleable.GreenTabLayout_indicatorElasticBaseMultiple, 1f)

            }
        } finally {
            a?.recycle()
        }

    }

    private fun addTabView(text: String) {
        indicatorLayout.addTabView(text)
    }

    private fun addTabView(text: String, textView: GreenTextView) {
        indicatorLayout.addTabView(text, textView)
    }

    fun setupWithViewPager(viewPager: ViewPager) {
        setupWithViewPager(viewPager, GreenTextView(context))
    }

    /**
     * 支持使用自定义的TextView 来 编辑 文本的UI表现，比如动态效果
     * 要求，第二个参数，t 必须是GreenTextView的子类
     *
     * 需要特别注意的是，一旦使用了 GreenTextView 特殊效果，原本的字体颜色可能会失效
     * 这是由TextView类的Paint特性决定的，shader的优先级要大于setTextColor
     *
     * @param viewPager
     * @param t 传null表示使用默认的TextView，不附带特效。 传GreenTextView的子类表示，使用该子类的特效
     */
    fun <T : GreenTextView> setupWithViewPager(viewPager: ViewPager, t: T?) {
        this.mViewPager = viewPager
        viewPager.addOnPageChangeListener(this)
        val adapter = viewPager.adapter ?: return
        val count = adapter.count // 栏目数量
        for (i in 0 until count) {
            val pageTitle = adapter.getPageTitle(i)
            var newInstance = GreenTextView(context)// 常规情况，使用默认的GreenTextView
            if (t != null) {// 如果传入了具体的类型，那么就反射创建
                // 现在知道了你的类型，那么现在我想要用你的类型反射创建出一个对象
                val constructor =
                    t::class.java.getConstructor(Context::class.java)
                newInstance = constructor.newInstance(context)
            }

            addTabView(pageTitle.toString(), newInstance)
        }
    }

    private var settingFlag = false // 用于方法 dealAttrTabViewDynamicSizeWhenScrolling
    /**
     *  处理属性 tabViewDynamicSizeWhenScrolling
     */
    private fun dealAttrTabViewDynamicSizeWhenScrolling(
        positionOffset: Float,
        currentTabView: GreenTabView,
        nextTabView: GreenTabView
    ) {
        if (tabViewAttrs.tabViewDynamicSizeWhenScrolling) {
            if (positionOffset != 0f) {
                // 在这里，让当前字体变小，next的字体变大
                val diffSize =
                    tabViewAttrs.tabViewTextSizeSelected - tabViewAttrs.tabViewTextSize
                when (mScrollState) {
                    ViewPager.SCROLL_STATE_DRAGGING -> {
                        currentTabViewTextSizeRealtime =
                            tabViewAttrs.tabViewTextSizeSelected - diffSize * positionOffset
                        currentTabView.titleTextView.setTextSize(
                            TypedValue.COMPLEX_UNIT_PX,
                            currentTabViewTextSizeRealtime
                        )

                        nextTabViewTextSizeRealtime =
                            tabViewAttrs.tabViewTextSize + diffSize * positionOffset
                        nextTabView.titleTextView.setTextSize(
                            TypedValue.COMPLEX_UNIT_PX,
                            nextTabViewTextSizeRealtime
                        )

                        settingFlag = false
                    }
                    ViewPager.SCROLL_STATE_SETTLING -> {
                        // OK，定位到问题，在 mScrollState 为setting状态时，positionOffset的变化没有 dragging时那么细致
                        // 只要不处理 SETTING下的字体大小变化，也可以达成效果
                        if (!settingFlag)
                            indicatorLayout.resetTabViewsStatueByAnimator(indicatorLayout[mCurrentPosition] as GreenTabView)
                        settingFlag = true
                    }
                }
            }
        }
    }


    private var settingFlag2 = false // 用于方法dealTextShaderWhenScrolling
    /**
     * 处理着色器的问题
     */
    private fun dealTextShaderWhenScrolling(
        positionOffset: Float,
        currentTabView: GreenTabView,
        nextTabView: GreenTabView
    ) {
        // 當前tabView和nextTabView都要更新shader
        when (mScrollState) {
            ViewPager.SCROLL_STATE_DRAGGING -> {
                // 只有在拖拽状态下才允许变化shader

                // 判断当前是否到达了最左端
                if (mCurrentPosition == 0 && positionOffset == 0f) {
                    Log.d("dealTextShader", "已经到达了最左端")
                } else {
                    currentTabView.updateTextViewShader(positionOffset, mCurrentPosition)
                    nextTabView.updateTextViewShader(positionOffset, mCurrentPosition)
                }
                settingFlag2 = false
                mSettingPositionOffset = -1f
            }
            ViewPager.SCROLL_STATE_SETTLING -> {
                // OK，定位到问题，在 mScrollState 为setting状态时，positionOffset的变化没有 dragging时那么细致
                // 这里能不能确定回弹的方向？
                var direction = 0
                if (mSettingPositionOffset == -1f) {//  如果是初始值-1
                    mSettingPositionOffset = positionOffset // 那就赋值
                } else {
                    direction = if (mSettingPositionOffset > positionOffset) {
                        Log.d("mSettingPositionOffset", "<<<")
                        -1 //向左回弹
                    } else {
                        Log.d("mSettingPositionOffset", ">>>")
                        1  // 向右回弹
                    }
                    Log.d("directionTag", "$direction")

                    // 只要不处理 SETTING下的字体大小变化，也可以达成效果
                    if (!settingFlag2) {
                        currentTabView.notifySetting(positionOffset, mCurrentPosition, direction)
                        nextTabView.notifySetting(positionOffset, mCurrentPosition, direction)
                    }
                    settingFlag2 = true
                }
                // setting状态可能有多次触发，抓住第一次出发的时机，用属性动画矫正特效
            }
        }
    }

    private var mSettingPositionOffset = -1f

    /**
     * 这段代码值得研究，无论左右，都是position+1即可
     *
     */
    fun scrollTabLayout(position: Int, positionOffset: Float) {
        // 如果是向左, 就用当前的tabView滑动到左边一个tabView
        val currentTabView = indicatorLayout.getChildAt(position) as GreenTabView
        val currentLeft = currentTabView.left
        val currentRight = currentTabView.right

        val nextTabView = indicatorLayout.getChildAt(position + 1) // 目标TabView
        if (nextTabView != null) {
            val nextGreenTabView = nextTabView as GreenTabView
            dealAttrTabViewDynamicSizeWhenScrolling(
                positionOffset,
                currentTabView,
                nextGreenTabView
            )

            //  处理字体shader（着色器）的问题
            dealTextShaderWhenScrolling(
                positionOffset,
                currentTabView,
                nextGreenTabView
            )

            // 滚动tabView，使得被选中的TabView尽量处于正中
            val nextLeft = nextTabView.left
            val nextRight = nextTabView.right

            val leftDiff = nextLeft - currentLeft
            val rightDiff = nextRight - currentRight

            indicatorLayout.updateIndicatorPosition(
                currentLeft + (leftDiff * positionOffset).toInt(),
                currentRight + (rightDiff * positionOffset).toInt(),
                positionOffset
            )
        }
    }

    private var mScrollState: Int = ViewPager.SCROLL_STATE_IDLE
    /**
     * @see ViewPager#SCROLL_STATE_IDLE
     * @see ViewPager#SCROLL_STATE_DRAGGING
     * @see ViewPager#SCROLL_STATE_SETTLING
     */
    override fun onPageScrollStateChanged(state: Int) {
        mScrollState = state
    }

    override fun onPageScrolled(position: Int, positionOffset: Float, positionOffsetPixels: Int) {
        Log.d("positionOffset", "$positionOffset")
        scrollTabLayout(position, positionOffset)
    }

    var mCurrentPosition = 0
    override fun onPageSelected(position: Int) {
        mCurrentPosition = position
        indicatorLayout.updateIndicatorPositionByAnimator(mCurrentPosition)//也许这里不应该再去更新indicator的位置，而是应该直接滚动最外层布局
    }
}
