package com.zhou.studytablayout.ui.custom

import android.animation.ValueAnimator
import android.content.Context
import android.graphics.Canvas
import android.graphics.Paint
import android.graphics.Rect
import android.os.Build
import android.util.AttributeSet
import android.util.Log
import android.view.View
import android.view.ViewGroup
import android.widget.HorizontalScrollView
import android.widget.LinearLayout
import android.widget.TextView
import androidx.annotation.RequiresApi
import androidx.interpolator.view.animation.FastOutSlowInInterpolator
import com.zhou.studytablayout.R
import com.zhou.studytablayout.util.dpToPx
import kotlin.properties.Delegates


/**
 * 第一阶段，作为一个容器，你可以addTab，然后也可以左右滑动
 * 第二阶段，绘制一个indicator横条, 先绘制出来再说，就放在容器底部
 * 第三阶段，让indicator横条宽度等于TextView的宽度，并且始终处于第一个TextView的正下方居中对齐
 * 第四阶段，让indicator可以随着title的点击而滑动到被点击的title正下方居中对齐
 * 第五阶段，当被点击的title的边界超出了屏幕时，滚动本体，让该title处于屏幕中央
 */
class MyTabLayout : HorizontalScrollView {
    constructor(context: Context?) : super(context) {
        init()
    }

    constructor(context: Context?, attrs: AttributeSet?) : super(context, attrs) {
        init()
    }

    constructor(context: Context?, attrs: AttributeSet?, defStyleAttr: Int) : super(
        context,
        attrs,
        defStyleAttr
    ) {
        init()
    }

    @RequiresApi(Build.VERSION_CODES.LOLLIPOP)
    constructor(
        context: Context?,
        attrs: AttributeSet?,
        defStyleAttr: Int,
        defStyleRes: Int
    ) : super(
        context,
        attrs,
        defStyleAttr,
        defStyleRes
    ) {
        init()
    }

    private fun init() {
        contentLayout = LinearLayout(context)
        contentLayout.orientation = LinearLayout.HORIZONTAL
        contentLayout.layoutParams = LinearLayout.LayoutParams(
            ViewGroup.LayoutParams.WRAP_CONTENT,
            ViewGroup.LayoutParams.MATCH_PARENT
        )
        addView(contentLayout)
        isHorizontalScrollBarEnabled = false
        overScrollMode = View.OVER_SCROLL_NEVER

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            setOnScrollChangeListener { _, scrollX, _, _, _ ->
                currentScrollPosition = scrollX
                postInvalidate()
            }
        }
    }

    private lateinit var contentLayout: LinearLayout// HorizontalScrollView 它作为一个scrollView，只能有一个子view，所以我这里要给一个容器把内容都装进去

    // 所以说，addTab的时候必须有
    fun addTab(tv: TextView?, position: Int, layoutParam: LinearLayout.LayoutParams) {
        contentLayout.addView(tv, position, layoutParam)
        tv?.setOnClickListener {
            updateIndicatorPosition(it.left, it.right)
            val rect = Rect()
            it.getHitRect(rect)
            Log.d("getHitRect", "${rect.left} | ${rect.right}")
            //  应该可以拿到父容器的rect范围
            Log.d("getHitRect", "${selfRect.left} | ${selfRect.right}")
//            if (!selfRect.contains(rect)) {// 如果你不在我的范围之内
//                // 那我就必须滚动自身来让你回到屏幕正中央
//                // 需要滚动多少呢？
//                val needToScroll =
//                    selfRect.right / 2 + (it.right - it.left) / 2 - (selfRect.right - rect.right) / 2
//                scrollBy(-needToScroll, 0)
//            }
            scrollBy(-100, 0)

        }
    }

    private lateinit var indicatorPaint: Paint
    private var indicatorColor by Delegates.notNull<Int>()
    private var indicatorHeightDp = 10f
    private var currentScrollPosition = 0

    var mIndicatorLeft: Int = 0
    var mIndicatorRight: Int = 0

    override fun onDraw(canvas: Canvas?) {
        super.onDraw(canvas)
        indicatorPaint = Paint()
        indicatorColor = resources.getColor(R.color.c2)
        indicatorPaint.color = indicatorColor

        // 现在你要绘制在底部，要计算整个的高度
        val thisLeft = mIndicatorLeft + currentScrollPosition
        val thisTop = top + measuredHeight - dpToPx(
            context,
            indicatorHeightDp
        )
        val thisRight = mIndicatorRight + currentScrollPosition
        val thisBottom = thisTop + dpToPx(
            context,
            indicatorHeightDp
        )
        val rect = Rect(thisLeft, thisTop, thisRight, thisBottom)
        Log.d("rectTag", "${rect.left} : ${rect.right} | width = ${rect.width()}")
        canvas?.drawRect(rect, indicatorPaint)

        if (!inited) {
            val v0 = contentLayout.getChildAt(0)
            val v0Left = v0.left
            val v0Right = v0.right
            mIndicatorLeft = v0Left
            mIndicatorRight = v0Right
            if (!inited) {
                inited = true
                postInvalidate()
            }
            inited = true
        }

        //
        this.getHitRect(selfRect)
    }

    val selfRect = Rect()

    var mIndicatorAnimator: ValueAnimator? = null
    private var inited = false

    //  提供一个函数，对齐TextView标题栏，让 indicator 对齐标题栏的左右
    /**
     * 更新指示器的位置
     * @param left  textView的left
     * @param right textView的right
     */
    private fun updateIndicatorPosition(left: Int, right: Int) {
        mIndicatorAnimator?.cancel()// 开始一个新的动画之前，要先停止原先的动画

        val viewWidth = right - left
        if (mIndicatorLeft == 0 && mIndicatorRight == 0) {// 如果都没有赋予初始值
            mIndicatorRight = mIndicatorLeft + viewWidth// 那就强行赋值
        }

        val oldIndicatorLeft = mIndicatorLeft
        val oldIndicatorRight = mIndicatorRight

        val targetIndicatorLeft = left - currentScrollPosition
        val targetIndicatorRight = right - currentScrollPosition

        // 算出left的差距
        val diff = targetIndicatorLeft - oldIndicatorLeft // 差值
        Log.d("indicatorAnimator", "总diff   $diff")
        // 这里不可以直接直接刷新过来，必须用动画去做
        // 它应该是有一个变化范围
        mIndicatorAnimator = ValueAnimator.ofFloat(0f, 1f)
        mIndicatorAnimator?.duration = 200
        mIndicatorAnimator?.interpolator = FastOutSlowInInterpolator()
        mIndicatorAnimator?.addUpdateListener {
            val currentDiff = (it.animatedValue as Float) * diff
            Log.d("indicatorAnimator", "当前diff   $currentDiff")
            mIndicatorLeft = (oldIndicatorLeft.toFloat() + currentDiff).toInt()//慢慢滑过来
            mIndicatorRight = (oldIndicatorRight.toFloat() + currentDiff).toInt()
            Log.d("indicatorAnimator", "$mIndicatorLeft || $mIndicatorRight")
            postInvalidate()
        }

        mIndicatorAnimator?.start()
        // 现在来解决如何让indicator默认显示在第一个title的正下方, 只需要 对添加进来的TextView进行一次测量，算出宽度

    }

}