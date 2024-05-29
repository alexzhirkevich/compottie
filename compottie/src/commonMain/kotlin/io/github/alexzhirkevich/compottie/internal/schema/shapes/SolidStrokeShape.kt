package io.github.alexzhirkevich.compottie.internal.schema.shapes

import androidx.compose.ui.graphics.Canvas
import androidx.compose.ui.graphics.Matrix
import io.github.alexzhirkevich.compottie.internal.schema.properties.AnimatedColor
import io.github.alexzhirkevich.compottie.internal.schema.properties.BooleanInt
import io.github.alexzhirkevich.compottie.internal.schema.properties.AnimatedValue
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
@SerialName("st")
internal class SolidStrokeShape(

    @SerialName("mn")
    override val matchName : String? = null,

    @SerialName("nm")
    override val name : String? = null,

    @SerialName("hd")
    override val hidden : Boolean = false,

    @SerialName("a")
    val withAlpha : BooleanInt = BooleanInt.No,

    @SerialName("lc")
    override val lineCap : LineCap = LineCap.Round,

    @SerialName("lj")
    override val lineJoin : LineJoin = LineJoin.Round,

    @SerialName("ml")
    override val strokeMiter : Float = 0f,

    @SerialName("o")
    override val opacity : AnimatedValue,

    @SerialName("w")
    override val strokeWidth : AnimatedValue,

    @SerialName("c")
    val color : AnimatedColor,
) : BaseStrokeShape(), Shape {

    override fun draw(canvas: Canvas, parentMatrix: Matrix, parentAlpha: Float, frame: Int) {
        paint.color = color.interpolated(frame)

        super.draw(canvas, parentMatrix, parentAlpha, frame)
    }
}
