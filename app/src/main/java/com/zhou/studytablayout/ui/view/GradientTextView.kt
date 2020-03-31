package com.zhou.studytablayout.ui.view

import android.animation.ValueAnimator
import android.content.Context
import android.graphics.*
import android.util.AttributeSet
import android.util.Log
import com.zhou.studytablayout.common.GreenTextView

/**
 * 提供颜色渐变的TextView
 */
class GradientTextView : GreenTextView {
    constructor(context: Context, attrs: AttributeSet) : super(context, attrs)
    constructor(context: Context) : super(context)

    private var mLinearGradient: LinearGradient? = null
    private var mGradientMatrix: Matrix? = null
    private lateinit var mPaint: Paint
    private var mViewWidth = 0f
    private var mTranslate = 0f
    private val mAnimating = true

    private val fontColor = Color.BLACK
    private val shaderColor = Color.YELLOW

    override fun onSizeChanged(w: Int, h: Int, oldw: Int, oldh: Int) {
        super.onSizeChanged(w, h, oldw, oldh)
        if (mViewWidth == 0f) {
            mViewWidth = measuredWidth.toFloat()
            if (mViewWidth > 0) {
                mPaint = paint
                mLinearGradient = LinearGradient(
                    0f,// 初始状态，是隐藏在x轴负向，一个view宽的距离
                    0f,
                    mViewWidth,
                    0f,
                    intArrayOf(fontColor, shaderColor, shaderColor, fontColor),
                    floatArrayOf(0f, 0.1f, 0.9f, 1f),
                    Shader.TileMode.CLAMP
                )
                mPaint.shader = mLinearGradient
                mGradientMatrix = Matrix()
            }
        }
    }

    override fun onDraw(canvas: Canvas) {
        super.onDraw(canvas)
        if (mAnimating && mGradientMatrix != null) {
            mGradientMatrix!!.setTranslate(mTranslate, 0f)
            mLinearGradient!!.setLocalMatrix(mGradientMatrix)
        }
    }


    /**
     * 处理滑动
     */
    private fun dealSwap(positionOffset: Float, isSelected: Boolean) {
        // 如果不是初始值，那说明已经赋值过，那么用 参数positionOffset 和 它对比，来得出滑动的方向
        Log.d(
            "setMatrixTranslate",
            " positionOffset：$positionOffset  isSelected：$isSelected   "
        )
        // 来，先判定滑动的方向，因为方向会决定从哪个角度
        mTranslate = if (mPositionOffset < positionOffset) {// 手指向左
            if (isSelected) {// 如果当前是选中状态，那么 offset会从0到1 会如何变化？
                mViewWidth * positionOffset // OK，没问题。
            } else {
                -mViewWidth * (1 - positionOffset)
            }
        } else {// 手指向右
            if (isSelected) {// 如果当前是选中状态，那么 offset会从0到1 会如何变化？
                -mViewWidth * (1 - positionOffset) // OK，没问题。
            } else {
                mViewWidth * positionOffset
            }
        }
        postInvalidate()
    }

    /**
     * 由外部参数控制shader的位置
     * @param positionOffset 只会从0到1变化
     * @param isSelected 是否选中
     */
    override fun handlerPositionOffset(positionOffset: Float, isSelected: Boolean) {
        if (mPositionOffset == -1f) {// 如果你是初始值
            mPositionOffset = positionOffset // 那就先赋值
        } else {
            dealSwap(positionOffset, isSelected)
        }
    }

    /**
     * 消除shader
     */
    override fun removeShader(direction: Int) {
        mTranslate = if (direction > 0) {
            -mViewWidth
        } else
            mViewWidth
        postInvalidate()
    }

    override fun addShader(direction: Int) {
        // 属性动画实现shader平滑移动
        val from =
            if (direction < 0) {
                -mViewWidth
            } else {
                mViewWidth
            }
        startAnimator(from, 0f)
    }

    override fun onSetting(positionOffset: Float, isSelected: Boolean, direction: Int) {
        Log.d(
            "onSettingTag",
            "isSelected:$isSelected   positionOffset:$positionOffset direction:$direction"
        )
        mPositionOffset = -1f

        val targetTranslate = if (isSelected) {
            0f
        } else {
            if (direction > 0f) {// 向右回弹
                mViewWidth
            } else {
                Log.d("onSettingTag2", "难道这里还要分情况么？mTranslate:$mTranslate  mViewWidth:$mViewWidth")
                if (mTranslate == mViewWidth || mTranslate == -mViewWidth) {
                    mTranslate // 如果已经到达了最右边，那就保持你这个样子就行了, 可是你是怎么到最右边的？
                } else
                    -mViewWidth
            }

        }
        val thisTranslate = mTranslate
        startAnimator(thisTranslate, targetTranslate)
    }

    private fun startAnimator(from: Float, targetTranslate: Float) {
        if (animator != null) animator?.cancel()
        // 属性动画实现shader平滑移动

        animator = ValueAnimator.ofFloat(from, targetTranslate)
        animator?.run {
            duration = animatorDuration
            addUpdateListener {
                mTranslate = it.animatedValue as Float
                postInvalidate()
            }
            start()
        }
    }

    private var mPositionOffset: Float = -1f

    private val animatorDuration = 200L
    private var animator: ValueAnimator? = null
}