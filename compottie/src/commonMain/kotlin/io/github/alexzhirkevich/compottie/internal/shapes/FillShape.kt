package io.github.alexzhirkevich.compottie.internal.shapes

import androidx.compose.ui.geometry.MutableRect
import androidx.compose.ui.graphics.Matrix
import androidx.compose.ui.graphics.Paint
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.drawscope.DrawScope
import androidx.compose.ui.graphics.drawscope.drawIntoCanvas
import androidx.compose.ui.util.fastForEach
import io.github.alexzhirkevich.compottie.internal.content.Content
import io.github.alexzhirkevich.compottie.internal.content.DrawingContent
import io.github.alexzhirkevich.compottie.internal.content.PathContent
import io.github.alexzhirkevich.compottie.internal.platform.addPath
import io.github.alexzhirkevich.compottie.internal.animation.AnimatedColor
import io.github.alexzhirkevich.compottie.internal.animation.AnimatedValue
import io.github.alexzhirkevich.compottie.internal.helpers.BooleanInt
import io.github.alexzhirkevich.compottie.internal.utils.set
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable
import kotlinx.serialization.Transient

@Serializable
@SerialName("fl")
internal class FillShape(

    @SerialName("mn")
    override val matchName : String? = null,

    @SerialName("nm")
    override val name : String? = null,

    @SerialName("hd")
    override val hidden : Boolean = false,

    @SerialName("a")
    val withAlpha : BooleanInt = BooleanInt.No,

    @SerialName("d")
    val direction : Int = 1,

    @SerialName("o")
    val opacity : AnimatedValue?,

    @SerialName("c")
    val color : AnimatedColor,
) : Shape, DrawingContent {

    @Transient
    private val path = Path()

    @Transient
    private var paths: List<PathContent> = emptyList()

    @Transient
    private val paint = Paint()

    @Transient
    private var roundShape : RoundShape? = null

    override fun draw(drawScope: DrawScope, parentMatrix: Matrix, parentAlpha: Float, frame: Float) {

        if (hidden) {
            return
        }

        paint.color = color.interpolated(frame)

        paint.alpha = opacity?.interpolated(frame)?.let {
            (parentAlpha * it / 100f).coerceIn(0f, 1f)
        } ?: parentAlpha

        path.reset()

        paths.fastForEach {
            path.addPath(it.getPath(frame), parentMatrix)
        }

        roundShape?.applyTo(paint, frame)

        drawScope.drawIntoCanvas { canvas ->
            canvas.drawPath(path, paint)
        }
    }
    override fun getBounds(
        drawScope: DrawScope,
        parentMatrix: Matrix,
        applyParents: Boolean,
        frame: Float,
        outBounds: MutableRect
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

        roundShape = contentsBefore?.find { it is RoundShape } as? RoundShape
    }
}