package io.github.alexzhirkevich.compottie.internal.schema.shapes

import androidx.compose.ui.geometry.MutableRect
import androidx.compose.ui.graphics.Canvas
import androidx.compose.ui.graphics.Matrix
import androidx.compose.ui.graphics.Paint
import androidx.compose.ui.graphics.Shader
import io.github.alexzhirkevich.compottie.internal.schema.properties.GradientColors
import io.github.alexzhirkevich.compottie.internal.schema.properties.AnimatedVector2
import io.github.alexzhirkevich.compottie.internal.schema.properties.AnimatedValue
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable
import kotlinx.serialization.Transient

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
    override val lineCap : LineCap,

    @SerialName("lj")
    override val lineJoin : LineJoin,

    @SerialName("ml")
    val miterLimit : Float? = null,

    @SerialName("o")
    override val opacity : AnimatedValue,

    @SerialName("w")
    val width : AnimatedValue,

    @SerialName("s")
    val startPoint : AnimatedVector2,

    @SerialName("e")
    val endPoint : AnimatedVector2,

    /**
     * Gradient Highlight Length. Only if type is Radial
     * */
    @SerialName("h")
    val highlightLength : AnimatedValue? = null,

    /**
     * Highlight Angle. Only if type is Radial
     * */
    @SerialName("a")
    val highlightAngle : AnimatedValue? = null,

    @SerialName("g")
    val colors : GradientColors,

    @SerialName("t")
    val type : GradientType,
)  : BaseStroke(), Shape {

    @Transient
    private val boundsRect = MutableRect(0f,0f,0f,0f)

    override fun draw(canvas: Canvas, parentMatrix: Matrix, parentAlpha: Float, frame: Int) {

        if (hidden) {
            return
        }
        getBounds(boundsRect, parentMatrix, false, frame)

        val shader = if (type == GradientType.LINEAR) {
            linearGradient
        } else {
            radialGradient
        }
        shader.setLocalMatrix(parentMatrix)
        paint.setShader(shader)

        super.draw(canvas, parentMatrix, parentAlpha)

        super.draw(canvas, parentMatrix, parentAlpha, frame)
    }

    override fun setupPaint(paint: Paint, frame: Int) {
        super.setupPaint(paint, frame)

        paint.shader = when (type){
            GradientType.Linear -> getLinearGradient(frame)
            GradientType.Radial -> getRadialGradient(frame)
            else -> error("Unknown gradient type: $type")
        }
    }

    private fun getLinearGradient(time: Int) : Shader {


    }

    private fun getRadialGradient(time: Int) : Shader {
        val startPoint = startPoint.interpolated(time)
        val endPoint = endPoint.interpolated(time)
        val gradientColor = colors
        val colors = applyDynamicColorsIfNeeded(gradientColor!!.colors)
        val positions = gradientColor.positions
        val x0 = startPoint!!.x
        val y0 = startPoint.y
        val x1 = endPoint!!.x
        val y1 = endPoint.y
        gradient = LinearGradient(x0, y0, x1, y1, colors, positions, Shader.TileMode.CLAMP)
        linearGradientCache.put(gradientHash.toLong(), gradient)
        return gradient
    }
}

