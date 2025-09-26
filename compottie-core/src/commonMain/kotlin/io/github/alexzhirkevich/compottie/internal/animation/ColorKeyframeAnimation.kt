package io.github.alexzhirkevich.compottie.internal.animation

import androidx.compose.ui.graphics.Color
import io.github.alexzhirkevich.compottie.internal.AnimationState

internal fun interface ColorKeyframeMapper {
    fun ColorKeyframe.map(start : Color, end : Color, progress: Float) : ULong
}

internal class ColorKeyframeAnimation(
    index: Int?,
    keyframes: List<ColorKeyframe>,
    private val emptyValue : Color,
    private val map : ColorKeyframeMapper,
) : BaseKeyframeAnimation<Color, Color, ColorKeyframe>(
    index = index,
    sourceKeyframes = keyframes,
    emptyValue = emptyValue,
    map = { s, e, p -> Color(map.run { map(s,e,p) }) }
) {

    override fun rawColor(state: AnimationState): ULong {
        val kfId = keyframeNumber(state)
        val range = keyframesMappingRanges[kfId]

        if (range?.first == null || range.second == null)
            return emptyValue.value

        return with(keyframes[kfId.coerceIn(keyframes.indices)]) {
            with(map) {
                map(range.first!!, range.second!!, progress(kfId, state))
            }
        }
    }

    override fun raw(state: AnimationState): Color {
        return Color(rawColor(state))
    }
}

