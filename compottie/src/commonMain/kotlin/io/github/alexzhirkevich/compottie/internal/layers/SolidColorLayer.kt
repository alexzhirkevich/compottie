package io.github.alexzhirkevich.compottie.internal.layers

import androidx.compose.ui.geometry.MutableRect
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Matrix
import androidx.compose.ui.graphics.Paint
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.drawscope.DrawScope
import io.github.alexzhirkevich.compottie.Compottie
import io.github.alexzhirkevich.compottie.internal.AnimationState
import io.github.alexzhirkevich.compottie.internal.animation.interpolatedNorm
import io.github.alexzhirkevich.compottie.internal.effects.LayerEffect
import io.github.alexzhirkevich.compottie.internal.helpers.BooleanInt
import io.github.alexzhirkevich.compottie.internal.helpers.LottieBlendMode
import io.github.alexzhirkevich.compottie.internal.helpers.Mask
import io.github.alexzhirkevich.compottie.internal.helpers.MatteMode
import io.github.alexzhirkevich.compottie.internal.helpers.Transform
import io.github.alexzhirkevich.compottie.internal.utils.set
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable
import kotlinx.serialization.Transient


@SerialName("1")
@Serializable
internal class SolidColorLayer(
    @SerialName("ks")
    override val transform: Transform = Transform(),

    @SerialName("ao")
    override val autoOrient: BooleanInt = BooleanInt.No,

    @SerialName("ddd")
    override val is3d: BooleanInt = BooleanInt.No,

    @SerialName("ind")
    override val index: Int? = null,

    @SerialName("bm")
    override val blendMode: LottieBlendMode = LottieBlendMode.Normal,

    @SerialName("cl")
    override val clazz: String? = null,

    @SerialName("ln")
    override val htmlId: String? = null,

    @SerialName("ip")
    override val inPoint: Float? = null,

    @SerialName("op")
    override val outPoint: Float? = null,

    @SerialName("st")
    override val startTime: Float? = null,

    @SerialName("nm")
    override val name: String? = null,

    @SerialName("sr")
    override val timeStretch: Float = 1f,

    @SerialName("parent")
    override val parent: Int? = null,

    @SerialName("tt")
    override val matteMode: MatteMode? = null,

    @SerialName("tp")
    override val matteParent: Int? = null,

    @SerialName("td")
    override val matteTarget: BooleanInt? = null,

    @SerialName("hd")
    override val hidden: Boolean = false,

    @SerialName("ct")
    override val collapseTransform: BooleanInt = BooleanInt.No,

    @SerialName("masksProperties")
    override val masks: List<Mask>? = null,

    @SerialName("hasMask")
    override val hasMask: Boolean? = null,

    @SerialName("sw")
    val width : Float,

    @SerialName("sh")
    val height : Float,

    @SerialName("sc")
    val colorHex : String,

    @SerialName("ef")
    override var effects: List<LayerEffect> = emptyList()
) : BaseLayer() {

    @Transient
    private val rect = MutableRect(0f, 0f, 0f, 0f)

    @Transient
    private val paint= Paint().apply {
        isAntiAlias = true

        try {
            val hex = colorHex.substringAfter("#")

            val chunked = hex.chunked(2).map { it.toInt(16) }
            val (r, g, b) = chunked

            val a = if (chunked.size == 4) {
                chunked.last()
            } else 255

            color = Color(red = r, green = g, blue = b, alpha = a)
        } catch (t: Throwable) {
            // TODO: sometimes colors are exported as #d9.0147ae147aedf.fdf3b645a1c8e6.028f5c28f5c8
            Compottie.logger?.warn("Solid color layer (${name}) with unrecognized color: $colorHex")
            color = Color.Transparent
        }
    }


    @Transient
    private val path = Path()

    override fun drawLayer(
        drawScope: DrawScope,
        parentMatrix: Matrix,
        parentAlpha: Float,
        state: AnimationState
    ) {

        if (hidden) {
            return
        }

        paint.alpha = (parentAlpha * (transform.opacity?.interpolatedNorm(state)?: 1f)).coerceIn(0f, 1f)

        if (paint.alpha == 0f) {
            return
        }

        path.rewind()

        parentMatrix.map(Offset.Zero).let { path.moveTo(it.x, it.y) }
        parentMatrix.map(Offset(width, 0f)).let { path.lineTo(it.x, it.y) }
        parentMatrix.map(Offset(width, height)).let { path.lineTo(it.x, it.y) }
        parentMatrix.map(Offset(0f, height)).let { path.lineTo(it.x, it.y) }

        path.close()

        drawScope.drawContext.canvas.drawPath(path, paint)
    }

    override fun getBounds(
        drawScope: DrawScope,
        parentMatrix: Matrix,
        applyParents: Boolean,
        state: AnimationState,
        outBounds: MutableRect
    ) {
        super.getBounds(drawScope, parentMatrix, applyParents, state, outBounds)
        rect.set(0f, 0f, width, height)
        boundsMatrix.map(rect)
        outBounds.set(rect)
    }

    override fun deepCopy(): Layer {
        return SolidColorLayer(
            transform = transform.deepCopy(),
            autoOrient = autoOrient,
            is3d = is3d,
            index = index,
            blendMode = blendMode,
            clazz = clazz,
            htmlId = htmlId,
            inPoint = inPoint,
            outPoint = outPoint,
            startTime = startTime,
            name = name,
            timeStretch = timeStretch,
            parent = parent,
            matteMode = matteMode,
            matteParent = matteParent,
            matteTarget = matteTarget,
            hidden = hidden,
            collapseTransform = collapseTransform,
            masks = masks?.map(Mask::deepCopy),
            hasMask = hasMask,
            width = width,
            height = height,
            colorHex = colorHex,
            effects = effects.map(LayerEffect::copy)
        )
    }
}