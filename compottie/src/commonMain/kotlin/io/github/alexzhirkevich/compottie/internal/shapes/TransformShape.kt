package io.github.alexzhirkevich.compottie.internal.shapes

import io.github.alexzhirkevich.compottie.dynamic.DynamicShapeLayerProvider
import io.github.alexzhirkevich.compottie.dynamic.DynamicShapeProvider
import io.github.alexzhirkevich.compottie.dynamic.derive
import io.github.alexzhirkevich.compottie.dynamic.layerPath
import io.github.alexzhirkevich.compottie.internal.AnimationState
import io.github.alexzhirkevich.compottie.internal.animation.AnimatedNumber
import io.github.alexzhirkevich.compottie.internal.animation.AnimatedTransform
import io.github.alexzhirkevich.compottie.internal.animation.AnimatedVector2
import io.github.alexzhirkevich.compottie.internal.animation.defaultAnchorPoint
import io.github.alexzhirkevich.compottie.internal.animation.defaultOpacity
import io.github.alexzhirkevich.compottie.internal.animation.defaultPosition
import io.github.alexzhirkevich.compottie.internal.animation.defaultRotation
import io.github.alexzhirkevich.compottie.internal.animation.defaultScale
import io.github.alexzhirkevich.compottie.internal.animation.defaultSkew
import io.github.alexzhirkevich.compottie.internal.animation.defaultSkewAxis
import io.github.alexzhirkevich.compottie.internal.content.Content
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable
import kotlinx.serialization.Transient

@Serializable
@SerialName("tr")
internal class TransformShape(

    @SerialName("mn")
    override val matchName : String? = null,

    @SerialName("nm")
    override val name : String? = null,

    @SerialName("hd")
    override val hidden : Boolean = false,

    @SerialName("a")
    override val anchorPoint : AnimatedVector2 = AnimatedVector2.defaultAnchorPoint(),

    @SerialName("p")
    override val position : AnimatedVector2  = AnimatedVector2.defaultPosition(),

    @SerialName("s")
    override val scale : AnimatedVector2  = AnimatedVector2.defaultScale(),

    @SerialName("r")
    override val rotation : AnimatedNumber = AnimatedNumber.defaultRotation(),

    @SerialName("o")
    override val opacity : AnimatedNumber = AnimatedNumber.defaultOpacity(),

    @SerialName("sk")
    override val skew: AnimatedNumber = AnimatedNumber.defaultSkew(),

    @SerialName("sa")
    override val skewAxis: AnimatedNumber = AnimatedNumber.defaultSkewAxis(),
) : AnimatedTransform(), Shape {

    @Transient
    private var dynamicShape : DynamicShapeProvider? = null

    override fun setContents(contentsBefore: List<Content>, contentsAfter: List<Content>) {

    }

    override fun isHidden(state: AnimationState): Boolean {
        return dynamicShape?.hidden.derive(hidden, state)
    }

    override fun setDynamicProperties(basePath: String?, properties: DynamicShapeLayerProvider?) {
        super.setDynamicProperties(basePath, properties)

        if (name != null) {
            dynamicShape = properties?.get(layerPath(basePath, name))
        }
    }

    override fun deepCopy(): Shape {
        return TransformShape(
            matchName = matchName,
            name = name,
            hidden = hidden,
            anchorPoint = anchorPoint.copy(),
            position = position.copy(),
            scale = scale.copy(),
            rotation = rotation.copy(),
            opacity = opacity.copy(),
            skew = skew.copy(),
            skewAxis = skewAxis.copy()
        )
    }
}

