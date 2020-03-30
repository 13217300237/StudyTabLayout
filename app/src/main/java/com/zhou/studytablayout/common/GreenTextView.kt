package com.zhou.studytablayout.common

import android.content.Context
import android.util.AttributeSet
import androidx.appcompat.widget.AppCompatTextView

open class GreenTextView : AppCompatTextView {
    constructor(context: Context, attrs: AttributeSet) : super(context, attrs)
    constructor(context: Context) : super(context)

    /**
     * 可重写，接收来自viewpager的position参数，做出随心所欲的textView文字特效
     *
     * @param isSelected 是不是当前选中的TabView
     * @param positionOffset 偏移值   0<= positionOffset <=1
     */
    open fun setMatrixTranslate(positionOffset: Float, isSelected: Boolean) {}

    /**
     * 如果发生了滑动过程中特效残留的情况，可以重写此方法用来清除特效
     */
    open fun removeShader() {}

    /**
     * 通知，即将进入setting状态
     */
    open fun onSetting(isSelected: Boolean) {}
}