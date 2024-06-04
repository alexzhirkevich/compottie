package io.github.alexzhirkevich.compottie.internal.layers

import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Matrix
import androidx.compose.ui.graphics.Paint
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.drawscope.DrawScope
import androidx.compose.ui.graphics.drawscope.drawIntoCanvas
import io.github.alexzhirkevich.compottie.internal.effects.LayerEffect
import io.github.alexzhirkevich.compottie.internal.helpers.BooleanInt
import io.github.alexzhirkevich.compottie.internal.helpers.LottieBlendMode
import io.github.alexzhirkevich.compottie.internal.helpers.Mask
import io.github.alexzhirkevich.compottie.internal.helpers.MatteMode
import io.github.alexzhirkevich.compottie.internal.helpers.Transform
import io.github.alexzhirkevich.compottie.internal.helpers.asComposeBlendMode
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
    override val startTime: Int? = null,

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

    @SerialName("sw")
    val width : Float,

    @SerialName("sh")
    val height : Float,

    @SerialName("sc")
    val colorHex : String,

    @SerialName("ef")
    override val effects: List<LayerEffect> = emptyList()
) : BaseLayer() {

    private val color: Color by lazy {

        val hex = colorHex.substringAfter("#")

        val chunked = hex.chunked(2).map { it.toInt(16) }
        val (r, g, b) = chunked

        val a = if (chunked.size == 4) {
            chunked.last()
        } else 255

        Color(red = r, green = g, blue = b, alpha = a)
    }

    private val paint by lazy {
        Paint().apply {
            isAntiAlias = true
            blendMode = this@SolidColorLayer.blendMode.asComposeBlendMode()
        }
    }

    @Transient
    private val path = Path()

    override fun drawLayer(
        drawScope: DrawScope,
        parentMatrix: Matrix,
        parentAlpha: Float,
        frame: Float
    ) {

        if (hidden) {
            return
        }
        paint.color = color

        paint.alpha = (color.alpha * parentAlpha *
                (transform.opacity?.interpolated(frame)?.div(100f) ?: 1f)).coerceIn(0f, 1f)

        if (paint.alpha == 0f) {
            return
        }

        path.reset()

        parentMatrix.map(Offset.Zero).let { path.moveTo(it.x, it.y) }
        parentMatrix.map(Offset(width, 0f)).let { path.lineTo(it.x, it.y) }
        parentMatrix.map(Offset(width, height)).let { path.lineTo(it.x, it.y) }
        parentMatrix.map(Offset(0f, height)).let { path.lineTo(it.x, it.y) }

        path.close()


        drawScope.drawIntoCanvas {
            it.drawPath(path, paint)
        }
    }
}