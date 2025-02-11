package io.github.alexzhirkevich.compottie.internal.effects

import androidx.compose.ui.graphics.Paint
import io.github.alexzhirkevich.compottie.internal.AnimationState
import io.github.alexzhirkevich.compottie.internal.animation.RawProperty
import kotlinx.serialization.Contextual
import kotlinx.serialization.ExperimentalSerializationApi
import kotlinx.serialization.Serializable
import kotlinx.serialization.json.JsonClassDiscriminator

@OptIn(ExperimentalSerializationApi::class)
@Serializable
@JsonClassDiscriminator("ty")
internal sealed class LayerEffect {

    abstract val enabled: Boolean
    abstract val name: String?
    abstract val index: Int?
    abstract val values: List<EffectValue<*>>

    val valueByName by lazy {
        values.associateBy { it.name.orEmpty() }
    }

    val valueByIndex by lazy {
        values.associateBy { it.index ?: Int.MIN_VALUE }
    }

    abstract fun apply(
        paint : Paint,
        animationState: AnimationState,
        effectState: LayerEffectsState
    )

    abstract fun copy(): LayerEffect

    @Serializable
    class UnsupportedEffect : LayerEffect() {

        override val values: List<EffectValue<@Contextual RawProperty<@Contextual Any>>> = emptyList()
        override fun apply(
            paint: Paint,
            animationState: AnimationState,
            effectState: LayerEffectsState
        ) {
        }

        override val name: String? = null

        override val index: Int? = null

        override val enabled: Boolean = true

        override fun copy(): LayerEffect {
            return UnsupportedEffect()
        }
    }
}