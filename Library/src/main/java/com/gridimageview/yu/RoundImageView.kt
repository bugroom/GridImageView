package com.gridimageview.yu

import android.content.Context
import android.graphics.*
import android.os.Build
import android.util.AttributeSet
import androidx.annotation.ColorRes
import androidx.appcompat.widget.AppCompatImageView

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

    // private val xfermode: PorterDuffXfermode

    private val mPaint = Paint()

    private val mPath = Path()

    private val rectF = RectF()

    private val radii = FloatArray(8)

    private var borderWidth: Float
    private var borderColor: Int

    private var leftTopRadius: Float
    private var rightTopRadius: Float
    private var leftBottomRadius: Float
    private var rightBottomRadius: Float

    init {

        isClickable = true
        isFocusable = true
        scaleType = ScaleType.CENTER_CROP

        /*
        xfermode = if (Build.VERSION.SDK_INT <= Build.VERSION_CODES.O_MR1) {
            PorterDuffXfermode(PorterDuff.Mode.DST_IN)
        } else {
            PorterDuffXfermode(PorterDuff.Mode.DST_OUT)
        }

         */

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

    override fun onDraw(canvas: Canvas) {
        mPaint.reset()
        mPath.reset()
        rectF.set(0f, 0f, width.toFloat(), height.toFloat())
        // 横轴半径和纵轴半径， 8 个 数组组成 左上开始
        refreshRadii()
        // 顺时针绘制 CW， 添加圆角矩形
        mPath.addRoundRect(rectF, radii, Path.Direction.CW)
        // 对画布进行裁剪
        canvas.clipPath(mPath)
        super.onDraw(canvas)
        mPaint.style = Paint.Style.STROKE
        mPaint.isAntiAlias = true
        mPaint.color = borderColor
        mPaint.strokeWidth = borderWidth
        // mPaint.xfermode = xfermode
        // 绘制边界
        canvas.drawPath(mPath, mPaint)
    }

    private fun refreshRadii() {
        radii[0] = leftTopRadius.also { radii[1] = it }
        radii[2] = rightTopRadius.also { radii[3] = it }
        radii[4] = rightBottomRadius.also { radii[5] = it }
        radii[6] = leftBottomRadius.also { radii[7] = it }
    }
}