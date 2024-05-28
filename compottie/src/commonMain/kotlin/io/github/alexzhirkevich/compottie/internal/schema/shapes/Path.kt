package io.github.alexzhirkevich.compottie.internal.schema.shapes

import androidx.compose.ui.graphics.Path
import io.github.alexzhirkevich.compottie.internal.schema.properties.ShapeProperties
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable
import kotlinx.serialization.Transient

@Serializable
@SerialName("sh")
internal class Path(
    @SerialName("mn")
    override val matchName : String? = null,

    @SerialName("nm")
    override val name : String? = null,

    @SerialName("hd")
    override val hidden : Boolean = false,

    @SerialName("ks")
    val properties : ShapeProperties
) : LayoutShape {

    @Transient
    private var path = Path()

    override fun getPath(time: Int): Path {
        path.rewind()

        val shape = properties.interpolated(time)

        val first = shape.vertices.first()

        path.moveTo(first[0], first[1])

        for (i in shape.vertices.indices){
            path.cubicTo(
                shape.vertices[i][0],
                shape.vertices[i][1],
                shape.inPoints[i][0],
                shape.inPoints[i][1],
                shape.outPoints[i][0],
                shape.outPoints[i][1],
            )
        }

        if (shape.closed){
            path.close()
        }
        return path
    }
}