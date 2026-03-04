package io.github.alexzhirkevich.compottie.dot

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
@SerialName("Position")
internal class PositionRule(
    override val id: String,
    override val value: FloatArray? = null,
    override val expression: String? = null,
    override val keyframes: List<VectorDotKeyframe>? = null,
) : PropertyThemeRule<FloatArray> by VectorRule(id, value, expression, keyframes, )

