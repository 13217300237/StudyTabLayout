package com.zhou.studytablayout.ui.view

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
    private val shaderColor = Color.BLUE
    private val shaderColor2 = Color.YELLOW

    override fun onSizeChanged(w: Int, h: Int, oldw: Int, oldh: Int) {
        super.onSizeChanged(w, h, oldw, oldh)
        if (mViewWidth == 0f) {
            mViewWidth = measuredWidth.toFloat()
            if (mViewWidth > 0) {
                mPaint = paint
                mLinearGradient = LinearGradient(
                    -mViewWidth,// 初始状态，是隐藏在x轴负向，一个view宽的距离
                    0f,
                    0f,
                    0f,
                    intArrayOf(fontColor, shaderColor, shaderColor, fontColor),
                    floatArrayOf(0f, 0f, 0.9f, 1f),
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
     * 由外部参数控制shader的位置
     * @param positionOffset 当前
     */
    override fun setMatrixTranslate(positionOffset: Float, isSelected: Boolean) {

        Log.d("setMatrixTranslate", "positionOffset：$positionOffset  isSelected：$isSelected")

        mTranslate = if (isSelected) {// 如果当前是选中状态，那么 offset会从0到1 会如何变化？
            mViewWidth * (1 + positionOffset) // OK，没问题。
        } else {// 它应该是慢慢显示shader
            // 手指向右
            mViewWidth * (positionOffset)
        }

        // 来看看到底 mTranslate 等于多少的时候，才会完全显示
//        mTranslate = mViewWidth // 看来处于mViewWidth的时候才会完全显示

        postInvalidate()
    }

    override fun removeShader() {
        super.removeShader()
        mTranslate = -mViewWidth
        postInvalidate()
    }

}