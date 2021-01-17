package com.gridimageview.yu

import android.view.View
import android.view.ViewGroup
import org.jetbrains.annotations.NotNull

interface OnImageItemClickListener {
    fun onImageItemClick(parent: ViewGroup, v: View, position: Int)
}