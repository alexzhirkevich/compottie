package io.github.alexzhirkevich.compottie.internal.shapes

import androidx.compose.ui.geometry.MutableRect
import androidx.compose.ui.graphics.Canvas
import androidx.compose.ui.graphics.Matrix
import androidx.compose.ui.graphics.Paint
import androidx.compose.ui.graphics.Path
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

    private val path = Path()

    private var paths: List<PathContent> = emptyList()

    private val paint = Paint()

    override fun draw(canvas: Canvas, parentMatrix: Matrix, parentAlpha: Float, frame: Float) {

        if (hidden) {
            return
        }

        paint.alpha = opacity?.interpolated(frame)?.let {
            (parentAlpha * it / 100f).coerceIn(0f, 1f)
        } ?: parentAlpha

        paint.color = color.interpolated(frame)

        path.reset()

        for (i in paths.indices) {
            path.addPath(paths[i].getPath(frame), parentMatrix)
        }

        canvas.drawPath(path, paint)
    }
    override fun getBounds(
        outBounds: MutableRect,
        parentMatrix: Matrix,
        applyParents: Boolean,
        frame: Float
    ) {

        path.reset()
        paths.fastForEach {
            this.path.addPath(it.getPath(frame), parentMatrix)
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