package io.github.alexzhirkevich.compottie.internal.effects

import io.github.alexzhirkevich.compottie.internal.helpers.BooleanIntSerializer
import io.github.alexzhirkevich.compottie.internal.utils.getAs
import kotlinx.serialization.Contextual
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
@SerialName("20")
internal class TintEffect(
    @SerialName("ef")
    override val values : List<EffectValue<@Contextual Any?>>,

    @SerialName("nm")
    override val name : String? = null,

    @SerialName("ix")
    override val index : Int? = null,

    @SerialName("en")
    @Serializable(with = BooleanIntSerializer::class)
    override val enabled : Boolean = true,
) : LayerEffect {

    val black
        get() = values.getAs<EffectValue.Color>(0)?.value


    val white
        get() = values.getAs<EffectValue.Color>(1)?.value

    val intensity
        get() = values.getAs<EffectValue.Slider>(2)?.value

    override fun copy(): LayerEffect {
        return TintEffect(values.map(EffectValue<Any?>::copy))
    }
}