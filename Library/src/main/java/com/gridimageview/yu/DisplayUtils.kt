package com.gridimageview.yu

import android.content.Context

fun Float.dp2Px(context: Context): Float {
    val scale = context.resources.displayMetrics.density
    return (scale * this + 0.5).toFloat()
}

fun getWindowHeight(context: Context): Int {
    return context.resources.displayMetrics.heightPixels + 100 + getNavigationBarHeight(context)
}

private fun getNavigationBarHeight(context: Context): Int {
    val res = context.resources
    val resId = res.getIdentifier("navigation_bar_height", "dimen", "android")
    return res.getDimensionPixelSize(resId)
}