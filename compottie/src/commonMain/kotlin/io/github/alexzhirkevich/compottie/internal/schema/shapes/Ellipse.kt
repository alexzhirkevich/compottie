package io.github.alexzhirkevich.compottie.internal.schema.shapes

import androidx.compose.ui.geometry.Rect
import androidx.compose.ui.graphics.Path
import io.github.alexzhirkevich.compottie.internal.schema.Content
import io.github.alexzhirkevich.compottie.internal.schema.PathContent
import io.github.alexzhirkevich.compottie.internal.schema.properties.TrimPathType
import io.github.alexzhirkevich.compottie.internal.schema.properties.Vector
import io.github.alexzhirkevich.compottie.internal.schema.shapes.util.CompoundTrimPath
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable
import kotlinx.serialization.Transient

@Serializable
@SerialName("el")
internal class Ellipse(

    @SerialName("mn")
    override val matchName : String? = null,

    @SerialName("nm")
    override val name : String? = null,

    @SerialName("hd")
    override val hidden : Boolean = false,

    @SerialName("d")
    val direction : Int = 1,

    @SerialName("p")
    val position : Vector,

    @SerialName("s")
    val size : Vector,
) : Shape, PathContent {

    @Transient
    private val path = Path()

    @Transient
    private val trimPaths = CompoundTrimPath()

    private var lastPosition : FloatArray? = null
    private var lastSize : FloatArray? = null

    override fun setContents(contentsBefore: List<Content>, contentsAfter: List<Content>) {
        contentsBefore.forEach {
            if (it is TrimPath && it.type == TrimPathType.Simultaneously){
                trimPaths.addTrimPath((it))
            }
        }
    }

    override fun getPath(time: Int): Path {

        if (hidden) {
            path.rewind()
            return path
        }

        val position = position.interpolated(time)
        val size = size.interpolated(time)

        if (lastPosition.contentEquals(position) && lastSize.contentEquals(size)){
            return path
        }

        lastPosition = position
        lastSize = size

        path.rewind()

        val top = (position[0] - size[0] / 2)
        val left = (position[1] - size[1] / 2)

        path.addOval(
            Rect(
                top = top,
                left = left,
                bottom = top + size[0],
                right = left + size[1],
            )
        )

        trimPaths.apply(path, time)

        return path
    }
}