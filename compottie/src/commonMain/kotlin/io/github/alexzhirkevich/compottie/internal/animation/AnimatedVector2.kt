package io.github.alexzhirkevich.compottie.internal.animation

import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.PathMeasure
import androidx.compose.ui.layout.ScaleFactor
import androidx.compose.ui.util.lerp
import io.github.alexzhirkevich.compottie.dynamic.PropertyProvider
import io.github.alexzhirkevich.compottie.dynamic.derive
import io.github.alexzhirkevich.compottie.dynamic.map
import io.github.alexzhirkevich.compottie.dynamic.toOffset
import io.github.alexzhirkevich.compottie.dynamic.toScaleFactor
import io.github.alexzhirkevich.compottie.dynamic.toSize
import io.github.alexzhirkevich.compottie.dynamic.toVec2
import io.github.alexzhirkevich.compottie.internal.AnimationState
import kotlinx.serialization.DeserializationStrategy
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable
import kotlinx.serialization.Transient
import kotlinx.serialization.json.JsonArray
import kotlinx.serialization.json.JsonContentPolymorphicSerializer
import kotlinx.serialization.json.JsonElement
import kotlinx.serialization.json.JsonObject
import kotlinx.serialization.json.booleanOrNull
import kotlinx.serialization.json.intOrNull
import kotlinx.serialization.json.jsonObject
import kotlinx.serialization.json.jsonPrimitive
import kotlin.math.hypot

internal typealias Vec2 = Offset

internal fun Vec2(x : Float, y : Float) : Vec2 = Offset(x,y)

@Serializable(with = AnimatedVector2Serializer::class)
internal sealed class AnimatedVector2 : KeyframeAnimation<Vec2>, Indexable {

    protected var dynamic: PropertyProvider<Vec2>? = null
        private set

    fun dynamic(provider: PropertyProvider<Vec2>?) {
        dynamic = provider
    }

    protected abstract fun interpolatedInternal(state: AnimationState) : Vec2

    final override fun interpolated(state: AnimationState): Vec2 {
        return dynamic.derive(interpolatedInternal(state), state)
    }

    @Serializable
    class Default(
        @SerialName("k")
        val value: FloatArray,

        @SerialName("x")
        override val expression: String? = null,

        @SerialName("ix")
        override val index: String? = null
    ) : AnimatedVector2() {

        @Transient
        private val animationVector = Offset(value[0], value[1])

        override fun interpolatedInternal(state: AnimationState): Vec2 = animationVector
    }

    @Serializable
    class Animated(
        @SerialName("k")
        val value: List<VectorKeyframe>,

        @SerialName("x")
        override val expression: String? = null,

        @SerialName("ix")
        override val index: String? = null,
    ) : AnimatedVector2() {

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

        override fun interpolatedInternal(state: AnimationState): Offset {
            return delegate.interpolated(state).let {
                dynamic.derive(it, state)
            }
        }
    }

    @Serializable
    class Split(
        val x: AnimatedNumber,
        val y: AnimatedNumber,
    ) : AnimatedVector2() {

        override val expression: String? get() = null
        override val index: String? get() = null

        override fun interpolatedInternal(state: AnimationState): Vec2 {
            return Offset(
                x.interpolated(state),
                y.interpolated(state)
            )
        }
    }
}


internal fun AnimatedVector2.interpolatedNorm(state: AnimationState) = interpolated(state) / 100f

internal fun AnimatedVector2.dynamicOffset(
    provider: PropertyProvider<Offset>?
) {
    dynamic(provider?.map(from = Offset::toVec2, to = Vec2::toOffset))

}

internal fun AnimatedVector2.dynamicSize(
    provider: PropertyProvider<Size>?
) {
    dynamic(provider?.map(from = Size::toVec2, to = Vec2::toSize))

}

internal fun AnimatedVector2.dynamicScale(
    provider: PropertyProvider<ScaleFactor>?
) {
    dynamic(provider?.map(from = ScaleFactor::toVec2, to = Vec2::toScaleFactor))
}

internal class AnimatedVector2Serializer : JsonContentPolymorphicSerializer<AnimatedVector2>(AnimatedVector2::class) {

    override fun selectDeserializer(element: JsonElement): DeserializationStrategy<AnimatedVector2> {

        val k = element.jsonObject["k"]

        return when {
            element.jsonObject["s"]?.jsonPrimitive?.booleanOrNull == true ->
                AnimatedVector2.Split.serializer()

            element.jsonObject["a"]?.jsonPrimitive?.intOrNull == 1 ||
                    k is JsonArray && k[0] is JsonObject ->
                AnimatedVector2.Animated.serializer()

            else -> AnimatedVector2.Default.serializer()
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