package com.gridimageview.yu

import android.content.Context
import android.view.Window

fun Float.dp2Px(context: Context): Float {
    val scale = context.resources.displayMetrics.density
    return (scale * this + 0.5).toFloat()
}

fun getWindowHeight(context: Context): Int {
    return context.resources.displayMetrics.heightPixels
}