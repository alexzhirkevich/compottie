@file:Suppress("INVISIBLE_MEMBER", "INVISIBLE_REFERENCE")

package io.github.alexzhirkevich.compottie.dot

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
@SerialName("Position")
internal class PositionRule(
    override val id: String,
    override val animations: List<String>? = null,
    override val expression: String? = null,
    override val keyframes: List<VectorDotKeyframe>? = null,
    override val value: FloatArray? = null
) : ThemeRule<FloatArray> by VectorRule(id, animations, expression, keyframes, value)

