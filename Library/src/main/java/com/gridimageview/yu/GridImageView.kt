package com.gridimageview.yu

import android.app.Activity
import android.content.Context
import android.graphics.Bitmap
import android.graphics.Canvas
import android.graphics.Color
import android.graphics.drawable.AdaptiveIconDrawable
import android.graphics.drawable.BitmapDrawable
import android.graphics.drawable.Drawable
import android.media.Image
import android.os.Build
import android.util.AttributeSet
import android.util.Log
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.Toast
import com.bumptech.glide.Glide
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
     *  图片链接集合
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
     *  最多可显示的图片数量
     */
    private var imageMaxCount = 9

    /**
     *  要显示的图片数量
     */
    private var imageActualCount = 0

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

    /**
     *  单张图片是否满宽
     */
    private var singleViewFullWidth: Boolean

    /**
     *  图片提示样式
     */
    private var imageTipsStyle = RoundImageView.STYLE_TIPS_SMALL

    /**
     *  手机屏幕高度
     */
    private var windowHeight = 0

    /**
     *  当前 ViewGroup 宽度
     */
    private var parentWidth = 0

    /**
     *  第一行结束的位置用于显示 tips
     */
    private var firstLineEnd = 0

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
                getDimension(R.styleable.GridImageView_imageCornerRadius, 24f)
            imageSpacing =
                getDimension(R.styleable.GridImageView_imageSpacing, 2f).dp2Px(context).toInt()

            imagePlaceHolder = getDrawable(R.styleable.GridImageView_imagePlaceHolder)

            imageStrokeWidth =
                getDimension(R.styleable.GridImageView_imageBorderWidth, 0.5f).dp2Px(context)

            imageBorderColor = getColor(R.styleable.GridImageView_imageBorderColor, Color.LTGRAY)

            singleViewHandle = getBoolean(R.styleable.GridImageView_singleViewHandle, true)

            singleViewFullWidth = getBoolean(R.styleable.GridImageView_singleViewFullWidth, false)

            imageMaxCount = getInteger(R.styleable.GridImageView_imageMaxCount, 9)

            imageTipsStyle =
                getInteger(R.styleable.GridImageView_imageTipStyle, RoundImageView.STYLE_TIPS_SMALL)

            if (imageMaxCount !in 2..9) {
                Toast.makeText(
                    context,
                    "The imageMaxCount range of attribute a is 2 to 9",
                    Toast.LENGTH_LONG
                ).show()
                // throw RuntimeException("The imageMaxCount range of attribute a is 2 to 9")
                if (context is Activity) {
                    context.moveTaskToBack(false)
                }
            }

            recycle()
        }

        if (windowHeight == 0) {
            windowHeight = getWindowHeight(context)
        }
    }

    /**
     *  设置是否进行规则排序
     *  @param ruleSort 排序
     */
    fun setImageRuleSort(ruleSort: Boolean): GridImageView {
        this.isRuleSort = ruleSort
        return this
    }

    /**
     *  设置图片宽高(单张图片显示时使用)
     *  @param width 图片宽
     *  @param height 图片高
     */
    fun setImageViewSize(width: Int, height: Int): GridImageView {
        this.imageViewWidth = width
        this.imageViewHeight = height
        return this
    }

    /**
     *  设置加载占位图
     *  @param placeHolder drawable
     */
    fun setImagePlaceHolder(placeHolder: Drawable): GridImageView {
        this.imagePlaceHolder = placeHolder
        return this
    }

    /**
     *  设置图片之间的间隔
     *  @param spacing 间隔
     */
    fun setImageSpacing(spacing: Int): GridImageView {
        this.imageSpacing = spacing
        return this
    }

    /**
     *  单张图片满宽显示
     */
    fun setSingleViewFullWidth(isFull: Boolean): GridImageView {
        this.singleViewFullWidth = isFull
        return this
    }

    /**
     *  设置 ImageView 提示风格
     *  @param style RoundImageView.STYLE_TIPS_SMALL and RoundImageView.STYLE_TIPS_BIG
     */
    fun setImageTipsStyle(style: Int) {
        this.imageTipsStyle = style
    }

    /**
     *  刷新布局
     */
    fun refresh() {
        requestLayout()
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

        mUrls.clear()
        mUrls.addAll(urls)
        imageActualCount = min(urls.size, imageMaxCount)

        createView()
        rvHandle()

        refreshData(urls)
    }

    /**
     *  刷新数据
     */
    private fun refreshData(urls: List<String>) {
        rowCount = getRowCount()

        if (isRuleSort) {
            spanCount = getSpanCount() as Int
            firstLineEnd = spanCount - 1
        } else {
            spanCounts = getSpanCount() as IntArray
            firstLineEnd = spanCounts[0] - 1
        }

        if (imageTipsStyle == RoundImageView.STYLE_TIPS_BIG) {
            firstLineEnd = imageActualCount - 1
        }
        val child: View? = getChildAt(firstLineEnd)
        if (child != null && child is RoundImageView) {
            child.setImageMaxCount(imageMaxCount)
            child.setImageCount(urls.size)
            child.setImageTipsStyle(imageTipsStyle)
        }

        for (i in 0 until imageActualCount) {
            val childView = getChildAt(i)
            if (childView is ImageView) {
                refreshImageTip(childView, urls[i])
            }
        }
        requestLayout()
    }

    /**
     *  构建 ImageView
     */
    private fun createView() {
        if (imageActualCount > childCount) {
            for (i in childCount until imageActualCount) {
                val roundImageView = RoundImageView(context)
                roundImageView.setStrokeWidth(imageStrokeWidth)
                roundImageView.setStrokeColor(imageBorderColor)
                roundImageView.setOnClickListener {
                    if (::onImageItemClickListener.isInitialized) {
                        onImageItemClickListener.onImageItemClick(this, it, i)
                    }
                }
                addView(roundImageView)
            }
        }
    }

    /**
     *  RecyclerView解决复用问题
     */
    private fun rvHandle() {
        for (i in 0 until childCount) {
            val child = getChildAt(i)
            if (i < imageActualCount) {
                if (child.visibility == GONE) {
                    child.visibility = VISIBLE
                }
            } else {
                if (child.visibility == VISIBLE) {
                    child.visibility = GONE
                }
            }
        }
    }

    override fun onMeasure(widthMeasureSpec: Int, heightMeasureSpec: Int) {
        if (imageActualCount == 0) {
            setMeasuredDimension(0, 0)
            return
        }

        if (parentWidth == 0) {
            parentWidth = MeasureSpec.getSize(widthMeasureSpec)
        }

        if (imageActualCount == 1) {
            loadSingleImageView(parentWidth - paddingLeft - paddingRight, mUrls[0])
            setMeasuredDimension(
                parentWidth,
                imageViewHeight + paddingTop + paddingBottom
            )
            return
        }

        val availableWidth = parentWidth - paddingLeft - paddingRight
        var resultWidth: Int
        var resultHeight = 0

        if (isRuleSort) {
            val childWidth = (availableWidth - imageSpacing * (spanCount - 1)) / spanCount
            val childWidthMeasureSpec = MeasureSpec.makeMeasureSpec(childWidth, MeasureSpec.EXACTLY)
            for (i in 0 until imageActualCount) {
                val child = getChildAt(i)
                child.measure(childWidthMeasureSpec, childWidthMeasureSpec)
            }
            resultWidth =
                if (imageActualCount == 2) childWidth * 2 + imageSpacing else availableWidth
            resultHeight = rowCount * childWidth + (rowCount - 1) * imageSpacing
        } else {
            var index = 0
            spanCounts.forEachIndexed { row, span ->
                val childWidth =
                    if (span == 1) availableWidth else (availableWidth - imageSpacing * (span - 1)) / span
                val childHeight = when (span) {
                    1 -> childWidth / 2
                    2 -> {
                        val i = if (imageActualCount > 4) 3 else 2
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

        if (imageActualCount == 0 || childCount == 0) {
            return
        }

        if (imageActualCount == 1) {
            val child = getChildAt(0)
            if (child is RoundImageView) {
                child.setImageRadius(imageCornerRadius)
            }
            child.layout(
                paddingLeft,
                paddingTop,
                paddingLeft + imageViewWidth,
                paddingTop + imageViewHeight
            )
            return
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
            for (i in 0 until imageActualCount) {
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
                    if (child is RoundImageView) {
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
        for (i in 0 until imageActualCount) {
            val childView = getChildAt(i)
            if (childView is ImageView) {
                Glide.with(context).load(mUrls[i]).centerCrop()
                    .override(childView.measuredWidth, childView.measuredHeight)
                    .placeholder(imagePlaceHolder).error(imagePlaceHolder).into(childView)
            }
        }
    }

    /**
     *  单个 childView 进行宽高处理
     *  @param parentWidth 父容器宽度
     */
    private fun singleImageViewSurplus(parentWidth: Int) {
        if (imageViewWidth == 0 || imageViewHeight == 0) return
        if (singleViewFullWidth) {
            val scale = parentWidth.toFloat() / imageViewWidth
            imageViewWidth = parentWidth
            imageViewHeight = (scale * imageViewHeight).toInt()
        } else {
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
    }

    /**
     *  加载单个ImageView
     */
    private fun loadSingleImageView(parentWidth: Int, url: String) {
        val child = getChildAt(0)
        if (child !is ImageView) {
            throw ClassCastException("childView must be ImageView.\nchildView必须是ImageView")
        }

        if (child is RoundImageView) {
            child.setImageCount(0)
        }

        val isHandle = singleViewHandle && (imageViewWidth !in 250..parentWidth
                || imageViewHeight !in 250..parentWidth)

        if (imageViewWidth <= 0 || imageViewHeight <= 0) {
            Log.w(TAG, "please set imageWidth and imageHeight.")
            Glide.with(context).load(url)
                .override(Target.SIZE_ORIGINAL)
                .placeholder(imagePlaceHolder)
                .listener(object : RequestListener<Drawable>() {
                    override fun onRequestSuccess(resource: Drawable): Boolean {
                        imageViewWidth = resource.intrinsicWidth
                        imageViewHeight = resource.intrinsicHeight
                        // 单个 View 进行宽高处理
                        if (isHandle) {
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
            // 单个 View 进行宽高处理
            if (isHandle) {
                singleImageViewSurplus(parentWidth)
            }
            refreshImageTip(child, url)
            Glide.with(context).load(url).override(imageViewWidth, imageViewHeight)
                .centerCrop().placeholder(imagePlaceHolder).into(child)
        }
    }

    /**
     *  设置各角度的圆角
     */
    private fun View.drawRoundCorner(i: Int, row: Int, span: Int, index: Int? = null) {
        this.apply {
            if (this is RoundImageView) {
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

    /**
     *  刷新角标提示 长图、动图
     */
    private fun refreshImageTip(child: ImageView, url: String) {
        if (child is RoundImageView) {
            Glide.with(context).load(url).override(Target.SIZE_ORIGINAL)
                .listener(object : RequestListener<Drawable>() {
                    override fun onRequestSuccess(resource: Drawable): Boolean {
                        if (resource is GifDrawable) {
                            child.setImageType(RoundImageView.TYPE_GIF)
                        } else {
                            val bitmap = getBitmap(resource)
                            child.setImageType(if (bitmap.height > windowHeight && bitmap.width < 2340) RoundImageView.TYPE_LONG else 0)
                        }
                        return true
                    }

                    override fun onRequestFail() {}

                }).preload()
        }
    }

    /**
     *  得到图片的 Bitmap
     */
    fun getBitmap(drawable: Drawable): Bitmap {
        val bitmap: Bitmap
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O && drawable is AdaptiveIconDrawable) {
            bitmap = Bitmap.createBitmap(
                drawable.getIntrinsicWidth(),
                drawable.getIntrinsicHeight(),
                Bitmap.Config.ARGB_8888
            )
            val c = Canvas(bitmap)
            drawable.setBounds(0, 0, c.width, c.height)
            drawable.draw(c)
        } else {
            bitmap = (drawable as BitmapDrawable).bitmap
        }
        return bitmap
    }

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
            if (imageActualCount == 1) 1 else if (imageActualCount == 2 || imageActualCount == 4) 2 else 3
        } else {
            when (imageActualCount) {
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
            if (imageActualCount <= 3) 1 else if (imageActualCount <= 6) 2 else 3
        } else {
            if (imageActualCount <= 2) 1 else {
                if (imageActualCount <= 6) 2 else 3
            }
        }
    }
}
