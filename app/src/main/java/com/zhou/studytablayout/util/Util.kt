package com.zhou.studytablayout.util

import android.content.Context

fun dpToPx(ctx: Context, dpValue: Float): Int {
    var scale = ctx.resources.displayMetrics.density
    val s = (scale * dpValue) + 0.5f
    return s.toInt()
}