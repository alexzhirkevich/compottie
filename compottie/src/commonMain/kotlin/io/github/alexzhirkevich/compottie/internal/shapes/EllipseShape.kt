package io.github.alexzhirkevich.compottie.internal.shapes

import androidx.compose.ui.graphics.Path
import androidx.compose.ui.util.fastForEach
import io.github.alexzhirkevich.compottie.internal.AnimationState
import io.github.alexzhirkevich.compottie.internal.content.Content
import io.github.alexzhirkevich.compottie.internal.content.PathContent
import io.github.alexzhirkevich.compottie.internal.animation.AnimatedVector2
import io.github.alexzhirkevich.compottie.internal.helpers.CompoundTrimPath
import io.github.alexzhirkevich.compottie.internal.layers.Layer
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable
import kotlinx.serialization.Transient

@Serializable
@SerialName("el")
internal class EllipseShape(

    @SerialName("mn")
    override val matchName : String? = null,

    @SerialName("nm")
    override val name : String? = null,

    @SerialName("hd")
    override val hidden : Boolean = false,

    @SerialName("d")
    val direction : Int = 1,

    @SerialName("p")
    val position : AnimatedVector2,

    @SerialName("s")
    val size : AnimatedVector2,
) : Shape, PathContent {

    @Transient
    override lateinit var layer: Layer

    @Transient
    private val path = Path()

    @Transient
    private val trimPaths = CompoundTrimPath()

    override fun getPath(state: AnimationState): Path {

        if (hidden) {
            path.rewind()
            return path
        }

        val size = size.interpolated(state)
        val halfWidth = size.x / 2f
        val halfHeight = size.y / 2f

        // TODO: handle bounds
        val cpW = halfWidth * ELLIPSE_CONTROL_POINT_PERCENTAGE
        val cpH = halfHeight * ELLIPSE_CONTROL_POINT_PERCENTAGE

        path.rewind()
//        if (circleShape.isReversed) {
//            path.moveTo(0f, -halfHeight)
//            path.cubicTo(0 - cpW, -halfHeight, -halfWidth, 0 - cpH, -halfWidth, 0f)
//            path.cubicTo(-halfWidth, 0 + cpH, 0 - cpW, halfHeight, 0f, halfHeight)
//            path.cubicTo(0 + cpW, halfHeight, halfWidth, 0 + cpH, halfWidth, 0f)
//            path.cubicTo(halfWidth, 0 - cpH, 0 + cpW, -halfHeight, 0f, -halfHeight)
//        } else {
            path.moveTo(0f, -halfHeight)
            path.cubicTo(0 + cpW, -halfHeight, halfWidth, 0 - cpH, halfWidth, 0f)
            path.cubicTo(halfWidth, 0 + cpH, 0 + cpW, halfHeight, 0f, halfHeight)
            path.cubicTo(0 - cpW, halfHeight, -halfWidth, 0 + cpH, -halfWidth, 0f)
            path.cubicTo(-halfWidth, 0 - cpH, 0 - cpW, -halfHeight, 0f, -halfHeight)
//        }

        val position = position.interpolated(state)

        path.translate(position)

        path.close()

        trimPaths.apply(path, state)

        return path
    }

    override fun setContents(contentsBefore: List<Content>, contentsAfter: List<Content>) {
        contentsBefore.fastForEach {
            if (it.isSimultaneousTrimPath()) {
                trimPaths.addTrimPath(it)
            }
        }
    }
}
private const val ELLIPSE_CONTROL_POINT_PERCENTAGE = 0.55228f
