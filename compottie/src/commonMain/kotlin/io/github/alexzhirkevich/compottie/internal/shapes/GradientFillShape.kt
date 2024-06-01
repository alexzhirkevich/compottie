package io.github.alexzhirkevich.compottie.internal.shapes

import androidx.compose.ui.geometry.MutableRect
import androidx.compose.ui.graphics.Canvas
import androidx.compose.ui.graphics.Matrix
import androidx.compose.ui.graphics.Paint
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.Shader
import androidx.compose.ui.graphics.drawscope.DrawScope
import androidx.compose.ui.graphics.drawscope.drawIntoCanvas
import androidx.compose.ui.util.fastForEach
import io.github.alexzhirkevich.compottie.internal.content.Content
import io.github.alexzhirkevich.compottie.internal.content.DrawingContent
import io.github.alexzhirkevich.compottie.internal.content.PathContent
import io.github.alexzhirkevich.compottie.internal.platform.GradientShader
import io.github.alexzhirkevich.compottie.internal.platform.addPath
import io.github.alexzhirkevich.compottie.internal.animation.AnimatedValue
import io.github.alexzhirkevich.compottie.internal.animation.AnimatedVector2
import io.github.alexzhirkevich.compottie.internal.animation.GradientColors
import io.github.alexzhirkevich.compottie.internal.animation.GradientType
import io.github.alexzhirkevich.compottie.internal.utils.set
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable
import kotlinx.serialization.Transient

@Serializable
@SerialName("gf")
internal class GradientFillShape(

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

    @Transient

    private val path = Path()
    @Transient
    private var paths: List<PathContent> = emptyList()

    @Transient
    private val paint = Paint()


    @Transient
    private val gradientCache = LinkedHashMap<Int, Shader>()


    override fun draw(drawScope: DrawScope, parentMatrix: Matrix, parentAlpha: Float, frame: Float) {

        paint.alpha = if (opacity != null) {
            (parentAlpha * opacity.interpolated(frame) / 100f).coerceIn(0f, 1f)
        }
        else {
            parentAlpha
        }

        paint.shader = GradientShader(
            type = type,
            startPoint = startPoint,
            endPoint = endPoint,
            colors = colors,
            frame = frame,
            matrix = parentMatrix,
            cache = gradientCache
        )
        drawScope.drawIntoCanvas {
            it.drawPath(path, paint)
        }
    }

    override fun getBounds(
        outBounds: MutableRect,
        parentMatrix: Matrix,
        applyParents: Boolean,
        frame: Float
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

