package io.github.alexzhirkevich.compottie.internal.animation

import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.isSpecified
import androidx.compose.ui.geometry.isUnspecified
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.PathMeasure
import androidx.compose.ui.util.lerp
import kotlinx.serialization.ExperimentalSerializationApi
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable
import kotlinx.serialization.Transient
import kotlinx.serialization.json.JsonClassDiscriminator
import kotlin.math.hypot

typealias Vec2 = Offset

@OptIn(ExperimentalSerializationApi::class)
@Serializable()
@JsonClassDiscriminator("a")
internal sealed interface AnimatedVector2 : KeyframeAnimation<Vec2>, Indexable {

    @Serializable
    @SerialName("0")
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

        override fun interpolated(frame: Float): Vec2 = animationVector
    }

    @Serializable
    @SerialName("1")
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
            keyframes = value,
            emptyValue = Offset.Zero,
            map = { s, e, p, _ ->

                if (inTangent != null && outTangent != null && !s.contentEquals(e)) {
                    path.reset()
                    path.createPath(s, e, outTangent, inTangent)
                    pathMeasure.setPath(path, false)

                    val length = pathMeasure.length

                    val distance: Float = easingX.transform(p) * length

                    val pos = pathMeasure.getPosition(distance)
                    val tangent = pathMeasure.getTangent(distance)

                    when {
                        distance < 0 ->  pos + tangent * distance
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

        override fun interpolated(frame: Float): Offset {
            return delegate.interpolated(frame)
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