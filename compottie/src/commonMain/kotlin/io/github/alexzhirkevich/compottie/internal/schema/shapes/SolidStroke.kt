package io.github.alexzhirkevich.compottie.internal.schema.shapes

import androidx.compose.ui.graphics.Paint
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.StrokeJoin
import io.github.alexzhirkevich.compottie.internal.schema.properties.AnimatedColor
import io.github.alexzhirkevich.compottie.internal.schema.properties.BooleanInt
import io.github.alexzhirkevich.compottie.internal.schema.properties.AnimatedValue
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable
import kotlin.jvm.JvmInline

@Serializable
@SerialName("st")
internal class SolidStroke(

    @SerialName("mn")
    override val matchName : String? = null,

    @SerialName("nm")
    override val name : String? = null,

    @SerialName("hd")
    override val hidden : Boolean = false,

    @SerialName("a")
    val withAlpha : BooleanInt = BooleanInt.No,

    @SerialName("lc")
    override val lineCap : LineCap,

    @SerialName("lj")
    override val lineJoin : LineJoin,

    @SerialName("ml")
    override val strokeMiter : Float = 0f,

    @SerialName("o")
    override val opacity : AnimatedValue,

    @SerialName("w")
    override val strokeWidth : AnimatedValue,

    @SerialName("c")
    val color : AnimatedColor,
) : BaseStroke(), Shape {

    override fun setupPaint(paint: Paint, frame: Int) {
        super.setupPaint(paint,  frame)
        paint.color = color.interpolated(frame)
    }
}

@Serializable
@JvmInline
internal value class LineCap(val type : Byte) {
    companion object {
        val Butt = LineCap(1)
        val Round = LineCap(2)
        val Square = LineCap(3)
    }
}

@Serializable
@JvmInline
internal value class LineJoin(val type : Byte) {
    companion object {
        val Miter = LineJoin(1)
        val Round = LineJoin(2)
        val Bevel = LineJoin(3)
    }
}

internal fun LineJoin.asStrokeJoin() : StrokeJoin {
    return when(this){
        LineJoin.Miter -> StrokeJoin.Miter
        LineJoin.Round -> StrokeJoin.Round
        LineJoin.Bevel -> StrokeJoin.Bevel
        else -> error("Unknown line join: $this")
    }
}

internal fun LineCap.asStrokeCap() : StrokeCap {
    return when(this){
        LineCap.Butt -> StrokeCap.Butt
        LineCap.Round -> StrokeCap.Round
        LineCap.Square -> StrokeCap.Square
        else -> error("Unknown line cap: $this")
    }
}