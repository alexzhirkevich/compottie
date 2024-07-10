package io.github.alexzhirkevich.compottie.internal.animation

import androidx.compose.ui.util.fastMap
import io.github.alexzhirkevich.compottie.internal.AnimationState
import io.github.alexzhirkevich.compottie.internal.helpers.ColorsWithStops
import kotlinx.serialization.ExperimentalSerializationApi
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable
import kotlinx.serialization.Transient
import kotlinx.serialization.json.JsonClassDiscriminator
import kotlin.jvm.JvmInline



@OptIn(ExperimentalSerializationApi::class)
@Serializable
@JsonClassDiscriminator("a")
internal abstract class AnimatedGradient : ExpressionProperty<ColorsWithStops>() {

    @Transient
    var numberOfColors: Int = 0


    private val tempExpressionColors by lazy {
        ColorsWithStops(numberOfColors)
    }

    abstract fun copy() : AnimatedGradient

    override fun mapEvaluated(e: Any): ColorsWithStops {
        return when (e) {
            is ColorsWithStops -> e
            is List<*> -> {
                tempExpressionColors.fill(
                    (e as List<Number>).fastMap(Number::toFloat),
                    numberOfColors
                )
                tempExpressionColors
            }

            else -> error("Failed to cast $e to gradient vector")
        }
    }

    @SerialName("0")
    @Serializable
    class Default(
        @SerialName("k")
        val colorsVector: List<Float>,

        @SerialName("ix")
        override val index: Int? = null,

        @SerialName("x")
        override val expression: String? = null
    ) : AnimatedGradient() {

        private val tempColors by lazy {
            ColorsWithStops(numberOfColors).apply {
                fill(colorsVector, numberOfColors)
            }
        }

        override fun raw(state: AnimationState): ColorsWithStops {
            return tempColors
        }

        override fun copy(): AnimatedGradient {
            return Default(
                colorsVector = colorsVector,
                index = index,
                expression = expression
            )
        }
    }

    @SerialName("1")
    @Serializable
    class Animated(
        @SerialName("k")
        override val keyframes: List<VectorKeyframe>,
        @SerialName("ix")
        override val index: Int? = null,
        @SerialName("x")
        override val expression: String? = null
    ) : AnimatedGradient(), AnimatedKeyframeProperty<ColorsWithStops, VectorKeyframe> {

        private val tempColors by lazy {
            ColorsWithStops(numberOfColors)
        }

        private val tempColorsA by lazy {
            ColorsWithStops(numberOfColors)
        }

        private val tempColorsB by lazy {
            ColorsWithStops(numberOfColors)
        }

        @Transient
        private val delegate = BaseKeyframeAnimation(
            index = index,
            keyframes = keyframes,
            emptyValue = tempColors
        ) { s, e, p ->
            val progress = easingX.transform(p)

            tempColorsA.fill(s, numberOfColors)
            tempColorsB.fill(e, numberOfColors)

            tempColors.apply {
                interpolateBetween(tempColorsA, tempColorsB, progress)
            }
        }

        override fun copy(): AnimatedGradient {
            return Animated(
                keyframes = keyframes,
                index = index,
                expression = expression
            )
        }

        override fun raw(state: AnimationState): ColorsWithStops {
            return delegate.raw(state)
        }
    }
}
