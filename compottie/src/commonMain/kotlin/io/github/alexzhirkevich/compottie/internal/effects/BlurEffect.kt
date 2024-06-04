package io.github.alexzhirkevich.compottie.internal.effects

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable
import kotlinx.serialization.Transient

@Serializable
@SerialName("29")
internal class BlurEffect(
    @SerialName("ef")
    val values : List<EffectValue>
) : LayerEffect {

    @Transient
    val radius = (values[0] as EffectValue.Slider).value
}