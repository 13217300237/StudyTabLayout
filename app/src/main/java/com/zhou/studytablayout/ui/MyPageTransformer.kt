package com.zhou.studytablayout.ui

import android.content.Context
import android.os.Build
import android.util.Log
import android.view.View
import androidx.viewpager.widget.ViewPager
import kotlin.math.pow

/**
 * 仿蚂蚁财富的viewpager切换效果，所有view重叠，但是有缩放倍数差，有x位置差，向左滑动，当前view渐变消失
 *
 * 使用方法：
 * Java代码：
 *    vp.setPageTransformer(true, new MyPageTransformer(getContext().getApplicationContext(), vpAdapter.getCount()));
 * kt代码:
 *    viewPager.setPageTransformer(true,MyPageTransformer(this.applicationContext, size))
 */
class MyPageTransformer : ViewPager.PageTransformer {

    private val offsetX = 12f //单位： dp
    private val scaleBaseProportion = 0.95 // 缩放比率
    private val alphaBaseProportion = 0.5 // 透明度比率
    private val context: Context

    private val size: Int

    /**
     * ctx 上下文
     * s  adapter的count
     */
    constructor(ctx: Context, s: Int) {
        context = ctx
        size = s
    }

    override fun transformPage(view: View, position: Float) {
        Log.d("setPageTransformer", "view:${view.hashCode()} position:$position")
        view.translationX = -position * view.width + position * dpToPx(
            context,
            offsetX
        )
        view.scaleX = scaleBaseProportion.pow(position.toDouble()).toFloat()
        view.scaleY = scaleBaseProportion.pow(position.toDouble()).toFloat()
        //现在处理透明度
        when {
            position < 0 -> {
                view.alpha = position + 1
                view.translationX = view.translationX + position * view.width
            }
            else -> {
                view.alpha = alphaBaseProportion.pow(position.toDouble() - 1)
                    .toFloat()// alpha = 0.8的p-1次方
            }
        }

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            view.z = size - position
        }
    }

    private fun dpToPx(ctx: Context, dpValue: Float): Int {
        var scale = ctx.resources.displayMetrics.density
        val s = (scale * dpValue) + 0.5f
        return s.toInt()
    }
}