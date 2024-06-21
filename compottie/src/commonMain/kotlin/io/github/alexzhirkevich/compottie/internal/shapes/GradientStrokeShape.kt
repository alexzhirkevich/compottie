package io.github.alexzhirkevich.compottie.internal.shapes

import androidx.compose.ui.graphics.Matrix
import androidx.compose.ui.graphics.drawscope.DrawScope
import io.github.alexzhirkevich.compottie.internal.AnimationState
import io.github.alexzhirkevich.compottie.internal.platform.GradientShader
import io.github.alexzhirkevich.compottie.internal.animation.AnimatedNumber
import io.github.alexzhirkevich.compottie.internal.animation.AnimatedVector2
import io.github.alexzhirkevich.compottie.internal.animation.GradientColors
import io.github.alexzhirkevich.compottie.internal.animation.GradientType
import io.github.alexzhirkevich.compottie.internal.helpers.StrokeDash
import io.github.alexzhirkevich.compottie.internal.layers.Layer
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable
import kotlinx.serialization.Transient

@Serializable
@SerialName("gs")
internal class GradientStrokeShape(

    @SerialName("mn")
    override val matchName : String? = null,

    @SerialName("nm")
    override val name : String? = null,

    @SerialName("hd")
    override val hidden : Boolean = false,

    @SerialName("lc")
    override val lineCap : LineCap = LineCap.Round,

    @SerialName("lj")
    override val lineJoin : LineJoin = LineJoin.Round,

    @SerialName("ml")
    override val strokeMiter : Float = 0f,

    @SerialName("o")
    override val opacity : AnimatedNumber = AnimatedNumber.Default(100f),

    @SerialName("w")
    override val strokeWidth : AnimatedNumber,

    @SerialName("d")
    override val strokeDash: List<StrokeDash>? = null,

    @SerialName("s")
    val startPoint : AnimatedVector2,

    @SerialName("e")
    val endPoint : AnimatedVector2,

    /**
     * Gradient Highlight Length. Only if type is Radial
     * */
    @SerialName("h")
    val highlightLength : AnimatedNumber? = null,

    /**
     * Highlight Angle. Only if type is Radial
     * */
    @SerialName("a")
    val highlightAngle : AnimatedNumber? = null,

    @SerialName("g")
    val colors : GradientColors,

    @SerialName("t")
    val type : GradientType = GradientType.Linear
)  : BaseStrokeShape(), Shape {



    override fun draw(drawScope: DrawScope, parentMatrix: Matrix, parentAlpha: Float, state: AnimationState) {

        if (dynamicStroke?.gradient == null) {
            paint.shader = GradientShader(
                type = type,
                startPoint = startPoint,
                endPoint = endPoint,
                colors = colors,
                state = state,
                matrix = parentMatrix,
                cache = gradientCache
            )
        }

        super.draw(drawScope, parentMatrix, parentAlpha, state)
    }
}

