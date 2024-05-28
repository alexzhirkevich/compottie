package com.airbnb.lottie.animation.content

import android.graphics.Canvas
import android.graphics.Matrix
import android.graphics.Paint
import android.graphics.Path
import androidx.compose.ui.geometry.Rect
import com.airbnb.lottie.LottieComposition
import com.airbnb.lottie.animation.LPaint
import com.airbnb.lottie.animation.keyframe.BaseKeyframeAnimation
import com.airbnb.lottie.model.content.ContentModel
import com.airbnb.lottie.utils.Utils

class ContentGroup internal constructor(
  lottieDrawable: LottieDrawable,
  layer: BaseLayer?,
  override val name: String,
  private val hidden: Boolean,
  @JvmField val contents: List<Content?>,
  transform: AnimatableTransform?
) : DrawingContent, PathContent, BaseKeyframeAnimation.AnimationListener, KeyPathElement {
    private val offScreenPaint: Paint = LPaint()
    private val offScreenRectF: RectF = RectF()

    private val matrix = Matrix()
    private val path = Path()
    private val rect: Rect = Rect()

    private val lottieDrawable: LottieDrawable = lottieDrawable
    private var pathContents: MutableList<PathContent>? = null
    private var transformAnimation: TransformKeyframeAnimation? = null

    constructor(
        lottieDrawable: LottieDrawable,
        layer: BaseLayer,
        shapeGroup: ShapeGroup,
        composition: LottieComposition
    ) : this(
        lottieDrawable,
        layer,
        shapeGroup.getName(),
        shapeGroup.isHidden(),
        contentsFromModels(lottieDrawable, composition, layer, shapeGroup.getItems()),
        findTransform(shapeGroup.getItems())
    )

    init {
        if (transform != null) {
            transformAnimation = transform.createAnimation()
            transformAnimation.addAnimationsToLayer(layer)
            transformAnimation.addListener(this)
        }

        val greedyContents: MutableList<GreedyContent> = ArrayList()
        for (i in contents.indices.reversed()) {
            val content = contents[i]
            if (content is GreedyContent) {
                greedyContents.add(content as GreedyContent)
            }
        }

        for (i in greedyContents.indices.reversed()) {
            greedyContents[i].absorbContent(contents.listIterator(contents.size))
        }
    }

    override fun onValueChanged() {
        lottieDrawable.invalidateSelf()
    }

    override fun setContents(contentsBefore: List<Content?>?, contentsAfter: List<Content?>?) {
        // Do nothing with contents after.
        val myContentsBefore: MutableList<Content?> = ArrayList(
            contentsBefore!!.size + contents.size
        )
        myContentsBefore.addAll(contentsBefore)

        for (i in contents.indices.reversed()) {
            val content = contents[i]
            content!!.setContents(myContentsBefore, contents.subList(0, i))
            myContentsBefore.add(content)
        }
    }

    val pathList: List<PathContent>
        get() {
            if (pathContents == null) {
                pathContents = ArrayList()
                for (i in contents.indices) {
                    val content = contents[i]
                    if (content is PathContent) {
                        pathContents.add(content)
                    }
                }
            }
            return pathContents!!
        }

    val transformationMatrix: Matrix
        get() {
            if (transformAnimation != null) {
                return transformAnimation.getMatrix()
            }
            matrix.reset()
            return matrix
        }

    override fun getPath(): Path {
        // TODO: cache this somehow.
        matrix.reset()
        if (transformAnimation != null) {
            matrix.set(transformAnimation.getMatrix())
        }
        path.reset()
        if (hidden) {
            return path
        }
        for (i in contents.indices.reversed()) {
            val content = contents[i]
            if (content is PathContent) {
                path.addPath(content.path, matrix)
            }
        }
        return path
    }

    fun draw(canvas: Canvas, parentMatrix: Matrix?, parentAlpha: Int) {
        if (hidden) {
            return
        }
        matrix.set(parentMatrix)
        val layerAlpha: Int
        if (transformAnimation != null) {
            matrix.preConcat(transformAnimation.getMatrix())
            val opacity =
                if (transformAnimation.getOpacity() == null) 100 else transformAnimation.getOpacity().value
            layerAlpha = ((opacity / 100f * parentAlpha / 255f) * 255).toInt()
        } else {
            layerAlpha = parentAlpha
        }

        // Apply off-screen rendering only when needed in order to improve rendering performance.
        val isRenderingWithOffScreen =
            lottieDrawable.isApplyingOpacityToLayersEnabled() && hasTwoOrMoreDrawableContent() && layerAlpha != 255
        if (isRenderingWithOffScreen) {
            offScreenRectF.set(0f, 0f, 0f, 0f)
            getBounds(offScreenRectF, matrix, true)
            offScreenPaint.alpha = layerAlpha
            Utils.saveLayerCompat(canvas, offScreenRectF, offScreenPaint)
        }

        val childAlpha = if (isRenderingWithOffScreen) 255 else layerAlpha
        for (i in contents.indices.reversed()) {
            val content: Any? = contents[i]
            if (content is DrawingContent) {
                content.draw(canvas, matrix, childAlpha)
            }
        }

        if (isRenderingWithOffScreen) {
            canvas.restore()
        }
    }

    private fun hasTwoOrMoreDrawableContent(): Boolean {
        var drawableContentCount = 0
        for (i in contents.indices) {
            if (contents[i] is DrawingContent) {
                drawableContentCount += 1
                if (drawableContentCount >= 2) {
                    return true
                }
            }
        }
        return false
    }

    fun getBounds(outBounds: RectF, parentMatrix: Matrix?, applyParents: Boolean) {
        matrix.set(parentMatrix)
        if (transformAnimation != null) {
            matrix.preConcat(transformAnimation.getMatrix())
        }
        rect.set(0f, 0f, 0f, 0f)
        for (i in contents.indices.reversed()) {
            val content = contents[i]
            if (content is DrawingContent) {
                content.getBounds(rect, matrix, applyParents)
                outBounds.union(rect)
            }
        }
    }

    override fun resolveKeyPath(
        keyPath: KeyPath,
        depth: Int,
        accumulator: MutableList<KeyPath>,
        currentPartialKeyPath: KeyPath
    ) {
        var currentPartialKeyPath: KeyPath = currentPartialKeyPath
        if (!keyPath.matches(name, depth) && "__container" != name) {
            return
        }

        if ("__container" != name) {
            currentPartialKeyPath = currentPartialKeyPath.addKey(name)

            if (keyPath.fullyResolvesTo(name, depth)) {
                accumulator.add(currentPartialKeyPath.resolve(this))
            }
        }

        if (keyPath.propagateToChildren(name, depth)) {
            val newDepth: Int = depth + keyPath.incrementDepthBy(name, depth)
            for (i in contents.indices) {
                val content = contents[i]
                if (content is KeyPathElement) {
                    val element: KeyPathElement? = content as KeyPathElement?
                    element.resolveKeyPath(keyPath, newDepth, accumulator, currentPartialKeyPath)
                }
            }
        }
    }

    override fun <T> addValueCallback(property: T, callback: LottieValueCallback<T>?) {
        if (transformAnimation != null) {
            transformAnimation.applyValueCallback<T>(property, callback)
        }
    }

    companion object {
        private fun contentsFromModels(
            drawable: LottieDrawable, composition: LottieComposition, layer: BaseLayer,
            contentModels: List<ContentModel>
        ): List<Content?> {
            val contents: MutableList<Content?> = ArrayList(contentModels.size)
            for (i in contentModels.indices) {
                val content = contentModels[i].toContent(drawable, composition, layer)
                if (content != null) {
                    contents.add(content)
                }
            }
            return contents
        }

        fun findTransform(contentModels: List<ContentModel?>): AnimatableTransform? {
            for (i in contentModels.indices) {
                val contentModel = contentModels[i]
                if (contentModel is AnimatableTransform) {
                    return contentModel as AnimatableTransform?
                }
            }
            return null
        }
    }
}
