package io.github.alexzhirkevich.compottie.internal.effects

import io.github.alexzhirkevich.compottie.internal.utils.getAs
import kotlinx.serialization.Contextual
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable
import kotlinx.serialization.Transient

@Serializable
@SerialName("29")
internal class BlurEffect(
    @SerialName("ef")
    override val values : List<EffectValue<@Contextual Any?>>,

    @SerialName("nm")
    override val name : String? = null,

    @SerialName("ix")
    override val index : Int? = null,
) : LayerEffect {

    val radius get() = values.getAs<EffectValue.Slider>(0)?.value
    override fun copy(): LayerEffect {
        return BlurEffect(values.map(EffectValue<Any?>::copy))
    }
}