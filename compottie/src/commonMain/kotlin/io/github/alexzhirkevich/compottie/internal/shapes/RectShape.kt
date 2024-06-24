package io.github.alexzhirkevich.compottie.internal.shapes

import androidx.compose.ui.geometry.Rect
import androidx.compose.ui.graphics.Path
import io.github.alexzhirkevich.compottie.dynamic.DynamicEllipseProvider
import io.github.alexzhirkevich.compottie.dynamic.DynamicRectProvider
import io.github.alexzhirkevich.compottie.dynamic.DynamicShapeLayerProvider
import io.github.alexzhirkevich.compottie.dynamic.DynamicShapeProvider
import io.github.alexzhirkevich.compottie.dynamic.derive
import io.github.alexzhirkevich.compottie.dynamic.layerPath
import io.github.alexzhirkevich.compottie.dynamic.toSize
import io.github.alexzhirkevich.compottie.internal.AnimationState
import io.github.alexzhirkevich.compottie.internal.content.Content
import io.github.alexzhirkevich.compottie.internal.content.PathContent
import io.github.alexzhirkevich.compottie.internal.animation.AnimatedNumber
import io.github.alexzhirkevich.compottie.internal.animation.AnimatedVector2
import io.github.alexzhirkevich.compottie.internal.animation.dynamicOffset
import io.github.alexzhirkevich.compottie.internal.animation.dynamicSize
import io.github.alexzhirkevich.compottie.internal.helpers.CompoundTrimPath
import io.github.alexzhirkevich.compottie.internal.helpers.CompoundSimultaneousTrimPath
import io.github.alexzhirkevich.compottie.internal.layers.Layer
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
    val roundedCorners : AnimatedNumber? = null,
) : Shape, PathContent {



    @Transient
    private val path = Path()

    @Transient
    private var trimPaths : CompoundTrimPath? = null

    @Transient
    private var dynamicShape : DynamicShapeProvider? = null

    override fun setContents(contentsBefore: List<Content>, contentsAfter: List<Content>) {
        trimPaths = CompoundSimultaneousTrimPath(contentsBefore)
    }

    override fun getPath(state: AnimationState): Path {

        if (dynamicShape?.hidden.derive(hidden, state)) {
            path.rewind()
            return path
        }

        path.rewind()

        val position = position.interpolated(state)
        val size = size.interpolated(state)
        var radius =  roundedCorners?.interpolated(state) ?: 0f

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

        trimPaths?.apply(path, state)

        return path
    }

    override fun setDynamicProperties(basePath: String?, properties: DynamicShapeLayerProvider) {
        super.setDynamicProperties(basePath, properties)

        if (name != null) {
            dynamicShape = properties[layerPath(basePath, name)]
            val dynamicRect = dynamicShape as? DynamicRectProvider?

            position.dynamicOffset(dynamicRect?.position)
            size.dynamicSize(dynamicRect?.size)
            roundedCorners?.dynamic(dynamicRect?.roundCorners)
        }
    }

    override fun deepCopy(): Shape {
        return RectShape(
            matchName = matchName,
            name = name,
            hidden = hidden,
            direction = direction,
            position = position.copy(),
            size = size.copy(),
            roundedCorners = roundedCorners?.copy()
        )
    }
}