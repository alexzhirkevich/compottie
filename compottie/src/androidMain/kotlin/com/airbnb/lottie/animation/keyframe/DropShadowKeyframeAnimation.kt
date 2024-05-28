package com.airbnb.lottie.animation.keyframe

import android.annotation.SuppressLint
import android.graphics.Color
import android.graphics.Paint
import com.airbnb.lottie.model.layer.BaseLayer
import com.airbnb.lottie.parser.DropShadowEffect
import com.airbnb.lottie.value.LottieFrameInfo
import com.airbnb.lottie.value.LottieValueCallback
import kotlin.math.cos
import kotlin.math.sin

@SuppressLint("RestrictedApi")
class DropShadowKeyframeAnimation(
    private val listener: BaseKeyframeAnimation.AnimationListener,
    layer: BaseLayer,
    dropShadowEffect: DropShadowEffect
) : BaseKeyframeAnimation.AnimationListener {
    private val color: BaseKeyframeAnimation<Int, Int> =
        dropShadowEffect.color.createAnimation()
    private val opacity: BaseKeyframeAnimation<Float, Float>
    private val direction: BaseKeyframeAnimation<Float, Float>
    private val distance: BaseKeyframeAnimation<Float, Float>
    private val radius: BaseKeyframeAnimation<Float, Float>

    private var isDirty = true

    init {
        color.addUpdateListener(this)
        layer.addAnimation(color)
        opacity = dropShadowEffect.opacity.createAnimation()
        opacity.addUpdateListener(this)
        layer.addAnimation(opacity)
        direction = dropShadowEffect.direction.createAnimation()
        direction.addUpdateListener(this)
        layer.addAnimation(direction)
        distance = dropShadowEffect.distance.createAnimation()
        distance.addUpdateListener(this)
        layer.addAnimation(distance)
        radius = dropShadowEffect.radius.createAnimation()
        radius.addUpdateListener(this)
        layer.addAnimation(radius)
    }

    override fun onValueChanged() {
        isDirty = true
        listener.onValueChanged()
    }

    fun applyTo(paint: Paint) {
        if (!isDirty) {
            return
        }
        isDirty = false

        val directionRad: Double = (direction.getValue().toDouble()) * DEG_TO_RAD
        val distance: Float = distance.getValue()
        val x = (sin(directionRad).toFloat()) * distance
        val y = (cos(directionRad + Math.PI)
            .toFloat()) * distance
        val baseColor: Int = color.getValue()
        val opacity = Math.round(opacity.getValue()).toInt()
        val color =
            Color.argb(opacity, Color.red(baseColor), Color.green(baseColor), Color.blue(baseColor))
        val radius: Float = radius.getValue()
        paint.setShadowLayer(radius, x, y, color)
    }

    fun setColorCallback(callback: LottieValueCallback<Int>?) {
        color.setValueCallback(callback)
    }

    fun setOpacityCallback(callback: LottieValueCallback<Float?>?) {
        if (callback == null) {
            opacity.setValueCallback(null)
            return
        }
        opacity.setValueCallback(object : LottieValueCallback<Float?>() {
            override fun getValue(frameInfo: LottieFrameInfo<Float?>): Float? {
                val value = callback.getValue(frameInfo) ?: return null
                // Convert [0,100] to [0,255] because other dynamic properties use [0,100].
                return value * 2.55f
            }
        })
    }

    fun setDirectionCallback(callback: LottieValueCallback<Float>?) {
        direction.setValueCallback(callback)
    }

    fun setDistanceCallback(callback: LottieValueCallback<Float>?) {
        distance.setValueCallback(callback)
    }

    fun setRadiusCallback(callback: LottieValueCallback<Float>?) {
        radius.setValueCallback(callback)
    }

    companion object {
        private const val DEG_TO_RAD = Math.PI / 180.0
    }
}
