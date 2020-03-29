package com.zhou.studytablayout.common

import android.content.Context
import android.util.AttributeSet
import androidx.appcompat.widget.AppCompatTextView

open class GreenTextView : AppCompatTextView {
    constructor(context: Context, attrs: AttributeSet) : super(context, attrs)
    constructor(context: Context) : super(context)

    /**
     * 可重写，接收来自viewpager的position参数，做出随心所欲的textView文字特效
     */
    open fun setMatrixTranslate(positionOffset: Float, isSelected: Boolean) {

    }

    open fun removeShader() {

    }
}