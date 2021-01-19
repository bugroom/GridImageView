package com.gridimageview.yu

import android.content.Context
import android.graphics.Bitmap
import android.graphics.Color
import android.graphics.drawable.Drawable
import android.text.InputType
import android.util.AttributeSet
import android.util.Log
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import com.bumptech.glide.Glide
import com.bumptech.glide.gifdecoder.GifDecoder
import com.bumptech.glide.load.resource.gif.GifDrawable
import com.bumptech.glide.request.target.Target
import kotlin.math.min

/**
 *   宫格方式显示 ImageView
 *   update 2021.1.16
 *   @author 冬日暖雨
 */
class GridImageView(context: Context, attributeSet: AttributeSet?, defStyleAttr: Int) :
    ViewGroup(context, attributeSet, defStyleAttr) {

    /**
     *  图片 Url集合
     */
    private val mUrls = mutableListOf<String>()

    /**
     *  列数(规则排序）
     */
    private var spanCount = 0

    /**
     *  行数
     */
    private var rowCount = 0

    /**
     *  列数(非规则显示)
     */
    private lateinit var spanCounts: IntArray

    /**
     *  图片间隔
     */
    private var imageSpacing: Int

    /**
     *  是否进行规则排序
     */
    private var isRuleSort: Boolean

    /**
     *  左上右下各圆角半径
     */
    private var imageCornerRadius: Float

    /**
     *  图片加载占位图
     */
    private var imagePlaceHolder: Drawable?

    /**
     *  图片边框宽度
     */
    private var imageStrokeWidth: Float

    /**
     *  图片边框颜色
     */
    private var imageBorderColor: Int

    /**
     *  是否对图片宽高进行处理
     */
    private var singleViewHandle: Boolean

    private var imageTipsGravity: Int

    /**
     *  单个显示的图片宽高度
     */
    private var imageViewWidth = -2
    private var imageViewHeight = -2

    private lateinit var onImageItemClickListener: OnImageItemClickListener

    companion object {

        private const val TAG = "GridImageView"

    }

    constructor(context: Context, attributeSet: AttributeSet) : this(
        context,
        attributeSet,
        R.attr.gridImageViewStyle
    )

    init {

        context.obtainStyledAttributes(
            attributeSet,
            R.styleable.GridImageView,
            defStyleAttr,
            R.style.GridImageViewStyle
        ).apply {

            isRuleSort = getBoolean(R.styleable.GridImageView_isRuleSort, false)
            imageCornerRadius =
                getDimension(R.styleable.GridImageView_imageCornerRadius, 12f).dp2Px(context)
            imageSpacing =
                getDimension(R.styleable.GridImageView_imageSpacing, 2f).dp2Px(context).toInt()

            imagePlaceHolder = getDrawable(R.styleable.GridImageView_imagePlaceHolder)

            imageStrokeWidth =
                getDimension(R.styleable.GridImageView_imageBorderWidth, 0.5f).dp2Px(context)

            imageBorderColor = getColor(R.styleable.GridImageView_imageBorderColor, Color.LTGRAY)

            singleViewHandle = getBoolean(R.styleable.GridImageView_singleViewHandle, true)

            imageTipsGravity = getInteger(R.styleable.GridImageView_imageTipsGravity, RoundImageView.GRAVITY_TOP)

            recycle()

        }
    }

    /**
     *  设置是否进行规则排序
     *  @param ruleSort 排序
     */
    fun setImageRuleSort(ruleSort: Boolean) {
        this.isRuleSort = ruleSort
        requestLayout()
    }

    /**
     *  设置图片宽高(单张图片显示时使用)
     *  @param width 图片宽
     *  @param height 图片高
     */
    fun setImageViewSize(width: Int, height: Int) {
        this.imageViewWidth = width
        this.imageViewHeight = height
    }

    /**
     *  设置加载占位图
     *  @param placeHolder drawable
     */
    fun setImagePlaceHolder(placeHolder: Drawable) {
        this.imagePlaceHolder = placeHolder
    }

    /**
     *  设置图片之间的间隔
     *  @param spacing 间隔
     */
    fun setImageSpacing(spacing: Int) {
        this.imageSpacing = spacing
    }

    /**
     *  设置图片提示位置
     *  @param gravity 0 And 1 -> top bottom
     */
    fun setImageTipsGravity(gravity: Int) {
        this.imageTipsGravity = gravity
    }

    fun setOnImageItemClickListener(listener: OnImageItemClickListener) {
        this.onImageItemClickListener = listener
    }

    fun setImageUrls(urls: List<String>?) {
        if (urls.isNullOrEmpty()) {
            visibility = GONE
            return
        }
        if (visibility == GONE) visibility = VISIBLE

        val size = min(urls.size, 9)

        mUrls.clear()
        if (urls.size > 9) {
            urls.forEachIndexed { index, url ->
                if (index < 9) {
                    mUrls.add(url)
                }
            }
        } else {
            mUrls.addAll(urls)
        }

        if (size > childCount) {
            for (i in childCount until size) {
                val roundImageView = RoundImageView(context)
                roundImageView.setStrokeWidth(imageStrokeWidth)
                roundImageView.setStrokeColor(imageBorderColor)
                roundImageView.setImageTipsGravity(imageTipsGravity)
                roundImageView.setOnClickListener {
                    if (::onImageItemClickListener.isInitialized) {
                        onImageItemClickListener.onImageItemClick(this, it, i)
                    }
                }
                addView(roundImageView)
            }
        }

        /**
         *  RecyclerView解决复用问题
         */
        for (i in 0 until childCount) {
            val child = getChildAt(i)
            if (i < size) {
                if (child.visibility == GONE) {
                    child.visibility = VISIBLE
                }
            } else {
                if (child.visibility == VISIBLE) {
                    child.visibility = GONE
                }
            }
        }

        if (urls.size > 9) {
            val child = getChildAt(8)
            if (child is RoundImageView) {
                child.setImageCount(urls.size)
            }
        }

        if (size == 1) return

        for (i in 0 until size) {
            val child = getChildAt(i)
            if (child is ImageView) {
                if (child is RoundImageView) child.setImageType(0)
                Glide.with(context).load(mUrls[i]).centerCrop()
                    .override(Target.SIZE_ORIGINAL)
                    .listener(object : RequestListener<Drawable>() {

                        override fun onRequestSuccess(resource: Drawable): Boolean {
                            if (child is RoundImageView) {
                                val type = when {
                                    resource is GifDrawable -> RoundImageView.TYPE_GIF
                                    resource.intrinsicHeight > getWindowHeight(context) -> RoundImageView.TYPE_LONG
                                    else -> 0
                                }
                                child.setImageType(type)
                            }

                            return false
                        }

                        override fun onRequestFail() {}

                    }).placeholder(imagePlaceHolder).error(imagePlaceHolder).into(child)
            }
        }
    }

    override fun onMeasure(widthMeasureSpec: Int, heightMeasureSpec: Int) {

        if (mUrls.size == 0) {
            setMeasuredDimension(0, 0)
            return
        }

        val parentWidth = MeasureSpec.getSize(widthMeasureSpec)
        if (mUrls.size == 1)
            loadSingleImageView(parentWidth)

        val availableWidth = parentWidth - paddingLeft - paddingRight
        var resultWidth: Int
        var resultHeight = 0
        rowCount = getRowCount()

        if (isRuleSort) {
            spanCount = getSpanCount() as Int
            val childWidth = if (mUrls.size == 1) imageViewWidth
            else (availableWidth - imageSpacing * (spanCount - 1)) / spanCount
            val childWidthMeasureSpec = MeasureSpec.makeMeasureSpec(childWidth, MeasureSpec.EXACTLY)
            val childHeightMeasureSpec = if (mUrls.size == 1)
                MeasureSpec.makeMeasureSpec(
                    imageViewHeight,
                    MeasureSpec.EXACTLY
                ) else childWidthMeasureSpec

            for (i in 0 until mUrls.size) {
                val child = getChildAt(i)
                child.measure(childWidthMeasureSpec, childHeightMeasureSpec)
            }
            resultWidth = if (mUrls.size == 2) childWidth * 2 + imageSpacing else availableWidth
            resultHeight = rowCount * childWidth + (rowCount - 1) * imageSpacing
            if (mUrls.size == 1) {
                resultWidth = imageViewWidth
                resultHeight = imageViewHeight
            }
        } else {
            spanCounts = getSpanCount() as IntArray
            var index = 0
            spanCounts.forEachIndexed { row, span ->
                val childWidth = if (span == 1) if (mUrls.size == 1) imageViewWidth
                else availableWidth else (availableWidth - imageSpacing * (span - 1)) / span
                val childHeight = when (span) {
                    1 -> if (mUrls.size == 1) imageViewHeight else childWidth / 2
                    2 -> {
                        val i = if (mUrls.size > 4) 3 else 2
                        if (i == 2) {
                            availableWidth / i
                        } else {
                            (availableWidth - imageSpacing * 2) / 3
                        }
                    }
                    else -> childWidth
                }
                val childWidthMeasureSpec =
                    MeasureSpec.makeMeasureSpec(childWidth, MeasureSpec.EXACTLY)
                val childHeightMeasureSpec =
                    MeasureSpec.makeMeasureSpec(childHeight, MeasureSpec.EXACTLY)
                resultHeight += childHeight
                if (row != spanCounts.size - 1) {
                    resultHeight += imageSpacing
                }
                for (i in index until span + index) {
                    val child = getChildAt(i)
                    child.measure(childWidthMeasureSpec, childHeightMeasureSpec)
                }
                index += span
            }
            resultWidth = availableWidth
        }
        resultWidth += paddingLeft + paddingRight
        resultHeight += paddingTop + paddingBottom
        setMeasuredDimension(resultWidth, resultHeight)
    }

    override fun onLayout(changed: Boolean, l: Int, t: Int, r: Int, b: Int) {

        if (mUrls.size == 0) {
            return
        }

        if (mUrls.size == 1) {
            val child = getChildAt(0)
            if (child is RoundImageView) {
                child.setImageRadius(imageCornerRadius)
            }
        }

        var childLeft: Int
        var childTop: Int
        var childRight: Int
        var childBottom: Int
        if (isRuleSort) {
            var row: Int
            var span: Int
            var childWidth: Int
            var childHeight: Int
            for (i in 0 until mUrls.size) {
                val child = getChildAt(i)
                childWidth = child.measuredWidth
                childHeight = child.measuredHeight
                span = i % spanCount
                row = i / spanCount
                childLeft = span * (childWidth + imageSpacing) + paddingLeft
                childTop = row * (childHeight + imageSpacing) + paddingTop
                childRight = childLeft + childWidth
                childBottom = childTop + childHeight
                child.layout(childLeft, childTop, childRight, childBottom)
                child.drawRoundCorner(i, row, span)
            }
        } else {
            var index = 0
            childTop = paddingTop
            childBottom = 0
            spanCounts.forEachIndexed { row, span ->
                if (span == 1) {
                    index++
                    val child = getChildAt(0)
                    child.layout(
                        paddingLeft, paddingTop, paddingLeft + child.measuredWidth,
                        paddingTop + child.measuredHeight
                    )
                    childTop += child.measuredHeight + imageSpacing
                    childBottom = childTop + child.measuredHeight
                    if (child is RoundImageView && mUrls.size != 1) {
                        child.setLeftTopRadius(imageCornerRadius)
                        child.setRightTopRadius(imageCornerRadius)
                        child.setLeftBottomRadius(0f)
                        child.setRightBottomRadius(0f)
                    }
                } else {
                    childLeft = paddingLeft
                    childRight = 0
                    for (i in index until index + span) {
                        val child = getChildAt(i)
                        childBottom = childTop + child.measuredHeight
                        childRight = childLeft + child.measuredWidth
                        child.layout(childLeft, childTop, childRight, childBottom)
                        childLeft += child.measuredWidth + imageSpacing
                        if (i == index + span - 1) {
                            childTop += child.measuredHeight + imageSpacing
                        }
                        child.drawRoundCorner(i, row, span, index)
                    }
                    index += span
                }
            }
        }
    }

    /**
     *  单个 childView 进行宽高处理
     *  @param parentWidth 父容器宽度
     */
    private fun singleImageViewSurplus(parentWidth: Int) {
        singleViewHandle = false
        if (imageViewHeight > parentWidth / 2 || imageViewWidth >= parentWidth) {
            var x = 3
            if (imageViewWidth < imageViewHeight) {
                x = 4
                if (imageViewHeight > 2 * imageViewWidth) x = 6
            }
            val a = parentWidth * 2 / x
            val scaleX = imageViewWidth / a.toFloat()
            imageViewWidth = a
            imageViewHeight = (imageViewHeight / scaleX).toInt()
            if (imageViewHeight >= parentWidth * 2 / 3) imageViewHeight = parentWidth / 2
        } else {
            val width = parentWidth * 2 / 3
            if (imageViewWidth < width) {
                val scaleX = imageViewWidth / width.toFloat()
                imageViewWidth = width
                imageViewHeight = (imageViewHeight / scaleX).toInt()
            }
        }
    }

    /**
     *  加载单个ImageView
     */
    private fun loadSingleImageView(parentWidth: Int) {
        val child = getChildAt(0)
        if (child !is ImageView) {
            throw ClassCastException("childView must be ImageView.\nchildView必须是ImageView")
        }

        if (imageViewWidth <= 0 || imageViewHeight <= 0) {
            Log.w(TAG, "please set imageWidth and imageHeight.")
            Glide.with(context).load(mUrls[0])
                .override(Target.SIZE_ORIGINAL)
                .placeholder(imagePlaceHolder)
                .listener(object : RequestListener<Drawable>() {
                    override fun onRequestSuccess(resource: Drawable): Boolean {
                        imageViewWidth = resource.intrinsicWidth
                        imageViewHeight = resource.intrinsicHeight
                        // 单个 View 进行宽高处理
                        if (singleViewHandle || imageViewHeight > parentWidth || imageViewWidth > parentWidth) {
                            singleImageViewSurplus(parentWidth)
                        }
                        return false
                    }

                    override fun onRequestFail() {
                        // child.setImageDrawable(imagePlaceHolder)
                    }
                })
                .into(child)
        } else {
            var type = 0
            if (child is RoundImageView) {
                if (imageViewHeight > getWindowHeight(context)) {
                    type = RoundImageView.TYPE_LONG
                }
            }
            // 单个 View 进行宽高处理
            if (singleViewHandle || imageViewHeight > parentWidth || imageViewWidth > parentWidth) {
                singleImageViewSurplus(parentWidth)
            }
            Glide.with(context).load(mUrls[0]).override(imageViewWidth, imageViewHeight)
                .listener(object : RequestListener<Drawable>() {
                    override fun onRequestSuccess(resource: Drawable): Boolean {
                        if (resource is GifDrawable) {
                           type = RoundImageView.TYPE_GIF
                        }
                        if (child is RoundImageView) {
                            child.setImageType(type)
                        }
                        return false
                    }

                    override fun onRequestFail() {}

                })
                .centerCrop().placeholder(imagePlaceHolder).into(child)
        }
    }

    /**
     *  设置各角度的圆角
     */
    private fun View.drawRoundCorner(i: Int, row: Int, span: Int, index: Int? = null) {
        this.apply {
            if (this is RoundImageView) {
                if (mUrls.size == 1) {
                    return@apply
                }
                var leftTop = 0f
                var rightTop = 0f
                var leftBottom = 0f
                var rightBottom = 0f
                if (index == null) {
                    if (i == 0) {
                        leftTop = imageCornerRadius
                    }
                    if (i == spanCount - 1) {
                        rightTop = imageCornerRadius
                    }
                    if (row == rowCount - 1) {
                        if (span == 0) {
                            leftBottom = imageCornerRadius
                        }
                        if (span == spanCount - 1) {
                            rightBottom = imageCornerRadius
                        }
                    }
                } else {
                    if (row == 0) {
                        if (i == index) {
                            leftTop = imageCornerRadius
                        }
                        if (i == index + span - 1) {
                            rightTop = imageCornerRadius
                        }
                    }
                    if (row == spanCounts.size - 1) {
                        if (i == index) {
                            leftBottom = imageCornerRadius
                        }
                        if (i == index + span - 1) {
                            rightBottom = imageCornerRadius
                        }
                    }
                }
                setLeftTopRadius(leftTop)
                setRightTopRadius(rightTop)
                setRightBottomRadius(rightBottom)
                setLeftBottomRadius(leftBottom)
            }
        }
    }

    /*
    private fun randomColorDrawable() {
        if (imagePlaceHolder == null) {
            val red = (150..200).random()
            val green = (150..200).random()
            val blue = (150..200).random()
            imagePlaceHolder = ColorDrawable(Color.rgb(red, green, blue))
        }
    }
     */

    /**
     *      2 3 4 5 6 7 8 9
     * span 2 3 2 3 3 3 3 3
     * row  1 1 2 2 2 3 3 3
     */

    /**           2    3    4    5     6      7      8       9
     *    span    2   1,2  1,3  2,3  1,2,3  2,2,3  2,3,3   1,4,4
     *    row     1    2    2    2     2      3      3      3
     */
    private fun getSpanCount(): Any {
        return if (isRuleSort) {
            if (mUrls.size == 1) 1 else if (mUrls.size == 2 || mUrls.size == 4) 2 else 3
        } else {
            when (mUrls.size) {
                1 -> intArrayOf(1)
                2 -> intArrayOf(2)
                3 -> intArrayOf(1, 2)
                4 -> intArrayOf(1, 3)
                5 -> intArrayOf(2, 3)
                6 -> intArrayOf(1, 2, 3)
                7 -> intArrayOf(2, 2, 3)
                8 -> intArrayOf(2, 3, 3)
                9 -> intArrayOf(1, 4, 4)
                else -> intArrayOf(0)
            }
        }
    }

    private fun getRowCount(): Int {
        return if (isRuleSort) {
            if (mUrls.size <= 3) 1 else if (mUrls.size <= 6) 2 else 3
        } else {
            if (mUrls.size <= 2) 1 else {
                if (mUrls.size <= 6) 2 else 3
            }
        }
    }
}
