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

    abstract fun copy() : AnimatedVector2

    protected abstract fun interpolatedInternal(state: AnimationState) : Vec2

    final override fun interpolated(state: AnimationState): Vec2 {
        return dynamic.derive(interpolatedInternal(state), state)
    }

    @Serializable
    class Default(
        @SerialName("k")
        val value: List<Float>,

        @SerialName("x")
        override val expression: String? = null,

        @SerialName("ix")
        override val index: String? = null
    ) : AnimatedVector2() {

        @Transient
        private val vec = Vec2(value[0], value[1])

        override fun interpolatedInternal(state: AnimationState): Vec2 = vec

        override fun copy(): AnimatedVector2 {
            return Default(
                value = value,
                expression = expression,
                index = index
            )
        }
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

        private val path = Path()

        private val pathMeasure = PathMeasure()

        @Transient
        private val delegate: KeyframeAnimation<Vec2> = BaseKeyframeAnimation(
            expression = expression,
            keyframes = value,
            emptyValue = Offset.Zero,
            map = { s, e, p ->
                if (inTangent != null && outTangent != null && s != e) {
                    path.rewind()
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

        override fun copy(): AnimatedVector2 {
            return Animated(
                value = value,
                expression = expression,
                index = index
            )
        }
    }

    @Serializable
    class Split(
        val x: AnimatedNumber,
        val y: AnimatedNumber,
    ) : AnimatedVector2() {

        override val expression: String? get() = null

        override val index: String? get() = null

        override fun copy(): AnimatedVector2 {
            return Split(x.copy(), y.copy())
        }

        override fun interpolatedInternal(state: AnimationState): Vec2 {
            return Vec2(
                x.interpolated(state),
                y.interpolated(state)
            )
        }
    }
}

internal fun AnimatedVector2.Companion.defaultPosition() : AnimatedVector2 =
    AnimatedVector2.Default(FloatList3)

internal fun AnimatedVector2.Companion.defaultAnchorPoint() : AnimatedVector2 =
    AnimatedVector2.Default(FloatList3)

internal fun AnimatedVector2.Companion.defaultScale() : AnimatedVector2 =
    AnimatedVector2.Default(FloatList3_100)

private val FloatList3 = listOf(0f,0f,0f)
private val FloatList3_100 = listOf(100f, 100f, 100f)


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
    startPoint : List<Float>,
    endPoint: List<Float>,
    cp1: List<Float>,
    cp2: List<Float>
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

private fun List<Float>.hypot() = hypot(this[0], this[1])