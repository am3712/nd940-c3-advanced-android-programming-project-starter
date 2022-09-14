package com.udacity

import android.animation.Animator
import android.animation.AnimatorListenerAdapter
import android.animation.ValueAnimator
import android.content.Context
import android.graphics.*
import android.util.AttributeSet
import android.util.Log
import android.view.View
import androidx.core.content.withStyledAttributes
import kotlin.properties.Delegates

private const val TAG = "LoadingButton"

class LoadingButton @JvmOverloads constructor(
    context: Context, attrs: AttributeSet? = null, defStyleAttr: Int = 0
) : View(context, attrs, defStyleAttr) {
    private var widthSize = 0
    private var heightSize = 0

    private var labelXPos: Float = 0f
    private var labelYPos: Float = 0f
    private var circleXPos: Float = 0f
    private var circleYPos: Float = 0f
    private lateinit var rectF: RectF

    private var buttonBackgroundColor = 0
    private var buttonAnimatedBackgroundColor = 0
    private var buttonTextColor = 0
    private var circleColor = 0

    private val paint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
        style = Paint.Style.FILL
        textAlign = Paint.Align.CENTER
        textSize = 55.0f
    }
    private var progress = 0.0f
    private var label = resources.getString(R.string.button_name)

    private val valueAnimator = ValueAnimator.ofFloat(0f, 1f).apply {
        duration = 2000
        // interpolator = DecelerateInterpolator()
        addUpdateListener {
            progress = it.animatedValue as Float
            Log.i(TAG, "valueAnimator progress: $progress")
            invalidate()
        }

        addListener(object : AnimatorListenerAdapter() {
            override fun onAnimationStart(animation: Animator?) {
                label = resources.getString(R.string.button_loading)
                calculateRectF()
                buttonState = ButtonState.Loading
                isClickable = false
            }

            override fun onAnimationEnd(animation: Animator?) {
                label = resources.getString(R.string.button_name)
                calculateRectF()
                progress = 0f
                buttonState = ButtonState.Completed
                isClickable = true
                invalidate()
            }
        })
    }


    private var buttonState: ButtonState by Delegates.observable(ButtonState.Completed as ButtonState) { p, old, new ->
        if (new == ButtonState.Clicked) {
            valueAnimator.start()
        }
    }

    init {
        isClickable = true
        context.withStyledAttributes(attrs, R.styleable.LoadingButton) {
            buttonBackgroundColor = getColor(R.styleable.LoadingButton_backgroundColor, 0)
            buttonAnimatedBackgroundColor =
                getColor(R.styleable.LoadingButton_animatedBackgroundColor, 0)
            buttonTextColor = getColor(R.styleable.LoadingButton_textColor, Color.WHITE)
            circleColor = getColor(R.styleable.LoadingButton_circleColor, 0)
        }
    }


    override fun onDraw(canvas: Canvas?) {
        super.onDraw(canvas)

        paint.apply {
            isAntiAlias = false
            style = Paint.Style.FILL
            color = buttonBackgroundColor
            strokeWidth = 0f
        }
        // Draw background of Button.
        canvas?.drawRect(0f, 0f, widthSize.toFloat(), heightSize.toFloat(), paint)

        // Draw animated background of Button
        paint.color = buttonAnimatedBackgroundColor
        canvas?.drawRect(0f, 0f, widthSize.toFloat() * progress, heightSize.toFloat(), paint)

        // Draw the text label.
        paint.color = buttonTextColor
        canvas?.drawText(label, labelXPos, labelYPos, paint)

        // draw circle
        // use getLabelBounds(label).width() / 2 OR Paint.measureText to getWidth of label

        paint.apply {
            isAntiAlias = true
            style = Paint.Style.STROKE
            strokeWidth = STROKE_WIDTH
        }
        // canvas?.drawCircle(circleXPos, (heightSize / 2).toFloat(), circleRadius, paint)
        paint.color = circleColor
        canvas?.drawArc(rectF, 0f, progress * 360f, false, paint)
    }

    private fun getLabelBounds(label: String): Rect {
        val bounds = Rect()
        paint.getTextBounds(label, 0, label.length, bounds)
        return bounds
    }

    override fun onMeasure(widthMeasureSpec: Int, heightMeasureSpec: Int) {
        val minw: Int = paddingLeft + paddingRight + suggestedMinimumWidth
        val w: Int = resolveSizeAndState(minw, widthMeasureSpec, 1)
        val h: Int = resolveSizeAndState(
            View.MeasureSpec.getSize(w),
            heightMeasureSpec,
            0
        )
        widthSize = w
        heightSize = h
        setMeasuredDimension(w, h)
    }

    override fun onSizeChanged(w: Int, h: Int, oldw: Int, oldh: Int) {
        super.onSizeChanged(w, h, oldw, oldh)
        labelXPos = (widthSize / 2).toFloat()
        labelYPos =
            heightSize / 2 - (paint.descent() + paint.ascent()) / 2 // https://stackoverflow.com/questions/20900412/center-text-in-canvas-android
        calculateRectF()
    }

    private fun calculateRectF() {
        circleXPos = labelXPos + paint.measureText(label) / 2 + STROKE_WIDTH / 2
        circleYPos = labelYPos - STROKE_WIDTH
        rectF = RectF(
            circleXPos,
            circleYPos,
            circleXPos + STROKE_WIDTH,
            circleYPos + STROKE_WIDTH
        )
    }

    override fun performClick(): Boolean {
        buttonState = ButtonState.Clicked
        if (super.performClick()) return true
        return true
    }

    companion object {
        private const val STROKE_WIDTH = 32F
    }

}