package io.github.alexzhirkevich.compottie.internal.effects

import io.github.alexzhirkevich.compottie.internal.animation.RawProperty
import io.github.alexzhirkevich.compottie.internal.helpers.BooleanIntSerializer
import io.github.alexzhirkevich.compottie.internal.utils.getAs
import kotlinx.serialization.Contextual
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
@SerialName("21")
internal class FillEffect(
    @SerialName("ef")
    override val values : List<EffectValue<@Contextual RawProperty<@Contextual Any>>>,

    @SerialName("nm")
    override val name : String? = null,

    @SerialName("ix")
    override val index : Int? = null,

    @SerialName("en")
    @Serializable(with = BooleanIntSerializer::class)
    override val enabled : Boolean = true,
) : LayerEffect() {

    val color get() = values.getAs<EffectValue.Color>(2)?.value

    val opacity get() = values.getAs<EffectValue.Slider>(6)?.value

    override fun copy(): LayerEffect {
        return FillEffect(values.map(EffectValue<RawProperty<Any>>::copy))
    }
}