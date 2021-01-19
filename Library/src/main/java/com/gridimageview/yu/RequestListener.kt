package com.gridimageview.yu

import com.bumptech.glide.load.DataSource
import com.bumptech.glide.load.engine.GlideException
import com.bumptech.glide.request.RequestListener
import com.bumptech.glide.request.target.Target

abstract class RequestListener<T> : RequestListener<T> {
    override fun onResourceReady(
        resource: T?,
        model: Any?,
        target: Target<T>?,
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
        target: Target<T>?,
        isFirstResource: Boolean
    ): Boolean {
        onRequestFail()
        return false
    }

    abstract fun onRequestSuccess(resource: T): Boolean

    abstract fun onRequestFail()
}