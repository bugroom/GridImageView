package com.gridimageview.yu

import android.content.Context
import android.graphics.*
import android.os.Build
import android.util.AttributeSet
import android.util.Log
import androidx.annotation.ColorRes
import androidx.appcompat.widget.AppCompatImageView
import kotlin.math.abs

/**
 *   圆角 ImageView 带边框
 *   @author Yu
 */
class RoundImageView(context: Context, attributeSet: AttributeSet?, defStyleAttr: Int) :
    AppCompatImageView(context, attributeSet, defStyleAttr) {

    constructor(context: Context) : this(context, null, R.attr.roundImageViewStyle)

    constructor(context: Context, attributeSet: AttributeSet?) : this(
        context,
        attributeSet,
        R.attr.roundImageViewStyle
    )

    companion object {

        private const val TAG = "RoundImageView"

        const val TYPE_LONG = 2
        const val TYPE_GIF = 3

        const val STYLE_TIPS_SMALL = 0
        const val STYLE_TIPS_BIG = 1
    }

    // private val xfermode: PorterDuffXfermode

    private val mPaint = Paint()

    private val mPath = Path()

    private val rectF = RectF()

    private val radii = FloatArray(8)

    private var borderWidth: Float
    private var borderColor: Int

    private var imageCount = 0
    private var imageMaxCount = 9
    private var imageType = 0
    private var imageTipsColor = Color.argb(120, 0, 0, 0)
    private var imageTipsStyle = STYLE_TIPS_SMALL
    private var imageCountTipsX = 0f
    private var imageCountTipsY = 0f

    private var leftTopRadius: Float
    private var rightTopRadius: Float
    private var leftBottomRadius: Float
    private var rightBottomRadius: Float

    init {

        isClickable = true
        isFocusable = true
        scaleType = ScaleType.CENTER_CROP

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            val attrs = intArrayOf(R.attr.selectableItemBackground)
            val typedArray = context.obtainStyledAttributes(attrs)
            foreground = typedArray.getDrawable(0)
            typedArray.recycle()
        }

        context.obtainStyledAttributes(
            attributeSet,
            R.styleable.RoundImageView,
            defStyleAttr, 0
        ).apply {

            leftTopRadius =
                getDimension(R.styleable.RoundImageView_leftTopRadius, 0f).dp2Px(context)
            leftBottomRadius =
                getDimension(R.styleable.RoundImageView_leftBottomRadius, 0f).dp2Px(context)
            rightTopRadius =
                getDimension(R.styleable.RoundImageView_rightTopRadius, 0f).dp2Px(context)
            rightBottomRadius =
                getDimension(R.styleable.RoundImageView_rightBottomRadius, 0f).dp2Px(context)

            borderWidth = getDimension(R.styleable.RoundImageView_strokeWidth, 0.5f).dp2Px(context)

            borderColor = getColor(R.styleable.RoundImageView_strokeColor, Color.LTGRAY)

            recycle()
        }
    }

    fun setImageRadius(radius: Float) {
        this.leftTopRadius = radius
        this.leftBottomRadius = radius
        this.rightTopRadius = radius
        this.rightBottomRadius = radius
    }

    fun setLeftTopRadius(radius: Float) {
        this.leftTopRadius = radius
    }

    fun setLeftBottomRadius(radius: Float) {
        this.leftBottomRadius = radius
    }

    fun setRightTopRadius(radius: Float) {
        this.rightTopRadius = radius
    }

    fun setRightBottomRadius(radius: Float) {
        this.rightBottomRadius = radius
    }

    fun setStrokeWidth(width: Float) {
        this.borderWidth = width
    }

    fun setStrokeColor(@ColorRes color: Int) {
        this.borderColor = color
    }

    fun setImageCount(count: Int) {
        this.imageCount = count
    }

    fun setImageMaxCount(count: Int) {
        this.imageMaxCount = count
    }

    fun setImageTipsStyle(style: Int) {
        this.imageTipsStyle = style
    }

    /**
     *  设置图片具体类型
     *  @param type #TYPE_LONG And #TYPE_GIF
     */
    fun setImageType(type: Int) {
        if (type == TYPE_LONG || type == TYPE_GIF || type == 0) {
            this.imageType = type
        } else {
            Log.w(TAG, "type must be TYPE_LONG and TYPE_GIF")
        }
    }

    override fun onDraw(canvas: Canvas) {
        mPaint.reset()
        mPath.reset()
        rectF.set(0f, 0f, width.toFloat(), height.toFloat())
        // 横轴半径和纵轴半径， 8 个 数组组成 左上开始
        refreshRadii()
        // 顺时针绘制 CW， 添加圆角矩形
        mPath.addRoundRect(rectF, radii, Path.Direction.CCW)
        // 对画布进行裁剪
        canvas.clipPath(mPath)
        super.onDraw(canvas)
        mPaint.style = if (imageTipsStyle == STYLE_TIPS_SMALL || imageCount <= imageMaxCount)
            Paint.Style.STROKE else {
            Paint.Style.FILL
        }
        mPaint.isAntiAlias = true
        mPaint.color = if (imageTipsStyle == STYLE_TIPS_SMALL || imageCount <= imageMaxCount)
            borderColor else imageTipsColor
        mPaint.strokeWidth = borderWidth
        // mPaint.xfermode = xfermode
        // 绘制边界
        canvas.drawPath(mPath, mPaint)
        if (imageCount > imageMaxCount) {
            if (imageTipsStyle == STYLE_TIPS_SMALL) {
                drawTextPath(canvas, imageCount + "图", true)
            } else {
                val str = "+${imageCount - imageMaxCount}"
                mPaint.color = Color.WHITE
                mPaint.textSize = if (width > 350) 100f else 65f
                val strWidth = mPaint.measureText(str)
                val strHeight = getTextHeight()
                canvas.drawText(str, width / 2 - strWidth / 2, height / 2 + strHeight / 2, mPaint)
            }
        }
        if (imageType == TYPE_LONG || imageType == TYPE_GIF) {
            val str = if (imageType == TYPE_LONG) "长图" else "动图"
            drawTextPath(canvas, str, false)
        }
    }

    private fun refreshRadii() {
        radii[0] = leftTopRadius.also { radii[1] = it }
        radii[2] = rightTopRadius.also { radii[3] = it }
        radii[4] = rightBottomRadius.also { radii[5] = it }
        radii[6] = leftBottomRadius.also { radii[7] = it }
    }

    private fun drawTextPath(canvas: Canvas, str: String, isTop: Boolean) {
        mPath.reset()
        mPaint.color = imageTipsColor
        mPaint.style = Paint.Style.FILL
        mPaint.textSize = 28f
        val strWidth = mPaint.measureText(str)
        val strHeight = getTextHeight()
        imageCountTipsX = width - strWidth - 30
        imageCountTipsY = if (isTop) {
            0f
        } else {
            height - strHeight - 30
        }
        mPath.addRect(
            imageCountTipsX,
            imageCountTipsY,
            width.toFloat(),
            imageCountTipsY + strHeight + 30,
            Path.Direction.CW
        )
        canvas.drawPath(mPath, mPaint)
        mPaint.color = Color.WHITE
        canvas.drawTextOnPath(
            str,
            mPath,
            15f,
            (strHeight * 2 + 30) / 2,
            mPaint
        )
    }

    private fun getTextHeight(): Float {
        val fontMatrix = mPaint.fontMetrics
        return abs(fontMatrix.ascent) - fontMatrix.descent
    }

    private operator fun Int.plus(s: String): String {
        val builder = StringBuilder()
        return builder.append(this).append(s).toString()
    }
}
