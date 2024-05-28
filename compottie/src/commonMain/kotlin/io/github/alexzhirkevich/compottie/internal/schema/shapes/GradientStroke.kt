package io.github.alexzhirkevich.compottie.internal.schema.shapes

import androidx.compose.ui.graphics.Paint
import androidx.compose.ui.graphics.Shader
import io.github.alexzhirkevich.compottie.internal.schema.properties.GradientColors
import io.github.alexzhirkevich.compottie.internal.schema.properties.Vector
import io.github.alexzhirkevich.compottie.internal.schema.properties.Value
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
@SerialName("gs")
internal class GradientStroke(

    @SerialName("mn")
    override val matchName : String? = null,

    @SerialName("nm")
    override val name : String? = null,

    @SerialName("hd")
    override val hidden : Boolean = false,

    @SerialName("lc")
    val lineCap : LineCap,

    @SerialName("lj")
    val lineJoin : LineJoin,

    @SerialName("ml")
    val miterLimit : Float? = null,

    @SerialName("o")
    val opacity : Value,

    @SerialName("w")
    val width : Value,

    @SerialName("s")
    val startPoint : Vector,

    @SerialName("e")
    val endPoint : Vector,

    /**
     * Gradient Highlight Length. Only if type is Radial
     * */
    @SerialName("h")
    val highlightLength : Value? = null,

    /**
     * Highlight Angle. Only if type is Radial
     * */
    @SerialName("a")
    val highlightAngle : Value? = null,

    @SerialName("g")
    val colors : GradientColors,

    @SerialName("t")
    val type : GradientType,
)  : BaseStroke(), Shape {

    override fun setupPaint(paint: Paint, time: Int) {
        super.setupPaint(paint, time)

        paint.shader = when (type){
            GradientType.Linear -> getLinearGradient(time)
            GradientType.Radial -> getRadialGradient(time)
            else -> error("Unknown gradient type: $type")
        }
    }

    private fun getLinearGradient(time: Int) : Shader {
        val startPoint = startPoint.interpolated(time)
        val endPoint = endPoint.interpolated(time)
        val color = colors.
    }

    private fun getRadialGradient(time: Int) : Shader {
        TODO()
    }
}

