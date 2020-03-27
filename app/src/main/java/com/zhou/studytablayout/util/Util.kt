package com.zhou.studytablayout.util

import android.content.Context
import android.graphics.Typeface

fun dpToPx(ctx: Context, dpValue: Float): Int {
    var scale = ctx.resources.displayMetrics.density
    val s = (scale * dpValue) + 0.5f
    return s.toInt()
}

/**
 * 将sp值转换为px值，保证文字大小不变
 *
 * @param spValue
 * @param fontScale
 * （DisplayMetrics类中属性scaledDensity）
 * @return
 */
fun sp2px(context: Context, spValue: Float): Int {
    val fontScale = context.resources.displayMetrics.scaledDensity
    return (spValue * fontScale + 0.5f).toInt()
}

/**
 * 此类用于获取特殊字体
 */
fun getFontTypeFace(mContext: Context): Typeface {
    return Typeface.createFromAsset(mContext.resources.assets, "fonts/DIN_Medium.ttf")
}