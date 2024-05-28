package io.github.alexzhirkevich.compottie.internal.schema.shapes

import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.LinearGradientShader
import androidx.compose.ui.graphics.Paint
import androidx.compose.ui.graphics.PaintingStyle
import androidx.compose.ui.graphics.RadialGradientShader
import io.github.alexzhirkevich.compottie.internal.schema.properties.GradientColors
import io.github.alexzhirkevich.compottie.internal.schema.properties.Vector
import io.github.alexzhirkevich.compottie.internal.schema.properties.Value
import io.github.alexzhirkevich.compottie.internal.schema.util.toOffset
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable
import kotlin.jvm.JvmInline

@Serializable
@SerialName("gf")
internal class GradientFill(

    @SerialName("mn")
    override val matchName : String? = null,

    @SerialName("nm")
    override val name : String? = null,

    @SerialName("hd")
    override val hidden : Boolean = false,

    @SerialName("o")
    val opacity : Value,

    @SerialName("s")
    val startPoint : Vector,

    @SerialName("e")
    val endPoint : Vector,

    @SerialName("t")
    val type : GradientType,

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
) : DrawShape {

    override fun applyTo(paint: Paint, time: Int) {
        paint.style = PaintingStyle.Fill
        paint.alpha = opacity.interpolated(time)

        val interpolatedColors = colors.colors.interpolated(time)

        val colorStops = List(colors.numberOfColors) {
            interpolatedColors[it * 4]
        }

        val colors = List(colors.numberOfColors) {
            Color(
                interpolatedColors[it * 4 + 1],
                interpolatedColors[it * 4 + 2],
                interpolatedColors[it * 4 + 3]
            )
        }

        paint.shader = when (type) {

            GradientType.Linear -> LinearGradientShader(
                from = startPoint.interpolated(time).toOffset(),
                to = endPoint.interpolated(time).toOffset(),
                colorStops = colorStops,
                colors = colors
            )

            GradientType.Radial -> RadialGradientShader(
                colorStops = colorStops,
                colors = colors,
                center = startPoint.interpolated(time).toOffset(),
                radius = highlightLength?.interpolated(time) ?: 0f,
            )
            else -> error("Unknown gradient type: $type")
        }
    }
}


@Serializable
@JvmInline
internal value class GradientType(val type : Byte) {
    companion object {
        val Linear = GradientType(1)
        val Radial = GradientType(2)
    }
}