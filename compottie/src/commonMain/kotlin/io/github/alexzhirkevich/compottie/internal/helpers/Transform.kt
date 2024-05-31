package io.github.alexzhirkevich.compottie.internal.schema.helpers

import io.github.alexzhirkevich.compottie.internal.schema.animation.AnimatedTransform
import io.github.alexzhirkevich.compottie.internal.schema.animation.AnimatedVector2
import io.github.alexzhirkevich.compottie.internal.schema.animation.AnimatedValue
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
internal class Transform(

    @SerialName("a")
    override val anchorPoint : AnimatedVector2? = null ,

    @SerialName("p")
    override val position : AnimatedVector2? = null,

    @SerialName("s")
    override val scale : AnimatedVector2? = null,

    @SerialName("r")
    override val rotation : AnimatedValue? = null,

    @SerialName("o")
    override val opacity : AnimatedValue? = null,

    @SerialName("sk")
    override val skew: AnimatedValue? = null,

    @SerialName("sa")
    override val skewAxis: AnimatedValue? = null,
) : AnimatedTransform() {

}


