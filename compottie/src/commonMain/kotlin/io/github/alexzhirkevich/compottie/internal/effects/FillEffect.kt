package io.github.alexzhirkevich.compottie.internal.effects

import io.github.alexzhirkevich.compottie.internal.utils.getAs
import kotlinx.serialization.Contextual
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
@SerialName("21")
internal class FillEffect(
    @SerialName("ef")
    override val values : List<EffectValue<@Contextual Any?>>
) : LayerEffect {

    val color get() = values.getAs<EffectValue.Color>(2)?.value

    val opacity get() = values.getAs<EffectValue.Slider>(6)?.value

}