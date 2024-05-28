package io.github.alexzhirkevich.compottie.internal.schema.helpers

import io.github.alexzhirkevich.compottie.internal.schema.properties.Vector
import io.github.alexzhirkevich.compottie.internal.schema.properties.Value
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
internal class Transform(

    @SerialName("a")
    override val anchorPoint : Vector = Vector.Default(value = floatArrayOf(0f, 0f, 0f)),

    @SerialName("p")
    override val position : Vector? = null,

    @SerialName("s")
    override val scale : Vector? = null,

    @SerialName("r")
    override val rotation : Value? = null,

    @SerialName("o")
    override val opacity : Value = Value.Default(value = 100f),

    @SerialName("sk")
    override val skew: Value = Value.Default(value = 0f),

    @SerialName("sa")
    override val skewAxis: Value = Value.Default(value = 0f),
) : LottieTransform()


