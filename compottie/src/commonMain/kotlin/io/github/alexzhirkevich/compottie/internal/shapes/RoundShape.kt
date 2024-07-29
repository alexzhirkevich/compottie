package io.github.alexzhirkevich.compottie.internal.shapes

import androidx.compose.ui.graphics.Paint
import androidx.compose.ui.graphics.PathEffect
import io.github.alexzhirkevich.compottie.dynamic.DynamicShapeLayerProvider
import io.github.alexzhirkevich.compottie.dynamic.DynamicShapeProvider
import io.github.alexzhirkevich.compottie.dynamic.derive
import io.github.alexzhirkevich.compottie.dynamic.layerPath
import io.github.alexzhirkevich.compottie.internal.AnimationState
import io.github.alexzhirkevich.compottie.internal.animation.AnimatedNumber
import io.github.alexzhirkevich.compottie.internal.content.Content
import io.github.alexzhirkevich.compottie.internal.utils.appendPathEffect
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable
import kotlinx.serialization.Transient

@Serializable
@SerialName("rd")
internal class RoundShape(
    @SerialName("mn")
    override val matchName : String? = null,

    @SerialName("nm")
    override val name : String? = null,

    override val hidden : Boolean = false,

    @SerialName("r")
    val radius : AnimatedNumber,
) : Shape {



    @Transient
    private var dynamicShape : DynamicShapeProvider? = null

    fun isHidden(state : AnimationState) : Boolean {
        return dynamicShape?.hidden.derive(hidden, state)
    }

    override fun setContents(contentsBefore: List<Content>, contentsAfter: List<Content>) {

    }

    override fun setDynamicProperties(basePath: String?, properties: DynamicShapeLayerProvider?) {
        super.setDynamicProperties(basePath, properties)

        if (name != null) {
            dynamicShape = properties?.get(layerPath(basePath, name))
        }
    }

    override fun deepCopy(): Shape {
        return RoundShape(
            matchName = matchName,
            name = name,
            hidden = hidden,
            radius = radius.copy()
        )
    }

}

internal fun RoundShape.applyTo(paint: Paint, state: AnimationState) {
    if (!isHidden(state)) {
        val radius = radius.interpolated(state)
        if (radius > 1) {
            paint.appendPathEffect(PathEffect.cornerPathEffect(radius))
        }
    }
}
