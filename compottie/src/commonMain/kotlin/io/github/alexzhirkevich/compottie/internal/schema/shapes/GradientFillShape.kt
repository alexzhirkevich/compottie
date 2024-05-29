package io.github.alexzhirkevich.compottie.internal.schema.shapes

import androidx.compose.ui.geometry.MutableRect
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Canvas
import androidx.compose.ui.graphics.Matrix
import androidx.compose.ui.graphics.Paint
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.Shader
import androidx.compose.ui.graphics.TileMode
import androidx.compose.ui.util.fastForEach
import io.github.alexzhirkevich.compottie.internal.content.Content
import io.github.alexzhirkevich.compottie.internal.content.DrawingContent
import io.github.alexzhirkevich.compottie.internal.content.PathContent
import io.github.alexzhirkevich.compottie.internal.platform.MakeLinearGradient
import io.github.alexzhirkevich.compottie.internal.platform.MakeRadialGradient
import io.github.alexzhirkevich.compottie.internal.platform.addPath
import io.github.alexzhirkevich.compottie.internal.schema.properties.AnimatedVector2
import io.github.alexzhirkevich.compottie.internal.schema.properties.AnimatedValue
import io.github.alexzhirkevich.compottie.internal.schema.properties.GradientColors
import io.github.alexzhirkevich.compottie.internal.schema.properties.x
import io.github.alexzhirkevich.compottie.internal.schema.properties.y
import io.github.alexzhirkevich.compottie.internal.utils.set
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable
import kotlin.jvm.JvmInline
import kotlin.math.hypot

@Serializable
@SerialName("gf")
internal class GradientFill(

    @SerialName("mn")
    override val matchName : String? = null,

    @SerialName("nm")
    override val name : String? = null,

    @SerialName("hd")
    override val hidden : Boolean = false,

    @SerialName("o")
    val opacity : AnimatedValue? = null,

    @SerialName("s")
    val startPoint : AnimatedVector2,

    @SerialName("e")
    val endPoint : AnimatedVector2,

    @SerialName("t")
    val type : GradientType,

    /**
     * Gradient Highlight Length. Only if type is Radial
     * */
    @SerialName("h")
    val highlightLength : AnimatedValue? = null,

    /**
     * Highlight Angle. Only if type is Radial
     * */
    @SerialName("a")
    val highlightAngle : AnimatedValue? = null,

    @SerialName("g")
    val colors : GradientColors,
) : Shape, DrawingContent {

    private val path = Path()

    private var paths: List<PathContent> = emptyList()

    private val paint = Paint()


    override fun draw(canvas: Canvas, parentMatrix: Matrix, parentAlpha: Float, frame: Int) {

        paint.alpha = if (opacity != null) {
            (parentAlpha * opacity.interpolated(frame) / 100f).coerceIn(0f, 1f)
        }
        else {
            parentAlpha
        }

        paint.shader = GradientShader(type, startPoint, endPoint, colors, frame,parentMatrix)
    }

    override fun getBounds(
        outBounds: MutableRect,
        parentMatrix: Matrix,
        applyParents: Boolean,
        frame: Int
    ) {
        path.reset()
        paths.fastForEach {
            path.addPath(it.getPath(frame), parentMatrix)
        }
        outBounds.set(path.getBounds())
        // Add padding to account for rounding errors.
        outBounds.set(
            outBounds.left - 1,
            outBounds.top - 1,
            outBounds.right + 1,
            outBounds.bottom + 1
        )
    }

    override fun setContents(contentsBefore: List<Content>, contentsAfter: List<Content>) {
        paths = contentsAfter.filterIsInstance<PathContent>()
    }
}

internal fun GradientShader(
    type: GradientType,
    startPoint: AnimatedVector2,
    endPoint: AnimatedVector2,
    colors: GradientColors,
    frame : Int,
    matrix: Matrix
) : Shader {
    val start = startPoint.interpolated(frame)
    val end = endPoint.interpolated(frame)

    colors.colors.numberOfColors = colors.numberOfColors

    val c = colors.colors.interpolated(frame)

    return if (type == GradientType.Linear) {
        MakeLinearGradient(
            from = Offset(start.x, start.y),
            to = Offset(end.x, end.y),
            colors = c.colors,
            colorStops = c.colorStops,
            tileMode = TileMode.Clamp,
            matrix = matrix,
        )
    } else {
        val r = hypot((end.x - start.x), (end.y - start.y))

        MakeRadialGradient(
            radius = r,
            center = Offset(start.x, start.y),
            colors = c.colors,
            colorStops = c.colorStops,
            tileMode = TileMode.Clamp,
            matrix = matrix
        )
    }
}


@Serializable
@JvmInline
internal value class GradientType(val type : Byte) {
    companion object {
        val Linear = GradientType(1)
        val Radial = GradientType(2)
    }
}