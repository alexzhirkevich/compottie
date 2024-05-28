package com.airbnb.lottie.animation.content

import android.graphics.Path
import android.graphics.PathMeasure
import com.airbnb.lottie.animation.keyframe.BaseKeyframeAnimation
import com.airbnb.lottie.value.LottieValueCallback
import kotlin.math.atan2
import kotlin.math.ceil
import kotlin.math.cos
import kotlin.math.floor
import kotlin.math.sin

class PolystarContent
    (
    lottieDrawable: LottieDrawable, layer: BaseLayer,
    polystarShape: PolystarShape
) : PathContent, BaseKeyframeAnimation.AnimationListener, KeyPathElementContent {
    private val path = Path()
    private val lastSegmentPath = Path()
    private val lastSegmentPathMeasure = PathMeasure()
    private val lastSegmentPosition = FloatArray(2)

    override val name: String = polystarShape.getName()
    private val lottieDrawable: LottieDrawable = lottieDrawable
    private val type: PolystarShape.Type = polystarShape.getType()
    private val hidden: Boolean = polystarShape.isHidden()
    private val isReversed: Boolean = polystarShape.isReversed()
    private val pointsAnimation: BaseKeyframeAnimation<*, Float> =
        polystarShape.getPoints().createAnimation()
    private val positionAnimation: BaseKeyframeAnimation<*, PointF> =
        polystarShape.getPosition().createAnimation()
    private val rotationAnimation: BaseKeyframeAnimation<*, Float>? =
        polystarShape.getRotation().createAnimation()
    private var innerRadiusAnimation: BaseKeyframeAnimation<*, Float>? = null
    private val outerRadiusAnimation: BaseKeyframeAnimation<*, Float> =
        polystarShape.getOuterRadius().createAnimation()
    private var innerRoundednessAnimation: BaseKeyframeAnimation<*, Float>? = null
    private val outerRoundednessAnimation: BaseKeyframeAnimation<*, Float>? =
        polystarShape.getOuterRoundedness().createAnimation()

    private val trimPaths = CompoundTrimPathContent()
    private var isPathValid = false

    init {
        if (type == PolystarShape.Type.STAR) {
            innerRadiusAnimation = polystarShape.getInnerRadius().createAnimation()
            innerRoundednessAnimation = polystarShape.getInnerRoundedness().createAnimation()
        } else {
            innerRadiusAnimation = null
            innerRoundednessAnimation = null
        }

        layer.addAnimation(pointsAnimation)
        layer.addAnimation(positionAnimation)
        layer.addAnimation(rotationAnimation)
        layer.addAnimation(outerRadiusAnimation)
        layer.addAnimation(outerRoundednessAnimation)
        if (type == PolystarShape.Type.STAR) {
            layer.addAnimation(innerRadiusAnimation)
            layer.addAnimation(innerRoundednessAnimation)
        }

        pointsAnimation.addUpdateListener(this)
        positionAnimation.addUpdateListener(this)
        rotationAnimation.addUpdateListener(this)
        outerRadiusAnimation.addUpdateListener(this)
        outerRoundednessAnimation.addUpdateListener(this)
        if (type == PolystarShape.Type.STAR) {
            innerRadiusAnimation.addUpdateListener(this)
            innerRoundednessAnimation.addUpdateListener(this)
        }
    }

    override fun onValueChanged() {
        invalidate()
    }

    private fun invalidate() {
        isPathValid = false
        lottieDrawable.invalidateSelf()
    }

    override fun setContents(contentsBefore: List<Content?>?, contentsAfter: List<Content?>?) {
        for (i in contentsBefore!!.indices) {
            val content = contentsBefore[i]
            if (content is TrimPathContent &&
                content.type == ShapeTrimPath.Type.SIMULTANEOUSLY
            ) {
                val trimPath = content
                trimPaths.addTrimPath(trimPath)
                trimPath.addListener(this)
            }
        }
    }

    override fun getPath(): Path {
        if (isPathValid) {
            return path
        }

        path.reset()

        if (hidden) {
            isPathValid = true
            return path
        }

        when (type) {
            PolystarShape.Type.STAR -> createStarPath()
            PolystarShape.Type.POLYGON -> createPolygonPath()
        }
        path.close()

        trimPaths.apply(path)

        isPathValid = true
        return path
    }

    private fun createStarPath() {
        val points: Float = pointsAnimation.value
        var currentAngle =
            (if (rotationAnimation == null) 0f else rotationAnimation.value).toDouble()
        // Start at +y instead of +x
        currentAngle -= 90.0
        // convert to radians
        currentAngle = Math.toRadians(currentAngle)
        // adjust current angle for partial points
        var anglePerPoint = (2 * Math.PI / points).toFloat()
        if (isReversed) {
            anglePerPoint *= -1f
        }
        val halfAnglePerPoint = anglePerPoint / 2.0f
        val partialPointAmount = points - points.toInt()
        if (partialPointAmount != 0f) {
            currentAngle += (halfAnglePerPoint * (1f - partialPointAmount)).toDouble()
        }

        val outerRadius: Float = outerRadiusAnimation.value
        val innerRadius: Float = innerRadiusAnimation.value

        var innerRoundedness = 0f
        if (innerRoundednessAnimation != null) {
            innerRoundedness = innerRoundednessAnimation.value / 100f
        }
        var outerRoundedness = 0f
        if (outerRoundednessAnimation != null) {
            outerRoundedness = outerRoundednessAnimation.value / 100f
        }

        var x: Float
        var y: Float
        var previousX: Float
        var previousY: Float
        var partialPointRadius = 0f
        if (partialPointAmount != 0f) {
            partialPointRadius = innerRadius + partialPointAmount * (outerRadius - innerRadius)
            x = (partialPointRadius * cos(currentAngle)).toFloat()
            y = (partialPointRadius * sin(currentAngle)).toFloat()
            path.moveTo(x, y)
            currentAngle += (anglePerPoint * partialPointAmount / 2f).toDouble()
        } else {
            x = (outerRadius * cos(currentAngle)).toFloat()
            y = (outerRadius * sin(currentAngle)).toFloat()
            path.moveTo(x, y)
            currentAngle += halfAnglePerPoint.toDouble()
        }

        // True means the line will go to outer radius. False means inner radius.
        var longSegment = false
        val numPoints = ceil(points.toDouble()) * 2
        var i = 0
        while (i < numPoints) {
            var radius = if (longSegment) outerRadius else innerRadius
            var dTheta = halfAnglePerPoint
            if (partialPointRadius != 0f && i.toDouble() == numPoints - 2) {
                dTheta = anglePerPoint * partialPointAmount / 2f
            }
            if (partialPointRadius != 0f && i.toDouble() == numPoints - 1) {
                radius = partialPointRadius
            }
            previousX = x
            previousY = y
            x = (radius * cos(currentAngle)).toFloat()
            y = (radius * sin(currentAngle)).toFloat()

            if (innerRoundedness == 0f && outerRoundedness == 0f) {
                path.lineTo(x, y)
            } else {
                val cp1Theta =
                    (atan2(previousY.toDouble(), previousX.toDouble()) - Math.PI / 2f).toFloat()
                val cp1Dx = cos(cp1Theta.toDouble()).toFloat()
                val cp1Dy = sin(cp1Theta.toDouble()).toFloat()

                val cp2Theta = (atan2(y.toDouble(), x.toDouble()) - Math.PI / 2f).toFloat()
                val cp2Dx = cos(cp2Theta.toDouble()).toFloat()
                val cp2Dy = sin(cp2Theta.toDouble()).toFloat()

                val cp1Roundedness = if (longSegment) innerRoundedness else outerRoundedness
                val cp2Roundedness = if (longSegment) outerRoundedness else innerRoundedness
                val cp1Radius = if (longSegment) innerRadius else outerRadius
                val cp2Radius = if (longSegment) outerRadius else innerRadius

                var cp1x = cp1Radius * cp1Roundedness * POLYSTAR_MAGIC_NUMBER * cp1Dx
                var cp1y = cp1Radius * cp1Roundedness * POLYSTAR_MAGIC_NUMBER * cp1Dy
                var cp2x = cp2Radius * cp2Roundedness * POLYSTAR_MAGIC_NUMBER * cp2Dx
                var cp2y = cp2Radius * cp2Roundedness * POLYSTAR_MAGIC_NUMBER * cp2Dy
                if (partialPointAmount != 0f) {
                    if (i == 0) {
                        cp1x *= partialPointAmount
                        cp1y *= partialPointAmount
                    } else if (i.toDouble() == numPoints - 1) {
                        cp2x *= partialPointAmount
                        cp2y *= partialPointAmount
                    }
                }

                path.cubicTo(previousX - cp1x, previousY - cp1y, x + cp2x, y + cp2y, x, y)
            }

            currentAngle += dTheta.toDouble()
            longSegment = !longSegment
            i++
        }


        val position: PointF = positionAnimation.value
        path.offset(position.x, position.y)
        path.close()
    }

    private fun createPolygonPath() {
        val points = floor(pointsAnimation.value.toDouble()).toInt()
        var currentAngle =
            (if (rotationAnimation == null) 0f else rotationAnimation.value).toDouble()
        // Start at +y instead of +x
        currentAngle -= 90.0
        // convert to radians
        currentAngle = Math.toRadians(currentAngle)
        // adjust current angle for partial points
        val anglePerPoint = (2 * Math.PI / points).toFloat()

        val roundedness: Float = outerRoundednessAnimation.value / 100f
        val radius: Float = outerRadiusAnimation.value
        var x: Float
        var y: Float
        var previousX: Float
        var previousY: Float
        x = (radius * cos(currentAngle)).toFloat()
        y = (radius * sin(currentAngle)).toFloat()
        path.moveTo(x, y)
        currentAngle += anglePerPoint.toDouble()

        val numPoints = ceil(points.toDouble())
        var i = 0
        while (i < numPoints) {
            previousX = x
            previousY = y
            x = (radius * cos(currentAngle)).toFloat()
            y = (radius * sin(currentAngle)).toFloat()

            if (roundedness != 0f) {
                val cp1Theta =
                    (atan2(previousY.toDouble(), previousX.toDouble()) - Math.PI / 2f).toFloat()
                val cp1Dx = cos(cp1Theta.toDouble()).toFloat()
                val cp1Dy = sin(cp1Theta.toDouble()).toFloat()

                val cp2Theta = (atan2(y.toDouble(), x.toDouble()) - Math.PI / 2f).toFloat()
                val cp2Dx = cos(cp2Theta.toDouble()).toFloat()
                val cp2Dy = sin(cp2Theta.toDouble()).toFloat()

                val cp1x = radius * roundedness * POLYGON_MAGIC_NUMBER * cp1Dx
                val cp1y = radius * roundedness * POLYGON_MAGIC_NUMBER * cp1Dy
                val cp2x = radius * roundedness * POLYGON_MAGIC_NUMBER * cp2Dx
                val cp2y = radius * roundedness * POLYGON_MAGIC_NUMBER * cp2Dy

                if (i.toDouble() == numPoints - 1) {
                    // When there is a huge stroke, it will flash if the path ends where it starts.
                    // We want the final bezier curve to end *slightly* before the start.
                    // The close() call at the end will complete the polystar.
                    // https://github.com/airbnb/lottie-android/issues/2329
                    lastSegmentPath.reset()
                    lastSegmentPath.moveTo(previousX, previousY)
                    lastSegmentPath.cubicTo(
                        previousX - cp1x,
                        previousY - cp1y,
                        x + cp2x,
                        y + cp2y,
                        x,
                        y
                    )
                    lastSegmentPathMeasure.setPath(lastSegmentPath, false)
                    lastSegmentPathMeasure.getPosTan(
                        lastSegmentPathMeasure.length * 0.9999f,
                        lastSegmentPosition,
                        null
                    )
                    path.cubicTo(
                        previousX - cp1x,
                        previousY - cp1y,
                        x + cp2x,
                        y + cp2y,
                        lastSegmentPosition[0],
                        lastSegmentPosition[1]
                    )
                } else {
                    path.cubicTo(previousX - cp1x, previousY - cp1y, x + cp2x, y + cp2y, x, y)
                }
            } else {
                if (i.toDouble() == numPoints - 1) {
                    // When there is a huge stroke, it will flash if the path ends where it starts.
                    // The close() call should make the path effectively equivalent.
                    // https://github.com/airbnb/lottie-android/issues/2329
                    i++
                    continue
                }
                path.lineTo(x, y)
            }

            currentAngle += anglePerPoint.toDouble()
            i++
        }

        val position: PointF = positionAnimation.value
        path.offset(position.x, position.y)
        path.close()
    }

    override fun resolveKeyPath(
        keyPath: KeyPath, depth: Int, accumulator: List<KeyPath>, currentPartialKeyPath: KeyPath
    ) {
        MiscUtils.resolveKeyPath(keyPath, depth, accumulator, currentPartialKeyPath, this)
    }

    override fun <T> addValueCallback(property: T, callback: LottieValueCallback<T?>?) {
        if (property === LottieProperty.POLYSTAR_POINTS) {
            pointsAnimation.setValueCallback(callback as LottieValueCallback<Float?>?)
        } else if (property === LottieProperty.POLYSTAR_ROTATION) {
            rotationAnimation.setValueCallback(callback as LottieValueCallback<Float?>?)
        } else if (property === LottieProperty.POSITION) {
            positionAnimation.setValueCallback(callback as LottieValueCallback<PointF?>?)
        } else if (property === LottieProperty.POLYSTAR_INNER_RADIUS && innerRadiusAnimation != null) {
            innerRadiusAnimation.setValueCallback(callback as LottieValueCallback<Float?>?)
        } else if (property === LottieProperty.POLYSTAR_OUTER_RADIUS) {
            outerRadiusAnimation.setValueCallback(callback as LottieValueCallback<Float?>?)
        } else if (property === LottieProperty.POLYSTAR_INNER_ROUNDEDNESS && innerRoundednessAnimation != null) {
            innerRoundednessAnimation.setValueCallback(callback as LottieValueCallback<Float?>?)
        } else if (property === LottieProperty.POLYSTAR_OUTER_ROUNDEDNESS) {
            outerRoundednessAnimation.setValueCallback(callback as LottieValueCallback<Float?>?)
        }
    }

    companion object {
        /**
         * This was empirically derived by creating polystars, converting them to
         * curves, and calculating a scale factor.
         * It works best for polygons and stars with 3 points and needs more
         * work otherwise.
         */
        private const val POLYSTAR_MAGIC_NUMBER = .47829f
        private const val POLYGON_MAGIC_NUMBER = .25f
    }
}
