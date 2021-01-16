package com.gridimageview.yu

import android.view.View
import android.view.ViewGroup

interface OnImageItemClickListener {
    fun onImageItemClick(parent: ViewGroup, v: View, position: Int)
}