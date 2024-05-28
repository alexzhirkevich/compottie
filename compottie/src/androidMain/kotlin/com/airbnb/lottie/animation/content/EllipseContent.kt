package com.airbnb.lottie.animation.content

import android.annotation.SuppressLint
import android.graphics.PointF
import androidx.compose.ui.graphics.Path
import com.airbnb.lottie.LottieDrawable
import com.airbnb.lottie.LottieProperty
import com.airbnb.lottie.animation.keyframe.BaseKeyframeAnimation
import com.airbnb.lottie.model.KeyPath
import com.airbnb.lottie.model.content.CircleShape
import com.airbnb.lottie.model.content.ShapeTrimPath
import com.airbnb.lottie.model.layer.BaseLayer
import com.airbnb.lottie.utils.MiscUtils
import com.airbnb.lottie.value.LottieValueCallback

@SuppressLint("RestrictedApi")
class EllipseContent
    (
    private val lottieDrawable: LottieDrawable,
    layer: BaseLayer,
    private val circleShape: CircleShape
) : PathContent, BaseKeyframeAnimation.AnimationListener, KeyPathElementContent {
    private val path = Path()

    override val name: String = circleShape.name
    private val sizeAnimation: BaseKeyframeAnimation<*, PointF> =
        circleShape.size.createAnimation()
    private val positionAnimation: BaseKeyframeAnimation<*, PointF> =
        circleShape.position.createAnimation()

    private val trimPaths = CompoundTrimPathContent()
    private var isPathValid = false

    init {
        layer.addAnimation(sizeAnimation)
        layer.addAnimation(positionAnimation)

        sizeAnimation.addUpdateListener(this)
        positionAnimation.addUpdateListener(this)
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
            if (content is TrimPathContent && content.type == ShapeTrimPath.Type.SIMULTANEOUSLY) {
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

        if (circleShape.isHidden) {
            isPathValid = true
            return path
        }

        val size = sizeAnimation.value
        val halfWidth = size.x / 2f
        val halfHeight = size.y / 2f

        // TODO: handle bounds
        val cpW = halfWidth * ELLIPSE_CONTROL_POINT_PERCENTAGE
        val cpH = halfHeight * ELLIPSE_CONTROL_POINT_PERCENTAGE

        path.reset()
        if (circleShape.isReversed) {
            path.moveTo(0f, -halfHeight)
            path.cubicTo(0 - cpW, -halfHeight, -halfWidth, 0 - cpH, -halfWidth, 0f)
            path.cubicTo(-halfWidth, 0 + cpH, 0 - cpW, halfHeight, 0f, halfHeight)
            path.cubicTo(0 + cpW, halfHeight, halfWidth, 0 + cpH, halfWidth, 0f)
            path.cubicTo(halfWidth, 0 - cpH, 0 + cpW, -halfHeight, 0f, -halfHeight)
        } else {
            path.moveTo(0f, -halfHeight)
            path.cubicTo(0 + cpW, -halfHeight, halfWidth, 0 - cpH, halfWidth, 0f)
            path.cubicTo(halfWidth, 0 + cpH, 0 + cpW, halfHeight, 0f, halfHeight)
            path.cubicTo(0 - cpW, halfHeight, -halfWidth, 0 + cpH, -halfWidth, 0f)
            path.cubicTo(-halfWidth, 0 - cpH, 0 - cpW, -halfHeight, 0f, -halfHeight)
        }

        val position = positionAnimation.value
        path.translate(position.x, position.y)

        path.close()

        trimPaths.apply(path)

        isPathValid = true
        return path
    }

    override fun resolveKeyPath(
        keyPath: KeyPath, depth: Int, accumulator: List<KeyPath>, currentPartialKeyPath: KeyPath
    ) {
        MiscUtils.resolveKeyPath(keyPath, depth, accumulator, currentPartialKeyPath, this)
    }

    override fun <T> addValueCallback(property: T, callback: LottieValueCallback<T>?) {
        if (property === LottieProperty.ELLIPSE_SIZE) {
            sizeAnimation.setValueCallback(callback as LottieValueCallback<PointF>?)
        } else if (property === LottieProperty.POSITION) {
            positionAnimation.setValueCallback(callback as LottieValueCallback<PointF>?)
        }
    }

    companion object {
        private const val ELLIPSE_CONTROL_POINT_PERCENTAGE = 0.55228f
    }
}
