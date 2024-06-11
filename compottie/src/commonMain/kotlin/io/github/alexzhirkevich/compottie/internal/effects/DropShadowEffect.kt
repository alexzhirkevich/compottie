package io.github.alexzhirkevich.compottie.internal.effects

import io.github.alexzhirkevich.compottie.internal.animation.AnimatedColor
import kotlinx.serialization.Contextual
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
@SerialName("25")
internal class DropShadowEffect(
    @SerialName("ef")
    override val values : List<EffectValue<@Contextual Any?>>
) : LayerEffect {

    val color  get() = (values.getOrNull(0) as? EffectValue.Color)?.value
    val opacity  get() = (values.getOrNull(1) as? EffectValue.Slider)?.value
    val angle  get() = (values.getOrNull(2) as? EffectValue.Angle)?.value
    val distance  get() = (values.getOrNull(3) as? EffectValue.Slider)?.value
    val blur  get() = (values.getOrNull(4) as? EffectValue.Slider)?.value
}
