package io.github.alexzhirkevich.compottie.internal.schema.properties

import androidx.compose.animation.VectorConverter
import androidx.compose.animation.core.Animation
import androidx.compose.animation.core.AnimationVector
import androidx.compose.animation.core.AnimationVector2D
import androidx.compose.animation.core.AnimationVector4D
import androidx.compose.animation.core.Easing
import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.TwoWayConverter
import androidx.compose.animation.core.createAnimation
import androidx.compose.animation.core.keyframes
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.colorspace.ColorSpace

internal interface Keyframe<T> {
    val start : T?
    val end : T?
    val time : Int
    val easing : Easing
}

internal fun List<ValueKeyframe>.toAnimation() =
    asComposeKeyframes { it[0] }.vectorize(
    converter = TwoWayConverter(
        convertToVector = {
            AnimationVector(it)
        },
        convertFromVector = { it.value }
    )
).createAnimation(
    initialValue = AnimationVector(first().start?.getOrNull(0) ?: 0f),
    targetValue = AnimationVector(
        last().start?.getOrNull(0) ?: getOrNull(lastIndex-1)?.end?.getOrNull(0) ?: 0f
    ),
    initialVelocity = ZeroVector1d
)


internal fun List<VectorKeyframe>.to2DAnimation(): Animation<AnimationVector2D, AnimationVector2D> {

    val start = first().start
    val end = last().start ?: getOrNull(lastIndex-1)?.end

    val initialValue = if (start != null){
        AnimationVector(start[0], start[1])
    } else {
        ZeroVector2D
    }

    val targetValue = if (end != null){
        AnimationVector(end[0], end[1])
    } else {
        ZeroVector2D
    }

    return asComposeKeyframes { it }
        .vectorize(
            converter = TwoWayConverter(
                convertToVector = {
                    AnimationVector(it[0], it[1])
                },
                convertFromVector = {
                    floatArrayOf(it.v1, it.v2)
                }
            )
        ).createAnimation(
            initialValue = initialValue,
            targetValue = targetValue,
            initialVelocity = ZeroVector2D
        )
}
internal fun List<VectorKeyframe>.toColorAnimation(
    colorSpace: ColorSpace = first().start?.toColor()?.colorSpace ?: Color.Black.colorSpace
): Animation<AnimationVector4D, AnimationVector4D> {

    val start = first().start
    val end = last().end ?: getOrNull(lastIndex-1)?.end

    val initialValue = if (start != null){
        AnimationVector(
            start.getOrNull(3) ?: 1f,
            start[0],
            start[1],
            start[2],
        )
    } else {
        ZeroVector4D
    }

    val targetValue = if (end != null){
        AnimationVector(
            end.getOrNull(3) ?: 1f,
            end[0],
            end[1],
            end[2],
        )
    } else {
        ZeroVector4D
    }

    return asComposeKeyframes { it.toColor() }
        .vectorize(
            converter = Color.VectorConverter(colorSpace)
        ).createAnimation(
            initialValue = initialValue,
            targetValue = targetValue,
            initialVelocity = ZeroVector4D
        )
}

private fun <T, R> List<Keyframe<T>>.asComposeKeyframes(
    map : (T) -> R,
) = keyframes {
    durationMillis = last().time

    forEachIndexed { idx, it ->
        it.start?.let { s->
            map(s) at it.time using (it.easing ?: LinearEasing)
        }

        it.end?.takeIf { idx != lastIndex && idx != 0 }?.let{ e ->
            map(e) at get(idx-1).time
        }
    }
}

private fun FloatArray.toColor() = Color(this[0], this[1], this[2], getOrNull(3) ?: 0f)

private val ZeroVector1d = AnimationVector(0f)
private val ZeroVector2D = AnimationVector(0f,0f,)
private val ZeroVector4D = AnimationVector(0f,0f, 0f, 0f)
