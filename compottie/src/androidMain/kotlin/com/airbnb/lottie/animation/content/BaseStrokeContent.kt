package com.airbnb.lottie.animation.content

import android.annotation.SuppressLint
import android.graphics.DashPathEffect
import android.graphics.PathMeasure
import androidx.annotation.CallSuper
import androidx.compose.ui.graphics.Canvas
import androidx.compose.ui.graphics.ColorFilter
import androidx.compose.ui.graphics.Paint
import androidx.compose.ui.graphics.PaintingStyle
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.StrokeJoin
import com.airbnb.lottie.L
import com.airbnb.lottie.LottieDrawable
import com.airbnb.lottie.LottieProperty
import com.airbnb.lottie.animation.keyframe.BaseKeyframeAnimation
import com.airbnb.lottie.animation.keyframe.DropShadowKeyframeAnimation
import com.airbnb.lottie.animation.keyframe.FloatKeyframeAnimation
import com.airbnb.lottie.animation.keyframe.IntegerKeyframeAnimation
import com.airbnb.lottie.animation.keyframe.ValueCallbackKeyframeAnimation
import com.airbnb.lottie.model.KeyPath
import com.airbnb.lottie.model.animatable.AnimatableFloatValue
import com.airbnb.lottie.model.animatable.AnimatableIntegerValue
import com.airbnb.lottie.model.content.ShapeTrimPath
import com.airbnb.lottie.model.layer.BaseLayer
import com.airbnb.lottie.utils.MiscUtils
import com.airbnb.lottie.utils.Utils
import com.airbnb.lottie.value.LottieValueCallback
import kotlin.math.min

@SuppressLint("RestrictedApi")
abstract class BaseStrokeContent internal constructor(
    private val lottieDrawable: LottieDrawable,
    protected val layer: BaseLayer,
    cap: StrokeCap,
    join: StrokeJoin,
    miterLimit: Float,
    opacity: AnimatableIntegerValue,
    width: AnimatableFloatValue,
    dashPattern: List<AnimatableFloatValue>,
    offset: AnimatableFloatValue?
) : BaseKeyframeAnimation.AnimationListener, KeyPathElementContent, DrawingContent {
    private val pm = PathMeasure()
    private val path = Path()
    private val trimPathPath = Path()
    private val pathGroups: MutableList<PathGroup> = ArrayList()
    private val dashPatternValues: FloatArray
    val paint: Paint = Paint().apply {
        isAntiAlias = true
    }

    private val widthAnimation: BaseKeyframeAnimation<*, Float>
    private val opacityAnimation: BaseKeyframeAnimation<*, Int>
    private val dashPatternAnimations: MutableList<BaseKeyframeAnimation<*, Float>>
    private var dashPatternOffsetAnimation: BaseKeyframeAnimation<*, Float> ?
    private var colorFilterAnimation: BaseKeyframeAnimation<ColorFilter, ColorFilter>? = null
    private var blurAnimation: BaseKeyframeAnimation<Float, Float>? = null
    var blurMaskFilterRadius: Float = 0f

    private var dropShadowAnimation: DropShadowKeyframeAnimation? = null

    init {
        paint.style = PaintingStyle.Stroke
        paint.strokeCap = cap
        paint.strokeJoin = join
        paint.strokeMiterLimit = miterLimit

        opacityAnimation = opacity.createAnimation()
        widthAnimation = width.createAnimation()

        dashPatternOffsetAnimation = offset?.createAnimation()
        dashPatternAnimations = ArrayList(dashPattern.size)
        dashPatternValues = FloatArray(dashPattern.size)

        for (i in dashPattern.indices) {
            dashPatternAnimations.add(dashPattern[i].createAnimation())
        }

        layer.addAnimation(opacityAnimation)
        layer.addAnimation(widthAnimation)
        for (i in dashPatternAnimations.indices) {
            layer.addAnimation(dashPatternAnimations[i])
        }
        if (dashPatternOffsetAnimation != null) {
            layer.addAnimation(dashPatternOffsetAnimation)
        }

        opacityAnimation.addUpdateListener(this)
        widthAnimation.addUpdateListener(this)

        for (i in dashPattern.indices) {
            dashPatternAnimations[i].addUpdateListener(this)
        }
        dashPatternOffsetAnimation?.addUpdateListener(this)

        if (layer.blurEffect != null) {
            blurAnimation = layer.blurEffect!!.blurriness.createAnimation()
            blurAnimation.addUpdateListener(this)
            layer.addAnimation(blurAnimation)
        }
        if (layer.dropShadowEffect != null) {
            dropShadowAnimation = DropShadowKeyframeAnimation(
                this, layer, layer.dropShadowEffect
            )
        }
    }

    override fun onValueChanged() {
        lottieDrawable.invalidateSelf()
    }

    override fun setContents(contentsBefore: List<Content?>?, contentsAfter: List<Content?>?) {
        var trimPathContentBefore: TrimPathContent? = null
        for (i in contentsBefore!!.indices.reversed()) {
            val content = contentsBefore[i]
            if (content is TrimPathContent &&
                content.type == ShapeTrimPath.Type.INDIVIDUALLY
            ) {
                trimPathContentBefore = content
            }
        }
        trimPathContentBefore?.addListener(this)

        var currentPathGroup: PathGroup? = null
        for (i in contentsAfter!!.indices.reversed()) {
            val content = contentsAfter[i]
            if (content is TrimPathContent &&
                content.type == ShapeTrimPath.Type.INDIVIDUALLY
            ) {
                if (currentPathGroup != null) {
                    pathGroups.add(currentPathGroup)
                }
                currentPathGroup = PathGroup(content as TrimPathContent?)
                content.addListener(this)
            } else if (content is PathContent) {
                if (currentPathGroup == null) {
                    currentPathGroup = PathGroup(trimPathContentBefore)
                }
                currentPathGroup.paths.add(content)
            }
        }
        if (currentPathGroup != null) {
            pathGroups.add(currentPathGroup)
        }
    }

    override fun draw(canvas: Canvas, parentMatrix: Matrix, parentAlpha: Int) {
        if (L.isTraceEnabled()) {
            L.beginSection("StrokeContent#draw")
        }
        if (Utils.hasZeroScaleAxis(parentMatrix)) {
            if (L.isTraceEnabled()) {
                L.endSection("StrokeContent#draw")
            }
            return
        }
        val alpha =
            ((parentAlpha / 255f * (opacityAnimation as IntegerKeyframeAnimation).intValue / 100f) * 255).toInt()
        paint.alpha = MiscUtils.clamp(alpha, 0, 255)
        paint.strokeWidth =
            (widthAnimation as FloatKeyframeAnimation).floatValue * Utils.getScale(
                parentMatrix
            )
        if (paint.strokeWidth <= 0) {
            // Android draws a hairline stroke for 0, After Effects doesn't.
            if (L.isTraceEnabled()) {
                L.endSection("StrokeContent#draw")
            }
            return
        }
        applyDashPatternIfNeeded(parentMatrix)

        if (colorFilterAnimation != null) {
            paint.setColorFilter(colorFilterAnimation.getValue())
        }

        if (blurAnimation != null) {
            val blurRadius: Float = blurAnimation.getValue()
            if (blurRadius == 0f) {
                paint.setMaskFilter(null)
            } else if (blurRadius != blurMaskFilterRadius) {
                val blur = layer.getBlurMaskFilter(blurRadius)
                paint.setMaskFilter(blur)
            }
            blurMaskFilterRadius = blurRadius
        }
        if (dropShadowAnimation != null) {
            dropShadowAnimation.applyTo(paint)
        }

        for (i in pathGroups.indices) {
            val pathGroup = pathGroups[i]


            if (pathGroup.trimPath != null) {
                applyTrimPath(canvas, pathGroup, parentMatrix)
            } else {
                if (L.isTraceEnabled()) {
                    L.beginSection("StrokeContent#buildPath")
                }
                path.reset()
                for (j in pathGroup.paths.indices.reversed()) {
                    path.addPath(pathGroup.paths[j].getPath(), parentMatrix)
                }
                if (L.isTraceEnabled()) {
                    L.endSection("StrokeContent#buildPath")
                    L.beginSection("StrokeContent#drawPath")
                }
                canvas.drawPath(path, paint)
                if (L.isTraceEnabled()) {
                    L.endSection("StrokeContent#drawPath")
                }
            }
        }
        if (L.isTraceEnabled()) {
            L.endSection("StrokeContent#draw")
        }
    }

    private fun applyTrimPath(canvas: Canvas, pathGroup: PathGroup, parentMatrix: Matrix) {
        if (L.isTraceEnabled()) {
            L.beginSection("StrokeContent#applyTrimPath")
        }
        if (pathGroup.trimPath == null) {
            if (L.isTraceEnabled()) {
                L.endSection("StrokeContent#applyTrimPath")
            }
            return
        }
        path.reset()
        for (j in pathGroup.paths.indices.reversed()) {
            path.addPath(pathGroup.paths[j].getPath(), parentMatrix)
        }
        val animStartValue = pathGroup.trimPath.start.value / 100f
        val animEndValue = pathGroup.trimPath.end.value / 100f
        val animOffsetValue = pathGroup.trimPath.offset.value / 360f

        // If the start-end is ~100, consider it to be the full path.
        if (animStartValue < 0.01f && animEndValue > 0.99f) {
            canvas.drawPath(path, paint)
            if (L.isTraceEnabled()) {
                L.endSection("StrokeContent#applyTrimPath")
            }
            return
        }

        pm.setPath(path, false)
        var totalLength = pm.length
        while (pm.nextContour()) {
            totalLength += pm.length
        }
        val offsetLength = totalLength * animOffsetValue
        val startLength = totalLength * animStartValue + offsetLength
        val endLength = min(
            (totalLength * animEndValue + offsetLength).toDouble(),
            (startLength + totalLength - 1f).toDouble()
        ).toFloat()

        var currentLength = 0f
        for (j in pathGroup.paths.indices.reversed()) {
            trimPathPath.set(pathGroup.paths[j].getPath())
            trimPathPath.transform(parentMatrix)
            pm.setPath(trimPathPath, false)
            val length = pm.length
            if (endLength > totalLength && endLength - totalLength < currentLength + length && currentLength < endLength - totalLength) {
                // Draw the segment when the end is greater than the length which wraps around to the
                // beginning.
                var startValue = if (startLength > totalLength) {
                    (startLength - totalLength) / length
                } else {
                    0f
                }
                val endValue = min(((endLength - totalLength) / length).toDouble(), 1.0)
                    .toFloat()
                Utils.applyTrimPathIfNeeded(trimPathPath, startValue, endValue, 0f)
                canvas.drawPath(trimPathPath, paint)
            } else
                if (currentLength + length < startLength || currentLength > endLength) {
                    // Do nothing
                } else if (currentLength + length <= endLength && startLength < currentLength) {
                    canvas.drawPath(trimPathPath, paint)
                } else {
                    var startValue = if (startLength < currentLength) {
                        0f
                    } else {
                        (startLength - currentLength) / length
                    }
                    var endValue = if (endLength > currentLength + length) {
                        1f
                    } else {
                        (endLength - currentLength) / length
                    }
                    Utils.applyTrimPathIfNeeded(trimPathPath, startValue, endValue, 0f)
                    canvas.drawPath(trimPathPath, paint)
                }
            currentLength += length
        }
        if (L.isTraceEnabled()) {
            L.endSection("StrokeContent#applyTrimPath")
        }
    }

    override fun getBounds(outBounds: RectF, parentMatrix: Matrix?, applyParents: Boolean) {
        if (L.isTraceEnabled()) {
            L.beginSection("StrokeContent#getBounds")
        }
        path.reset()
        for (i in pathGroups.indices) {
            val pathGroup = pathGroups[i]
            for (j in pathGroup.paths.indices) {
                path.addPath(pathGroup.paths[j].getPath(), parentMatrix!!)
            }
        }
        path.computeBounds(rect, false)

        val width = (widthAnimation as FloatKeyframeAnimation).floatValue
        rect[rect.left - width / 2f, rect.top - width / 2f, rect.right + width / 2f] =
            rect.bottom + width / 2f
        outBounds.set(rect)
        // Add padding to account for rounding errors.
        outBounds[outBounds.left - 1, outBounds.top - 1, outBounds.right + 1] = outBounds.bottom + 1
        if (L.isTraceEnabled()) {
            L.endSection("StrokeContent#getBounds")
        }
    }

    private fun applyDashPatternIfNeeded(parentMatrix: Matrix) {
        if (L.isTraceEnabled()) {
            L.beginSection("StrokeContent#applyDashPattern")
        }
        if (dashPatternAnimations.isEmpty()) {
            if (L.isTraceEnabled()) {
                L.endSection("StrokeContent#applyDashPattern")
            }
            return
        }

        val scale = Utils.getScale(parentMatrix)
        for (i in dashPatternAnimations.indices) {
            dashPatternValues[i] = dashPatternAnimations[i].getValue()
            // If the value of the dash pattern or gap is too small, the number of individual sections
            // approaches infinity as the value approaches 0.
            // To mitigate this, we essentially put a minimum value on the dash pattern size of 1px
            // and a minimum gap size of 0.01.
            if (i % 2 == 0) {
                if (dashPatternValues[i] < 1f) {
                    dashPatternValues[i] = 1f
                }
            } else {
                if (dashPatternValues[i] < 0.1f) {
                    dashPatternValues[i] = 0.1f
                }
            }
            dashPatternValues[i] *= scale
        }
        val offset =
            if (dashPatternOffsetAnimation == null) 0f else dashPatternOffsetAnimation.getValue() * scale
        paint.setPathEffect(DashPathEffect(dashPatternValues, offset))
        if (L.isTraceEnabled()) {
            L.endSection("StrokeContent#applyDashPattern")
        }
    }

    override fun resolveKeyPath(
        keyPath: KeyPath, depth: Int, accumulator: List<KeyPath>, currentPartialKeyPath: KeyPath
    ) {
        MiscUtils.resolveKeyPath(keyPath, depth, accumulator, currentPartialKeyPath, this)
    }

    @CallSuper
    override fun <T> addValueCallback(property: T, callback: LottieValueCallback<T>?) {
        if (property === LottieProperty.OPACITY) {
            opacityAnimation.setValueCallback(callback as LottieValueCallback<Int>?)
        } else if (property === LottieProperty.STROKE_WIDTH) {
            widthAnimation.setValueCallback(callback as LottieValueCallback<Float>?)
        } else if (property === LottieProperty.COLOR_FILTER) {
            if (colorFilterAnimation != null) {
                layer.removeAnimation(colorFilterAnimation)
            }

            if (callback == null) {
                colorFilterAnimation = null
            } else {
                colorFilterAnimation =
                    ValueCallbackKeyframeAnimation(callback as LottieValueCallback<ColorFilter>?)
                colorFilterAnimation.addUpdateListener(this)
                layer.addAnimation(colorFilterAnimation)
            }
        } else if (property === LottieProperty.BLUR_RADIUS) {
            if (blurAnimation != null) {
                blurAnimation!!.setValueCallback(callback as LottieValueCallback<Float>?)
            } else {
                blurAnimation =
                    ValueCallbackKeyframeAnimation(callback as LottieValueCallback<Float>?)
                blurAnimation.addUpdateListener(this)
                layer.addAnimation(blurAnimation)
            }
        } else if (property === LottieProperty.DROP_SHADOW_COLOR && dropShadowAnimation != null) {
            dropShadowAnimation.setColorCallback(callback as LottieValueCallback<Int>?)
        } else if (property === LottieProperty.DROP_SHADOW_OPACITY && dropShadowAnimation != null) {
            dropShadowAnimation.setOpacityCallback(callback as LottieValueCallback<Float>?)
        } else if (property === LottieProperty.DROP_SHADOW_DIRECTION && dropShadowAnimation != null) {
            dropShadowAnimation.setDirectionCallback(callback as LottieValueCallback<Float>?)
        } else if (property === LottieProperty.DROP_SHADOW_DISTANCE && dropShadowAnimation != null) {
            dropShadowAnimation.setDistanceCallback(callback as LottieValueCallback<Float>?)
        } else if (property === LottieProperty.DROP_SHADOW_RADIUS && dropShadowAnimation != null) {
            dropShadowAnimation.setRadiusCallback(callback as LottieValueCallback<Float>?)
        }
    }

    /**
     * Data class to help drawing trim paths individually.
     */
    private class PathGroup(val trimPath: TrimPathContent?) {
        val paths: MutableList<PathContent> = ArrayList()
    }
}
