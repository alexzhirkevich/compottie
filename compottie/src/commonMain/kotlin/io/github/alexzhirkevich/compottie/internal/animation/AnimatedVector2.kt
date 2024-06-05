package io.github.alexzhirkevich.compottie.internal.animation

import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.PathMeasure
import androidx.compose.ui.util.lerp
import io.github.alexzhirkevich.compottie.internal.AnimationState
import kotlinx.serialization.DeserializationStrategy
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable
import kotlinx.serialization.Transient
import kotlinx.serialization.json.JsonContentPolymorphicSerializer
import kotlinx.serialization.json.JsonElement
import kotlinx.serialization.json.booleanOrNull
import kotlinx.serialization.json.intOrNull
import kotlinx.serialization.json.jsonObject
import kotlinx.serialization.json.jsonPrimitive
import kotlin.math.hypot

internal typealias Vec2 = Offset

@Serializable(with = AnimatedVector2Serializer::class)
internal sealed interface AnimatedVector2 : KeyframeAnimation<Vec2>, Indexable {

    @Serializable
    class Default(
        @SerialName("k")
        val value: FloatArray,

        @SerialName("x")
        override val expression: String? = null,

        @SerialName("ix")
        override val index: String? = null
    ) : AnimatedVector2 {

        @Transient
        private val animationVector = Offset(value[0], value[1])

        override fun interpolated(state: AnimationState): Vec2 = animationVector
    }

    @Serializable
    class Animated(
        @SerialName("k")
        val value: List<VectorKeyframe>,

        @SerialName("x")
        override val expression: String? = null,

        @SerialName("ix")
        override val index: String? = null,
    ) : AnimatedVector2 {

        private val path by lazy {
            Path()
        }

        private val pathMeasure by lazy {
            PathMeasure()
        }

        @Transient
        private val delegate: KeyframeAnimation<Vec2> = BaseKeyframeAnimation(
            expression = expression,
            keyframes = value,
            emptyValue = Offset.Zero,
            map = { s, e, p ->

                if (inTangent != null && outTangent != null && !s.contentEquals(e)) {
                    path.reset()
                    path.createPath(s, e, outTangent, inTangent)
                    pathMeasure.setPath(path, false)

                    val length = pathMeasure.length

                    val distance: Float = easingX.transform(p) * length

                    val pos = pathMeasure.getPosition(distance)
                    val tangent = pathMeasure.getTangent(distance)

                    when {
                        distance < 0 -> pos + tangent * distance
                        distance > length -> pos + tangent * (distance - length)
                        else -> pos
                    }
                } else {
                    Offset(
                        lerp(s[0], e[0], easingX.transform(p)),
                        lerp(s[1], e[1], easingY.transform(p))
                    )
                }
            }
        )

        override fun interpolated(state: AnimationState): Offset {
            return delegate.interpolated(state)
        }
    }

    @Serializable
    class Split(
        val x: AnimatedValue,
        val y: AnimatedValue,
    ) : AnimatedVector2 {

        override val expression: String? get() = null
        override val index: String? get() = null

        override fun interpolated(state: AnimationState): Vec2 {
            return Offset(
                x.interpolated(state),
                y.interpolated(state)
            )
        }
    }
}


internal class AnimatedVector2Serializer : JsonContentPolymorphicSerializer<AnimatedVector2>(AnimatedVector2::class){

    override fun selectDeserializer(element: JsonElement): DeserializationStrategy<AnimatedVector2> {

        return when {
            element.jsonObject["s"]?.jsonPrimitive?.booleanOrNull == true ->
                AnimatedVector2.Split.serializer()

            element.jsonObject["a"]?.jsonPrimitive?.intOrNull == 1 ->
                AnimatedVector2.Animated.serializer()

            element.jsonObject["a"]?.jsonPrimitive?.intOrNull == 0 ->
                AnimatedVector2.Default.serializer()

            else -> error("Unknown transform")

        }
    }
}

private fun Path.createPath(
    startPoint : FloatArray,
    endPoint: FloatArray,
    cp1: FloatArray,
    cp2: FloatArray
) {
    moveTo(startPoint[0], startPoint[1])

    if ((cp1.hypot() != 0f || cp2.hypot() != 0f)) {
        cubicTo(
            startPoint[0] + cp1[0],
            startPoint[1] + cp1[1],
            endPoint[0] + cp2[0],
            endPoint[1] + cp2[1],
            endPoint[0], endPoint[1]
        )
    } else {
        lineTo(endPoint[0], endPoint[1])
    }
}

private fun FloatArray.hypot() = hypot(this[0], this[1])