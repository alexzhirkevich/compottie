package io.github.alexzhirkevich.compottie.dot

import io.github.alexzhirkevich.compottie.internal.animation.AnimatedColor
import io.github.alexzhirkevich.compottie.internal.animation.RawProperty
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
@SerialName("Color")
internal class ColorRule(
    override val id: String,
    override val animations: List<String>? = null,
    override val expression: String? = null,
    override val keyframes: List<VectorDotKeyframe>? = null,
    override val value: FloatArray? = null
) : ThemeRule<FloatArray> {

    override fun property(): RawProperty<*> {
        return when {
            value != null || expression != null -> AnimatedColor.Default(
                value = value ?: floatArrayOf(0f,0f,0f,0f),
                expression = expression
            )
            keyframes != null -> AnimatedColor.Animated(
                keyframes = keyframes.map { it.toCoreKeyframe() },
                expression = expression
            )
            else -> AnimatedColor.Default(floatArrayOf(0f,0f,0f,0f))
        }
    }
}
