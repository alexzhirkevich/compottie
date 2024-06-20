package io.github.alexzhirkevich.compottie.internal.animation

import androidx.compose.ui.graphics.Path
import io.github.alexzhirkevich.compottie.internal.AnimationState
import io.github.alexzhirkevich.compottie.internal.helpers.Bezier
import kotlinx.serialization.DeserializationStrategy
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable
import kotlinx.serialization.Transient
import kotlinx.serialization.json.JsonContentPolymorphicSerializer
import kotlinx.serialization.json.JsonElement
import kotlinx.serialization.json.JsonObject
import kotlinx.serialization.json.intOrNull
import kotlinx.serialization.json.jsonObject
import kotlinx.serialization.json.jsonPrimitive

@Serializable(with = AnimatedShapeSerializer::class)
internal sealed interface AnimatedShape : KeyframeAnimation<Path>, Indexable {

    fun interpolatedRaw(state: AnimationState): Path

    @Serializable
    class Default(
        @SerialName("x")
        override val expression: String? = null,

        @SerialName("ix")
        override val index: String? = null,

        @SerialName("k")
        val bezier: Bezier,
    ) : AnimatedShape {

        @Transient
        private val tmpPath = Path()

        override fun interpolated(state: AnimationState): Path {
            tmpPath.reset()
            bezier.mapPath(tmpPath)
            return tmpPath
        }

        override fun interpolatedRaw(state: AnimationState): Path {
            return interpolated(state)
        }
    }

    @Serializable
    class Animated(
        @SerialName("x")
        override val expression: String? = null,

        @SerialName("ix")
        override val index: String? = null,

        @SerialName("k")
        val keyframes: List<BezierKeyframe>,
    ) : AnimatedShape, KeyframeAnimation<Path> {

        @Transient
        private val tmpPath = Path()

        @Transient
        private val tmpBezier = Bezier()

        @Transient
        private var delegate = BaseKeyframeAnimation(
            expression = expression,
            keyframes = keyframes,
            emptyValue = tmpPath,
            map = { s, e, p ->
                tmpBezier.interpolateBetween(s, e, easingX.transform(p))
                tmpBezier.mapPath(tmpPath)
                tmpPath
            }
        )

        @Transient
        private var rawDelegate = BaseKeyframeAnimation(
            expression = expression,
            keyframes = keyframes,
            emptyValue = tmpPath,
            map = { s, e, p ->
                tmpBezier.interpolateBetween(s, e, easingX.transform(p))

                Path().apply {
                    tmpBezier.mapPath(this)
                }
            }
        )

        override fun interpolatedRaw(state: AnimationState): Path {
            return rawDelegate.interpolated(state)
        }

        override fun interpolated(state: AnimationState): Path {
            return delegate.interpolated(state)
        }
    }
}

internal class AnimatedShapeSerializer : JsonContentPolymorphicSerializer<AnimatedShape>(
    baseClass = AnimatedShape::class
){
    override fun selectDeserializer(element: JsonElement): DeserializationStrategy<AnimatedShape> {
        val k = requireNotNull(element.jsonObject["k"]){
            "Animated shape must have 'k' parameter"
        }

        return if (element.jsonObject["a"]?.jsonPrimitive?.intOrNull == 0 || k is JsonObject){
            AnimatedShape.Default.serializer()
        } else {
            AnimatedShape.Animated.serializer()
        }
    }

}

