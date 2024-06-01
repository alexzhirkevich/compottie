package io.github.alexzhirkevich.compottie.internal.shapes

import androidx.compose.ui.geometry.Rect
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.util.fastForEach
import io.github.alexzhirkevich.compottie.internal.content.Content
import io.github.alexzhirkevich.compottie.internal.content.PathContent
import io.github.alexzhirkevich.compottie.internal.animation.AnimatedValue
import io.github.alexzhirkevich.compottie.internal.animation.AnimatedVector2
import io.github.alexzhirkevich.compottie.internal.helpers.CompoundTrimPath
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable
import kotlinx.serialization.Transient
import kotlin.math.min

@Serializable
@SerialName("rc")
internal class RectShape(

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

    @SerialName("r")
    val roundedCorners : AnimatedValue? = null,
) : Shape, PathContent {

    @Transient
    private val path = Path()

    @Transient
    private val trimPaths = CompoundTrimPath()

    override fun setContents(contentsBefore: List<Content>, contentsAfter: List<Content>) {
        contentsBefore.fastForEach {
            if (it.isSimultaneousTrimPath()) {
                trimPaths.addTrimPath(it)
            }
        }
    }

    override fun getPath(frame: Float): Path {

        if (hidden) {
            path.rewind()
            return path
        }

        val position = position.interpolated(frame)
        val size = size.interpolated(frame)
        var radius = roundedCorners?.interpolated(frame) ?: 0F

        val halfWidth = size.x / 2f
        val halfHeight = size.y / 2f

        val maxRadius = min(halfWidth.toDouble(), halfHeight.toDouble()).toFloat()
        if (radius > maxRadius) {
            radius = maxRadius
        }

        path.moveTo(position.x + halfWidth, position.y - halfHeight + radius)

        path.lineTo(position.x + halfWidth, position.y + halfHeight - radius)

        if (radius > 0) {
            path.arcTo(
                Rect(
                    position.x + halfWidth - 2 * radius,
                    position.y + halfHeight - 2 * radius,
                    position.x + halfWidth,
                    position.y + halfHeight
                ), 0f, 90f, false
            )
        }

        path.lineTo(position.x - halfWidth + radius, position.y + halfHeight)

        if (radius > 0) {
            path.arcTo(
                Rect(
                    position.x - halfWidth,
                    position.y + halfHeight - 2 * radius,
                    position.x - halfWidth + 2 * radius,
                    position.y + halfHeight
                ), 90f, 90f, false
            )
        }

        path.lineTo(position.x - halfWidth, position.y - halfHeight + radius)

        if (radius > 0) {

            path.arcTo(
                Rect(
                    position.x - halfWidth,
                    position.y - halfHeight,
                    position.x - halfWidth + 2 * radius,
                    position.y - halfHeight + 2 * radius
                ), 180f, 90f, false
            )
        }

        path.lineTo(position.x + halfWidth - radius, position.y - halfHeight)

        if (radius > 0) {

            path.arcTo(
                Rect(
                    position.x + halfWidth - 2 * radius,
                    position.y - halfHeight,
                    position.x + halfWidth,
                    position.y - halfHeight + 2 * radius
                ), 270f, 90f, false
            )
        }
        path.close()

        trimPaths.apply(path, frame)

        return path
    }
}