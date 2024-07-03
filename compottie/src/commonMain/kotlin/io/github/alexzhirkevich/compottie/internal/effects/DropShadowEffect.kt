package io.github.alexzhirkevich.compottie.internal.effects

import io.github.alexzhirkevich.compottie.internal.animation.AnimatedColor
import io.github.alexzhirkevich.compottie.internal.utils.getAs
import kotlinx.serialization.Contextual
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
@SerialName("25")
internal class DropShadowEffect(
    @SerialName("ef")
    override val values : List<EffectValue<@Contextual Any?>>,

    @SerialName("nm")
    override val name : String? = null,

    @SerialName("ix")
    override val index : Int? = null,
) : LayerEffect {

    val color  get() = values.getAs<EffectValue.Color>(0)?.value
    val opacity  get() = values.getAs<EffectValue.Slider>(1)?.value
    val angle  get() = values.getAs<EffectValue.Angle>(2)?.value
    val distance  get() = values.getAs<EffectValue.Slider>(3)?.value
    val blur  get() = values.getAs<EffectValue.Slider>(4)?.value

    override fun copy(): LayerEffect {
        return DropShadowEffect(values.map(EffectValue<Any?>::copy))
    }
}