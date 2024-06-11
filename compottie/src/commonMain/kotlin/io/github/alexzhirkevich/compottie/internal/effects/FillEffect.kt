package io.github.alexzhirkevich.compottie.internal.effects

import kotlinx.serialization.Contextual
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
@SerialName("21")
internal class FillEffect(
    @SerialName("ef")
    override val values : List<EffectValue<@Contextual Any?>>
) : LayerEffect {

    val color get() = (values.getOrNull(2) as? EffectValue.Color)?.value

    val opacity get() = (values.getOrNull(6) as? EffectValue.Slider)?.value

}