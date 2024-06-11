package io.github.alexzhirkevich.compottie.internal.effects

import kotlinx.serialization.Contextual
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
@SerialName("20")
internal class TintEffect(
    @SerialName("ef")
    override val values : List<EffectValue<@Contextual Any?>>
) : LayerEffect {

    val black by lazy {
        (values.getOrNull(0) as? EffectValue.Color)?.value
    }

    val white by lazy {
        (values.getOrNull(1) as? EffectValue.Color)?.value
    }

    val intensity by lazy {
        (values.getOrNull(2) as? EffectValue.Slider)?.value
    }
}