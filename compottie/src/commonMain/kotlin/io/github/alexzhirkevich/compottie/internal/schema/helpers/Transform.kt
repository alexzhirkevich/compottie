package io.github.alexzhirkevich.compottie.internal.schema.helpers

import io.github.alexzhirkevich.compottie.internal.schema.properties.AnimatedVector2
import io.github.alexzhirkevich.compottie.internal.schema.properties.AnimatedValue
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
internal class Transform(

    @SerialName("a")
    override val anchorPoint : AnimatedVector2 = AnimatedVector2.Default(value = floatArrayOf(0f, 0f, 0f)),

    @SerialName("p")
    override val position : AnimatedVector2? = null,

    @SerialName("s")
    override val scale : AnimatedVector2? = null,

    @SerialName("r")
    override val rotation : AnimatedValue? = null,

    @SerialName("o")
    override val opacity : AnimatedValue? = null,

    @SerialName("sk")
    override val skew: AnimatedValue = AnimatedValue.Default(value = 0f),

    @SerialName("sa")
    override val skewAxis: AnimatedValue = AnimatedValue.Default(value = 0f),
) : LottieTransform()


