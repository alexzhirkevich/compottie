package io.github.alexzhirkevich.compottie.internal.schema.shapes

import androidx.compose.ui.geometry.Rect
import androidx.compose.ui.geometry.RoundRect
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.util.fastForEach
import io.github.alexzhirkevich.compottie.internal.content.Content
import io.github.alexzhirkevich.compottie.internal.content.PathContent
import io.github.alexzhirkevich.compottie.internal.schema.animation.AnimatedValue
import io.github.alexzhirkevich.compottie.internal.schema.animation.AnimatedVector2
import io.github.alexzhirkevich.compottie.internal.schema.shapes.util.CompoundTrimPath
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable
import kotlinx.serialization.Transient

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

    override fun getPath(frame: Int): Path {

        if (hidden) {
            path.rewind()
            return path
        }

        val position = position.interpolated(frame)
        val size = size.interpolated(frame)
        val radius = roundedCorners?.interpolated(frame)

        path.rewind()

        val rect = Rect(
            top = position.x - size.x/2,
            left = position.y - size.y/2,
            bottom = position.x + size.x/2,
            right = position.y + size.y/2,
        )

        if (radius != null) {
            path.addRoundRect(
                RoundRect(
                    rect = rect,
                    radiusX = radius,
                    radiusY = radius
                )
            )
        } else {
            path.addRect(rect)
        }

        trimPaths.apply(path, frame)

        return path
    }
}