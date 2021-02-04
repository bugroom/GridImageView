package com.gridimageview.yu

import android.content.Context
import android.content.res.Configuration

internal fun Float.dp2Px(context: Context): Float {
    val scale = context.resources.displayMetrics.density
    return (scale * this + 0.5).toFloat()
}

internal fun getWindowHeight(context: Context): Int {
    return context.resources.displayMetrics.heightPixels + 100 + getNavigationBarHeight(context)
}

internal fun getNavigationBarHeight(context: Context): Int {
    val res = context.resources
    val resId = res.getIdentifier("navigation_bar_height", "dimen", "android")
    return res.getDimensionPixelSize(resId)
}

internal fun isNightMode(context: Context): Boolean {
    val uiMode = context.resources.configuration.uiMode and Configuration.UI_MODE_NIGHT_MASK
    return uiMode == Configuration.UI_MODE_NIGHT_YES
}