package com.gridimageview.yu

import android.graphics.drawable.Drawable
import com.bumptech.glide.load.DataSource
import com.bumptech.glide.load.engine.GlideException
import com.bumptech.glide.request.RequestListener
import com.bumptech.glide.request.target.Target

abstract class RequestListener: RequestListener<Drawable> {
    override fun onResourceReady(
        resource: Drawable?,
        model: Any?,
        target: Target<Drawable>?,
        dataSource: DataSource?,
        isFirstResource: Boolean
    ): Boolean {
        if (resource != null) {
           return onRequestSuccess(resource)
        }
        return false
    }

    override fun onLoadFailed(
        e: GlideException?,
        model: Any?,
        target: Target<Drawable>?,
        isFirstResource: Boolean
    ): Boolean {
        onRequestFail()
        return false
    }

    abstract fun onRequestSuccess(resource: Drawable): Boolean

    abstract fun onRequestFail()
}