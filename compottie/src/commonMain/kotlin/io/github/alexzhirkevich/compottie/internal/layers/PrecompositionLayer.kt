package io.github.alexzhirkevich.compottie.internal.layers

import androidx.compose.ui.geometry.MutableRect
import androidx.compose.ui.graphics.Matrix
import androidx.compose.ui.graphics.Paint
import androidx.compose.ui.graphics.drawscope.DrawScope
import androidx.compose.ui.graphics.drawscope.drawIntoCanvas
import androidx.compose.ui.util.fastForEachReversed
import io.github.alexzhirkevich.compottie.internal.animation.AnimatedValue
import io.github.alexzhirkevich.compottie.internal.assets.LottieAsset
import io.github.alexzhirkevich.compottie.internal.content.DrawingContent
import io.github.alexzhirkevich.compottie.internal.helpers.BooleanInt
import io.github.alexzhirkevich.compottie.internal.helpers.Mask
import io.github.alexzhirkevich.compottie.internal.helpers.Transform
import io.github.alexzhirkevich.compottie.internal.platform.clipRect
import io.github.alexzhirkevich.compottie.internal.utils.Utils
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable
import kotlinx.serialization.Transient

@SerialName("0")
@Serializable
internal class PrecompositionLayer(
    val refId : String,

    @SerialName("w")
    val width : Float,

    @SerialName("h")
    val height : Float,

    @SerialName("tm")
    val timeRemapping : AnimatedValue? = null,

    @SerialName("ddd")
    override val is3d: BooleanInt = BooleanInt.No,

    @SerialName("ind")
    override val index: Int? = null,

    @SerialName("ip")
    override val inPoint: Float? = null,

    @SerialName("op")
    override val outPoint: Float? = null,

    @SerialName("st")
    override val startTime: Int? = null,

    @SerialName("nm")
    override val name: String? = null,

    @SerialName("sr")
    override val timeStretch: Float = 1f,

    @SerialName("parent")
    override val parent: Int? = null,

    @SerialName("hd")
    override val hidden: Boolean = false,

    @SerialName("masksProperties")
    override val masks: List<Mask>? = null,

    @SerialName("ks")
    override val transform: Transform,
) : BaseLayer() {

    @Transient
    private val newClipRect = MutableRect(0f, 0f, 0f, 0f)

    @Transient
    private val layerPaint = Paint()

    private val layers by lazy {
        (assets[refId] as? LottieAsset.PrecompositionAsset?)?.layers
    }

    override fun drawLayer(
        drawScope: DrawScope,
        parentMatrix: Matrix,
        parentAlpha: Float,
        frame: Float
    ) {

        newClipRect.set(0f, 0f, width, height)
        parentMatrix.map(newClipRect)

        // Apply off-screen rendering only when needed in order to improve rendering performance.
        val isDrawingWithOffScreen = layers.isNullOrEmpty() && parentAlpha < 1f

        drawScope.drawIntoCanvas { canvas ->

            if (isDrawingWithOffScreen) {
                layerPaint.alpha = parentAlpha
                Utils.saveLayerCompat(canvas, newClipRect, layerPaint)
            } else {
                canvas.save()
            }

            val childAlpha = if (isDrawingWithOffScreen) 1f else parentAlpha

            layers?.fastForEachReversed { layer ->
                // Only clip precomps. This mimics the way After Effects renders animations.
                val ignoreClipOnThisLayer = isContainerLayer

                if (!ignoreClipOnThisLayer && !newClipRect.isEmpty) {
                    canvas.clipRect(newClipRect)
                }

                (layer as? DrawingContent)?.draw(
                    drawScope,
                    parentMatrix,
                    childAlpha,
                    remappedFrame(frame)
                )
            }

            canvas.restore()
        }
    }

    private fun remappedFrame(frame: Float): Float {

        if (timeRemapping == null)
            return frame

        val f = if (timeStretch != 0f && !isContainerLayer) {
            frame / timeStretch
        } else frame

        return timeRemapping.interpolated(f) *
                composition.frameRate - composition.startFrame
    }
}