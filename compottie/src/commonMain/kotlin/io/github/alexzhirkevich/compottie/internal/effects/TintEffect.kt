package io.github.alexzhirkevich.compottie.internal.effects

import io.github.alexzhirkevich.compottie.internal.utils.getAs
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
        values.getAs<EffectValue.Color>(0)?.value
    }

    val white by lazy {
        values.getAs<EffectValue.Color>(1)?.value
    }

    val intensity by lazy {
        values.getAs<EffectValue.Slider>(2)?.value
    }
}