package io.github.alexzhirkevich.compottie.internal.schema.shapes

import androidx.compose.ui.geometry.Rect
import androidx.compose.ui.geometry.RoundRect
import androidx.compose.ui.graphics.Path
import io.github.alexzhirkevich.compottie.internal.schema.Content
import io.github.alexzhirkevich.compottie.internal.schema.PathContent
import io.github.alexzhirkevich.compottie.internal.schema.properties.TrimPathType
import io.github.alexzhirkevich.compottie.internal.schema.properties.Value
import io.github.alexzhirkevich.compottie.internal.schema.properties.Vector
import io.github.alexzhirkevich.compottie.internal.schema.shapes.util.CompoundTrimPath
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable
import kotlinx.serialization.Transient

@Serializable
@SerialName("rc")
internal class Rect(

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

    @SerialName("r")
    val roundedCorners : Value? = null,
) : Shape, PathContent {

    @Transient
    private val path = Path()

    @Transient
    private val trimPaths = CompoundTrimPath()

    override fun setContents(contentsBefore: List<Content>, contentsAfter: List<Content>) {
        contentsBefore.forEach {
            if (it is TrimPath && it.type == TrimPathType.Simultaneously) {
                trimPaths.addTrimPath(it)
            }
        }
//       if (content is com.airbnb.lottie.animation.content.RoundedCornersContent) {
//                roundedCornersAnimation =
//                    (content as com.airbnb.lottie.animation.content.RoundedCornersContent).getRoundedCorners()
//            }
//        }
    }

    @Transient
    private var lastPosition: FloatArray? = null

    @Transient
    private var lastSize: FloatArray? = null

    @Transient
    private var lastCorners: Float? = null


    override fun getPath(time: Int): Path {

        if (hidden) {
            path.rewind()
            return path
        }

        val position = position.interpolated(time)
        val size = size.interpolated(time)
        val radius = roundedCorners?.interpolated(time)

        if (lastPosition.contentEquals(position) &&
            lastSize.contentEquals(size) &&
            lastCorners == radius
        ) {
            return path
        }

        lastPosition = position
        lastSize = size
        lastCorners = radius

        path.rewind()

        val left = (position[0] - size[0] / 2)
        val top = (position[1] - size[1] / 2)
        val bottom = top + size[1]
        val right = left + size[0]


        val rect = Rect(
            top = top,
            left = left,
            bottom = bottom,
            right = right,
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

        trimPaths.apply(path, time)

        return path
    }
}