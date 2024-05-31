package io.github.alexzhirkevich.compottie.internal.schema.shapes

import androidx.compose.ui.geometry.Rect
import androidx.compose.ui.graphics.Path
import io.github.alexzhirkevich.compottie.internal.content.Content
import io.github.alexzhirkevich.compottie.internal.content.PathContent
import io.github.alexzhirkevich.compottie.internal.schema.animation.AnimatedVector2
import io.github.alexzhirkevich.compottie.internal.schema.shapes.util.CompoundTrimPath
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
    private val path = Path()

    @Transient
    private val trimPaths = CompoundTrimPath()

    override fun getPath(frame: Int): Path {
        if (hidden) {
            return path
        }

        val pos = position.interpolated(frame)
        val size = size.interpolated(frame)

        path.reset()

        path.addOval(
            Rect(
                left = pos.x - size.x/2,
                top = pos.y - size.y/2,
                right = pos.x + size.y/2,
                bottom = pos.y + size.y/2
            )
        )

        return path
    }

    override fun setContents(contentsBefore: List<Content>, contentsAfter: List<Content>) {
        for (i in contentsBefore.indices) {
            val content = contentsBefore[i]
            if (content.isSimultaneousTrimPath()) {
                trimPaths.addTrimPath(content)
            }
        }
    }
}