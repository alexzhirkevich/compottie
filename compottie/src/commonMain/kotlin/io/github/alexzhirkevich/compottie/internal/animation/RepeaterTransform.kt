package io.github.alexzhirkevich.compottie.internal.animation

import androidx.compose.ui.graphics.Matrix
import io.github.alexzhirkevich.compottie.internal.utils.preRotate
import io.github.alexzhirkevich.compottie.internal.utils.preScale
import io.github.alexzhirkevich.compottie.internal.utils.preTranslate
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable
import kotlin.math.pow

@Serializable
internal class RepeaterTransform(
    @SerialName("a")
    override val anchorPoint : AnimatedVector2? = null ,

    @SerialName("p")
    override val position : AnimatedVector2? = null,

    @SerialName("s")
    override val scale : AnimatedVector2? = null,

    @SerialName("r")
    override val rotation : AnimatedValue? = null,

    @SerialName("o")
    override val opacity : AnimatedValue? = null,

    @SerialName("sk")
    override val skew: AnimatedValue? = null,

    @SerialName("sa")
    override val skewAxis: AnimatedValue? = null,

    @SerialName("so")
    val startOpacity : AnimatedValue? = null,

    @SerialName("eo")
    val endOpacity : AnimatedValue? = null,
) : AnimatedTransform() {

    fun repeaterMatrix(frame: Float, amount: Float): Matrix {
        matrix.reset()

        position?.interpolated(frame)?.let {
            matrix.preTranslate(
                it.x * amount,
                it.y * amount
            )
        }

        scale?.interpolated(frame)?.let {
            matrix.preScale(
                it.x.div(100f).pow(amount),
                it.y.div(100f).pow(amount)
            )
        }

        rotation?.interpolated(frame)?.let {
            val anchorPoint = anchorPoint?.interpolated(it)

            if (anchorPoint != null) {
                matrix.translate(anchorPoint.x, anchorPoint.y)
            }
            matrix.preRotate(it * amount)

            if (anchorPoint != null) {
                matrix.translate(-anchorPoint.x, -anchorPoint.y)
            }
        }

        return matrix
    }
}