package io.github.alexzhirkevich.compottie.internal.animation

import androidx.compose.ui.graphics.Path
import androidx.compose.ui.util.fastForEach
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
internal sealed interface AnimatedShape : PropertyAnimation<Path> {

    fun interpolatedMutable(state: AnimationState): Path

    fun copy() : AnimatedShape

    fun setClosed(closed : Boolean)

    @Serializable
    class Default(
        @SerialName("x")
        val expression: String? = null,

        @SerialName("ix")
        override val index: Int? = null,

        @SerialName("k")
        val bezier: Bezier,
    ) : AnimatedShape {

        @Transient
        private val tmpPath = Path()

        override fun setClosed(closed: Boolean) {
            bezier.setIsClosed(closed)
        }

        override fun interpolated(state: AnimationState): Path {
            bezier.mapPath(tmpPath)
            return tmpPath
        }

        override fun interpolatedMutable(state: AnimationState): Path {
            return Path().apply { bezier.mapPath(this) }
        }

        override fun copy(): AnimatedShape {
            return Default(
                expression = expression,
                index = index,
                bezier = bezier
            )
        }
    }

    @Serializable
    class Animated(
        @SerialName("x")
        val expression: String? = null,

        @SerialName("ix")
        override val index: Int? = null,

        @SerialName("k")
        override val keyframes: List<BezierKeyframe>,
    ) : AnimatedShape, KeyframeAnimation<Path, BezierKeyframe> {

        @Transient
        private val tmpPath = Path()

        @Transient
        private val tmpBezier = Bezier()

        @Transient
        private var delegate = BaseKeyframeAnimation(
            index = index,
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
            index = index,
            keyframes = keyframes,
            emptyValue = tmpPath,
            map = { s, e, p ->
                tmpBezier.interpolateBetween(s, e, easingX.transform(p))

                Path().apply {
                    tmpBezier.mapPath(this)
                }
            }
        )

        override fun setClosed(closed: Boolean) {
            keyframes.fastForEach {
                it.start?.setIsClosed(closed)
                it.end?.setIsClosed(closed)
            }
        }

        override fun interpolatedMutable(state: AnimationState): Path {
            return rawDelegate.interpolated(state)
        }

        override fun copy(): AnimatedShape {
            return Animated(
                expression = expression,
                index = index,
                keyframes = keyframes
            )
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

